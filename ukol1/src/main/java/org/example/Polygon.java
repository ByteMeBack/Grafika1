package org.example;

import java.util.ArrayList;
import java.util.List;


public class Polygon {
    private final List<Point> vertices = new ArrayList<>(); // Seznam vrcholů

    public void addVertex(Point point) {
        vertices.add(point);
    }

    public List<Point> getVertices() {
        return vertices;
    }

    public void clear() {
        vertices.clear();
    }

    public int size() {
        return vertices.size();
    }

    public Point getVertex(int index) {
        return vertices.get(index);
    }
    public void insertVertex(int index, Point point) {
        if (index < 0) index = 0;
        if (index > vertices.size()) index = vertices.size();
        vertices.add(index, point);
    }

}

