package org.timetable.algorithm.model;

import java.util.List;


public record StudyPlanEvolve(List<SubjectEvolve> subjectEvolves, List<GroupEvolve> groupEvolves) {
}
