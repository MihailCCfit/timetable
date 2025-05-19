package org.timetable.algorithm.model;

public record GroupEvolve(String id) {
    @Override
    public String toString() {
        return id;
    }
}
