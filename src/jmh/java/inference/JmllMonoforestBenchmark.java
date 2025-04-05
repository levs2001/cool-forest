package inference;

import static inference.BenchUtils.FEATURES_VECS;

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
import ru.leo.forest.converter.MonoforestConverter;
import ru.leo.forest.converter.MonoforestConverterImpl;
import ru.leo.forest.rank.FuncRanker;
import ru.leo.forest.rank.Ranker;

@State(Scope.Benchmark)
public class JmllMonoforestBenchmark {
    private static final MonoforestConverter monoforestConverter = new MonoforestConverterImpl();

    private volatile Ranker ranker;
    @Param({
        "monoforest_10_4",
        "monoforest_100_6",
        "model_1000_6",
        "model_5000_6"
    })
    private String modelName;

    @Setup
    public void setup() throws IOException {
        var modelPath = BenchUtils.getModelPath(modelName + ".json");
        var func = monoforestConverter.readMonoforest(modelPath);
        ranker = new FuncRanker(func);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3, time = 10)
    @Measurement(iterations = 3, time = 10)
    public void test(Blackhole bh) {
        bh.consume(ranker.predictVec(FEATURES_VECS));
    }
}
