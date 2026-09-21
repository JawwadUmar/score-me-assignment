package test;

import algorithm.CradScheduler;
import model.ScheduleResult;
import model.SchedulingInstance;
import model.Task;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class CradSchedulerTest {

    @Test
    void singleTaskIsScheduled() {
        Task t = new Task(
                "T0",
                new double[]{1, 1, 1, 1},
                1,
                3,
                5
        );

        Map<String, Task> tasks = Map.of("T0", t);

        double[][] capacity = {
                {10, 10, 10, 10},
                {10, 10, 10, 10},
                {10, 10, 10, 10}
        };

        SchedulingInstance instance =
                new SchedulingInstance(
                        tasks,
                        List.of(),
                        capacity
                );

        ScheduleResult result =
                new CradScheduler(1.0).solve(instance);

        assertTrue(result.feasible);
        assertEquals(1, result.assignment.get("T0"));
    }

    @Test
    void tightSlaWindowIsRespected() {
        Task t = new Task(
                "T0",
                new double[]{1, 1, 1, 1},
                2,
                2,
                5
        );

        SchedulingInstance instance =
                new SchedulingInstance(
                        Map.of("T0", t),
                        List.of(),
                        new double[][]{
                                {10, 10, 10, 10},
                                {10, 10, 10, 10},
                                {10, 10, 10, 10}
                        }
                );

        ScheduleResult result =
                new CradScheduler(1.0).solve(instance);

        assertTrue(result.feasible);
        assertEquals(2, result.assignment.get("T0"));
    }

    @Test
    void zeroCapacitySlotCannotAcceptTask() {
        Task t = new Task(
                "T0",
                new double[]{1, 1, 1, 1},
                1,
                2,
                1
        );

        SchedulingInstance instance =
                new SchedulingInstance(
                        Map.of("T0", t),
                        List.of(),
                        new double[][]{
                                {0, 0, 0, 0},
                                {10, 10, 10, 10}
                        }
                );

        ScheduleResult result =
                new CradScheduler(1.0).solve(instance);

        assertTrue(result.feasible);
        assertEquals(1, result.assignment.get("T0"));
    }

    @Test
    void completeConflictGraphExceedingKIsInfeasible() {
        Map<String, Task> tasks = new LinkedHashMap<>();

        for (int i = 0; i < 4; i++) {
            tasks.put(
                    String.valueOf(i),
                    new Task(
                            String.valueOf(i),
                            new double[]{1, 1, 1, 1},
                            1,
                            3,
                            1
                    )
            );
        }

        List<int[]> conflicts = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                conflicts.add(new int[]{i, j});
            }
        }

        double[][] capacity = {
                {10, 10, 10, 10},
                {10, 10, 10, 10},
                {10, 10, 10, 10}
        };

        SchedulingInstance instance =
                new SchedulingInstance(
                        tasks,
                        conflicts,
                        capacity
                );

        ScheduleResult result =
                new CradScheduler(1.0).solve(instance);

        assertFalse(result.feasible);
        assertTrue(
                result.violationReason.contains(
                        "No feasible slot"
                )
        );
    }

    /**
     * Independently validates F1, F2 and F3.
     *
     * This deliberately does not reuse the scheduler's internal
     * candidate-generation logic, reducing the risk that a bug in
     * the scheduler and a bug in the validator cancel each other out.
     */
    public static String validate(
            SchedulingInstance instance,
            Map<String, Integer> assignment
    ) {
        if (assignment.size() != instance.tasks.size()) {
            return "Assignment does not contain every task";
        }

        // F3: SLA windows
        for (Task task : instance.tasks.values()) {
            Integer slot = assignment.get(task.id);

            if (slot == null) {
                return "Missing assignment for " + task.id;
            }

            if (slot < task.lower || slot > task.upper) {
                return "F3 violated by " + task.id;
            }
        }

        // F1: conflicts
        for (Map.Entry<String, Set<String>> entry
                : instance.neighbors.entrySet()) {

            String a = entry.getKey();
            Integer slotA = assignment.get(a);

            for (String b : entry.getValue()) {
                Integer slotB = assignment.get(b);

                if (Objects.equals(slotA, slotB)) {
                    return "F1 violated: " + a + " conflicts with " + b;
                }
            }
        }

        // F2: resources
        double[][] load =
                new double[instance.k][4];

        for (Task task : instance.tasks.values()) {
            int slot = assignment.get(task.id);

            for (int d = 0; d < 4; d++) {
                load[slot][d] += task.resources[d];

                if (load[slot][d]
                        > instance.capacities[slot][d] + 1e-9) {

                    return "F2 violated in slot "
                            + slot + ", dimension " + d;
                }
            }
        }

        return null;
    }
}
