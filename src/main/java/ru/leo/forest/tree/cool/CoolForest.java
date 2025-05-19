package ru.leo.forest.tree.cool;

import com.expleague.commons.math.vectors.Vec;
import com.expleague.ml.BFGrid;
import com.expleague.ml.data.impl.BinarizedDataSet;
import ru.leo.forest.converter.ConverterUtils;
import ru.leo.forest.rank.Ranker;

public sealed interface CoolForest extends Ranker {
    double value(BinarizedDataSet bds, int index);

    void add(BinarizedDataSet bds, double[] value);

    abstract sealed class Stub implements CoolForest
        permits CoolForestSimple, CoolForestSupported, CoolForestSupportedGiant, ScaledCoolForest {
        private final BFGrid grid;

        protected Stub(BFGrid grid) {
            this.grid = grid;
        }

        public double[] predictVec(Vec[] vecs) {
            var bds = ConverterUtils.makeBds(vecs, grid);
            double[] result = new double[vecs.length];
            add(bds, result);
            return result;
        }

        private double[] predictBds(BinarizedDataSet bds) {
            int vectorsCount = bds.bins(0).length;
            double[] result = new double[vectorsCount];

            for (int i = 0; i < vectorsCount; i++) {
                result[i] = value(bds, i);
            }

            return result;
        }

        public double[] predictDouble(double[][] features) {
            return predictVec(ConverterUtils.toVecs(features));
        }

        @Override
        public float[] predictFloat(float[][] features) {
            throw new NoSuchMethodError();
        }
    }
}
