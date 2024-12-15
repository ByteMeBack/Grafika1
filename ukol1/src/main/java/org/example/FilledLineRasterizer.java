package org.example;

public class FilledLineRasterizer extends LineRasterizer {
    private final Canvas canvas;

    public FilledLineRasterizer(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void rasterize(Line line) {
        Point start = line.getStart();
        Point end = line.getEnd();

        int x0 = start.getX();
        int y0 = start.getY();
        int x1 = end.getX();
        int y1 = end.getY();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int steps = Math.max(dx, dy);

        float xStep = (float) (x1 - x0) / steps;
        float yStep = (float) (y1 - y0) / steps;

        float x = x0;
        float y = y0;

        for (int i = 0; i <= steps; i++) {
            canvas.addPixel(Math.round(x), Math.round(y));
            x += xStep;
            y += yStep;
        }

        canvas.repaint();
    }

    public void drawThickLine(Point start, Point end, int thickness) {
        int x0 = start.getX();
        int y0 = start.getY();
        int x1 = end.getX();
        int y1 = end.getY();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int steps = Math.max(dx, dy);

        float xStep = (float) (x1 - x0) / steps;
        float yStep = (float) (y1 - y0) / steps;

        float x = x0;
        float y = y0;

        for (int i = 0; i <= steps; i++) {
            drawCircle(Math.round(x), Math.round(y), thickness);
            x += xStep;
            y += yStep;
        }

        canvas.repaint();
    }

    private void drawCircle(int cx, int cy, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                if (x * x + y * y <= radius * radius) {
                    canvas.addPixel(cx + x, cy + y);
                }
            }
        }
    }
}
