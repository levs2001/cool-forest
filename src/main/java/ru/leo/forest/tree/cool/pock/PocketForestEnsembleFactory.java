package ru.leo.forest.tree.cool.pock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PocketForestEnsembleFactory {
    private static final Logger log = LoggerFactory.getLogger(PocketForestEnsembleFactory.class);
    private static final int MONOMS_MAX_COUNT = 5000;

    public static PocketForest createRowPocketForest(Monoms monoms) {
        log.info("Monoms size: {}", monoms.monoms().size());

        var blocksList = createInternal(monoms);
        var rowPocketForest = PocketForestFactory.fromPocketForestEnsemble(blocksList);
        log.info("Row pocket forest final matrix size: {}", rowPocketForest.blocks().length);
        return rowPocketForest;
    }

    public static PocketForestEnsemble createEnsemble(Monoms monoms) {
        log.info("Monoms size: {}", monoms.monoms().size());
        var blocksList = createInternal(monoms);
        return new PocketForestEnsemble(blocksList.toArray(new long[0][][]));
    }

    public static List<long[][]> createInternal(Monoms monoms) {
        var top = monoms.monoms().stream()
            .sorted((m1, m2) -> Float.compare(m1.bias(), m2.bias()))
            .limit(MONOMS_MAX_COUNT)
            .toList();
        var topMonoms = Monoms.create(top, monoms.bias());
        List<long[][]> blocksList = new ArrayList<>();
        log.info("Starting ensemble building. Monoms count: {}", topMonoms.size());
        var grouped = topMonoms.groupedMonoms();
        createInternal(topMonoms, grouped, blocksList);
        log.info("Created pocket forest ensemble, size: {}, max matrix size: {}",
            blocksList.size(), blocksList.stream().map(m -> m.length).max(Comparator.comparingInt(d -> d)).get());
        for (int i = 0; i < blocksList.size(); i++) {
            log.info("Matrix {}, size: {}", i, blocksList.get(i).length);
        }
        return blocksList;
    }

    private static void createInternal(Monoms monoms, List<Monoms.MonomGroup> grouped, List<long[][]> matrixes) {
        if (monoms.size() == 0) {
            return;
        }

        log.info("Ensemble building step, matrixes size: {}, monoms left count: {}", matrixes.size(), monoms.size());

        var bestGroup = grouped.stream().max(
            Comparator.comparingInt(v -> v.monoms().size())
//            Comparator.comparingDouble(
//                v -> Math.log(v.monoms().size()) / Math.log(v.key().size())
//            )
        ).get();
        log.info("Best group: {}, monoms count: {}", bestGroup.key(), bestGroup.monoms().size());
        var matrix = PocketForestFactory.create(Monoms.create(bestGroup.monoms(), monoms.bias())).blocks();
        log.info("matrix size: {}", matrix.length);
        matrixes.add(matrix);
        var newMonoms = monoms.monoms().stream().filter(m -> !m.isIncludedIn(bestGroup.key())).toList();
        var newGrouped = grouped.stream()
            .map(g -> new Monoms.MonomGroup(
                g.key(),
                g.monoms().stream().filter(m -> !m.isIncludedIn(bestGroup.key())).toList())
            )
            .toList();
        createInternal(Monoms.create(newMonoms, 0.0f), newGrouped, matrixes);
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
