package ru.leo.forest.converter;

import com.expleague.ml.func.FuncEnsemble;
import com.expleague.ml.models.ModelTools;
import com.expleague.ml.models.ObliviousTree;
import java.io.IOException;
import java.nio.file.Path;
import ru.leo.forest.tree.BinOptimizedEnsemble;
import ru.leo.forest.tree.SimpleEnsemble;

public interface Converter {
    ModelTools.CompiledOTEnsemble readMonoforest(Path catboostJsonPath) throws IOException;

    FuncEnsemble<ObliviousTree> read(Path catboostJsonPath) throws IOException;

    SimpleEnsemble readSimple(Path catboostJsonPath) throws IOException;

    BinOptimizedEnsemble readOTBin(Path catboostJsonPath) throws IOException;
}
