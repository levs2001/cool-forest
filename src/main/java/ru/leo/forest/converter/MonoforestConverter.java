package ru.leo.forest.converter;

import com.expleague.ml.models.ModelTools;
import java.io.IOException;
import java.nio.file.Path;

public interface MonoforestConverter {
    ModelTools.CompiledOTEnsemble readMonoforest(Path monoforestJsonPath) throws IOException;
}
