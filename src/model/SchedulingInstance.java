package model;

import java.util.*;

public final class SchedulingInstance {

    public final Map<String, Task> tasks;
    public final Map<String, Set<String>> neighbors;
    public final double[][] capacities;
    public final int k;

    public SchedulingInstance(
            Map<String, Task> tasks,
            List<int[]> conflicts,
            double[][] capacities
    ) {
        this.tasks = new LinkedHashMap<>(tasks);
        this.capacities = capacities;
        this.k = capacities.length;

        this.neighbors = new HashMap<>();

        for (String id : tasks.keySet()) {
            neighbors.put(id, new HashSet<>());
        }

        for (int[] edge : conflicts) {
            String a = String.valueOf(edge[0]);
            String b = String.valueOf(edge[1]);

            neighbors.computeIfAbsent(a, x -> new HashSet<>()).add(b);
            neighbors.computeIfAbsent(b, x -> new HashSet<>()).add(a);
        }
    }
}

