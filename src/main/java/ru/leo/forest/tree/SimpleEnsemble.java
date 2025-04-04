package ru.leo.forest.tree;

import com.expleague.commons.math.DiscontinuousTrans;
import com.expleague.commons.math.Func;
import com.expleague.commons.math.Trans;
import com.expleague.commons.math.vectors.Mx;
import com.expleague.commons.math.vectors.Vec;
import java.util.List;
import org.jetbrains.annotations.Nullable;

// TODO: avoid all mappings
public record SimpleEnsemble(List<SimpleObliviousTree> trees, double scale, double bias) implements Func {
    @Override
    public double value(Vec vec) {
        double res = 0;
        for (var tree : trees) {
            res += tree.value(vec);
        }

        return res * scale + bias;
    }

    @Override
    public int dim() {
        return 0;
    }


    @Override
    public int xdim() {
        return 0;
    }

    @Override
    public int ydim() {
        return 0;
    }

    @Nullable
    @Override
    public Trans gradient() {
        return null;
    }

    @Nullable
    @Override
    public DiscontinuousTrans subgradient() {
        return null;
    }

    @Override
    public Vec trans(Vec x) {
        return null;
    }

    @Override
    public Vec transTo(Vec x, Vec to) {
        return null;
    }

    @Override
    public Mx transAll(Mx x) {
        return null;
    }

    @Override
    public Mx transAll(Mx ds, boolean parallel) {
        return null;
    }

    @Override
    public Vec apply(Vec vec) {
        return null;
    }
}
