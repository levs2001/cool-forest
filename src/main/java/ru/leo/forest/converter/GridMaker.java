package ru.leo.forest.converter;

import com.expleague.ml.BFGrid;
import java.util.List;
import ru.leo.forest.tree.Split;

public interface GridMaker {
    BFGrid makeGrid(List<Split> splits);
}
