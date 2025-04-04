package ru.leo.forest.tree;

import java.util.List;

public record Monom(List<Split> splits, double value) {
}
