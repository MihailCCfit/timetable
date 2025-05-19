package org.timetable.algorithm.scheduling;

import org.timetable.algorithm.constraints.PenaltyChecker;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AlgorithmProcessingStatus {
    volatile double percentage = 1;
    volatile boolean running = false;

    volatile PenaltyChecker.CheckResult checkResult = null;

    CompletableFuture<List<ScheduleResult>> result = new CompletableFuture<>();

    public double getPercentage() {
        return percentage;
    }

    public boolean isRunning() {
        return running;
    }

    public CompletableFuture<List<ScheduleResult>> getResult() {
        return result;
    }
}