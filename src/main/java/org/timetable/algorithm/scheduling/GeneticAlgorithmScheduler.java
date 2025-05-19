package org.timetable.algorithm.scheduling;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStart;
import io.jenetics.util.*;
import lombok.NoArgsConstructor;
import org.timetable.algorithm.constraints.PenaltyChecker;
import org.timetable.algorithm.model.*;
import org.timetable.algorithm.scheduling.operators.RandomMutator;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;


public class GeneticAlgorithmScheduler implements AlgorithmScheduler {
    private ArrayList<AudienceTimeCell> cells = new ArrayList<>();
    private Map<SubjectType, List<AudienceTimeCell>> audienceMap = new HashMap<>();
    private Map<AudienceTimeCell, Integer> audienceToIndex = new HashMap<>();
    private ArrayList<LessonGene> lessonGenes = new ArrayList<>();
    private int satisfiedScheduleAmount = 3;
    private PenaltyChecker penaltyChecker;
    private Map<GroupEvolve, List<Integer>> groupToIndexes = new HashMap<>();
    private TableTimeSetting tableTimeSetting;
    Consumer<PenaltyChecker.CheckResult> observer = (c -> {});

    public GeneticAlgorithmScheduler(PenaltyChecker penaltyChecker, TableTimeSetting tableTimeSetting) {
        this.penaltyChecker = penaltyChecker;
        this.tableTimeSetting = tableTimeSetting;
    }

    public GeneticAlgorithmScheduler(PenaltyChecker penaltyChecker,
                                     TableTimeSetting tableTimeSetting,
                                     int satisfiedScheduleAmount,
                                     Consumer<PenaltyChecker.CheckResult> observer) {
        this.penaltyChecker = penaltyChecker;
        this.satisfiedScheduleAmount = satisfiedScheduleAmount;
        this.observer = observer;
    }

    public final AlgorithmProcessingStatus algorithmProcessingStatus = new AlgorithmProcessingStatus();

    public AlgorithmProcessingStatus asyncStart(List<StudyPlanEvolve> studyPlanEvolves,
                                                List<AudienceEvolve> audiences,
                                                Executor executor,
                                                TableTimeSetting tableTimeSetting) {
        executor.execute(() -> this.algorithm(studyPlanEvolves, audiences, tableTimeSetting));
        return algorithmProcessingStatus;
    }

    /**
     * @param studyPlanEvolves
     * @param audiences
     * @return few schedules
     */
    @Override
    public List<ScheduleResult> algorithm(List<StudyPlanEvolve> studyPlanEvolves,
                                          List<AudienceEvolve> audiences,
                                          TableTimeSetting tableTimeSetting) {
        this.tableTimeSetting = tableTimeSetting;
        algorithmProcessingStatus.running = true;
        cells.clear();
        lessonGenes.clear();
        prepareData(studyPlanEvolves, audiences);
        final Factory<Genotype<IntegerGene>> gtf =
                Genotype.of(IntegerChromosome.of(0, cells.size()), lessonGenes.size());
        final Engine<IntegerGene, Double> engine = Engine
                .builder(this::fitness, gtf)
                .populationSize(300)
                .alterers(new RandomMutator(lessonGenes, audienceMap, audienceToIndex))
                .build();
        var evolutionResult = engine.evolve(EvolutionStart.empty());
        int goodPhenotypeCounter = 0;
        int maxCounter = 12000;
        double prev = -1e10;
        int i = 0;
        algorithmProcessingStatus.percentage = 0;
        var initialCheck = penaltyChecker.calculatePenalty(phenotypeToLessons(evolutionResult.bestPhenotype()));
        algorithmProcessingStatus.checkResult = initialCheck;
        double initialMaximum = initialCheck.total();
        while (goodPhenotypeCounter < satisfiedScheduleAmount) {
            evolutionResult = engine.evolve(evolutionResult.next());
            goodPhenotypeCounter = 0;
            double bestResult = evolutionResult.bestFitness();
            if (bestResult <= prev) {
                maxCounter--;
            } else {
                prev = bestResult;
                i++;
                algorithmProcessingStatus.percentage = initialMaximum != 0 ? 1 - bestResult / initialMaximum : 0;
                if (i >= 2) {
                    i = 0;
                    PenaltyChecker.CheckResult checkResult =
                            penaltyChecker.calculatePenalty(phenotypeToLessons(evolutionResult.bestPhenotype()));
                    observer.accept(checkResult);

                    algorithmProcessingStatus.checkResult = checkResult;
                }
                maxCounter = 12000;
            }
            if (bestResult >= 0) {
                for (Phenotype<IntegerGene, Double> phenotype : evolutionResult.population()) {
                    if (phenotype.fitness() >= 0) {
                        goodPhenotypeCounter++;
                        if (goodPhenotypeCounter >= satisfiedScheduleAmount) {
                            break;
                        }
                    }
                }
            }
            if (maxCounter <= 0) {
                break;
            }
        }
        List<ScheduleResult> sortedResults = evolutionResult
                .population()
                .stream()
                .sorted((ph1, ph2) -> Double.compare(ph2.fitness(), ph1.fitness()))
                .limit(satisfiedScheduleAmount)
                .map(this::phenotypeToLessons)
                .map((List<LessonWithTime> allLessons) ->
                        new ScheduleResult(allLessons, penaltyChecker.calculatePenalty(allLessons)))
                .toList();
        algorithmProcessingStatus.result.complete(sortedResults);
        algorithmProcessingStatus.running = false;
        return sortedResults;
    }

    private List<LessonWithTime> phenotypeToLessons(Phenotype<IntegerGene, Double> phenotype) {
        var gt = phenotype.genotype();
        List<LessonWithTime> timeList = new ArrayList<>();
        for (int i = 0; i < gt.length(); i++) {
            LessonGene lesson = lessonGenes.get(i);
            int cellIndex = gt.get(i).gene().intValue();
            AudienceTimeCell audCell = cells.get(cellIndex);
            LessonWithTime lessonWithTime = new LessonWithTime(audCell, lesson);
            timeList.add(lessonWithTime);
        }
        return timeList;
    }

    private void prepareData(List<StudyPlanEvolve> studyPlanEvolves, List<AudienceEvolve> audiences) {
        studyPlanEvolves.forEach(studyPlanEvolve -> {
            for (SubjectEvolve subjectEvolve : studyPlanEvolve.subjectEvolves()) {
                if (subjectEvolve.lectureAmount() != 0) {
                    LessonGene lessonGene = new LessonGene(new ArrayList<>(studyPlanEvolve.groupEvolves()),
                            subjectEvolve.lectureTeacherEvolve(), subjectEvolve);
                    for (GroupEvolve group : lessonGene.groups()) {
                        if (!groupToIndexes.containsKey(group)) {
                            groupToIndexes.put(group, new ArrayList<>());
                        }
                        groupToIndexes.get(group).add(lessonGenes.size());
                    }
                    lessonGenes.add(lessonGene);
                }
                for (GroupEvolve groupEvolve : studyPlanEvolve.groupEvolves()) {
                    for (int subId = 0; subId < subjectEvolve.seminarAmount(); subId++) {
                        LessonGene lessonGene = new LessonGene(groupEvolve,
                                subjectEvolve.teacherToGroup().get(groupEvolve.id()),
                                subjectEvolve.withSubId(subId));
                        for (GroupEvolve group : lessonGene.groups()) {
                            if (!groupToIndexes.containsKey(group)) {
                                groupToIndexes.put(group, new ArrayList<>());
                            }
                            groupToIndexes.get(group).add(lessonGenes.size());
                        }
                        lessonGenes.add(lessonGene);
                    }
                }
            }
        });
        int times = tableTimeSetting.maxDays() * tableTimeSetting.maxCells();
        int lastIndex = 0;

        for (AudienceEvolve auditory : audiences) {
            for (int i = 0; i < times; i++) {
                AudienceTimeCell cell = new AudienceTimeCell(auditory, i, tableTimeSetting);
                audienceToIndex.put(cell, lastIndex++);
                cells.add(cell);
                if (!audienceMap.containsKey(cell.audience().auditoryType())) {
                    audienceMap.put(cell.audience().auditoryType(), new ArrayList<>());
                }
                audienceMap.get(cell.audience().auditoryType()).add(cell);

            }
        }
    }

    private Double fitness(Genotype<IntegerGene> gt) {
        Map<Integer, List<LessonWithTime>> subjectCells = new HashMap<>();
        for (int i = 0; i < gt.length(); i++) {
            LessonGene lessonGene = lessonGenes.get(i);
            int cellIndex = gt.get(i).gene().intValue();
            AudienceTimeCell audCell = cells.get(cellIndex);
            LessonWithTime subjectCell = new LessonWithTime(audCell, lessonGene);
            if (!subjectCells.containsKey(audCell.time().toIndex())) {
                subjectCells.put(audCell.time().toIndex(), new ArrayList<>());
            }
            var timeList = subjectCells.get(audCell.time().toIndex());
            timeList.add(subjectCell);
        }
        double penalty = 0;


        for (Map.Entry<Integer, List<LessonWithTime>> integerListEntry : subjectCells.entrySet()) {
            List<LessonWithTime> timeCells = integerListEntry.getValue();
            penalty += penaltyChecker.calculatePenalty(timeCells).total();
        }
        return penalty;
    }
}
