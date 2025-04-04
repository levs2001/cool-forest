package inference;

import com.expleague.commons.math.vectors.Vec;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import java.io.IOException;
import java.nio.file.Path;
import ru.leo.utils.CsvReaderUtil;

public class BenchUtils {
    private static final Path RES_PATH = Path.of("./src/jmh/resources/");
    private static final Path MODELS_PATH = RES_PATH.resolve("models");
    private static final Path FEATURES_PATH = RES_PATH.resolve("data/X_2000.csv");
    public static final double[][] FEATURES_DOUBLE;
    public static final float[][] FEATURES_FLOAT;
    public static final Vec[] FEATURES_VECS;

    static {
        try {
            FEATURES_DOUBLE = CsvReaderUtil.readNumeric(FEATURES_PATH, false);
            FEATURES_FLOAT = toFloat(FEATURES_DOUBLE);
            FEATURES_VECS = toVecs(FEATURES_DOUBLE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getModelPath(String modelFileName) {
        return MODELS_PATH.resolve(modelFileName);
    }

    private static float[][] toFloat(double[][] features) {
        float[][] res = new float[features.length][features[0].length];
        for (int i = 0; i < features.length; i++) {
            for (int j = 0; j < features[i].length; j++) {
                res[i][j] = (float) features[i][j];
            }
        }

        return res;
    }

    public static Vec[] toVecs(double[][] features) {
        Vec[] res = new Vec[features.length];
        for (int i = 0; i < features.length; i++) {
            res[i] = new ArrayVec(features[i]);
        }

        return res;
    }
}
