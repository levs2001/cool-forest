package ru.leo.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CsvReaderUtil {
    public static double[][] readNumeric(Path file, boolean skipHeader) throws IOException {
        List<String> rows = Files.readAllLines(file);
        // Skip header
        int featuresInRowN = rows.getFirst().split(",").length;
        if (skipHeader) {
            rows.removeFirst();
        }
        double[][] result = new double[rows.size()][featuresInRowN];
        for (int i = 0; i < rows.size(); i++) {
            String[] vals = rows.get(i).split(",");
            for (int j = 0; j < vals.length; j++) {
                result[i][j] = Float.parseFloat(vals[j]);
            }
        }

        return result;
    }

    public static double[] readOne(Path file) throws IOException {
        List<String> rows = Files.readAllLines(file);
        double[] result = new double[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            result[i] = Float.parseFloat(rows.get(i));
        }

        return result;
    }


    public static float[][] readNumericWithOrder(String file, String[] order) throws IOException {
        List<String> rows = Files.readAllLines(Path.of(file));
        var header = rows.removeFirst();
        var colNames = header.split(",");
        var columns = new HashMap<String, List<Float>>(colNames.length);

        for (var row : rows) {
            int i = 0;
            for (var col : colNames) {
                var featuresInRow = row.split(",");
                columns.computeIfAbsent(col, k -> new ArrayList<>()).add(Float.parseFloat(featuresInRow[i++]));
            }
        }

        float[][] result = new float[rows.size()][colNames.length];
        for (int j = 0; j < order.length; j++) {
            for (int i = 0; i < rows.size(); i++) {
                result[i][j] = columns.get(order[i]).get(j);
            }
        }

        return result;
    }

    public static int[][] readCatHashes(String file) throws IOException {
        List<String> rows = Files.readAllLines(Path.of(file));
        // Skip header
        int featuresInRowN = rows.removeFirst().split(",").length - 1;
        int[][] result = new int[rows.size()][featuresInRowN];
        for (int i = 0; i < rows.size(); i++) {
            String[] vals = rows.get(i).split(",");
            // First index value skipped
            for (int j = 1; j < vals.length; j++) {
                result[i][j - 1] = Integer.parseInt(vals[j]);
            }
        }

        return result;
    }

    public static String[][] fromHashesToCat(int[][] hashes) {
        assert hashes.length > 0;
        int colsCount = hashes[0].length;
        String[][] result = new String[hashes.length][hashes[0].length];
        for (int i = 0; i < hashes.length; i++) {
            for (int j = 0; j < colsCount; j++) {
                result[i][j] = Integer.toString(hashes[i][j]);
            }
        }

        return result;
    }

    public static void write(String file, double[] toWrite) throws IOException {
        Files.write(Path.of(file), Arrays.stream(toWrite).mapToObj(Double::toString).toList());
    }
}
