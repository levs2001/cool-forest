package ru.leo.forest.tree.cool.pock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PocketForestEnsembleFactory {
    public static PocketForestEnsemble create(Monoms monoms) {
        List<long[][]> blocksList = new ArrayList<>();
        createInternal(monoms, blocksList);
        return new PocketForestEnsemble(blocksList.toArray(new long[0][][]));
    }

    private static void createInternal(Monoms monoms, List<long[][]> matrixes) {
        if (monoms.size() == 0) {
            return;
        }

        var grouped = monoms.groupedMonoms();
        var bestGroup = grouped.entrySet().stream().max(Comparator.comparingInt(v -> v.getValue().monoms().size())).get();
        var matrix = PocketForestFactory.create(Monoms.create(bestGroup.getValue().monoms(), monoms.bias())).blocks();
        matrixes.add(matrix);
        var newMonoms = monoms.monoms().stream().filter(m -> !m.isIncludedIn(bestGroup.getKey())).toList();

        createInternal(new Monoms(newMonoms, 0.0f, Monoms.firstIdx(newMonoms)), matrixes);
    }

    public static void emulateBuild(Monoms monoms) {
        if (monoms.size() == 0) {
            return;
        }
        var groupValues = monoms.groupValues();
        var bestGroup = groupValues.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).get();
        System.out.println(bestGroup);
        var bestIndices = bestGroup.getKey();
        var newMonoms = monoms.monoms().stream().filter(m -> !m.isIncludedIn(bestIndices)).toList();
        emulateBuild(Monoms.create(newMonoms, 0));
    }


}
