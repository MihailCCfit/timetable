package org.timetable.algorithm.model;

import java.util.Objects;

public record AudienceTimeCell(AudienceEvolve audience, TableTime time) {
    public AudienceTimeCell(AudienceEvolve audience, int timeIndex, TableTimeSetting timeSetting) {
        this(audience, new TableTime(timeIndex, timeSetting));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudienceTimeCell that = (AudienceTimeCell) o;
        return Objects.equals(audience, that.audience) && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(audience, time);
    }
}