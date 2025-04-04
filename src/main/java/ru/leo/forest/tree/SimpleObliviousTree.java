package ru.leo.forest.tree;

import com.expleague.commons.math.vectors.Vec;
import java.util.List;

public record SimpleObliviousTree(List<Split> splits, double[] leafValues) {

    private int getLeafIdx(Vec vec) {
        int res = 0;
        for (int i = 0; i < splits.size(); i++) {
            if (splits.get(i).value(vec)) {
                res += (1 << i);
            }
        }

        return res;
    }

    public double value(Vec vec) {
        return leafValues[getLeafIdx(vec)];
    }
}
