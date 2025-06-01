package ru.leo.forest.tree.cool.pock;

public class BlockUtil {
    public static long makeBlock(int nextIdx, float bias) {
        return ((long) nextIdx << 32) | (Float.floatToIntBits(bias) & 0xFFFFFFFFL);
    }

    public static float getBias(long block) {
        return Float.intBitsToFloat((int) (block & 0xFFFFFFFFL));
    }

    public static int getNextIdx(long block) {
        return (int) (block >> 32);
    }
}
