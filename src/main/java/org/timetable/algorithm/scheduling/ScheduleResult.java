package org.timetable.algorithm.scheduling;

import org.timetable.algorithm.constraints.PenaltyChecker;
import org.timetable.algorithm.model.LessonWithTime;

import java.util.List;

public record ScheduleResult(List<LessonWithTime> allLessons, PenaltyChecker.CheckResult checkResult) {

}