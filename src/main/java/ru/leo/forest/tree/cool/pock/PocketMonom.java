package ru.leo.forest.tree.cool.pock;

import com.expleague.ml.BFGrid;
import com.expleague.ml.models.ModelTools;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;

// TODO: Убрать лишнее
public record PocketMonom(int[] featureIndices, int[] featureBins, float bias) {
    public static final int NO_FEATURE = -1;

    // TODO: Выяснить почему в jmll bin не byte, а int
    // TODO: Если построение CoolForest будем долним сделать сет для featureIndices
    public static PocketMonom fromEntry(ModelTools.CompiledOTEnsemble.Entry entry, BFGrid grid) {
        int[] bfIndices = new int[entry.getBfIndices().length];
        int[] featureIdx = new int[bfIndices.length];
        int[] featureBins = new int[bfIndices.length];
        for (int i = 0; i < bfIndices.length; i++) {
            int bfIndex = entry.getBfIndices()[i];
            bfIndices[i] = bfIndex;
            var feature = grid.bf(bfIndex);
            featureIdx[i] = feature.findex();
            featureBins[i] = feature.bin();
            if (feature.bin() < 0 || feature.bin() > 255) {
                throw new IllegalStateException();
            }
        }

        return new PocketMonom(featureIdx, featureBins, (float) entry.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PocketMonom fullMonom = (PocketMonom) o;
        return Float.compare(bias, fullMonom.bias) == 0
            && Arrays.equals(featureIndices, fullMonom.featureIndices)
            && Arrays.equals(featureBins, fullMonom.featureBins);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(featureIndices), Arrays.hashCode(featureBins), bias);
    }

    public PocketMonom without(int featureIdx) {
        int deleteIndex = getIndex(featureIdx);
        if (deleteIndex == NO_FEATURE) {
            throw new NoSuchElementException("No feature: " + featureIdx);
        }
        int shortedLength = featureIndices.length - 1;
        int[] shortedFeatureIndices = new int[shortedLength];
        int[] shortedFeatureBins = new int[shortedLength];
        for (int i = 0; i < featureIndices.length; i++) {
            if (i == deleteIndex) {
                continue;
            }
            int realIndex = i > deleteIndex ? i - 1 : i;
            shortedFeatureIndices[realIndex] = featureIndices[i];
            shortedFeatureBins[realIndex] = featureBins[i];
        }

        return new PocketMonom(shortedFeatureIndices, shortedFeatureBins, bias);
    }

    public PocketMonom slice(int featureIdx, int featureBin) {
        int slicedFeatureIndex = getIndex(featureIdx);
        if (slicedFeatureIndex == NO_FEATURE) {
            return this;
        }
        if (featureBin <= featureBins[slicedFeatureIndex]) {
            // Условие не выполняется, можно не учитывать моном.
            return null;
        }

        int shortedLength = featureIndices.length - 1;
        if (shortedLength == 0) {
            return onlyBias(bias);
        }

        int[] shortedFeatureIndices = new int[shortedLength];
        int[] shortedFeatureBins = new int[shortedLength];
        for (int i = 0; i < featureIndices.length; i++) {
            if (i == slicedFeatureIndex) {
                continue;
            }
            int realIndex = i > slicedFeatureIndex ? i - 1 : i;
            shortedFeatureIndices[realIndex] = featureIndices[i];
            shortedFeatureBins[realIndex] = featureBins[i];
        }

        return new PocketMonom(shortedFeatureIndices, shortedFeatureBins, bias);
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

    public boolean isIncludedIn(IntList featureIndices) {
        for (int i = 0; i < this.featureIndices.length; i++) {
            if (i >= featureIndices.size() || this.featureIndices[i] != featureIndices.getInt(i)) {
                return false;
            }
        }
        return true;
    }

    public boolean isOnlyBias() {
        return featureIndices.length == 0;
    }

    public boolean containsFeature(int featureIdx) {
        return ArrayUtils.contains(featureIndices, featureIdx);
    }

    static PocketMonom onlyBias(float bias) {
        return new PocketMonom(EMPTY, EMPTY, bias);
    }

    private static final int[] EMPTY = new int[0];

    @Override
    public String toString() {
        return ArrayUtils.toString(featureIndices) + " " + ArrayUtils.toString(featureBins) + " " + bias;
    }
}
