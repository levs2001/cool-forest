package ru.leo.forest.tree.cool;

import com.expleague.ml.BFGrid;
import com.expleague.ml.models.ModelTools;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import ru.leo.forest.util.ByteMapImpl;

// TODO: От scale и bias надо потихоньку избавляться,
//  и еще посмотреть влияет ли он как-то на скорость
public class CoolForestFactory {
    private final int supportedTrigger;
    private final double supportedFeatureFreq;

    public CoolForestFactory(int supportedTrigger, double supportedFeatureFreq) {
        this.supportedTrigger = supportedTrigger;
        this.supportedFeatureFreq = supportedFeatureFreq;
    }

    public static CoolForest makeCoolForest(
        ModelTools.CompiledOTEnsemble monoforest,
        int monomsToMakeSupported,
        double supportedFeatureFreq
    ) {
        var factory = new CoolForestFactory(monomsToMakeSupported, supportedFeatureFreq);
        return factory.createScaledCoolForest(monoforest);
    }

    public CoolForest createScaledCoolForest(ModelTools.CompiledOTEnsemble monoforest) {
        return new ScaledCoolForest(
            monoforest.getGrid(),
            fromMonoforest(monoforest),
            monoforest.scale(),
            monoforest.bias()
        );
    }

    private CoolForest fromMonoforest(ModelTools.CompiledOTEnsemble monoforest) {
        var grid = monoforest.getGrid();
        var monoms = monoforest.getEntries().stream().map(e -> FullMonom.fromEntry(e, grid)).toList();

        return fromMonoms(monoms, grid);
    }

    private CoolForest fromMonoms(List<FullMonom> monoms, BFGrid bfGrid) {
        if (monoms.size() < supportedTrigger) {
            return new CoolForestSimple(bfGrid, monoms);
        }

        var featureUsage = getFeatureUsageFreq(monoms);
        var mostCommonFeature = featureUsage.int2IntEntrySet().stream()
            .max(Comparator.comparingInt(Int2IntMap.Entry::getIntValue)).get();
        int freq = mostCommonFeature.getIntValue();
        if ((double) freq / monoms.size() < supportedFeatureFreq) {
            return new CoolForestSimple(bfGrid, monoms);
        }
        // TODO: Появляются мономы с одним условием, что весьма бесполезно?

        int featureIdx = mostCommonFeature.getIntKey();
        // TODO: Проверить ситуации, когда размер extra 0. Это можно оптимизнуть.
        List<FullMonom> extra = new ArrayList<>();
        Int2ObjectMap<List<FullMonom>> byFVGroups = new Int2ObjectArrayMap<>();
        for (FullMonom monom : monoms) {
            int featureBin = monom.featureBin(featureIdx);
            if (featureBin != FullMonom.NO_FEATURE) {
                // TODO: Моном с чистым vslue тоже становится мономом.
                byFVGroups.computeIfAbsent(featureBin, k -> new ArrayList<>())
                    .add(monom.without(featureIdx));
            } else {
                extra.add(monom);
            }
        }

        // Int2ObjectArrayMap ищит ключ просто обходя массив с конца, здесь лучше использовать ассоциативный массив ByteMapImpl
        Int2ObjectMap<CoolForest> byFvFroupsForests = new ByteMapImpl<>();
        byFVGroups.forEach((bin, ms) -> byFvFroupsForests.put((int) bin, fromMonoms(ms, bfGrid)));

        return new CoolForestSupported(
            bfGrid,
            featureIdx,
            byFvFroupsForests,
            fromMonoms(extra, bfGrid)
        );
    }

//    там нечено оптимайзить, одинаковые условия monoforest сам отсеивает
    private static List<FullMonom> optimize(List<FullMonom> monoms) {
        Int2ObjectMap<List<FullMonom>> conditionGroups = new Int2ObjectOpenHashMap<>();
        monoms.forEach(
            m -> conditionGroups.computeIfAbsent(m.conditionsHashCode(), ign -> new ArrayList<>()).add(m)
        );

        if (monoms.size() == conditionGroups.size()) {
            System.out.println("Not optimized");
            return monoms;
        }

        List<FullMonom> result = new ArrayList<>();
        for (var entry : conditionGroups.int2ObjectEntrySet()) {
            var group = entry.getValue();
            var first = group.getFirst();
            double sum = 0;
            for (var m : group) {
                if (Arrays.equals(first.bfIndices(), m.bfIndices())) {
                    throw new IllegalStateException("Bad hash code for monom conditions");
                }
                sum += m.value();
            }
            result.add(
                new FullMonom(
                    first.featureIndices(),
                    first.featureBins(),
                    first.bfIndices(),
                    sum
                )
            );
        }
        System.out.printf("Optimized, %d -> %d", monoms.size(), result.size());
        return result;
    }

    private static Int2IntMap getFeatureUsageFreq(List<FullMonom> monoms) {
        Int2IntMap featureFreq = new Int2IntOpenHashMap();
        for (var monom : monoms) {
            for (int feature : monom.featureIndices()) {
                featureFreq.put(feature, featureFreq.getOrDefault(feature, 0) + 1);
            }
        }

        return featureFreq;
    }
}
