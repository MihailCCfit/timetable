package org.timetable.algorithm.model;

public record TeacherEvolve(String id, SubjectType teacherType) {
    @Override
    public String toString() {
        return id + ':' + teacherType;
    }
}
