package ru.leo.forest.converter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MonoforestConverterImplTest extends ModelsTestBase {
    private final MonoforestConverter monoforestConverter = new MonoforestConverterImpl();
    private static final Path MODEL_3_PATH = RES_PATH.resolve("monoforest_3.json");
    private static final Path MODEL_10_PATH = RES_PATH.resolve("monoforest_10_4.json");
    private static final Path MODEL_100_PATH = RES_PATH.resolve("monoforest_100_6.json");
    private static final Path MODEL_1000_PATH = RES_PATH.resolve("monoforest_1000_6.json");

    @ParameterizedTest
    @MethodSource("provideModelsAndResults")
    void testMonoforestJmll(Path model, double[] expected) throws IOException {
        var binEnsemble = monoforestConverter.readMonoforest(model);
        testEnseble(expected, binEnsemble);
    }

    private static Stream<Arguments> provideModelsAndResults() {
        return Stream.of(
            Arguments.of(MODEL_3_PATH, MODEL_3_RESULTS),
            Arguments.of(MODEL_10_PATH, MODEL_10_RESULTS),
            Arguments.of(MODEL_100_PATH, MODEL_100_RESULTS)
            // TODO: C моделью на 1000 деревьев что-то не так.
            // TODO: Не хватает мощности сетки? Вероятнее всего, так как если посмотреть на borders
            // TODO: в catboost, то там больше 128 границ.
//            Arguments.of(MODEL_1000_PATH, MODEL_1000_RESULTS)
        );
    }
}
