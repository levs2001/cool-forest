package inference;

import static inference.BenchUtils.FEATURES_FLOAT;
import static inference.BenchUtils.FEATURES_VECS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ai.catboost.CatBoostError;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import ru.leo.forest.converter.Converter;
import ru.leo.forest.converter.ConverterImpl;
import ru.leo.forest.tree.cool.pock.Monoms;
import ru.leo.forest.tree.cool.pock.PocketForest;
import ru.leo.forest.tree.cool.pock.PocketForestEnsembleFactory;

@State(Scope.Benchmark)
public class PocketForestEnsembleBenchmark {
    private final Converter converter = new ConverterImpl();

    private volatile PocketForest model;
    private volatile byte[][] features;
    @Param({
//        "model_100_6.json",
        "m_1749464806_osrch_64885_search_l2_conv__true_prices_control.json"
    })
    private String modelName;

    @Setup
    public void setup() throws CatBoostError, IOException {
        var modelPath = BenchUtils.getModelPath(modelName).toString();
        var monoforest = converter.readMonoforest(Path.of(modelPath));
        model = PocketForestEnsembleFactory.createRowPocketForest(Monoms.fromMonoforest(monoforest));

        var grid = monoforest.getGrid();
        var byteFeatures = new byte[FEATURES_VECS.length][];
        for (int i = 0; i < FEATURES_VECS.length; i++) {
            var v = FEATURES_VECS[i];
            byte[] bins = new byte[v.length()];
            grid.binarizeTo(v, bins);
            byteFeatures[i] = bins;
        }
        features = byteFeatures;
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3, time = 5)
    @Measurement(iterations = 3, time = 5)
    public void test(Blackhole bh) {
        bh.consume(model.scoreAll(features));
    }
}
