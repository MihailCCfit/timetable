package org.timetable.algorithm.model;

public record TableTime(int day, int cellNumber, TableTimeSetting tableTimeSetting) {
    public int toIndex() {
        return day * tableTimeSetting.maxCells() + cellNumber;
    }

    public TableTime(int index, TableTimeSetting timeSetting) {
        this(index / timeSetting.maxCells(), index % timeSetting.maxCells(), timeSetting);
    }

    @Override
    public String toString() {
        return day + "/" + cellNumber;
    }
}
