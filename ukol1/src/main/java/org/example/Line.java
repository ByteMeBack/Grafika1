package org.example;

public class Line {
    private Point start;
    private Point end;
    private boolean isThick;

    public Line(Point start, Point end, boolean isThick) {
        this.start = start;
        this.end = end;
        this.isThick = isThick;
    }

    public Point getStart() {
        return start;
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end = end;
    }

    public boolean isThick() {
        return isThick;
    }

    public void setThick(boolean isThick) {
        this.isThick = isThick;
    }

    @Override
    public String toString() {
        return "Line{" +
                "start=" + start +
                ", end=" + end +
                ", isThick=" + isThick +
                '}';
    }
}
