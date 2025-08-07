package ru.leo.forest.tree.cool.pock;

import com.expleague.ml.models.ModelTools;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public Monoms sliceFreq(int featureIdx, int bin) {
        // TODO: Можно сделать set со всеми индексами фичей в мономах и сразу проверять, есть ли вообще такая фича, если нет,
        //  то отдавать имеющийся объект
        float resultBias = 0;
        List<PocketMonom> resultMonoms = new ArrayList<>();
        for (var m : monoms) {
            var sliced = m.slice(featureIdx, bin);
            if (sliced == null) {
                continue;
            }
            if (sliced.isOnlyBias()) {
                resultBias += sliced.bias();
                continue;
            }
            resultMonoms.add(sliced);
        }

        var compacted = compact(resultMonoms);
        return new Monoms(
            compacted,
            resultBias,
            getBestFeatureIdx(compacted)
        );
    }


    public Monoms sliceOrdered(int featureIdx, int bin) {
        float resultBias = 0;
        List<PocketMonom> resultMonoms = new ArrayList<>();
        for (var m : monoms) {
            var sliced = m.slice(featureIdx, bin);
            if (sliced == null) {
                continue;
            }
            if (sliced.isOnlyBias()) {
                resultBias += sliced.bias();
                continue;
            }
            resultMonoms.add(sliced);
        }

        var compacted = compact(resultMonoms);
        return new Monoms(
            compacted,
            resultBias,
            firstIdx(compacted)
        );
    }

    static int firstIdx(List<PocketMonom> monoms) {
        return monoms.isEmpty() ? -1 : monoms.getFirst().featureIndices()[0];
    }

    // TODO: compact ломает все
    private static List<PocketMonom> compact(List<PocketMonom> monoms) {
        Int2ObjectMap<PocketMonom> monomsMap = new Int2ObjectOpenHashMap<>();
        for (var m : monoms) {
            int[] coolArray = new int[m.featureIndices().length + m.featureBins().length];
            System.arraycopy(m.featureIndices(), 0, coolArray, 0, m.featureIndices().length);
            System.arraycopy(m.featureBins(), 0, coolArray, m.featureBins().length, m.featureBins().length);
            int combinedHashCode = Arrays.hashCode(coolArray);
            var oldMonom = monomsMap.get(combinedHashCode);
            var oldBias = oldMonom == null ? 0.0f : oldMonom.bias();
            monomsMap.put(combinedHashCode, new PocketMonom(m.featureIndices(), m.featureBins(), oldBias + m.bias()));
        }

        return new ArrayList<>(monomsMap.values());
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


    /**
     * @return feature_idx -> used_bins
     */
    public Int2ObjectMap<IntSet> usageStats() {
        Int2ObjectMap<IntSet> res = new Int2ObjectOpenHashMap<>();
        for (var m : monoms) {
            for (int i = 0; i < m.featureIndices().length; i++) {
                res.computeIfAbsent(m.featureIndices()[i], ignored -> new IntOpenHashSet()).add(m.featureBins()[i]);
            }
        }

        return res;
    }

    public Map<IntList, MonomGroup> groupedMonoms() {
        Map<IntList, MonomGroup> result = new HashMap<>();
        var groups = featureGroups();
        for (var g : groups) {
            List<PocketMonom> group = new ArrayList<>();
            for (var m : monoms) {
                if (m.isIncludedIn(g)) {
                    group.add(m);
                }
            }
            result.put(g, new MonomGroup(group));
        }

        return result;

    }

    public Map<IntList, Integer> groupValues() {
        Map<IntList, Integer> result = new HashMap<>();
        var groups = featureGroups();
        for (var g : groups) {
            int value = 0;
            for (var m : monoms) {
                if (m.isIncludedIn(g)) {
                    value++;
                }
            }
            result.put(g, value);
        }

        return result;
    }

    public Set<IntList> featureGroups() {
        Set<IntList> result = new HashSet<>();
        for (var m : monoms) {
            result.add(new IntArrayList(m.featureIndices()));
        }

        return result;
    }

    public List<IntList> connectedFeatures() {
        return GraphUtil.findConnectedComponentsFromGroups(monoms.stream().map(PocketMonom::featureIndices).toList());
    }

    public static void main(String[] args) {
        System.out.println(GraphUtil.findConnectedComponentsFromGroups(List.of(new int[] {0, 1}, new int[] {2, 3})));
    }

    public record MonomGroup(List<PocketMonom> monoms) {

    }
}
