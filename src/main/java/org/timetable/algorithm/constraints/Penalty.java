package org.timetable.algorithm.constraints;

import java.util.Objects;

public class Penalty {
    String name;
    PenaltyFunction penaltyFunction;
    boolean isHard;

    public Penalty(String name, PenaltyFunction penaltyFunction, boolean isHard) {
        this.name = name;
        this.penaltyFunction = penaltyFunction;
        this.isHard = isHard;
    }

    @Override
    public String toString() {
        return "Penalty{" + name + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Penalty penalty = (Penalty) o;
        return Objects.equals(name, penalty.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
