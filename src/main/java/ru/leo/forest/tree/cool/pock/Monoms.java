package ru.leo.forest.tree.cool.pock;

import com.expleague.ml.models.ModelTools;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record Monoms(List<PocketMonom> monoms, float bias, int bestFeatureIdx) {
    public static Monoms fromMonoforest(ModelTools.CompiledOTEnsemble monoforest) {
        var grid = monoforest.getGrid();
        var monoms = monoforest.getEntries().stream().map(e -> PocketMonom.fromEntry(e, grid)).toList();
        return create(monoms, (float) monoforest.bias());
    }

    public static Monoms create(List<PocketMonom> monoms, float biasV) {
        List<PocketMonom> resultMonoms = new ArrayList<>(monoms.size());
        float bias = biasV;
        for (var m : monoms) {
            if (m.isOnlyBias()) {
                bias += m.bias();
                continue;
            }
            resultMonoms.add(m);
        }
        return new Monoms(resultMonoms, bias, getBestFeatureIdx(resultMonoms));
    }

    public Monoms slice(int featureIdx, int bin) {
        // TODO: Можно сделать set со всеми индексами фичей в мономах и сразу проверять, есть ли вообще такая фича, если нет,
        //  то отдавать имеющийся объект
        float resultBias = 0;
        List<PocketMonom> resultMonoms = new ArrayList<>();
        for (var m : monoms) {
            var sliced = m.slice(featureIdx, bin);
            if (sliced == null) {
                continue;
            }
            boolean onlyBias = sliced.isOnlyBias();
            if (onlyBias) {
                resultBias += sliced.bias();
                continue;
            }
            resultMonoms.add(sliced);
        }

        return new Monoms(resultMonoms, resultBias, getBestFeatureIdx(resultMonoms));
    }

    public int size() {
        return monoms.size();
    }

    private static int getBestFeatureIdx(List<PocketMonom> monoms) {
        if (monoms.isEmpty()) {
            return -1;
        }

        Int2IntMap featureFreq = new Int2IntOpenHashMap();
        for (var m : monoms) {
            for (int feature : m.featureIndices()) {
                featureFreq.put(feature, featureFreq.getOrDefault(feature, 0) + 1);
            }
        }

        return featureFreq.int2IntEntrySet().stream()
            .max(Comparator.comparingInt(Int2IntMap.Entry::getIntValue)).get().getIntKey();
    }
}
