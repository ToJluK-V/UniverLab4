package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.util.ArrayList;

public class GraphicsDisplay extends JPanel {

    private static final double TURN_CONSTANT = Math.PI / 2;
    private double turn = 0;

    private Double[][] graphicsData;
    private boolean[] pointsCon;
    private ArrayList<Double[]> pointList = new ArrayList<Double[]>();
    private ArrayList<Double> squares = new ArrayList<Double>();

    private boolean showAxis = true;
    private boolean showMarkers = true;
    private boolean showSquare = true;

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    private double scale;

    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;

    private Font axisFont;

    public GraphicsDisplay() {
        setBackground(Color.WHITE);

        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, new float[]{3, 1, 1, 1, 1, 1, 2, 1, 2, 1}, 0.0f);
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0F, null, 0.0f);

        axisFont = new Font("Serif", Font.BOLD, 36);
    }

    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        calculatePoints();
        calculateSquare();
        repaint();
    }

    public void setPointCon(boolean[] pointsCon) {
        this.pointsCon = pointsCon;
    }


    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    public void setShowSquare(boolean showSquare) {
        this.showSquare = showSquare;
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (graphicsData == null || graphicsData.length == 0) return;

        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length - 1][0];
        minY = graphicsData[0][1];
        maxY = minY;

        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] < minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1] > maxY) {
                maxY = graphicsData[i][1];
            }
        }

        double scaleX = getSize().getWidth() / (maxX - minX);
        double scaleY = getSize().getHeight() / (maxY - minY);

        scale = Math.min(scaleX, scaleY);

        if (scale == scaleX) {
            double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
            maxY += yIncrement;
            minY -= yIncrement;
        }
        if (scale == scaleY) {
            double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;
            maxX += xIncrement;
            minX -= xIncrement;
        }

        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();


        if (showAxis) paintAxis(canvas);

        paintGraphics(canvas);

        if (showMarkers) paintMarkers(canvas);

        if (showSquare) paintSquare(canvas);


        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }



    protected void paintSquare(Graphics2D canvas) {
        double sq = 0.0;
        double maxY = pointList.get(0)[1];
        double forX = pointList.get(0)[0];

        GeneralPath path = new GeneralPath();
        Point2D.Double p1 = xyToPoint(forX, 0);
        Point2D.Double p2 = xyToPoint(forX, maxY);

        path.moveTo(p1.getX(), p1.getY());
        path.lineTo(p2.getX(), p2.getY());

        int itr = 0;

        for (int i = 1; i < pointList.size(); i++) {
            double x2 = pointList.get(i)[0];
            double y2 = pointList.get(i)[1];

            Point2D.Double p3 = xyToPoint(x2, y2);
            path.lineTo(p3.getX(), p3.getY());


            if (Math.abs(y2) > Math.abs(maxY)) {
                maxY = y2;
            }

            sq += squares.get(itr++);

            if (y2 == 0 || i == pointList.size() - 1) {
                if (i == pointList.size() - 1) {
                    Point2D.Double kek = xyToPoint(x2, 0);
                    path.lineTo(kek.getX(), kek.getY());
                }

                path.closePath();
                canvas.setColor(Color.BLACK);
                canvas.fill(path);

                Point2D.Double labelPos = xyToPoint((x2 + forX) / 2.0, (0 + maxY) / 2.0);
                canvas.setPaint(Color.RED);
                canvas.setFont(new Font("TimesRoman", Font.BOLD, 13));
                canvas.drawString("s = " + String.format("%.2f", sq),
                        (float) (labelPos.getX() - (i != pointList.size() - 1 ? (x2 - forX) * 3 : 0)),
                        (float) (labelPos.getY() + maxY));

                path = new GeneralPath();
                Point2D.Double l = xyToPoint(x2, y2);
                path.moveTo(l.getX(), l.getY());

                forX = x2;
                maxY = y2;
                sq = 0;
            }
        }
    }

    protected void calculateSquare() {
        for (int i = 0; i < pointList.size() - 1; i++) {
            double x1 = pointList.get(i)[0];
            double y1 = pointList.get(i)[1];
            double x2 = pointList.get(i + 1)[0];
            double y2 = pointList.get(i + 1)[1];

            double sq = (Math.abs(y2) + Math.abs(y1)) * (x2 - x1) / 2.0;
            squares.add(sq);
        }
    }

    protected void calculatePoints() {
        for (int i = 0; i < graphicsData.length - 1; i++) {
            double x1 = graphicsData[i][0];
            double y1 = graphicsData[i][1];
            double x2 = graphicsData[i + 1][0];
            double y2 = graphicsData[i + 1][1];

            if (y1 * y2 < 0) {
                double k = (y2 - y1) / (x2 - x1);
                double b = y1 - k * x1;
                double x = -b / k;
                pointList.add(new Double[]{x1, y1});
                pointList.add(new Double[]{x, 0.0});
            } else {
                pointList.add(new Double[]{x1, y1});
            }
        }
        pointList.add(new Double[]{graphicsData[graphicsData.length - 1][0],
                graphicsData[graphicsData.length - 1][1]});
    }

    protected void paintGraphics(Graphics2D canvas) {
        canvas.setStroke(graphicsStroke);
        canvas.setColor(Color.RED);

        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            if (i > 0) {
                graphics.lineTo(point.getX(), point.getY());
            } else {
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        canvas.draw(graphics);
    }

    protected void paintMarkers(Graphics2D canvas) {

        canvas.setStroke(markerStroke);

        int itr = 0;
        for (Double[] point : graphicsData) {

            if (pointsCon[itr]) {
                canvas.setColor(Color.BLUE);
                canvas.setPaint(Color.BLUE);
            }
            if (!pointsCon[itr]) {
                canvas.setColor(Color.RED);
                canvas.setPaint(Color.RED);
            }

            GeneralPath path = new GeneralPath();
            Point2D.Double center = xyToPoint(point[0], point[1]);
            path.moveTo(center.getX(), center.getY());
            path.lineTo(center.getX() + 11, center.getY());
            path.moveTo(center.getX(), center.getY());
            path.lineTo(center.getX() - 11, center.getY());
            path.moveTo(center.getX(), center.getY());
            path.lineTo(center.getX(), center.getY() - 11);
            path.moveTo(center.getX(), center.getY());
            path.lineTo(center.getX(), center.getY() + 11);
            path.moveTo(center.getX(), center.getY());
            path.lineTo(center.getX() - 11, center.getY() - 11);
            path.moveTo(center.getX(), center.getY());
            path.lineTo(center.getX() - 11, center.getY() + 11);
            path.moveTo(center.getX(), center.getY());
            path.lineTo(center.getX() + 11, center.getY() - 11);
            path.moveTo(center.getX(), center.getY());
            path.lineTo(center.getX() + 11, center.getY() + 11);

            canvas.draw(path);
            itr++;
        }

    }

    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);
        canvas.setFont(axisFont);

        FontRenderContext context = canvas.getFontRenderContext();

        if (minX <= 0.0 && maxX >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));

            GeneralPath arrow = new GeneralPath();

            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());

            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);

            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);

            canvas.drawString("y", (float) labelPos.getX() + 10,
                    (float) (labelPos.getY() - bounds.getY()));
        }
        if (minY <= 0.0 && maxY >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));

            GeneralPath arrow = new GeneralPath();

            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);

            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);

            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);

            canvas.drawString("x", (float) (labelPos.getX() - bounds.getWidth() - 10),
                    (float) (labelPos.getY() + bounds.getY()));

        }

    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scale, deltaY * scale);
    }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        Point2D.Double dest = new Point2D.Double();
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }

}