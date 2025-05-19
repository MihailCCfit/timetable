package org.timetable.algorithm.scheduling;

import org.timetable.algorithm.model.AudienceEvolve;
import org.timetable.algorithm.model.StudyPlanEvolve;
import org.timetable.algorithm.model.TableTimeSetting;

import java.util.List;

public interface AlgorithmScheduler {

    List<ScheduleResult> algorithm(List<StudyPlanEvolve> studyPlanEvolves,
                                   List<AudienceEvolve> audiences,
                                   TableTimeSetting tableTimeSetting);
}
