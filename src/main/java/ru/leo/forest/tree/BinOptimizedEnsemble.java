package ru.leo.forest.tree;

import com.expleague.commons.math.vectors.Vec;
import com.expleague.ml.BFGrid;
import com.expleague.ml.BinOptimizedModel;
import com.expleague.ml.data.impl.BinarizedDataSet;
import java.util.List;
import ru.leo.forest.converter.ConverterUtils;
import ru.leo.forest.rank.Ranker;

public class BinOptimizedEnsemble extends BinOptimizedModel.Stub implements Ranker {
    private final List<BinOptimizedModel> models;
    private final BFGrid grid;
    private final double scale;
    private final double bias;

    public BinOptimizedEnsemble(List<BinOptimizedModel> models, BFGrid grid, double scale, double bias) {
        this.models = models;
        this.grid = grid;
        this.scale = scale;
        this.bias = bias;
    }

    @Override
    public double value(BinarizedDataSet bds, int index) {
        double result = 0;
        for (var model : models) {
            result += model.value(bds, index);
        }
        return result * scale + bias;
    }

    @Override
    public double[] predictVec(Vec[] vecs) {
        var bds = ConverterUtils.makeBds(vecs, grid);
        double[] result = new double[vecs.length];

        for (int i = 0; i < vecs.length; i++) {
            result[i] = value(bds, i);
        }

        return result;
    }

    @Override
    public double[] predictDouble(double[][] features) {
        return predictVec(ConverterUtils.toVecs(features));
    }

    @Override
    public double value(Vec vec) {
        throw new NoSuchMethodError();
    }

    @Override
    public int dim() {
        throw new NoSuchMethodError();
    }

    @Override
    public float[] predictFloat(float[][] features) {
        throw new NoSuchMethodError();
    }
}
