package ru.leo.forest.tree.cool;

import com.expleague.ml.BFGrid;
import com.expleague.ml.data.impl.BinarizedDataSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public final class CoolForestSupportedGiant extends CoolForest.Stub {
    private final int supportFeatureIdx;
    private final Int2ObjectMap<CoolForest> supported;
    private final int[] supportedKeys;
    private final CoolForest extra;

    public CoolForestSupportedGiant(
        BFGrid grid,
        int supportFeatureIdx,
        Int2ObjectMap<CoolForest> supported,
        CoolForest extra
    ) {
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
        if (supportedValue == 0) {
            return result;
        }
        var branch = supported.get(supportedValue - 1);
        if (branch == null) {
            int supportedKeyLast = getSupportedBranchKey(supportedValue);
            if (supportedKeyLast != -1) {
                branch = supported.get(supportedKeyLast);
            }
        }
        if (branch != null) {
            result += branch.value(bds, index);
        }

        return result;
    }

    @Override
    public void add(BinarizedDataSet bds, double[] value) {
        extra.add(bds, value);

        for (int index = 0; index < value.length; index++) {
            int supportedValue = bds.bins(supportFeatureIdx)[index];
            if (supportedValue == 0) {
                continue;
            }
            var branch = supported.get(supportedValue - 1);
            if (branch == null) {
                int supportedKeyLast = getSupportedBranchKey(supportedValue);
                if (supportedKeyLast != -1) {
                    branch = supported.get(supportedKeyLast);
                }
            }
            if (branch != null) {
                value[index] += branch.value(bds, index);
            }
        }
    }

    int getSupportedBranchKey(int supportedValue) {
        int supportedKeyLast = -1;
        for (int supportedKey : supportedKeys) {
            if (supportedValue <= supportedKey) {
                return supportedKeyLast;
            }
            supportedKeyLast = supportedKey;
        }

        return supportedKeyLast;
    }
}
