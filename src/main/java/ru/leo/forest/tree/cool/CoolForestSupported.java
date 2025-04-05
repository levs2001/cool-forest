package ru.leo.forest.tree.cool;

import com.expleague.ml.BFGrid;
import com.expleague.ml.data.impl.BinarizedDataSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

/**
 * Лес с опорным элементом, по которому сделано разбиение мономов
 **/
// TODO: Стоит подумать о том, чтобы более более большие элементы supported содержали более маленькие
//    В них больше мономов и ветвистость может быть лучше, а подобным полным разбиением мы ее убиваем.
//    + тогда не придется по значением мапки идти и доставать поддеревья, а за раз достанем.
public final class CoolForestSupported extends CoolForest.Stub {
    private final int supportFeatureIdx;
    private final Int2ObjectMap<CoolForest> supported;
    private final int[] supportedKeys;
    private final CoolForest extra;

    public CoolForestSupported(BFGrid grid, int supportFeatureIdx, Int2ObjectMap<CoolForest> supported, CoolForest extra) {
        super(grid);
        this.supportFeatureIdx = supportFeatureIdx;
        this.supported = supported;
        this.supportedKeys = supported.keySet().intStream().sorted().toArray();
        this.extra = extra;
    }

    @Override
    public double value(BinarizedDataSet bds, int index) {
        double result = extra.value(bds, index);

        int supportedValue = bds.bins(supportFeatureIdx)[index];
        for (int supportedKey : supportedKeys) {
            if (supportedValue <= supportedKey) {
                return result;
            }
            result += supported.get(supportedKey).value(bds, index);
        }
        return result;
    }

    @Override
    public void add(BinarizedDataSet bds, double[] value) {
        extra.add(bds, value);

        for (int index = 0; index < value.length; index++) {
            int supportedValue = bds.bins(supportFeatureIdx)[index];
            for (int supportedKey : supportedKeys) {
                if (supportedValue <= supportedKey) {
                    break;
                }
                value[index] += supported.get(supportedKey).value(bds, index);
            }
        }
    }
}
