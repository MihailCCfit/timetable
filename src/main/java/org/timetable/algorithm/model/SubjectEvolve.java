package org.timetable.algorithm.model;

import java.util.Map;

public record SubjectEvolve(String id,
                            int seminarAmount,
                            int lectureAmount,
                            Map<String, TeacherEvolve> teacherToGroup,
                            TeacherEvolve lectureTeacherEvolve,
                            int subId) {
    public SubjectEvolve(String id,
                         int seminarAmount,
                         int lectureAmount,
                         Map<String, TeacherEvolve> teacherToGroup,
                         TeacherEvolve lectureTeacherEvolve) {
        this(id, seminarAmount, lectureAmount, teacherToGroup, lectureTeacherEvolve, 0);
    }

    public SubjectEvolve withSubId(int subId) {
        return new SubjectEvolve(id, seminarAmount, lectureAmount, teacherToGroup, lectureTeacherEvolve, subId);
    }

    @Override
    public String toString() {
        return id;
    }
}
