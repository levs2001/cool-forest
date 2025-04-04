package ru.leo.forest.converter;

import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.impl.mx.RowsVecArrayMx;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.expleague.ml.BFGrid;
import com.expleague.ml.data.impl.BinarizedDataSet;
import com.expleague.ml.data.set.impl.VecDataSetImpl;

public class ConverterUtils {
    public static BinarizedDataSet makeBds(Vec[] vecs, BFGrid grid) {
        var mx = new RowsVecArrayMx(vecs);
        var dataset = new VecDataSetImpl(mx, null);
        return new BinarizedDataSet(dataset, grid);
    }

    public static Vec[] toVecs(double[][] features) {
        Vec[] res = new Vec[features.length];
        for (int i = 0; i < features.length; i++) {
            res[i] = new ArrayVec(features[i]);
        }

        return res;
    }

}
