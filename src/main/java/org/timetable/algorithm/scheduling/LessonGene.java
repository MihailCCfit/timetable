package org.timetable.algorithm.scheduling;

import org.timetable.algorithm.model.GroupEvolve;
import org.timetable.algorithm.model.SubjectEvolve;
import org.timetable.algorithm.model.TeacherEvolve;

import java.util.ArrayList;
import java.util.List;

public record LessonGene(List<GroupEvolve> groups, TeacherEvolve teacher, SubjectEvolve subject) {
    public LessonGene(GroupEvolve groupEvolve, TeacherEvolve teacherEvolve, SubjectEvolve subjectEvolve) {
        this(new ArrayList<>(List.of(groupEvolve)), teacherEvolve, subjectEvolve);
    }

    @Override
    public String toString() {
        return "{" + groups +
                "|T:" + teacher + "|" +
                subject + "}";
    }
}