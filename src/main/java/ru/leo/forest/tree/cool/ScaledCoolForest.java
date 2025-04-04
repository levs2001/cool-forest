package ru.leo.forest.tree.cool;

import com.expleague.ml.BFGrid;
import com.expleague.ml.data.impl.BinarizedDataSet;

public final class ScaledCoolForest extends CoolForest.Stub {
    private final CoolForest delegate;
    private final double scale;
    private final double bias;

    public ScaledCoolForest(BFGrid grid, CoolForest delegate, double scale, double bias) {
        super(grid);
        this.delegate = delegate;
        this.scale = scale;
        this.bias = bias;
    }

    @Override
    public double value(BinarizedDataSet bds, int index) {
        return delegate.value(bds, index) * scale + bias;
    }
}
