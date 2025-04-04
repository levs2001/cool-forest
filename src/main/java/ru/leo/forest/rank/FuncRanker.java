package ru.leo.forest.rank;

import com.expleague.commons.math.Func;
import com.expleague.commons.math.vectors.Vec;

public class FuncRanker implements Ranker {
    private final Func func;

    public FuncRanker(Func func) {
        this.func = func;
    }

    @Override
    public float[] predictFloat(float[][] features) {
        throw new NoSuchMethodError();
    }

    @Override
    public double[] predictDouble(double[][] features) {
        throw new NoSuchMethodError();
    }

    @Override
    public double[] predictVec(Vec[] vecs) {
        double[] result = new double[vecs.length];
        for (int i = 0; i < vecs.length; i++) {
            result[i] = func.value(vecs[i]);
        }
        return result;
    }
}
