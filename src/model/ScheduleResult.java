package model;

import java.util.*;

public final class ScheduleResult {

    public final Map<String, Integer> assignment;
    public final double penalty;
    public final long runtimeMs;
    public final boolean feasible;
    public final String violationReason;

    private ScheduleResult(
            Map<String, Integer> assignment,
            double penalty,
            long runtimeMs,
            boolean feasible,
            String violationReason
    ) {
        this.assignment = assignment;
        this.penalty = penalty;
        this.runtimeMs = runtimeMs;
        this.feasible = feasible;
        this.violationReason = violationReason;
    }

    public static ScheduleResult feasible(
            Map<String, Integer> assignment,
            double penalty,
            long runtimeMs
    ) {
        return new ScheduleResult(
                assignment,
                penalty,
                runtimeMs,
                true,
                ""
        );
    }

    public static ScheduleResult infeasible(
            Map<String, Integer> partialAssignment,
            long runtimeMs,
            String reason
    ) {
        return new ScheduleResult(
                partialAssignment,
                Double.POSITIVE_INFINITY,
                runtimeMs,
                false,
                reason
        );
    }
}

