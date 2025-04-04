package ru.leo.forest.converter;

import com.expleague.ml.BFGrid;
import com.expleague.ml.impl.BFGridConstructor;
import java.util.List;
import ru.leo.forest.tree.Split;

public class GridMakerImpl implements GridMaker {
    @Override
    public BFGrid makeGrid(List<Split> splits) {
        var constructor = new BFGridConstructor();
        for (var s : splits) {
            constructor.condition(s.featureIndex(), s.border());
        }
        return constructor.build();
    }
}
