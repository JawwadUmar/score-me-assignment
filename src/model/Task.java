package model;

public final class Task {
    public final String id;
    public final double[] resources;
    public final int lower;
    public final int upper;
    public final double weight;

    public Task(String id, double[] resources, int lower, int upper, double weight) {
        this.id = id;
        this.resources = resources;
        this.lower = lower;
        this.upper = upper;
        this.weight = weight;
    }
}

