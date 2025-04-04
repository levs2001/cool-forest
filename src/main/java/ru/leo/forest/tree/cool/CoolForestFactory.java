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

// TODO: От scale и bias надо потихоньку избавляться,
//  и еще посмотреть влияет ли он как-то на скорость
public class CoolForestFactory {
    private static final int MONOMS_TO_MAKE_SUPPORTED = 100;
    private static final double SUPPORTED_FEATURE_FREQ = 0.3;

    public static CoolForest scaledCoolForest(ModelTools.CompiledOTEnsemble monoforest) {
        return new ScaledCoolForest(
            monoforest.getGrid(),
            fromMonoforest(monoforest),
            monoforest.scale(),
            monoforest.bias()
        );
    }

    public static CoolForest fromMonoforest(ModelTools.CompiledOTEnsemble monoforest) {
        var grid = monoforest.getGrid();
        var monoms = monoforest.getEntries().stream().map(e -> FullMonom.fromEntry(e, grid)).toList();

        return fromMonoms(monoms, grid);
    }

    public static CoolForest fromMonoms(List<FullMonom> monoms, BFGrid bfGrid) {
        // TODO: Проверить, почему не оптимайзится, если судить по логам
        // TODO: CoolForest создается даже для пустого монома
        optimize(monoms);
        if (monoms.size() < MONOMS_TO_MAKE_SUPPORTED) {
            return new CoolForestSimple(bfGrid, monoms);
        }

        var featureUsage = getFeatureUsageFreq(monoms);
        var mostCommonFeature = featureUsage.int2IntEntrySet().stream()
            .max(Comparator.comparingInt(Int2IntMap.Entry::getIntValue)).get();
        int freq = mostCommonFeature.getIntValue();
        if ((double) freq / monoms.size() < SUPPORTED_FEATURE_FREQ) {
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
                // TODO: когда я without вызываю удаляются условия, так что возможно
                //  надо value схлопывать
                // TODO: Прилетает куча мономов без условий, их надо схлопывать в один моном
                // Посмотреть на пустые мономы можно на примере 100 деревьев в дебаггере
                byFVGroups.computeIfAbsent(featureBin, k -> new ArrayList<>())
                    .add(monom.without(featureIdx));
            } else {
                extra.add(monom);
            }
        }

        Int2ObjectMap<CoolForest> byFvFroupsForests = new Int2ObjectArrayMap<>();
        byFVGroups.forEach((bin, ms) -> byFvFroupsForests.put(bin, fromMonoms(ms, bfGrid)));

        return new CoolForestSupported(
            bfGrid,
            featureIdx,
            byFvFroupsForests,
            fromMonoms(extra, bfGrid)
        );
    }

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
