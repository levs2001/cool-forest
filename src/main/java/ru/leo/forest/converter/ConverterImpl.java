package ru.leo.forest.converter;

import com.expleague.ml.BFGrid;
import com.expleague.ml.BinOptimizedModel;
import com.expleague.ml.func.FuncEnsemble;
import com.expleague.ml.models.ModelTools;
import com.expleague.ml.models.ObliviousTree;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import ru.leo.forest.tree.BinOptimizedEnsemble;
import ru.leo.forest.tree.SimpleEnsemble;
import ru.leo.forest.tree.SimpleObliviousTree;
import ru.leo.forest.tree.Split;

public class ConverterImpl implements Converter {
    private static final String OBLIVIOUS_TREES = "oblivious_trees";
    private static final String SCALE_AND_BIAS = "scale_and_bias";
    private static final String LEAF_VALUES = "leaf_values";
    private static final String SPLITS = "splits";
    private static final String BORDER = "border";
    private static final String FLOAT_FEATURE_INDEX = "float_feature_index";
    private static final GridMaker gridMaker = new GridMakerImpl();

    @Override
    public ModelTools.CompiledOTEnsemble readMonoforest(Path catboostJsonPath) throws IOException {
        var simple = readSimple(catboostJsonPath);
        var func = fromSimpleToFunc(simple);
        return ModelTools.compile(func, simple.scale(), simple.bias());
    }

    @Override
    public FuncEnsemble<ObliviousTree> read(Path catboostJsonPath) throws IOException {
        return fromSimpleToFunc(readSimple(catboostJsonPath));
    }

    @Override
    public SimpleEnsemble readSimple(Path catboostJsonPath) throws IOException {
        var jsonObject = new JSONObject(Files.readString(catboostJsonPath));
        var treesJson = jsonObject.getJSONArray(OBLIVIOUS_TREES);
        List<SimpleObliviousTree> trees = new ArrayList<>(treesJson.length());
        for (int i = 0; i < treesJson.length(); i++) {
            trees.add(getTree(treesJson.getJSONObject(i)));
        }

        var scaleAndBias = jsonObject.getJSONArray(SCALE_AND_BIAS);
        var scale = scaleAndBias.getFloat(0);
        var bias = scaleAndBias.getJSONArray(1).getFloat(0);
        return new SimpleEnsemble(trees, scale, bias);
    }

    @Override
    public BinOptimizedEnsemble readOTBin(Path catboostJsonPath) throws IOException {
        var jmll = JmllEnsemble.fromSimple(readSimple(catboostJsonPath));
        return new BinOptimizedEnsemble(
            jmll.trees.stream().map(t -> (BinOptimizedModel) t).toList(),
            jmll.grid,
            jmll.scale,
            jmll.bias
        );
    }

    FuncEnsemble<ObliviousTree> fromSimpleToFunc(SimpleEnsemble simpleEnsemble) {
        var jmll = JmllEnsemble.fromSimple(simpleEnsemble);
        var res = new FuncEnsemble<>(jmll.trees, 1.0D, jmll.scale, jmll.bias);
        res.setWeighting(false);
        return res;
    }

    private static ObliviousTree mapTree(SimpleObliviousTree tree, BFGrid bfGrid) {
        List<BFGrid.Feature> features = new ArrayList<>(tree.splits().size());
        for (int i = tree.splits().size() - 1; i >= 0; i--) {
            // TODO: Проверить эту логику
            var split = tree.splits().get(i);
            var row = bfGrid.row(split.featureIndex());
            var bin = row.bin(split.border());
            features.add(row.bf(bin));
        }

        return new ObliviousTree(features, tree.leafValues());
    }

    private static SimpleObliviousTree getTree(JSONObject treeJson) {
        var leafValuesJson = treeJson.getJSONArray(LEAF_VALUES);
        var leafValues = new double[leafValuesJson.length()];
        for (int i = 0; i < leafValues.length; i++) {
            leafValues[i] = leafValuesJson.getDouble(i);
        }
        return new SimpleObliviousTree(getSplits(treeJson), leafValues);
    }

    public static List<Split> getSplits(JSONObject treeJson) {
        var splits = treeJson.getJSONArray(SPLITS);
        var result = new ArrayList<Split>(splits.length());
        for (int i = 0; i < splits.length(); i++) {
            result.add(getSplit(splits.getJSONObject(i)));
        }

        return result;
    }

    private static Split getSplit(JSONObject splitJson) {
        return new Split(splitJson.getDouble(BORDER), splitJson.getInt(FLOAT_FEATURE_INDEX));
    }

    record JmllEnsemble(List<ObliviousTree> trees, BFGrid grid, double scale, double bias) {
        static JmllEnsemble fromSimple(SimpleEnsemble simpleEnsemble) {
            List<Split> allSplits = new ArrayList<>();
            simpleEnsemble.trees().forEach(t -> allSplits.addAll(t.splits()));
            var grid = gridMaker.makeGrid(allSplits);

            var trees = simpleEnsemble.trees().stream().map(t -> mapTree(t, grid)).toList();
            return new JmllEnsemble(trees, grid, simpleEnsemble.scale(), simpleEnsemble.bias());
        }
    }
}
