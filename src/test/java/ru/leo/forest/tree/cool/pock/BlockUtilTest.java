package ru.leo.forest.tree.cool.pock;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BlockUtilTest {

    @ParameterizedTest
    @MethodSource("idxAndBias")
    void testCorrectBlock(int nextIdx, float bias) {
        long block = BlockUtil.makeBlock(nextIdx, bias);
        int actualNextIdx = BlockUtil.getNextIdx(block);
        float actualBias = BlockUtil.getBias(block);

        assertThat(actualNextIdx).isEqualTo(nextIdx);
        assertThat(actualBias).isEqualTo(bias);
    }

    private static Stream<Arguments> idxAndBias() {
        return Stream.of(
//            Arguments.of(10, 4.3f),
//            Arguments.of(-1, 6.4f),
            Arguments.of(900, -55.31f)
//            Arguments.of(0, 10f)
        );
    }

}