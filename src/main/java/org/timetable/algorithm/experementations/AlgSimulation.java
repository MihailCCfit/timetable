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
        //6 - число дней в неделю (некоторый цикл), 7 - число временных ячеек в течении дня
        var timeSetting = new TableTimeSetting(6, 7);

        //Далее идет однотипная генерация похожих вещей, можно настраивать разные числа

        int audiencesAmount = 50; // Число аудиторий для практики
        int lectureAmount = 8; // Число лекционных аудиторий


        //Заполнение аудиторий
        List<AudienceEvolve> audienceEvolves = new ArrayList<>(audiencesAmount + lectureAmount);
        for (int i = 1; i <= audiencesAmount; i++) {
            audienceEvolves.add(new AudienceEvolve("S" + i, SubjectType.PRACTICAL));
        }
        for (int i = 1; i <= lectureAmount; i++) {
            audienceEvolves.add(new AudienceEvolve("L" + i, SubjectType.LECTURE));
        }

        int groups = 25; //группы
        int studyPlans = 8; // число планов (чтобы группы не пересекались по лекциям)

        int subjectsPerPlan = 8; //Число предметов на каждую группу (на учебный план)
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

        //Формирование групп
        for (int i = 1; i <= groups; i++) {
            int studyPlan = i % studyPlans + 1;
            var list = planNumberToGroups.get(studyPlan);
            var group = new GroupEvolve("g:" + i);
            list.add(group);
        }

        //Создание учителей
        int teachersAmount = 4800;
        int lectureTeachersAmount = 1600;
        for (int i = 1; i <= teachersAmount; i++) {
            int studyPlan = i % studyPlans + 1;
            TeacherEvolve teacherEvolve = new TeacherEvolve("ts" + i, SubjectType.PRACTICAL);
            planToSeminarTeachers.get(studyPlan).add(teacherEvolve);

        }

        for (int i = 1; i <= lectureTeachersAmount; i++) {
            int studyPlan = i % studyPlans + 1;
            TeacherEvolve teacherEvolve = new TeacherEvolve("tl" + i, SubjectType.LECTURE);
            planToLectureTeachers.get(studyPlan).add(teacherEvolve);
        }

        //Заполнение учебных планов и предметов
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
        //Начало работы
        Instant instant = Instant.now();
        System.out.println("Start");
        ExecutorService service = Executors.newSingleThreadExecutor();

        //Создание класса составления расписания
        GeneticAlgorithmScheduler geneticAlgorithmScheduler = new GeneticAlgorithmScheduler(
                PenaltyChecker.newBuilder(timeSetting)
                        .addPenalties(Arrays.stream(PenaltyEnum.values()).map(PenaltyEnum::toPenalty).toList()).build(),
                timeSetting,
                3,
                (checkResult -> {
                    checkResult.penaltyToError().forEach((pen, res) -> {
                        System.out.println(pen + ":" + res.getSummaryPenalty());
                    });
                })
        );
        var algorithmStatus = geneticAlgorithmScheduler.asyncStart(plansList, audienceEvolves, service, timeSetting);
        algorithmStatus.getResult()
                .thenAccept(results -> { //Асинхронный вывод по получению результата
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
                    //вывод всех занятий
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
        // Если сверху убрать join, то можно посмотреть как выводятся проценты - прогресс выполнения

//        while (!geneticAlgorithmScheduler.algorithmProcessingStatus.getResult().isDone()) {
//            var perc = geneticAlgorithmScheduler.algorithmProcessingStatus.getPercentage();
//            System.out.println(perc);
//        }
//        var status = geneticAlgorithmScheduler.algorithmProcessingStatus;
//        while (true) {
//            System.out.println(status.getPercentage());
//            if (status.getResult().isDone()) {
//                break;
//            }
//            Thread.sleep(50);
//        }
        service.shutdown();
    }
}
