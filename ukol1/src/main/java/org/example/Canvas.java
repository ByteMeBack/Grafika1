package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class Canvas extends JPanel {
    private final Polygon polygon = new Polygon(); // Polygon na plátně
    private final java.util.List<Polygon> completedPolygons = new java.util.ArrayList<>();
    private final FilledLineRasterizer rasterizer;
    private BufferedImage buffer; // Vyrovnávací paměť pro kreslení
    private Point startPoint = null; // Počáteční bod při tažení čáry
    private Point currentPoint = null; // Aktuální bod během tažení
    private boolean isThickLine = false; // Režim kreslení (tenká/tlustá čára)
    private boolean isFreeDrawing = false; // Režim volného kreslení
    private boolean constrainToLines = false; // Režim vodorovné/svislé/úhlopříčné čáry
    private boolean editMode = false; // Režim editace vrcholů
    private Point selectedVertex = null; // Vybraný vrchol pro editaci
    private boolean movingVertex = false; // Příznak přesunu vrcholu
    private Point lastDrawnPoint = null; // Poslední bod ve volném kreslení
    private final java.util.List<Point> points = new java.util.ArrayList<>(); // Seznam bodů
    private final java.util.List<Boolean> lineThicknesses = new java.util.ArrayList<>(); // Typ čáry
    private final java.util.List<Line> freeDrawLines = new java.util.ArrayList<>(); // volná čára
    private final java.util.List<Color> lineColors = new java.util.ArrayList<>();
    private boolean isPolygonMode = false;
    private boolean polygonFirstTwoPointsSet = false;
    private Point polygonFirstPoint = null;
    private Point polygonSecondPoint = null;
    private Point polygonCurrentMousePosition = null;
    private int hoveredSegmentIndex = -1; // Index hrany polygonu, nad kterou je myš (-1 = žádná)
    private java.util.List<Point> insertionPoints = new java.util.ArrayList<>(); // Body pro vložení vrcholu
    private double insertionSpacing = 50.0; // Rozestup bodů na hraně (v pixelech)
    private double lineHoverTolerance = 10.0; // Tolerance pro detekci najetí myši nad hranu
    private Polygon hoveredPolygon = null; // Polygon, na jehož hranou se myš nachází





    public Canvas() {
        rasterizer = new FilledLineRasterizer(this);

        buffer = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);

        // Myš - kliknutí a tažení
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point clickPoint = new Point(e.getX(), e.getY());

                if (editMode) {
                    if (hoveredSegmentIndex != -1 && hoveredPolygon != null && !insertionPoints.isEmpty()) {
                        for (Point ip : insertionPoints) {
                            double dist = Math.hypot(clickPoint.getX() - ip.getX(), clickPoint.getY() - ip.getY());
                            if (dist < 10) {
                                hoveredPolygon.insertVertex(hoveredSegmentIndex + 1, new Point(ip.getX(), ip.getY()));
                                hoveredSegmentIndex = -1;
                                hoveredPolygon = null;
                                insertionPoints.clear();
                                repaint();
                                return;
                            }
                        }
                    }
                    return;
                }


                if (isPolygonMode) {
                    Point newVertex = clickPoint;
                    polygon.addVertex(newVertex);

                    if (polygon.size() == 1) {
                        polygonFirstPoint = newVertex;
                    } else if (polygon.size() == 2) {
                        polygonSecondPoint = newVertex;
                        polygonFirstTwoPointsSet = true;
                    } else {
                    }

                    repaint();
                } else {

                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (editMode) {
                    selectedVertex = findClosestVertex(new Point(e.getX(), e.getY()));
                    if (selectedVertex != null) {
                        movingVertex = true;
                    }
                } else if (isFreeDrawing && !isPolygonMode) {
                    startPoint = new Point(e.getX(), e.getY());
                    lastDrawnPoint = startPoint;
                    drawPoint(startPoint.getX(), startPoint.getY());
                } else if (!isPolygonMode) {
                    startPoint = new Point(e.getX(), e.getY());
                    currentPoint = startPoint;
                }
                requestFocusInWindow();
            }


            @Override
            public void mouseReleased(MouseEvent e) {
                if (movingVertex) {
                    movingVertex = false;
                    selectedVertex = null;
                } else if (!isFreeDrawing && !isPolygonMode && startPoint != null) {

                    Point endPoint = adjustPoint(new Point(e.getX(), e.getY()));

                    points.add(startPoint);
                    points.add(endPoint);
                    lineThicknesses.add(isThickLine);
                    lineColors.add(getCurrentColor());

                    if (isThickLine) {
                        rasterizer.drawThickLine(startPoint, endPoint, 5);
                    } else {
                        rasterizer.rasterize(new Line(startPoint, endPoint, false));
                    }
                }

                startPoint = null;
                currentPoint = null;
                lastDrawnPoint = null;
                repaint();
            }

        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (movingVertex && selectedVertex != null) {
                    selectedVertex.setX(e.getX());
                    selectedVertex.setY(e.getY());

                    redrawCanvas();
                    repaint();
                } else if (!isFreeDrawing && startPoint != null) {
                    currentPoint = adjustPoint(new Point(e.getX(), e.getY()));

                    repaint();
                } else if (isFreeDrawing) {
                    drawPoint(e.getX(), e.getY());
                }
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                if (editMode) {
                    Point mouse = new Point(e.getX(), e.getY());
                    double closestDist = Double.MAX_VALUE;
                    int closestSegment = -1;
                    Polygon foundPolygon = null;

                    if (polygon.size() >= 2) {
                        for (int i = 0; i < polygon.size(); i++) {
                            Point p1 = polygon.getVertex(i);
                            Point p2 = polygon.getVertex((i + 1) % polygon.size());
                            double dist = pointToSegmentDistance(mouse, p1, p2);
                            if (dist < closestDist) {
                                closestDist = dist;
                                closestSegment = i;
                                foundPolygon = polygon;
                            }
                        }
                    }

                    // Kontrola dokončených polygonů
                    for (Polygon poly : completedPolygons) {
                        if (poly.size() < 2) continue;
                        for (int i = 0; i < poly.size(); i++) {
                            Point p1 = poly.getVertex(i);
                            Point p2 = poly.getVertex((i + 1) % poly.size());
                            double dist = pointToSegmentDistance(mouse, p1, p2);
                            if (dist < closestDist) {
                                closestDist = dist;
                                closestSegment = i;
                                foundPolygon = poly;
                            }
                        }
                    }

                    if (foundPolygon != null && closestDist < lineHoverTolerance) {
                        hoveredSegmentIndex = closestSegment;
                        hoveredPolygon = foundPolygon;
                        computeInsertionPoints(hoveredPolygon, hoveredSegmentIndex, insertionSpacing);
                    } else {
                        hoveredSegmentIndex = -1;
                        hoveredPolygon = null;
                        insertionPoints.clear();
                    }

                    repaint();
                } else {
                    hoveredSegmentIndex = -1;
                    hoveredPolygon = null;
                    insertionPoints.clear();
                }

                if (isPolygonMode && polygonFirstTwoPointsSet) {
                    polygonCurrentMousePosition = new Point(e.getX(), e.getY());
                    repaint();
                }
            }

        });

        // Klávesové ovládání
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_C) {
                    clear();
                } else if (e.getKeyCode() == KeyEvent.VK_B) {
                    toggleLineThickness();
                } else if (e.getKeyCode() == KeyEvent.VK_V) {
                    toggleFreeDrawing();
                } else if (e.getKeyCode() == KeyEvent.VK_X) {
                    toggleEditMode();
                } else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    constrainToLines = true;
                } else if (e.getKeyCode() == KeyEvent.VK_P) {
                    togglePolygonMode();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    finalizePolygon();
                }

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    constrainToLines = false;
                }
            }
        });

        setFocusable(true);
        requestFocusInWindow();
    }
    private double pointToSegmentDistance(Point p, Point a, Point b) {
        double px = p.getX();
        double py = p.getY();
        double ax = a.getX();
        double ay = a.getY();
        double bx = b.getX();
        double by = b.getY();

        double dx = bx - ax;
        double dy = by - ay;
        double t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy);
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        double nx = ax + t * dx;
        double ny = ay + t * dy;
        return Math.hypot(px - nx, py - ny);
    }
    private void computeInsertionPoints(Polygon poly, int segmentIndex, double spacing) {
        insertionPoints.clear();
        if (poly.size() < 2) return;

        Point p1 = poly.getVertex(segmentIndex);
        Point p2 = poly.getVertex((segmentIndex + 1) % poly.size());

        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double length = Math.hypot(dx, dy);

        int count = (int) (length / spacing);
        if (count < 1) return;

        double segmentDist = length / (count + 1);

        double ux = dx / length;
        double uy = dy / length;

        for (int i = 1; i <= count; i++) {
            double dist = segmentDist * i; // i-tý insertion point
            int x = (int) Math.round(p1.getX() + ux * dist);
            int y = (int) Math.round(p1.getY() + uy * dist);
            insertionPoints.add(new Point(x, y));
        }
    }

    private void drawVertices(Graphics g) {
        g.setColor(Color.RED);
        for (Point vertex : polygon.getVertices()) {
            g.fillOval(vertex.getX() - 5, vertex.getY() - 5, 10, 10);
         }

        for (Polygon poly : completedPolygons) {
            for (Point vertex : poly.getVertices()) {
                g.fillOval(vertex.getX() - 5, vertex.getY() - 5, 10, 10);
            }
        }
    }


    private void drawCompletedPolygons(Graphics g) {
        g.setColor(Color.BLACK);
        for (Polygon poly : completedPolygons) {
            if (poly.size() > 1) {
                for (int i = 0; i < poly.size(); i++) {
                    Point p1 = poly.getVertex(i);
                    Point p2 = poly.getVertex((i + 1) % poly.size()); // modulo pro uzavření
                    g.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                }
            }
        }
    }


    private void finalizePolygon() {

        if (isPolygonMode && polygon.size() >= 2) {

            Polygon newPoly = new Polygon();
            for (Point v : polygon.getVertices()) {
                newPoly.addVertex(new Point(v.getX(), v.getY()));
            }

            completedPolygons.add(newPoly);

            polygon.clear();
            polygonFirstTwoPointsSet = false;
            polygonFirstPoint = null;
            polygonSecondPoint = null;
            polygonCurrentMousePosition = null;

            redrawCanvas();
            repaint();
            System.out.println("Polygon uložen a uzavřen. Připraven pro nový.");
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(buffer, 0, 0, null);

        drawCompletedPolygons(g);

        if (startPoint != null && currentPoint != null && !isFreeDrawing) {
            g.setColor(getCurrentColor());
            g.drawLine(startPoint.getX(), startPoint.getY(), currentPoint.getX(), currentPoint.getY());
        }

        drawPolygon(g);

        if (editMode) {
            drawVertices(g);

            if (hoveredSegmentIndex != -1) {
                g.setColor(Color.GREEN);
                for (Point ip : insertionPoints) {
                    g.fillOval(ip.getX() - 3, ip.getY() - 3, 6, 6);
                }
            }
        }
    }


    private void togglePolygonMode() {
        isPolygonMode = !isPolygonMode;
        if (!isPolygonMode) {

            polygon.clear();
            polygonFirstTwoPointsSet = false;
            polygonFirstPoint = null;
            polygonSecondPoint = null;
            polygonCurrentMousePosition = null;
            redrawCanvas();
            repaint();
            System.out.println("Režim polygonu: Vypnuto");
        } else {
            System.out.println("Režim polygonu: Zapnuto");
        }
    }


    private void redrawCanvas() {

        Graphics2D g2d = buffer.createGraphics();

        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());

        for (int i = 0; i < points.size() - 1; i += 2) {
            if (lineThicknesses.size() <= i / 2 || lineColors.size() <= i / 2) {

                continue;
            }

            Point start = points.get(i);
            Point end = points.get(i + 1);
            boolean isThick = lineThicknesses.get(i / 2);

            g2d.setColor(lineColors.get(i / 2));

            if (isThick) {
                rasterizer.drawThickLine(start, end, 5);
            } else {
                rasterizer.rasterize(new Line(start, end, false));
            }
        }

        for (Line line : freeDrawLines) {
            if (line.isThick()) {
                rasterizer.drawThickLine(line.getStart(), line.getEnd(), 5);
            } else {
                rasterizer.rasterize(line);
            }
        }

        g2d.dispose();

        repaint();
    }

    private void drawPolygon(Graphics g) {
        if (polygon.size() < 2) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);

        for (int i = 0; i < polygon.size(); i++) {
            Point p1 = polygon.getVertex(i);
            Point p2 = polygon.getVertex((i + 1) % polygon.size());

            if (isPolygonMode && polygon.size() > 2 && i == polygon.size() - 1) {
                g2d.setColor(Color.GREEN);
            } else {
                g2d.setColor(Color.BLACK);
            }

            g2d.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        }


        if (isPolygonMode && polygon.size() >= 2 && polygonCurrentMousePosition != null) {
            g2d.setColor(Color.RED);

            Point firstVertex = polygon.getVertex(0);

            Point lastVertex = polygon.getVertex(polygon.size() - 1);

            g2d.drawLine(firstVertex.getX(), firstVertex.getY(),
                    polygonCurrentMousePosition.getX(), polygonCurrentMousePosition.getY());
            g2d.drawLine(lastVertex.getX(), lastVertex.getY(),
                    polygonCurrentMousePosition.getX(), polygonCurrentMousePosition.getY());
        }
    }

    private void toggleEditMode() {
        editMode = !editMode;
        System.out.println("Režim editace vrcholů: " + (editMode ? "Zapnuto" : "Vypnuto"));
        repaint();
    }

    private Point findClosestVertex(Point point) {
        Point closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Point vertex : polygon.getVertices()) {
            double distance = Math.hypot(vertex.getX() - point.getX(), vertex.getY() - point.getY());
            if (distance < minDistance) {
                minDistance = distance;
                closest = vertex;
            }
        }

        for (Polygon poly : completedPolygons) {
            for (Point vertex : poly.getVertices()) {
                double distance = Math.hypot(vertex.getX() - point.getX(), vertex.getY() - point.getY());
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = vertex;
                }
            }
        }

        return (minDistance < 10) ? closest : null;
    }


    public void clear() {
        points.clear();
        lineThicknesses.clear();
        freeDrawLines.clear();
        polygon.clear();
        lineColors.clear();
        completedPolygons.clear();

        Graphics2D g2d = buffer.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        g2d.dispose();

        repaint();
    }




    public void toggleLineThickness() {
        isThickLine = !isThickLine;
        System.out.println("Režim kreslení: " + (isThickLine ? "Tlustá čára" : "Tenká čára"));
    }

    public void toggleFreeDrawing() {
        isFreeDrawing = !isFreeDrawing;
        System.out.println("Režim volného kreslení: " + (isFreeDrawing ? "Zapnuto" : "Vypnuto"));
    }

    private Point adjustPoint(Point p) {
        if (startPoint == null || !constrainToLines) {
            return p;
        }

        int dx = p.getX() - startPoint.getX();
        int dy = p.getY() - startPoint.getY();
        double slope = dy != 0 ? Math.abs((double) dx / dy) : Double.MAX_VALUE;
        double tolerance = 0.5;

        if (slope > 1 + tolerance) {
            // Vodorovná čára
            return new Point(p.getX(), startPoint.getY());
        } else if (slope < 1 - tolerance) {
            // Svislá čára
            return new Point(startPoint.getX(), p.getY());
        } else {
            // šikmá
            int signX = dx > 0 ? 1 : -1;
            int signY = dy > 0 ? 1 : -1;
            int length = Math.min(Math.abs(dx), Math.abs(dy));
            return new Point(
                    startPoint.getX() + length * signX,
                    startPoint.getY() + length * signY
            );
        }
    }

    private void drawLine(Point start, Point end) {
        if (isThickLine) {
            rasterizer.drawThickLine(start, end, 5);
        } else {
            rasterizer.rasterize(new Line(start, end, isThickLine));
        }
    }

    private void drawPoint(int x, int y) {
        Point newPoint = new Point(x, y);

        if (lastDrawnPoint != null) {
            Line line = new Line(lastDrawnPoint, newPoint, isThickLine);
            freeDrawLines.add(line);

            if (isThickLine) {
                rasterizer.drawThickLine(lastDrawnPoint, newPoint, 5);
            } else {
                rasterizer.rasterize(line);
            }
        } else {
            addPixel(x, y);
        }

        lastDrawnPoint = newPoint;
        repaint();
    }

    public void addPixel(int x, int y) {
        if (x >= 0 && x < buffer.getWidth() && y >= 0 && y < buffer.getHeight()) {
            buffer.setRGB(x, y, getCurrentColor().getRGB());
        }
    }

    public Color getCurrentColor() {
        if (isFreeDrawing) {
            return Color.RED;
        }
        return isThickLine ? Color.BLUE : Color.BLACK;
    }
}
