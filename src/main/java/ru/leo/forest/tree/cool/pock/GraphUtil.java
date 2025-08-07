package ru.leo.forest.tree.cool.pock;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;

public class GraphUtil {
    public static List<IntList> findConnectedComponentsFromGroups(List<int[]> indexGroups) {
        Int2ObjectMap<IntSet> graph = new Int2ObjectOpenHashMap<>();
        for (int[] group : indexGroups) {
            for (int i = 0; i < group.length; i++) {
                for (int j = i + 1; j < group.length; j++) {
                    graph.computeIfAbsent(group[i], k -> new IntOpenHashSet()).add(group[j]);
                    graph.computeIfAbsent(group[j], k -> new IntOpenHashSet()).add(group[i]);
                }
            }
        }

        return findConnectedComponents(graph);
    }

    /**
     * Получить компоненты связности.
     */
    private static List<IntList> findConnectedComponents(Int2ObjectMap<IntSet> graph) {
        boolean[] visited = new boolean[graph.size()];

        List<IntList> components = new ArrayList<>();
        for (var e : graph.int2ObjectEntrySet()) {
            int vertex = e.getIntKey();
            if (!visited[vertex]) {
                IntList component = new IntArrayList();
                dfs(vertex, graph, visited, component);
                components.add(component);
            }
        }

        return components;
    }

    private static void dfs(int vertex, Int2ObjectMap<IntSet> graph, boolean[] visited, IntList component) {
        visited[vertex] = true;
        component.add(vertex);

        for (int neighbour : graph.get(vertex)) {
            if (!visited[neighbour]) {
                dfs(neighbour, graph, visited, component);
            }
        }
    }
}
