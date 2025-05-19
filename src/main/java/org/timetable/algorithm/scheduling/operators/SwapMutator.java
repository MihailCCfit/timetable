package org.timetable.algorithm.scheduling.operators;

import io.jenetics.*;
import io.jenetics.internal.math.Randoms;
import io.jenetics.util.MSeq;

import java.util.random.RandomGenerator;

public class SwapMutator extends Mutator<IntegerGene, Double> {
    public SwapMutator(double probability) {
        super(probability);
    }

    public SwapMutator() {
        this(0.05);
    }

    @Override
    protected MutatorResult<Genotype<IntegerGene>> mutate(Genotype<IntegerGene> genotype, double p, RandomGenerator random) {
        MutatorResult<Genotype<IntegerGene>> result;
        if (genotype.length() > 1) {
            var genes = MSeq.of(genotype.chromosome());
            int mutations = (int) Randoms.indexes(random, genes.length(), p).peek((i) -> {
                genes.swap(i, random.nextInt(genes.length()));
            }).count();
            result = new MutatorResult<>(Genotype.of(genotype.chromosome().newInstance(genes.toISeq())), mutations);
        } else {
            result = new MutatorResult<>(genotype, 0);
        }

        return result;
    }
}