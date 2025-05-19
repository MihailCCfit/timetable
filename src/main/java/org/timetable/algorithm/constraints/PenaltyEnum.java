package org.timetable.algorithm.constraints;


import org.timetable.algorithm.model.GroupEvolve;
import org.timetable.algorithm.model.LessonWithTime;

import java.util.concurrent.atomic.AtomicReference;

import static org.timetable.algorithm.constraints.CalculateResult.ok;
import static org.timetable.algorithm.constraints.CalculateResult.problem;

/**
 * negative if penalty
 * large negative if it's hard constraint
 * small penalty if it's weak constraint
 * positive if good ending
 */
public enum PenaltyEnum {
    TeacherAndAudienceType(
            (data) -> {
                LessonWithTime lesson = data.currentLesson();
                if (!lesson.audience().auditoryType()
                        .equals(lesson.teacher().teacherType())) {
                    return problem(-40.0, "Teacher and audience has different types "
                            + lesson.teacher() + " " + lesson.audience());
                }
                return ok();
            },
            true
    ),
    AudienceUnique(
            (data) -> {
                LessonWithTime lesson = data.currentLesson();
                var lessonsInCell = data.getOtherLessons(lesson);
                //audit unique
                if (lessonsInCell.stream()
                        .anyMatch(lessonWithTime ->
                                lessonWithTime.audience().id()
                                        .equals(lesson.audience().id()))) {
                    return problem(-40.0, "Problem with audience " + lesson.audience());
                }
                return ok();
            },
            true
    ),
    TeacherUnique(
            (data) -> {
                LessonWithTime lesson = data.currentLesson();
                var lessonsInCell = data.getOtherLessons(lesson);
                if (lessonsInCell.stream()
                        .anyMatch(lessonWithTime ->
                                lessonWithTime.teacher().id()
                                        .equals(lesson.teacher().id()))) {
                    return problem(-40., "Teacher teach in on cell " + lesson.teacher());
                }
                return ok();
            },
            true
    ),
    GroupsInOneAuditory(
            (data) -> {
                LessonWithTime lesson = data.currentLesson();
                var otherLessons = data.getOtherLessons(lesson);
                AtomicReference<GroupEvolve> groupEvolve = new AtomicReference<>(new GroupEvolve(null));
                AtomicReference<LessonWithTime> lessonRef = new AtomicReference<>(null);
                if (
                        otherLessons.stream()
                                .anyMatch(lessonWithTime ->
                                        {
                                            for (GroupEvolve group : lessonWithTime.groups()) {
                                                if (lesson.groups().contains(group)) {
                                                    groupEvolve.set(group);
                                                    lessonRef.set(lessonWithTime);
                                                    return true;
                                                }
                                            }
                                            return false;
                                        }
                                )) {
                    return problem(-40.,
                            "Lesson " + lesson + " problem with groups " + groupEvolve.get()
                                    + " to lesson " + lessonRef.get());
                }
                return ok();
            },
            true
    ),
    ;
    final PenaltyFunction penaltyFunction;
    final boolean isHard;
    final String name;

    public PenaltyFunction getPenaltyFunction() {
        return penaltyFunction;
    }

    public Penalty toPenalty() {
        return new Penalty(name, penaltyFunction, isHard);
    }

    PenaltyEnum(PenaltyFunction penaltyFunction, boolean isHard, String name) {
        this.penaltyFunction = penaltyFunction;
        this.isHard = isHard;
        this.name = name;
    }

    PenaltyEnum(PenaltyFunction penaltyFunction, boolean isHard) {
        this.penaltyFunction = penaltyFunction;
        this.isHard = isHard;
        this.name = this.name();
    }

}
