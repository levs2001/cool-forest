package ru.leo.forest.tree.fast;

public class DeepDemo {
    long[][] blocks = new long[0][0];
    // TODO: Осознать
    public float value(byte[] bins) {
        float value = 0;
        int currentBlock = 0;
        while (currentBlock >= 0) {
            long composite = blocks[currentBlock][bins[(int) blocks[currentBlock][0]]];
            value += Float.intBitsToFloat((int) (composite & 0xFFFFFFFFL));
            currentBlock = (int) (composite >>> 32);
        }
        return value;
    }
}
