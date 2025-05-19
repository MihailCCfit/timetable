package org.timetable.algorithm.constraints;

import org.timetable.algorithm.scheduling.DataForConstraint;

import java.util.function.Function;

public interface PenaltyFunction extends Function<DataForConstraint, CalculateResult> {

}
