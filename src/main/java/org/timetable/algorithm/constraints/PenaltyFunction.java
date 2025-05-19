package org.timetable.algorithm.constraints;

import org.timetable.algorithm.scheduling.DataForConstraint;

import java.util.function.Function;

/**
 * The penalty functions takes information about each lesson with all other lessons. So be careful with repetitions
 */
public interface PenaltyFunction extends Function<DataForConstraint, CalculateResult> {

}
