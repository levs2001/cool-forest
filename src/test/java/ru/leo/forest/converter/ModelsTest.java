package ru.leo.forest.converter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.leo.forest.tree.cool.CoolForestFactory;

public class ModelsTest extends ModelsTestBase {
    private static final Path MODEL_3_PATH = RES_PATH.resolve("models/trees_3.json");
    private static final Path MODEL_10_PATH = RES_PATH.resolve("models/model_10_4.json");
    private static final Path MODEL_100_PATH = RES_PATH.resolve("models/model_100_6.json");
    private static final Path MODEL_1000_PATH = RES_PATH.resolve("models/model_1000_6.json");

    private final Converter converter = new ConverterImpl();

    @ParameterizedTest
    @MethodSource("provideModelsAndResults")
    void testTreesSimple(Path model, double[] expected) throws IOException {
        var ensemble = converter.readSimple(model);
        testEnseble(expected, ensemble);
    }

    @ParameterizedTest
    @MethodSource("provideModelsAndResults")
    void testTreesJmll(Path model, double[] expected) throws IOException {
        var ensemble = converter.read(model);
        testEnseble(expected, ensemble);
    }

    // TODO: На 1000 деревьев монофорест почему-то падает, возможно из-за мощности сетки
//    @Disabled
    @ParameterizedTest
    @MethodSource("provideModelsAndResults")
    void testMonoforestJmll(Path model, double[] expected) throws IOException {
        var binEnsemble = converter.readMonoforest(model);
        testEnseble(expected, binEnsemble);
    }

    @ParameterizedTest
    @MethodSource("provideModelsAndResults")
    void testBinTreeEnsemble(Path model, double[] expected) throws IOException {
        var ensemble = converter.readOTBin(model);
        var actual = ensemble.predictDouble(FEATURES);
        assertArrayEquals(expected, actual, 1e-5F);
    }

    @ParameterizedTest
    @MethodSource("provideModelsAndResults")
    void testCoolForest(Path model, double[] expected) throws IOException {
        var coolForest = CoolForestFactory.makeCoolForest(converter.readMonoforest(model), 100, 0.3);
        var actual = coolForest.predictDouble(FEATURES);
        assertArrayEquals(expected, actual, 1e-5F);
    }

    @ParameterizedTest
    @Disabled
    @MethodSource("provideModelsAndResults")
    void testCoolForestGiant(Path model, double[] expected) throws IOException {
        var coolForest = CoolForestFactory.makeCoolForestGiant(converter.readMonoforest(model), 500, 0.3);
        var actual = coolForest.predictDouble(FEATURES);
        assertArrayEquals(expected, actual, 1e-5F);
    }

    private static Stream<Arguments> provideModelsAndResults() {
        return Stream.of(
            Arguments.of(MODEL_3_PATH, MODEL_3_RESULTS),
            Arguments.of(MODEL_10_PATH, MODEL_10_RESULTS),
            Arguments.of(MODEL_100_PATH, MODEL_100_RESULTS),
            Arguments.of(MODEL_1000_PATH, MODEL_1000_RESULTS)
        );
    }
}