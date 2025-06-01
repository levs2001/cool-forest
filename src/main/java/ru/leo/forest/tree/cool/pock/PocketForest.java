package ru.leo.forest.tree.cool.pock;

public class PocketForest {
    private final long[][] blocks;

    public PocketForest(long[][] blocks) {
        this.blocks = blocks;
    }

    public float value(byte[] bins) {
        float value = 0;
        int currentBlockIdx = 0;
        while (currentBlockIdx >= 0) {
            // (int) blocks[currentBlock][0] - номер фичи (позиция в массиве бинов) данного блока
            // bins[(int) blocks[currentBlock][0]] - значение бина
            // blocks[currentBlock] - по сути мапа, отображает из значения бина в конкретный composite
            // composite содержит bias и указатель на следующий блок
            // TODO: Если значения бина из bins == 0, не ломается ли логика?
            // TODO: А что делать с extra, может его хранить в первом блоке и брать по умолчанию?
            //  Но тогда нужна рекурсия или что-то подобное...
            long block = blocks[currentBlockIdx][bins[(int) blocks[currentBlockIdx][PocketForestFactory.BINS_COUNT]]];
            value += BlockUtil.getBias(block);
            currentBlockIdx = BlockUtil.getNextIdx(block);
        }
        return value;
    }
}
