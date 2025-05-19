package ru.leo.forest.tree.cool;

import com.expleague.ml.BFGrid;
import com.expleague.ml.models.ModelTools;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import ru.leo.forest.util.ByteMapImpl;

public class CoolForestFactory {
    private final int supportedTrigger;
    private final double supportedFeatureFreq;
    private final boolean useGiant;

    public CoolForestFactory(int supportedTrigger, double supportedFeatureFreq, boolean useGiant) {
        this.supportedTrigger = supportedTrigger;
        this.supportedFeatureFreq = supportedFeatureFreq;
        this.useGiant = useGiant;
    }

    public static CoolForest makeCoolForest(
        ModelTools.CompiledOTEnsemble monoforest,
        int monomsToMakeSupported,
        double supportedFeatureFreq
    ) {
        var factory = new CoolForestFactory(monomsToMakeSupported, supportedFeatureFreq, false);
        return factory.createScaledCoolForest(monoforest);
    }

    public static CoolForest makeCoolForestGiant(
        ModelTools.CompiledOTEnsemble monoforest,
        int monomsToMakeSupported,
        double supportedFeatureFreq
    ) {
        var factory = new CoolForestFactory(monomsToMakeSupported, supportedFeatureFreq, true);
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

        int featureIdx = mostCommonFeature.getIntKey();
        List<FullMonom> extra = new ArrayList<>();
        Int2ObjectMap<List<FullMonom>> byFVGroups = new Int2ObjectArrayMap<>();
        for (FullMonom monom : monoms) {
            int featureBin = monom.featureBin(featureIdx);
            if (featureBin != FullMonom.NO_FEATURE) {
                byFVGroups.computeIfAbsent(featureBin, k -> new ArrayList<>())
                    .add(monom.without(featureIdx));
            } else {
                extra.add(monom);
            }
        }

        // Int2ObjectArrayMap ищит ключ просто обходя массив с конца, здесь лучше использовать ассоциативный массив ByteMapImpl
        Int2ObjectMap<CoolForest> byFvFroupsForests = new ByteMapImpl<>();
        if (useGiant) {
            List<FullMonom> branchMonoms = new ArrayList<>();
            int[] bins = byFVGroups.keySet().intStream().sorted().toArray();
            for (int bin : bins) {
                branchMonoms.addAll(byFVGroups.get(bin));
                byFvFroupsForests.put(bin, fromMonoms(new ArrayList<>(branchMonoms), bfGrid));
            }
            return new CoolForestSupportedGiant(bfGrid, featureIdx, byFvFroupsForests, fromMonoms(extra, bfGrid));
        } else {
            byFVGroups.forEach((bin, ms) -> byFvFroupsForests.put((int) bin, fromMonoms(ms, bfGrid)));
            return new CoolForestSupported(bfGrid, featureIdx, byFvFroupsForests, fromMonoms(extra, bfGrid));
        }
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
