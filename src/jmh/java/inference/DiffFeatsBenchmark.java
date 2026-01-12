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
public class DiffFeatsBenchmark {
    private static final Converter converter = new ConverterImpl();
    private static final int N = 100;
    private static final double Q = 0.2;

    @Param({
//        "model_4_100_6.json",
//        "model_5_100_6.json",
//        "model_6_100_6.json",
//        "model_7_100_6.json",
//        "model_8_100_6.json",
//        "model_1000_6.json"
        "m_1749464806_osrch_64885_search_l2_conv__true_prices_control.json"
    })
    private String modelName;

    private volatile CoolForest coolForest;

    @Setup
    public void setup() throws IOException {
        var modelPath = BenchUtils.getModelPath(modelName);
        coolForest = CoolForestFactory.makeCoolForestGiant(converter.readMonoforest(modelPath), N, Q);
//        coolForest = CoolForestFactory.makeCoolForest(converter.readMonoforest(modelPath), N, Q);
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
