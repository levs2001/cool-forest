package ru.leo.forest.converter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.expleague.commons.math.vectors.impl.vectors.ArrayVec;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.leo.forest.tree.cool.CoolForestFactory;
import ru.leo.forest.tree.cool.pock.Monoms;
import ru.leo.forest.tree.cool.pock.PocketForestEnsembleFactory;
import ru.leo.forest.tree.cool.pock.PocketForestFactory;
import ru.leo.utils.JsonUtil;

public class ModelsTest extends ModelsTestBase {
    private static final Path MODEL_3_PATH = RES_PATH.resolve("models/trees_3.json");
    private static final Path MODEL_10_PATH = RES_PATH.resolve("models/model_10_4.json");
    private static final Path MODEL_100_PATH = RES_PATH.resolve("models/model_100_6.json");
    private static final Path MODEL_1000_PATH = RES_PATH.resolve("models/model_1000_6.json");
    private static final Path MODEL_1000_PATH_2 = RES_PATH.resolve("models/model_8_1000_6.json");
    private static final Path PROD_MODEL =
        RES_PATH.resolve("models/prod/m_1749464806_osrch_64885_search_l2_conv__true_prices_control.json");
    private static final Path ARTYOM_MODEL =
        RES_PATH.resolve("models/prod/m_1753960023_osrch_68247_search_l2_rel_click_rh.json");

    private static final Logger log = LoggerFactory.getLogger(ModelsTest.class);

    private final Converter converter = new ConverterImpl();

    @Test
    void artemTest() throws IOException {
        var binEnsemble = converter.readMonoforest(ARTYOM_MODEL);
        var mapper = new ObjectMapper();
        mapper.writeValue(new File("./monoms__m_1753960023_osrch_68247_search_l2_rel_click_rh.json"), binEnsemble.getEntries());
    }

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
//    @Disabled
    @MethodSource("provideModelsAndResults")
    void testCoolForestGiant(Path model, double[] expected) throws IOException {
        var coolForest = CoolForestFactory.makeCoolForestGiant(converter.readMonoforest(model), 500, 0.3);
        var actual = coolForest.predictDouble(FEATURES);
        assertArrayEquals(expected, actual, 1e-5F);
    }

    @ParameterizedTest
//    @Disabled
    @MethodSource("provideModelsAndResults")
    void testPocketForest(Path model, double[] expected) throws IOException {
        var monoforest = converter.readMonoforest(model);
        var pocketForest = PocketForestFactory.create(
            Monoms.fromMonoforest(monoforest)
        );
        var grid = monoforest.getGrid();

        for (int i = 0; i < FEATURES.length; i++) {
            var f = FEATURES[i];
            byte[] bins = new byte[f.length];
            grid.binarizeTo(new ArrayVec(f), bins);
            var actual = pocketForest.value(bins);
            assertEquals((float) expected[i], actual, 1e-5F);
        }
    }

    @ParameterizedTest
    @MethodSource("provideModelsAndResults")
    void testPocketForestEnsemble(Path model, double[] expected) throws IOException {
        var monoforest = converter.readMonoforest(model);
        var pocketForest = PocketForestEnsembleFactory.createEnsemble(
            Monoms.fromMonoforest(monoforest)
        );
        var grid = monoforest.getGrid();

        for (int i = 0; i < FEATURES.length; i++) {
            var f = FEATURES[i];
            byte[] bins = new byte[f.length];
            grid.binarizeTo(new ArrayVec(f), bins);
            var actual = pocketForest.value(bins);
            assertEquals((float) expected[i], actual, 1e-5F);
        }
    }

    @ParameterizedTest
    @MethodSource("provideModelsAndResults")
    void testPocketForestFromEnsemble(Path model, double[] expected) throws IOException {
        var monoforest = converter.readMonoforest(model);
        var monoms = Monoms.fromMonoforest(monoforest);
        var entrys = monoms.monomIndicesCount().object2IntEntrySet().stream().sorted(Comparator.comparingInt(v -> -v.getIntValue())).toList();
        var mapper = new ObjectMapper();
        mapper.writeValue(new File("./stats__m_1749464806_osrch_64885_search_l2_conv__true_prices_control.json"), entrys);
        //        var monoforest = converter.readMonoforest(model);
//        var pocketForest = PocketForestEnsembleFactory.createRowPocketForest(Monoms.fromMonoforest(monoforest));
//        var grid = monoforest.getGrid();
//
//        for (int i = 0; i < FEATURES.length; i++) {
//            var f = FEATURES[i];
//            byte[] bins = new byte[f.length];
//            grid.binarizeTo(new ArrayVec(f), bins);
//            var actual = pocketForest.value(bins);
//            log.info("Score: {}", actual);
//            assertEquals((float) expected[i], actual, 1e-5F);
//        }
    }

    private static Stream<Arguments> provideModelsAndResults() {
        return Stream.of(
//            Arguments.of(MODEL_3_PATH, MODEL_3_RESULTS),
//            Arguments.of(MODEL_10_PATH, MODEL_10_RESULTS),
//            Arguments.of(MODEL_100_PATH, MODEL_100_RESULTS),
//            Arguments.of(MODEL_1000_PATH, MODEL_1000_RESULTS),
//            Arguments.of(MODEL_1000_PATH_2, MODEL_10  00_RESULTS),
            Arguments.of(PROD_MODEL, null)
        );
    }
}