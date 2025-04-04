package inference;

import static inference.BenchUtils.FEATURES_FLOAT;

import ai.catboost.CatBoostError;
import ai.catboost.CatBoostModel;
import java.io.IOException;
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

@State(Scope.Benchmark)
public class CatboostBenchmark {
    private volatile CatBoostModel model;
    @Param({
        "catboost_10_4",
        "catboost_100_6",
    })
    private String modelName;

    @Setup
    public void setup() throws CatBoostError, IOException {
        var modelPath = BenchUtils.getModelPath(modelName + ".cbm").toString();
        model = CatBoostModel.loadModel(modelPath);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3, time = 10)
    @Measurement(iterations = 3, time = 10)
    public void test(Blackhole bh) throws CatBoostError {
        bh.consume(model.predict(FEATURES_FLOAT, (String[][]) null));
    }
}
