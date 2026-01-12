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


    public static float[][] readNumericWithOrder(String file, String[] order, int count) throws IOException {
        List<String> rows = new ArrayList<>(count);
        try (var reader = Files.newBufferedReader(Path.of(file))) {
            for (int i = 0; i < count; i++) {
                rows.add(reader.readLine());
            }
        }
        var header = rows.removeFirst();
        var colNames = header.split(",");
        var columns = new HashMap<String, float[]>(colNames.length);

        for (int r = 0; r < rows.size(); r++) {
            int i = 0;
            for (var col : colNames) {
                var featuresInRow = rows.get(r).split(",");
                float fl;
                try {
                    fl = Float.parseFloat(featuresInRow[i++]);
                } catch (NumberFormatException e) {
                    fl = 0.0f;
                }
                columns.computeIfAbsent(col, k -> new float[rows.size()])[r] = fl;
            }
        }

        float[][] result = new float[rows.size()][order.length];
        for (int i = 0; i < rows.size(); i++) {
            for (int j = 0; j < order.length; j++) {
                result[i][j] = columns.get(order[j])[i];
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
            // First index bias skipped
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

    public static void write(String file, float[][] toWrite) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (float[] floats : toWrite) {
            for (int j = 0; j < floats.length; j++) {
                sb.append(floats[j]);
                if (j != floats.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("\n");
        }
        Files.writeString(Path.of(file), sb.toString());
    }

    public static void write(String file, double[] toWrite) throws IOException {
        Files.write(Path.of(file), Arrays.stream(toWrite).mapToObj(Double::toString).toList());
    }

    public static void main(String[] args) throws IOException {
        var d = CsvReaderUtil.readNumericWithOrder(
            "./real_models/first_thousand_rows.csv",
            FeaturesList.NAMES,
            2000
        );
        CsvReaderUtil.write("d.csv", d);
    }
}
