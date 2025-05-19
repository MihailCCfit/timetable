package org.timetable.algorithm.scheduling;

import org.timetable.algorithm.model.AudienceEvolve;
import org.timetable.algorithm.model.StudyPlanEvolve;
import org.timetable.algorithm.model.TableTimeSetting;

import java.util.List;

public interface AlgorithmScheduler {

    /**
     *
     * @param studyPlanEvolves - studyPlans
     * @param audiences - all audiences
     * @param tableTimeSetting - time setting (days and time in a day)
     * @return few (one or more) results - schedules with penalties if its exist
     */
    List<ScheduleResult> algorithm(List<StudyPlanEvolve> studyPlanEvolves,
                                   List<AudienceEvolve> audiences,
                                   TableTimeSetting tableTimeSetting);
}
