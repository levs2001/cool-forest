package ru.leo.forest.converter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.expleague.commons.math.Func;
import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import java.io.IOException;
import java.nio.file.Path;
import ru.leo.utils.CsvReaderUtil;

public class ModelsTestBase {
    protected static final Path RES_PATH = Path.of("./src/test/resources/leo/");
    private static final Path FEATURES_PATH = RES_PATH.resolve("X.csv");
    private static final Path MODEL_3_RESULTS_PATH = RES_PATH.resolve("res/trees_res_3.csv");
    private static final Path MODEL_10_RESULTS_PATH = RES_PATH.resolve("res/trees_res_10_4.csv");
    private static final Path MODEL_100_RESULTS_PATH = RES_PATH.resolve("res/trees_res_100_6.csv");
    private static final Path MODEL_1000_RESULTS_PATH = RES_PATH.resolve("res/trees_res_1000_6.csv");
    protected static final double[][] FEATURES;
    protected static final double[] MODEL_3_RESULTS;
    protected static final double[] MODEL_10_RESULTS;
    protected static final double[] MODEL_100_RESULTS;
    protected static final double[] MODEL_1000_RESULTS;

    static {
        try {
            FEATURES = CsvReaderUtil.readNumeric(FEATURES_PATH, false);
            MODEL_3_RESULTS = CsvReaderUtil.readOne(MODEL_3_RESULTS_PATH);
            MODEL_10_RESULTS = CsvReaderUtil.readOne(MODEL_10_RESULTS_PATH);
            MODEL_100_RESULTS = CsvReaderUtil.readOne(MODEL_100_RESULTS_PATH);
            MODEL_1000_RESULTS = CsvReaderUtil.readOne(MODEL_1000_RESULTS_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void testEnseble(double[] expected, Func ensemble) {
        double[] result = new double[FEATURES.length];
        for (int i = 0; i < FEATURES.length; i++) {
            var vec = new ArrayVec(FEATURES[i]);
            result[i] = ensemble.value(vec);
        }

        assertArrayEquals(expected, result, 1e-5F);
    }
}
