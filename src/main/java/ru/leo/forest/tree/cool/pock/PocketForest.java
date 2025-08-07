package ru.leo.forest.tree.cool.pock;

public record PocketForest(long[][] blocks) {
    public float value(byte[] bins) {
        return value(bins, blocks);
    }

    public static float value(byte[] bins, long[][] blocks) {
        float value = 0;
        int currentBlockIdx = 0;
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
