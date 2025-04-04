package ru.leo.forest.rank;

import com.expleague.commons.math.vectors.Vec;

public interface Ranker {
    float[] predictFloat(float[][] features);

    double[] predictDouble(double[][] features);

    double[] predictVec(Vec[] vecs);
}
