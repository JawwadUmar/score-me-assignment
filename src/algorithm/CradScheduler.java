package algorithm;

import java.util.*;
import model.Task;
import model.SchedulingInstance;
import model.ScheduleResult;

/**
 * CRAD = Conflict-first Resource-Aware DSATUR.
 *
 * Greedily assigns tasks using:
 * 1. DSATUR-style saturation,
 * 2. conflict degree,
 * 3. normalized task weight,
 * 4. task weight.
 *
 * A candidate slot must satisfy:
 * F1: no already-assigned conflicting task occupies the slot.
 * F2: resource capacity remains valid.
 * F3: slot lies inside the task's SLA window.
 */
public final class CradScheduler {

    private static final int DIMENSIONS = 4;

    private final double lambda;

    public CradScheduler(double lambda) {
        if (lambda < 0.0) {
            throw new IllegalArgumentException("lambda must be non-negative");
        }
        this.lambda = lambda;
    }

    /**
     * Schedules the supplied instance using the CRAD heuristic.
     *
     * The method never deliberately creates an F1/F2/F3 violation.
     * If no legal slot exists for the currently selected task, it
     * reports infeasibility for the current greedy construction.
     */
    public ScheduleResult solve(SchedulingInstance instance) {
        long start = System.nanoTime();

        Map<String, Integer> assignment = new LinkedHashMap<>();
        double[][] load = new double[instance.k][DIMENSIONS];

        Set<String> unassigned = new LinkedHashSet<>();
        unassigned.addAll(instance.tasks.keySet());

        while (!unassigned.isEmpty()) {

            String taskId = chooseNextTask(
                    instance,
                    assignment,
                    unassigned
            );

            Task task = instance.tasks.get(taskId);

            List<Integer> candidates = feasibleSlots(
                    instance,
                    task,
                    assignment,
                    load
            );

            if (candidates.isEmpty()) {
                long runtime = elapsedMillis(start);

                return ScheduleResult.infeasible(
                        assignment,
                        runtime,
                        "No feasible slot remains for task " + task.id
                );
            }

            int selectedSlot = chooseBestSlot(
                    instance,
                    task,
                    candidates,
                    assignment,
                    load
            );

            assignment.put(task.id, selectedSlot);
            addLoad(task, selectedSlot, load);

            unassigned.remove(task.id);
        }

        double penalty = calculatePenalty(instance, assignment);

        long runtime = elapsedMillis(start);

        return ScheduleResult.feasible(
                assignment,
                penalty,
                runtime
        );
    }

    /**
     * Implements the Task 3 priority rule:
     * saturation, degree, normalized weight, then weight.
     */
    private String chooseNextTask(
            SchedulingInstance instance,
            Map<String, Integer> assignment,
            Set<String> unassigned
    ) {
        String best = null;

        for (String id : unassigned) {
            Task t = instance.tasks.get(id);

            int saturation = saturation(t, instance, assignment);
            int degree = instance.neighbors.getOrDefault(id, Set.of()).size();

            int windowLength = t.upper - t.lower + 1;
            double normalizedWeight = t.weight / windowLength;

            if (best == null) {
                best = id;
                continue;
            }

            Task b = instance.tasks.get(best);

            int bSat = saturation(b, instance, assignment);
            int bDegree = instance.neighbors.getOrDefault(
                    best, Set.of()
            ).size();

            int bWindow = b.upper - b.lower + 1;
            double bNormWeight = b.weight / bWindow;

            if (compare(
                    saturation,
                    degree,
                    normalizedWeight,
                    t.weight,
                    bSat,
                    bDegree,
                    bNormWeight,
                    b.weight
            ) > 0) {
                best = id;
            }
        }

        return best;
    }

    /**
     * Lexicographic comparison of CRAD priority components.
     */
    private int compare(
            int satA,
            int degreeA,
            double normA,
            double weightA,
            int satB,
            int degreeB,
            double normB,
            double weightB
    ) {
        if (satA != satB)
            return Integer.compare(satA, satB);

        if (degreeA != degreeB)
            return Integer.compare(degreeA, degreeB);

        int norm = Double.compare(normA, normB);
        if (norm != 0)
            return norm;

        return Double.compare(weightA, weightB);
    }

    /**
     * Counts distinct slots occupied by already-assigned neighbors.
     */
    private int saturation(
            Task task,
            SchedulingInstance instance,
            Map<String, Integer> assignment
    ) {
        Set<Integer> used = new HashSet<>();

        for (String neighbor : instance.neighbors.getOrDefault(
                task.id, Set.of()
        )) {
            Integer slot = assignment.get(neighbor);

            if (slot != null) {
                used.add(slot);
            }
        }

        return used.size();
    }

    /**
     * Computes the slots satisfying SLA, conflict, and resource
     * constraints against the current partial assignment.
     */
    private List<Integer> feasibleSlots(
            SchedulingInstance instance,
            Task task,
            Map<String, Integer> assignment,
            double[][] load
    ) {
        List<Integer> result = new ArrayList<>();

        int startSlot = Math.max(0, task.lower);
        int endSlot = Math.min(instance.k - 1, task.upper);
        for (int slot = startSlot; slot <= endSlot; slot++) {

            if (!conflictFree(
                    task,
                    slot,
                    instance,
                    assignment
            )) {
                continue;
            }

            if (!resourceFeasible(
                    task,
                    slot,
                    instance,
                    load
            )) {
                continue;
            }

            result.add(slot);
        }

        return result;
    }

    /**
     * Tests F1 for one candidate slot.
     */
    private boolean conflictFree(
            Task task,
            int slot,
            SchedulingInstance instance,
            Map<String, Integer> assignment
    ) {
        for (String neighbor : instance.neighbors.getOrDefault(
                task.id, Set.of()
        )) {
            Integer neighborSlot = assignment.get(neighbor);

            if (neighborSlot != null && neighborSlot == slot) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tests F2 for one candidate slot.
     */
    private boolean resourceFeasible(
            Task task,
            int slot,
            SchedulingInstance instance,
            double[][] load
    ) {
        for (int d = 0; d < DIMENSIONS; d++) {
            if (load[slot][d] + task.resources[d]
                    > instance.capacities[slot][d] + 1e-9) {
                return false;
            }
        }

        return true;
    }

    /**
     * Chooses the candidate minimizing the incremental penalty.
     *
     * Ties are resolved by smaller slot index and then greater
     * remaining aggregate resource slack.
     */
    private int chooseBestSlot(
            SchedulingInstance instance,
            Task task,
            List<Integer> candidates,
            Map<String, Integer> assignment,
            double[][] load
    ) {
        int bestSlot = candidates.get(0);
        double bestDelta = Double.POSITIVE_INFINITY;
        double bestSlack = Double.NEGATIVE_INFINITY;

        for (int slot : candidates) {

            double delta = task.weight * slot
                    + lambda * incrementalImbalance(
                            instance,
                            task,
                            slot,
                            load
                    );

            double slack = totalSlack(
                    instance,
                    task,
                    slot,
                    load
            );

            if (delta < bestDelta - 1e-9
                    || (Math.abs(delta - bestDelta) <= 1e-9
                    && (slot < bestSlot
                    || (slot == bestSlot && slack > bestSlack)))) {

                bestSlot = slot;
                bestDelta = delta;
                bestSlack = slack;
            }
        }

        return bestSlot;
    }

    /**
     * Calculates the incremental contribution of assigning a task
     * to a particular slot to the load-imbalance term.
     */
    private double incrementalImbalance(
            SchedulingInstance instance,
            Task task,
            int slot,
            double[][] load
    ) {
        double[][] copy = copyLoad(load);

        for (int d = 0; d < DIMENSIONS; d++) {
            copy[slot][d] += task.resources[d];
        }

        return imbalance(instance, copy) - imbalance(instance, load);
    }

    /**
     * Computes the load imbalance penalty defined in Task 2.
     */
    private double imbalance(
            SchedulingInstance instance,
            double[][] load
    ) {
        double total = 0.0;

        for (int d = 0; d < DIMENSIONS; d++) {

            double mean = 0.0;

            for (int s = 0; s < instance.k; s++) {
                mean += load[s][d] / instance.capacities[s][d];
            }

            mean /= instance.k;

            for (int s = 0; s < instance.k; s++) {
                double utilization =
                        load[s][d] / instance.capacities[s][d];

                double diff = utilization - mean;
                total += diff * diff;
            }
        }

        return total;
    }

    /**
     * Computes the complete Task 2 penalty.
     */
    private double calculatePenalty(
            SchedulingInstance instance,
            Map<String, Integer> assignment
    ) {
        double base = 0.0;
        double[][] load = new double[instance.k][DIMENSIONS];

        for (Task task : instance.tasks.values()) {
            Integer slot = assignment.get(task.id);

            if (slot == null) {
                return Double.POSITIVE_INFINITY;
            }

            base += task.weight * slot;
            addLoad(task, slot, load);
        }

        return base + lambda * imbalance(instance, load);
    }

    /**
     * Computes aggregate remaining resource slack for a candidate slot.
     */
    private double totalSlack(
            SchedulingInstance instance,
            Task task,
            int slot,
            double[][] load
    ) {
        double result = 0.0;

        for (int d = 0; d < DIMENSIONS; d++) {
            result += instance.capacities[slot][d]
                    - load[slot][d]
                    - task.resources[d];
        }

        return result;
    }

    private void addLoad(
            Task task,
            int slot,
            double[][] load
    ) {
        for (int d = 0; d < DIMENSIONS; d++) {
            load[slot][d] += task.resources[d];
        }
    }

    private double[][] copyLoad(double[][] original) {
        double[][] result = new double[original.length][];

        for (int i = 0; i < original.length; i++) {
            result[i] = original[i].clone();
        }

        return result;
    }

    private long elapsedMillis(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}
