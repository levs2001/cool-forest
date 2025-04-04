package ru.leo.forest.tree;

import com.expleague.commons.math.vectors.Vec;

public record Split(double border, int featureIndex) {
    boolean value(Vec vec) {
        return vec.get(featureIndex) > border;
    }
}
