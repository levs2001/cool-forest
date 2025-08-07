package ru.leo.forest.tree.cool.pock;

public record PocketForestEnsemble(long[][][] blocksList) {

    public float value(byte[] bins) {
        float result = 0.0f;
        for (long[][] blocks : blocksList) {
            result += PocketForest.value(bins, blocks);
        }
        return result;
    }
}
