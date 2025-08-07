package ru.leo.forest.tree.cool.pock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PocketForestFactory {
    public static final int BINS_COUNT = 64;
    private static final int END_BLOCK_IDX = -1;

    public static PocketForest create(Monoms monoms) {
        List<long[]> blocksList = new ArrayList<>();
        createInternal(monoms, blocksList);
//
//        var usageStats = monoms.usageStats();
//        System.out.println(usageStats);
//        System.out.println("Combinations count: " + usageStats.values().stream().mapToLong(Set::size).reduce((a, b) -> a * b));
//        System.out.println("Connected componens: " + monoms.connectedFeatures());
//        System.out.println("Feature groups: " + monoms.featureGroups());
//        System.out.println("Group values: " + monoms.groupValues());
//        PocketForestEnsembleFactory.emulateBuild(monoms);
        return new PocketForest(blocksList.toArray(new long[0][]));
    }

    public static int createInternal(Monoms monoms, List<long[]> r) {
        // TODO: r очень быстро растет.
        // TODO: Фактически в b==0 мы пишем все мономы в которых вообще нет данной фичи
        // Можно попробовать полученный slice учесть, удалить из других веток и все ветки в конце направить на него.
        boolean first = r.isEmpty();
        int result = r.size();
        long[] str = new long[BINS_COUNT + 1];
        int featureIdx = monoms.bestFeatureIdx();
        str[BINS_COUNT] = featureIdx;
        r.add(str);

        Monoms prevSliced = monoms;
        int prevIdx = 0;
        for (int b = 0; b < BINS_COUNT; b++) {
            Monoms sliced = monoms.sliceFreq(featureIdx, b);
            int nextBlockIdx;
            if (sliced.monoms().equals(prevSliced.monoms())) {
                assert sliced.bestFeatureIdx() == prevSliced.bestFeatureIdx();
                nextBlockIdx = prevIdx;
            } else {
                nextBlockIdx = sliced.size() == 0 ? END_BLOCK_IDX : createInternal(sliced, r);
            }
            float bias = sliced.bias();
            if (first) {
                bias += monoms.bias();
            }
            long block = BlockUtil.makeBlock(nextBlockIdx, bias);
            str[b] = block;
            prevIdx = nextBlockIdx;
            prevSliced = sliced;
        }

        return result;
    }


    public static int createInternalOrdered(Monoms monoms, List<long[]> r) {
        // TODO: r очень быстро растет.
        // TODO: Фактически в b==0 мы пишем все мономы в которых вообще нет данной фичи
        // Можно попробовать полученный slice учесть, удалить из других веток и все ветки в конце направить на него.
        boolean first = r.isEmpty();
        int result = r.size();
        long[] str = new long[BINS_COUNT + 1];
        int featureIdx = monoms.bestFeatureIdx();
        str[BINS_COUNT] = featureIdx;
        r.add(str);

        Monoms prevSliced = monoms;
        int prevIdx = 0;
        for (int b = 0; b < BINS_COUNT; b++) {
            Monoms sliced = monoms.sliceOrdered(featureIdx, b);
            int nextBlockIdx;
            if (sliced.monoms().equals(prevSliced.monoms())) {
                assert sliced.bestFeatureIdx() == prevSliced.bestFeatureIdx();
                nextBlockIdx = prevIdx;
            } else {
                nextBlockIdx = sliced.size() == 0 ? END_BLOCK_IDX : createInternalOrdered(sliced, r);
            }
            float bias = sliced.bias();
            if (first) {
                bias += monoms.bias();
            }
            long block = BlockUtil.makeBlock(nextBlockIdx, bias);
            str[b] = block;
            prevIdx = nextBlockIdx;
            prevSliced = sliced;
        }

        return result;
    }

}
