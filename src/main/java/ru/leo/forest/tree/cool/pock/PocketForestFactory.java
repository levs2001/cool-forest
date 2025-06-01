package ru.leo.forest.tree.cool.pock;

import java.util.ArrayList;
import java.util.List;

public class PocketForestFactory {
    public static final int BINS_COUNT = 32;
    private static final int END_BLOCK_IDX = -1;

    public static PocketForest create(Monoms monoms) {
        List<long[]> blocksList = new ArrayList<>();
        createInternal(monoms, blocksList);

        return new PocketForest(blocksList.toArray(new long[0][]));
    }

    private static int createInternal(Monoms monoms, List<long[]> r) {
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
        long prevBlock = 0;
        for (int b = 0; b < BINS_COUNT; b++) {
            Monoms sliced = monoms.slice(featureIdx, b);
            long block;
            if (!sliced.equals(prevSliced)) {
                int nextBlockIdx = sliced.size() == 0 ? END_BLOCK_IDX : createInternal(sliced, r);
                float bias = sliced.bias();
                if (first) {
                    bias += monoms.bias();
                }
                block = BlockUtil.makeBlock(nextBlockIdx, bias);
            } else {
                block = prevBlock;
            }
            str[b] = block;
            prevBlock = block;
            prevSliced = sliced;
        }

        return result;
    }
}
