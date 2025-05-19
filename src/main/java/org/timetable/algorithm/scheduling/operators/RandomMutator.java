package org.timetable.algorithm.scheduling.operators;

import io.jenetics.*;
import io.jenetics.util.ISeq;
import org.timetable.algorithm.model.AudienceTimeCell;
import org.timetable.algorithm.model.SubjectType;
import org.timetable.algorithm.scheduling.LessonGene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

public class RandomMutator extends Mutator<IntegerGene, Double> {

    private final ArrayList<LessonGene> lessonGenes;
    private final Map<SubjectType, List<AudienceTimeCell>> audienceMap;
    private final Map<AudienceTimeCell, Integer> audienceToIndex;

    public RandomMutator(ArrayList<LessonGene> lessonGenes,
                         Map<SubjectType, List<AudienceTimeCell>> audienceMap,
                         Map<AudienceTimeCell, Integer> audienceToIndex) {
        this.lessonGenes = lessonGenes;
        this.audienceMap = audienceMap;
        this.audienceToIndex = audienceToIndex;
    }

    public RandomMutator(ArrayList<LessonGene> lessonGenes,
                         Map<SubjectType, List<AudienceTimeCell>> audienceMap,
                         Map<AudienceTimeCell, Integer> audienceToIndex,
                         double probability) {
        super(probability);
        this.lessonGenes = lessonGenes;
        this.audienceMap = audienceMap;
        this.audienceToIndex = audienceToIndex;
    }

    @Override
    protected MutatorResult<Genotype<IntegerGene>> mutate(Genotype<IntegerGene> genotype, double p, RandomGenerator random) {
        List<MutatorResult<Chromosome<IntegerGene>>> mutatorResults = new ArrayList<>();

        for (int i = 0; i < genotype.length(); i++) {
            IntegerChromosome chromosome = genotype.get(i).as(IntegerChromosome.class);

            if (random.nextDouble() < p) {
                mutatorResults.add(mutate(chromosome, i, p, random));
            } else {
                mutatorResults.add(new MutatorResult<>(chromosome, 0));
            }
        }

        final ISeq<MutatorResult<Chromosome<IntegerGene>>> result = ISeq.of(mutatorResults);

        return new MutatorResult<>(
                Genotype.of(result.map(MutatorResult::result)),
                result.stream().mapToInt(MutatorResult::mutations).sum()
        );
    }


    protected MutatorResult<Chromosome<IntegerGene>> mutate(Chromosome<IntegerGene> chromosome,
                                                            int index,
                                                            double p,
                                                            RandomGenerator random) {

        var integerChromosome = chromosome.as(IntegerChromosome.class);
        List<MutatorResult<IntegerGene>> mutatorResults = new ArrayList<>();
        var gene = integerChromosome.gene();
        if (random.nextDouble() < p) {
            mutatorResults.add(new MutatorResult<>(mutate(gene, index, random), 1));
        } else {
            mutatorResults.add(new MutatorResult<>(gene, 0));
        }
        final ISeq<MutatorResult<IntegerGene>> result = ISeq.of(mutatorResults);

        return new MutatorResult<>(
                chromosome.newInstance(result.map(MutatorResult::result)),
                result.stream().mapToInt(MutatorResult::mutations).sum()
        );
    }


    private IntegerGene mutate(IntegerGene gene, int index, RandomGenerator random) {
        LessonGene lesson = lessonGenes.get(index);
        List<AudienceTimeCell> possibleAudiences = audienceMap.get(lesson.teacher().teacherType());
        int randomCellIndex = random.nextInt(0, possibleAudiences.size());
        var cell = audienceToIndex.get(possibleAudiences.get(randomCellIndex));
        return gene.newInstance(cell);
    }
}