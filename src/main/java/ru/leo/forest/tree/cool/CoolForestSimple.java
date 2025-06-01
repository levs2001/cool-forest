package ru.leo.forest.tree.cool;

import com.expleague.ml.BFGrid;
import com.expleague.ml.data.impl.BinarizedDataSet;
import java.util.List;

/**
 * Просто набор мономов
 */
public final class CoolForestSimple extends CoolForest.Stub {
    private final List<FullMonom> monoms;

    public CoolForestSimple(BFGrid grid, List<FullMonom> monoms) {
        super(grid);
        this.monoms = monoms;
    }

    @Override
    public double value(BinarizedDataSet bds, int index) {
        double result = 0;
        for (final FullMonom monom : monoms) {
            final int[] featureIndices = monom.featureIndices();
            final int[] monomBins = monom.featureBins();
            double increment = monom.bias();
            for (int j = 0; j < featureIndices.length; j++) {
                if (bds.bins(featureIndices[j])[index] <= monomBins[j]) {
                    increment = 0;
                    break;
                }
            }
            result += increment;
        }

        return result;
    }

    @Override
    public void add(BinarizedDataSet bds, double[] value) {
        for (final FullMonom monom : monoms) {
            final int[] featureIndices = monom.featureIndices();
            final int[] monomBins = monom.featureBins();
            double increment = monom.bias();
            for (int index = 0; index < value.length; index++) {
                boolean isIncrement = true;
                for (int j = 0; j < featureIndices.length; j++) {
                    if (bds.bins(featureIndices[j])[index] <= monomBins[j]) {
                        isIncrement = false;
                        break;
                    }
                }
                if (isIncrement) {
                    value[index] += increment;
                }
            }
        }
    }
}
