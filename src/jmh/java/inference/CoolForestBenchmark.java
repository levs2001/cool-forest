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
import ru.leo.forest.tree.cool.CoolForest;
import ru.leo.forest.tree.cool.CoolForestFactory;

@State(Scope.Benchmark)
public class CoolForestBenchmark {
    private static final Converter converter = new ConverterImpl();

    @Param({
        "model_10_4",
        "model_100_6",
    })
    private String modelName;

    @Param({
        "10",
        "30",
        "50",
        "100",
        "300",
        "500"
    })
    private int supportedTrigger;

    @Param({
        "0.05",
        "0.1",
        "0.2",
        "0.3",
        "0.4",
        "0.5"
    })
    private double supportedFeatureFreq;
    private volatile CoolForest coolForest;

    @Setup
    public void setup() throws IOException {
        var modelPath = BenchUtils.getModelPath(modelName + ".json");
        coolForest = CoolForestFactory.makeCoolForest(converter.readMonoforest(modelPath), supportedTrigger, supportedFeatureFreq);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    @Fork(value = 1)
    @Warmup(iterations = 3, time = 5)
    @Measurement(iterations = 3, time = 5)
    public void test(Blackhole bh) {
        bh.consume(coolForest.predictVec(FEATURES_VECS));
    }
}
