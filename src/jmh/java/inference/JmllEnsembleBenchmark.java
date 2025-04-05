package inference;

import static inference.BenchUtils.FEATURES_VECS;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
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
import ru.leo.forest.rank.FuncRanker;
import ru.leo.forest.rank.Ranker;
import ru.leo.forest.tree.cool.CoolForestFactory;

@State(Scope.Benchmark)
public class JmllEnsembleBenchmark {
    private static final Converter converter = new ConverterImpl();

    private volatile Ranker ranker;
    @Param({
        "model_10_4",
        "model_100_6",
        "model_1000_6",
        "model_5000_6",
    })
    private String modelName;

    @Param({
        "simple",
        "jmll",
        "bin",
        "monoforest"
    })
    private String modelType;

    @Setup
    public void setup() throws IOException {
        var modelPath = BenchUtils.getModelPath(modelName + ".json");
        ranker = switch (modelType) {
            case "simple" -> new FuncRanker(converter.readSimple(modelPath));
            case "jmll" -> new FuncRanker(converter.read(modelPath));
            case "bin" -> converter.readOTBin(modelPath);
            case "monoforest" -> new FuncRanker(converter.readMonoforest(modelPath));
            default -> throw new IllegalStateException("Unexpected value: " + modelType);
        };
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = 1)
    @Warmup(iterations = 3, time = 5)
    @Measurement(iterations = 3, time = 5)
    public void test(Blackhole bh) {
        bh.consume(ranker.predictVec(FEATURES_VECS));
    }
}
