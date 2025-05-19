package org.timetable.algorithm;

import org.timetable.algorithm.model.*;
import org.timetable.algorithm.scheduling.AlgorithmScheduler;
import org.timetable.algorithm.scheduling.LessonGene;
import org.timetable.algorithm.scheduling.ScheduleResult;
import org.timetable.algorithm.constraints.PenaltyChecker;

import java.util.*;

public class BruteForceAlgorithm implements AlgorithmScheduler {
    private final PenaltyChecker penaltyChecker;
    private final ArrayList<AudienceTimeCell> cells = new ArrayList<>();
    private final ArrayList<LessonGene> lessonGenes = new ArrayList<>();

    public BruteForceAlgorithm(PenaltyChecker penaltyChecker) {
        this.penaltyChecker = penaltyChecker;
    }


    @Override
    public List<ScheduleResult> algorithm(List<StudyPlanEvolve> studyPlanEvolves,
                                          List<AudienceEvolve> audiences,
                                          TableTimeSetting tableTimeSetting) {

        int times = tableTimeSetting.maxDays() * tableTimeSetting.maxCells();
        for (AudienceEvolve auditory : audiences) {
            for (int i = 0; i < times; i++) {
                AudienceTimeCell cell = new AudienceTimeCell(auditory, i, tableTimeSetting);
                cells.add(cell);
            }
        }


        studyPlanEvolves.forEach(studyPlanEvolve -> {
            for (SubjectEvolve subjectEvolve : studyPlanEvolve.subjectEvolves()) {
                if (subjectEvolve.lectureAmount() != 0) {
                    LessonGene lessonGene = new LessonGene(new ArrayList<>(studyPlanEvolve.groupEvolves()),
                            subjectEvolve.lectureTeacherEvolve(), subjectEvolve);
                    lessonGenes.add(lessonGene);
                }
                for (GroupEvolve groupEvolve : studyPlanEvolve.groupEvolves()) {
                    for (int subId = 0; subId < subjectEvolve.seminarAmount(); subId++) {
                        LessonGene lessonGene = new LessonGene(groupEvolve,
                                subjectEvolve.teacherToGroup().get(groupEvolve.id()),
                                subjectEvolve.withSubId(subId));
                        lessonGenes.add(lessonGene);
                    }
                }
            }
        });

        ArrayList<Integer> lessonToCell = new ArrayList<>();
        for (int i = 0; i < lessonGenes.size(); i++) {
            lessonToCell.add(0);
        }

        int cellAmount = cells.size();
        ScheduleResult bestResult = null;
        for (int i = 0; i < (int) Math.pow(cells.size(), lessonGenes.size()); i++) {
            int val = i;
            for (int j = 0; j < lessonGenes.size(); j++) {
                lessonToCell.set(j, val % cellAmount);
                val = val / cellAmount;
            }

            List<LessonWithTime> lessonWithTimes = new ArrayList<>();
            for (int j = 0; j < lessonToCell.size(); j++) {
                LessonWithTime lesson = new LessonWithTime(cells.get(lessonToCell.get(j)), lessonGenes.get(j));
                lessonWithTimes.add(lesson);
            }
            ScheduleResult scheduleResult = new ScheduleResult(lessonWithTimes,
                    penaltyChecker.calculatePenalty(lessonWithTimes));
            if (bestResult != null) {
                if (scheduleResult.checkResult().total() > bestResult.checkResult().total()) {
                    bestResult = scheduleResult;
                }
            } else {
                bestResult = scheduleResult;
            }
            if (bestResult.checkResult().total() >= 0) {
                return List.of(bestResult);
            }
        }

        return List.of(bestResult);
    }
}
