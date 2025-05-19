package ru.leo.forest.tree.fast;

public class DeepDemo {
    long[][] blocks = new long[0][0];
    // TODO: Осознать
    public float value(byte[] bins) {
        float value = 0;
        int currentBlock = 0;
        while (currentBlock >= 0) {
            // (int) blocks[currentBlock][0] - номер фичи (позиция в массиве бинов) данного блока
            // bins[(int) blocks[currentBlock][0]] - значение бина
            // blocks[currentBlock] - по сути мапа, отображает из значения бина в конкретный composite
            // composite содержит bias и указатель на следующий блок
            // TODO: Если значения бина из bins == 0, не ломается ли логика?
            // TODO: А что делать с extra, может его хранить в первом блоке и брать по умолчанию?
            //  Но тогда нужна рекурсия или что-то подобное...
            long composite = blocks[currentBlock][bins[(int) blocks[currentBlock][0]]];
            value += Float.intBitsToFloat((int) (composite & 0xFFFFFFFFL));
            currentBlock = (int) (composite >>> 32);
        }
        return value;
    }
}
