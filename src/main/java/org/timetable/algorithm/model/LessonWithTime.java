package org.timetable.algorithm.model;

import org.timetable.algorithm.scheduling.LessonGene;

import java.util.List;

public record LessonWithTime(AudienceTimeCell cell, LessonGene lessonGene) {
    public AudienceEvolve audience() {
        return cell.audience();
    }

    public TableTime time() {
        return cell.time();
    }

    public TeacherEvolve teacher() {
        return lessonGene.teacher();
    }

    public List<GroupEvolve> groups() {
        return lessonGene.groups();
    }

    public SubjectEvolve subject() {
        return lessonGene.subject();
    }

    @Override
    public String toString() {
        return cell.audience() + "-" + cell.time().day() + "/" + cell.time().cellNumber() +
                ":" + lessonGene;
    }

}