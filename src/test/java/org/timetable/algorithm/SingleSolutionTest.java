package org.timetable.algorithm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.timetable.algorithm.constraints.CalculateResult;
import org.timetable.algorithm.constraints.Penalty;
import org.timetable.algorithm.constraints.PenaltyChecker;
import org.timetable.algorithm.constraints.PenaltyEnum;
import org.timetable.algorithm.model.*;
import org.timetable.algorithm.scheduling.AlgorithmScheduler;
import org.timetable.algorithm.scheduling.GeneticAlgorithmScheduler;
import org.timetable.algorithm.scheduling.ScheduleResult;

import java.util.*;
import java.util.stream.Stream;

public class SingleSolutionTest {

    @ParameterizedTest
    @MethodSource("provideScheduler")
    void singleSmallSolution(AlgorithmScheduler algorithmScheduler) {
        var timeSetting = new TableTimeSetting(2, 2);
        List<AudienceEvolve> audienceEvolves = List.of(
                new AudienceEvolve("a1", SubjectType.PRACTICAL),
                new AudienceEvolve("a2", SubjectType.PRACTICAL)
        );
        List<GroupEvolve> groups = List.of(new GroupEvolve("g0"));
        TeacherEvolve teacherEvolve1 = new TeacherEvolve("t1", SubjectType.PRACTICAL);
        TeacherEvolve teacherEvolve2 = new TeacherEvolve("t2", SubjectType.PRACTICAL);
        TeacherEvolve teacherEvolve3 = new TeacherEvolve("t3", SubjectType.PRACTICAL);

        SubjectEvolve subjectEvolve1 = new SubjectEvolve("s1",
                1,
                0,
                Map.of("g0", teacherEvolve1),
                null);
        SubjectEvolve subjectEvolve2 = new SubjectEvolve("s2",
                1,
                0,
                Map.of("g0", teacherEvolve2),
                null);
        SubjectEvolve subjectEvolve3 = new SubjectEvolve("s3",
                1,
                0,
                Map.of("g0", teacherEvolve3),
                null);
        List<SubjectEvolve> subjects = List.of(subjectEvolve1, subjectEvolve2, subjectEvolve3);
        StudyPlanEvolve studyPlanEvolve = new StudyPlanEvolve(subjects, groups);

        List<ScheduleResult> result = algorithmScheduler.algorithm(List.of(studyPlanEvolve), audienceEvolves, timeSetting);

        var res = result.get(0);
        for (LessonWithTime lesson : res.allLessons()) {
            if (lesson.lessonGene().subject().id().equals("s1")) {
                Assertions.assertEquals(0, lesson.cell().time().toIndex());
                Assertions.assertEquals("a1", lesson.cell().audience().id());
            }
            if (lesson.lessonGene().subject().id().equals("s2")) {
                Assertions.assertEquals(1, lesson.cell().time().toIndex());
                Assertions.assertEquals("a2", lesson.cell().audience().id());
            }
            if (lesson.lessonGene().subject().id().equals("s3")) {
                Assertions.assertEquals(3, lesson.cell().time().toIndex());
                Assertions.assertEquals("a2", lesson.cell().audience().id());
            }
        }
    }

    private static Stream<AlgorithmScheduler> provideScheduler() {
        var timeSetting = new TableTimeSetting(2, 2);

        List<Penalty> penalties = new ArrayList<>(
                Arrays.stream(PenaltyEnum.values()).map(PenaltyEnum::toPenalty).toList()
        );
        //Only one Solution is correct
        penalties.addAll(List.of(
                new Penalty(
                        "CorrectPlacePenalty",
                        (dataForConstraint -> {
                            var lesson = dataForConstraint.currentLesson();
                            if (lesson.lessonGene().subject().id().equals("s1")) {
                                if (lesson.cell().time().toIndex() != 0
                                        || !lesson.cell().audience().id().equals("a1")) {
                                    return CalculateResult.problem(-1.0, "Not correct place");
                                } else {
                                    return CalculateResult.ok();
                                }
                            }
                            if (lesson.lessonGene().subject().id().equals("s2")) {
                                if (lesson.cell().time().toIndex() != 1
                                        || !lesson.cell().audience().id().equals("a2")) {
                                    return CalculateResult.problem(-1.0, "Not correct place");
                                } else {
                                    return CalculateResult.ok();
                                }
                            }
                            if (lesson.lessonGene().subject().id().equals("s3")) {
                                if (lesson.cell().time().toIndex() != 3
                                        || !lesson.cell().audience().id().equals("a2")) {
                                    return CalculateResult.problem(-1.0, "Not correct place");
                                } else {
                                    return CalculateResult.ok();
                                }
                            } else {
                                return CalculateResult.ok();
                            }
                        }),
                        false
                )
        ));
        PenaltyChecker penaltyChecker = PenaltyChecker.newBuilder(timeSetting)
                .addPenalties(penalties).build();
        return Stream.of(
                new GeneticAlgorithmScheduler(
                        penaltyChecker,
                        timeSetting
                ),
                new BruteForceAlgorithm(penaltyChecker)
        );
    }
}
