package ru.leo.forest.tree.cool;

import com.expleague.ml.BFGrid;
import com.expleague.ml.models.ModelTools;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;

public record FullMonom(int[] featureIndices, int[] featureBins, int[] bfIndices, double value) {
    public static final int NO_FEATURE = -1;

    // TODO: Выяснить почему в jmll bin не byte, а int
    // TODO: Если построение CoolForest будем долним сделать сет для featureIndices
    public static FullMonom fromEntry(ModelTools.CompiledOTEnsemble.Entry entry, BFGrid grid) {
        int[] bfIndices = new int[entry.getBfIndices().length];
        int[] featureIdx = new int[bfIndices.length];
        int[] featureBins = new int[bfIndices.length];
        for (int i = 0; i < bfIndices.length; i++) {
            int bfIndex = entry.getBfIndices()[i];
            bfIndices[i] = bfIndex;
            var feature = grid.bf(bfIndex);
            featureIdx[i] = feature.findex();
            featureBins[i] = feature.bin();
        }

        return new FullMonom(featureIdx, featureBins, bfIndices, entry.getValue());
    }

    // TODO: Make better hash if need
    public int conditionsHashCode() {
        return Arrays.toString(bfIndices).hashCode();
    }

    public boolean isSameCondition(FullMonom other) {
        return Objects.deepEquals(this.bfIndices, other.bfIndices);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FullMonom fullMonom = (FullMonom) o;
        return Double.compare(value, fullMonom.value) == 0 && Objects.deepEquals(bfIndices, fullMonom.bfIndices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(featureIndices), Arrays.hashCode(featureBins), Arrays.hashCode(bfIndices), value);
    }

    public FullMonom without(int featureIdx) {
        int deleteIndex = getIndex(featureIdx);
        if (deleteIndex == NO_FEATURE) {
            throw new NoSuchElementException("No feature: " + featureIdx);
        }
        int shortedLength = featureIndices.length - 1;
        int[] shortedFeatureIndices = new int[shortedLength];
        int[] shortedFeatureBins = new int[shortedLength];
        int[] shortedBfIndices = new int[shortedLength];
        for (int i = 0; i < featureIndices.length; i++) {
            if (i == deleteIndex) {
                continue;
            }
            int realIndex = i > deleteIndex ? i - 1 : i;
            shortedFeatureIndices[realIndex] = featureIndices[i];
            shortedFeatureBins[realIndex] = featureBins[i];
            shortedBfIndices[realIndex] = bfIndices[i];
        }

        return new FullMonom(shortedFeatureIndices, shortedFeatureBins, shortedBfIndices, value);
    }

    public int bin(int index) {
        return featureBins[index];
    }

    public int featureBin(int featureIdx) {
        int index = getIndex(featureIdx);
        return index == NO_FEATURE ? NO_FEATURE : featureBins[index];
    }

    private int getIndex(int featureIdx) {
        for (int i = 0; i < featureIndices.length; i++) {
            if (featureIndices[i] == featureIdx) {
                return i;
            }
        }

        return NO_FEATURE;
    }


    public boolean containsFeature(int featureIdx) {
        return ArrayUtils.contains(featureIndices, featureIdx);
    }
}
