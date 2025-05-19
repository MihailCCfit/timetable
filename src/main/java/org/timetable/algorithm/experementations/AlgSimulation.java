package org.timetable.algorithm.experementations;

import org.timetable.algorithm.constraints.Penalty;
import org.timetable.algorithm.constraints.PenaltyChecker;
import org.timetable.algorithm.constraints.PenaltyEnum;
import org.timetable.algorithm.model.*;
import org.timetable.algorithm.scheduling.GeneticAlgorithmScheduler;
import org.timetable.algorithm.scheduling.ScheduleResult;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlgSimulation {

    public static void main(String[] args) throws InterruptedException {
        var timeSetting = new TableTimeSetting(6, 7);
        GeneticAlgorithmScheduler geneticAlgorithmScheduler = new GeneticAlgorithmScheduler(
                PenaltyChecker.newBuilder(timeSetting)
                        .addPenalties(Arrays.stream(PenaltyEnum.values()).map(PenaltyEnum::toPenalty).toList()).build(),
                timeSetting
        );

        int audiencesAmount = 50;
        int lectureAmount = 8;

        List<AudienceEvolve> audienceEvolves = new ArrayList<>(audiencesAmount + lectureAmount);
        for (int i = 1; i <= audiencesAmount; i++) {
            audienceEvolves.add(new AudienceEvolve("S" + i, SubjectType.PRACTICAL));
        }
        for (int i = 1; i <= lectureAmount; i++) {
            audienceEvolves.add(new AudienceEvolve("L" + i, SubjectType.LECTURE));
        }

        int groups = 25;
        int studyPlans = 8;

        int subjectsPerPlan = 8;
        Map<Integer, List<GroupEvolve>> planNumberToGroups = new HashMap<>();
        Map<Integer, List<SubjectEvolve>> planToSubjects = new HashMap<>();
        Map<Integer, List<TeacherEvolve>> planToLectureTeachers = new HashMap<>();
        Map<Integer, List<TeacherEvolve>> planToSeminarTeachers = new HashMap<>();
        for (int i = 1; i <= studyPlans; i++) {
            planNumberToGroups.put(i, new ArrayList<>(groups / studyPlans));
            planToSubjects.put(i, new ArrayList<>(subjectsPerPlan));
            planToLectureTeachers.put(i, new ArrayList<>());
            planToSeminarTeachers.put(i, new ArrayList<>());
        }


        List<GroupEvolve> groupEvolves = new ArrayList<>(groups);
        for (int i = 1; i <= groups; i++) {
            int studyPlan = i % studyPlans + 1;
            var list = planNumberToGroups.get(studyPlan);
            var group = new GroupEvolve("g:" + i);
            list.add(group);
            groupEvolves.add(group);
        }

        int teachersAmount = 4800;
        int lectureTeachersAmount = 1600;
        List<TeacherEvolve> teacherEvolves = new ArrayList<>();
        List<TeacherEvolve> lectureTeachers = new ArrayList<>();
        List<TeacherEvolve> seminarTeachers = new ArrayList<>();
        for (int i = 1; i <= teachersAmount; i++) {
            int studyPlan = i % studyPlans + 1;
            TeacherEvolve teacherEvolve = new TeacherEvolve("ts" + i, SubjectType.PRACTICAL);
            teacherEvolves.add(teacherEvolve);
            seminarTeachers.add(teacherEvolve);
            planToSeminarTeachers.get(studyPlan).add(teacherEvolve);

        }

        for (int i = 1; i <= lectureTeachersAmount; i++) {
            int studyPlan = i % studyPlans + 1;
            TeacherEvolve teacherEvolve = new TeacherEvolve("tl" + i, SubjectType.LECTURE);
            teacherEvolves.add(teacherEvolve);
            lectureTeachers.add(teacherEvolve);
            planToLectureTeachers.get(studyPlan).add(teacherEvolve);
        }

        planToSubjects.forEach((plaNumber, lsubjList) -> {
            List<TeacherEvolve> lectureT = new ArrayList<>(planToLectureTeachers.get(plaNumber));
            List<TeacherEvolve> seminarT = new ArrayList<>(planToSeminarTeachers.get(plaNumber));
            var plGroups = planNumberToGroups.get(plaNumber);


            for (int i = 1; i <= subjectsPerPlan; i++) {
                Map<String, TeacherEvolve> teacherEvolveMap = new HashMap<>();
                for (GroupEvolve plGroup : plGroups) {
                    teacherEvolveMap.put(plGroup.id(), seminarT.remove(seminarT.size() - 1));
                }
                SubjectEvolve subjectEvolve = new SubjectEvolve("sj:" + plaNumber + ":" + i, 1,
                        1, teacherEvolveMap, lectureT.remove(lectureT.size() - 1));
                lsubjList.add(subjectEvolve);
            }
        });
        List<StudyPlanEvolve> plansList = new ArrayList<>();
        for (int i = 1; i <= studyPlans; i++) {
            StudyPlanEvolve studyPlanEvolve = new StudyPlanEvolve(planToSubjects.get(i), planNumberToGroups.get(i));
            plansList.add(studyPlanEvolve);
        }
        Instant instant = Instant.now();
        System.out.println("Start");
        ExecutorService service = Executors.newSingleThreadExecutor();
        var algorithmStatus = geneticAlgorithmScheduler.asyncStart(plansList, audienceEvolves, service, timeSetting);
        algorithmStatus.getResult()
                .thenAccept(results -> {
                    Instant after = Instant.now();
                    System.out.println((double) (Date.from(after).getTime() - Date.from(instant).getTime()) / 1000);
                    System.out.println("total penalty: " + results.get(0).checkResult().total());
                    ScheduleResult firstSchedule = results.get(0);
                    System.out.println(firstSchedule.checkResult());
                    List<LessonWithTime> firstAllLessons = firstSchedule.allLessons();
                    firstAllLessons.sort((o1, o2) -> {
                        var t1 = o1.time();
                        var t2 = o2.time();
                        if (t1.day() != t2.day()) {
                            return Integer.compare(t1.day(), t2.day());
                        }
                        if (t1.cellNumber() != t2.cellNumber()) {
                            return Integer.compare(t1.cellNumber(), t2.cellNumber());
                        }
                        return 0;
                    });
                    List<LessonWithTime> firstLessonForGroup1 = firstAllLessons.stream()
                            .filter(lesson -> lesson.groups().stream().map(GroupEvolve::id).toList().contains("g:1"))
                            .toList();
                    for (LessonWithTime lessons : firstAllLessons) {
                        System.out.println(lessons.time() + ": " + lessons);
                    }


                    var checkResult = results.get(0).checkResult();
                    for (Map.Entry<Penalty, PenaltyChecker.PenaltyResult> penaltyPenaltyResultEntry : checkResult.penaltyToError().entrySet()) {
                        Penalty penalty = penaltyPenaltyResultEntry.getKey();
                        PenaltyChecker.PenaltyResult penResult = penaltyPenaltyResultEntry.getValue();
                        System.out.println(penalty + ":" + penResult);
                    }
                    System.out.println("\n\nGROUP1");
                    for (LessonWithTime lessons : firstLessonForGroup1) {
                        System.out.println(lessons.time() + ": " + lessons);
                    }
                }).join();
        while (!geneticAlgorithmScheduler.algorithmProcessingStatus.getResult().isDone()) {
            var perc = geneticAlgorithmScheduler.algorithmProcessingStatus.getPercentage();
            System.out.println(perc);
        }
        var status = geneticAlgorithmScheduler.algorithmProcessingStatus;
        while (true) {
            System.out.println(status.getPercentage());
            if (status.getResult().isDone()) {
                break;
            }
            Thread.sleep(50);
        }
        service.shutdown();
    }
}
