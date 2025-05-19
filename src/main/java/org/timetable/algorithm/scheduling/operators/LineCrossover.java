package org.timetable.algorithm.scheduling.operators;

import io.jenetics.Crossover;
import io.jenetics.IntegerGene;
import io.jenetics.NumericGene;
import io.jenetics.util.MSeq;
import io.jenetics.util.RandomRegistry;

import java.util.random.RandomGenerator;

public class LineCrossover extends Crossover<IntegerGene, Double> {

    protected LineCrossover() {
        super(0.001);
    }

    @Override
    protected int crossover(MSeq<IntegerGene> v, MSeq<IntegerGene> w) {
        RandomGenerator random = RandomRegistry.random();
        double min = ((NumericGene<?, ?>) v.get(0)).min().doubleValue();
        double max = ((NumericGene<?, ?>) v.get(0)).max().doubleValue();
        double a = random.nextDouble(0, 1.0);
        double b = random.nextDouble(0, 1.0);
        boolean changed = false;
        int i = 0;

        for (int n = Math.min(v.length(), w.length()); i < n; ++i) {
            double vi = ((NumericGene<?, ?>) v.get(i)).doubleValue();
            double wi = ((NumericGene<?, ?>) w.get(i)).doubleValue();
            double t = a * vi + (1.0 - a) * wi;
            double s = b * wi + (1.0 - b) * vi;
            if (t >= min && s >= min && t < max && s < max) {
                v.set(i, v.get(i).newInstance(t));
                w.set(i, w.get(i).newInstance(s));
                changed = true;
            }
        }

        return changed ? 2 : 0;
    }
}