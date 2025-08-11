package ru.leo.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import ru.leo.forest.tree.cool.pock.PocketForest;

public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void save(Path path, PocketForest forest) throws IOException {
        mapper.writeValue(path.toFile(), forest);
    }

    public static PocketForest load(Path path) throws IOException {
        return mapper.readValue(path.toFile(), PocketForest.class);
    }
}
