package ru.leo.forest.tree.cool;

import com.expleague.commons.math.vectors.Vec;
import com.expleague.ml.BFGrid;
import com.expleague.ml.data.impl.BinarizedDataSet;
import ru.leo.forest.converter.ConverterUtils;
import ru.leo.forest.rank.Ranker;

public sealed interface CoolForest extends Ranker {
    // TODO; Подумать, что лучше сюда пихнуть, чтобы было проще вычислять
    //  value без лишних затрат

    // TODO: Должны быть методы вычисления, подумать какие.

    double value(BinarizedDataSet bds, int index);

    abstract sealed class Stub implements CoolForest permits CoolForestSimple, CoolForestSupported, ScaledCoolForest {
        private final BFGrid grid;

        protected Stub(BFGrid grid) {
            this.grid = grid;
        }

        public double[] predictVec(Vec[] vecs) {
            var bds = ConverterUtils.makeBds(vecs, grid);
            double[] result = new double[vecs.length];

            for (int i = 0; i < vecs.length; i++) {
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
