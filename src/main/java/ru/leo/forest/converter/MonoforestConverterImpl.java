package ru.leo.forest.converter;

import com.expleague.ml.BFGrid;
import com.expleague.ml.models.ModelTools;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import ru.leo.forest.tree.Monom;
import ru.leo.forest.tree.Split;

public class MonoforestConverterImpl implements MonoforestConverter {
    private static final String VALUE = "value";
    private final GridMaker gridMaker = new GridMakerImpl();

    @Override
    public ModelTools.CompiledOTEnsemble readMonoforest(Path monoforestJsonPath) throws IOException {
        var monoms = readMonoms(monoforestJsonPath);

        List<Split> allSplits = new ArrayList<>();
        monoms.forEach(m -> allSplits.addAll(m.splits()));
        var grid = gridMaker.makeGrid(allSplits);

        var entrys = monoms.stream().map(m -> monomToEntry(m, grid)).toList();

        return new ModelTools.CompiledOTEnsemble(entrys, grid);
    }

    private ModelTools.CompiledOTEnsemble.Entry monomToEntry(Monom monom, BFGrid grid) {
        var splits = monom.splits();
        int[] bfIndexes = new int[splits.size()];
        for (int i = 0; i < splits.size(); i++) {
            var split = splits.get(i);
            var row = grid.row(split.featureIndex());
            var bin = row.bin(split.border());
            bfIndexes[i] = row.bf(bin).index();
        }
        return new ModelTools.CompiledOTEnsemble.Entry(bfIndexes, monom.value());
    }

    private List<Monom> readMonoms(Path monoforestJsonPath) throws IOException {
        var jsonMonoms = new JSONArray(Files.readString(monoforestJsonPath));
        List<Monom> result = new ArrayList<>(jsonMonoms.length());
        for (int i = 0; i < jsonMonoms.length(); i++) {
            var jsonMonom = jsonMonoms.getJSONObject(i);
            var value = jsonMonom.getDouble(VALUE);
            var splits = ConverterImpl.getSplits(jsonMonom);
            result.add(new Monom(splits, value));
        }

        return result;
    }
}
