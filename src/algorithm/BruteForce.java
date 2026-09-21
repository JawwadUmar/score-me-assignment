package algorithm;

import model.ScheduleResult;
import model.SchedulingInstance;
import model.Task;

import java.util.*;

public class BruteForce {
    private double bestPenalty = Double.POSITIVE_INFINITY;
    private final double lambda;
    private final int DIMENSIONS = 4;

    public BruteForce(double lambda) {
        this.lambda = lambda;
    }

    public double findOptimalPenalty(SchedulingInstance instance) {
        bestPenalty = Double.POSITIVE_INFINITY;
        String[] taskIds = instance.tasks.keySet().toArray(new String[0]);
        int[] assignment = new int[taskIds.length];
        Arrays.fill(assignment, -1);
        
        search(0, taskIds, assignment, instance);
        
        return bestPenalty;
    }

    private void search(int taskIndex, String[] taskIds, int[] assignment, SchedulingInstance instance) {
        if (taskIndex == taskIds.length) {
            double penalty = calculatePenalty(instance, assignment, taskIds);
            if (penalty < bestPenalty) {
                bestPenalty = penalty;
            }
            return;
        }

        String taskId = taskIds[taskIndex];
        Task task = instance.tasks.get(taskId);

        int startSlot = Math.max(0, task.lower);
        int endSlot = Math.min(instance.k - 1, task.upper);
        
        for (int slot = startSlot; slot <= endSlot; slot++) {
            if (isValid(taskIndex, slot, taskIds, assignment, instance)) {
                assignment[taskIndex] = slot;
                // pruning
                double currentBase = 0.0;
                for(int i=0; i<=taskIndex; i++) {
                    currentBase += instance.tasks.get(taskIds[i]).weight * assignment[i];
                }
                if(currentBase < bestPenalty) {
                    search(taskIndex + 1, taskIds, assignment, instance);
                }
                assignment[taskIndex] = -1;
            }
        }
    }

    private boolean isValid(int taskIndex, int slot, String[] taskIds, int[] assignment, SchedulingInstance instance) {
        String taskId = taskIds[taskIndex];
        Task task = instance.tasks.get(taskId);
        
        // F1: Conflicts
        for (int i = 0; i < taskIndex; i++) {
            if (assignment[i] == slot) {
                if (instance.neighbors.getOrDefault(taskId, Collections.emptySet()).contains(taskIds[i])) {
                    return false;
                }
            }
        }

        // F2: Capacity
        double[] currentLoad = new double[4];
        for (int i = 0; i < taskIndex; i++) {
            if (assignment[i] == slot) {
                for (int d = 0; d < 4; d++) {
                    currentLoad[d] += instance.tasks.get(taskIds[i]).resources[d];
                }
            }
        }
        for (int d = 0; d < 4; d++) {
            if (currentLoad[d] + task.resources[d] > instance.capacities[slot][d] + 1e-9) {
                return false;
            }
        }

        return true;
    }

    private double calculatePenalty(SchedulingInstance instance, int[] assignment, String[] taskIds) {
        double base = 0.0;
        double[][] load = new double[instance.k][DIMENSIONS];

        for (int i = 0; i < taskIds.length; i++) {
            int slot = assignment[i];
            Task task = instance.tasks.get(taskIds[i]);
            base += task.weight * slot;
            for (int d = 0; d < DIMENSIONS; d++) {
                load[slot][d] += task.resources[d];
            }
        }

        return base + lambda * imbalance(instance, load);
    }

    private double imbalance(SchedulingInstance instance, double[][] load) {
        double total = 0.0;
        for (int d = 0; d < DIMENSIONS; d++) {
            double mean = 0.0;
            for (int s = 0; s < instance.k; s++) {
                mean += load[s][d] / instance.capacities[s][d];
            }
            mean /= instance.k;
            for (int s = 0; s < instance.k; s++) {
                double utilization = load[s][d] / instance.capacities[s][d];
                double diff = utilization - mean;
                total += diff * diff;
            }
        }
        return total;
    }
}

