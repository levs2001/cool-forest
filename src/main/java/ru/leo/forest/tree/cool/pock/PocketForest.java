package ru.leo.forest.tree.cool.pock;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record PocketForest(long[][] blocks) {
    private static final Logger log = LoggerFactory.getLogger(PocketForest.class);

    public float value(byte[] bins) {
        return value(bins, blocks);
    }

    public float[] scoreAll(byte[][] features) {
        float[] result = new float[features.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = value(features[i]);
        }
        return result;
    }

    public static float value(byte[] binsB, long[][] blocks) {
        int[] bins = new int[binsB.length];
        // TODO: Избавиться от такого перевода
        for (int i = 0; i < binsB.length; i++) {
            bins[i] = binsB[i] >= 0 ? binsB[i] : binsB[i] + 256;
        }
        float value = 0;
        int currentBlockIdx = 0;
        IntSet s = new IntOpenHashSet();
        while (currentBlockIdx >= 0) {
            // (int) blocks[currentBlock][0] - номер фичи (позиция в массиве бинов) данного блока
            // bins[(int) blocks[currentBlock][0]] - значение бина
            // blocks[currentBlock] - по сути мапа, отображает из значения бина в конкретный composite
            // composite содержит bias и указатель на следующий блок
            long block = blocks[currentBlockIdx][bins[(int) blocks[currentBlockIdx][PocketForestFactory.BINS_COUNT]]];
            value += BlockUtil.getBias(block);
            currentBlockIdx = BlockUtil.getNextIdx(block);
        }
        return value;
    }

}
