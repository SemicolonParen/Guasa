/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.graph;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class GraphRenderer {

    public static void renderGraph(Graphics2D g2d, GraphLayout layout, int width, int height) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        for (GraphLayout.Edge edge : layout.getEdges()) {
            renderEdge(g2d, edge);
        }

        for (GraphLayout.Node node : layout.getNodes()) {
            renderNode(g2d, node);
        }
    }

    private static void renderNode(Graphics2D g2d, GraphLayout.Node node) {
        int x = (int) node.getX();
        int y = (int) node.getY();
        int size = 60;
        int halfSize = size / 2;

        Color nodeColor = node.isSelected() ? new Color(100, 150, 255) : new Color(200, 220, 255);
        g2d.setColor(nodeColor);
        g2d.fillOval(x - halfSize, y - halfSize, size, size);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x - halfSize, y - halfSize, size, size);

        String label = node.getLabel();
        if (label.length() > 15) {
            label = label.substring(0, 12) + "...";
        }

        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(label);
        g2d.drawString(label, x - textWidth / 2, y + 4);
    }

    private static void renderEdge(Graphics2D g2d, GraphLayout.Edge edge) {
        GraphLayout.Node source = edge.getSource();
        GraphLayout.Node target = edge.getTarget();

        int x1 = (int) source.getX();
        int y1 = (int) source.getY();
        int x2 = (int) target.getX();
        int y2 = (int) target.getY();

        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(x1, y1, x2, y2);

        drawArrowHead(g2d, x1, y1, x2, y2);

        if (edge.getLabel() != null && !edge.getLabel().isEmpty()) {
            int midX = (x1 + x2) / 2;
            int midY = (y1 + y2) / 2;

            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString(edge.getLabel(), midX + 5, midY - 5);
        }
    }

    private static void drawArrowHead(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 10;

        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        xPoints[0] = x2;
        yPoints[0] = y2;

        xPoints[1] = x2 - (int) (arrowSize * Math.cos(angle - Math.PI / 6));
        yPoints[1] = y2 - (int) (arrowSize * Math.sin(angle - Math.PI / 6));

        xPoints[2] = x2 - (int) (arrowSize * Math.cos(angle + Math.PI / 6));
        yPoints[2] = y2 - (int) (arrowSize * Math.sin(angle + Math.PI / 6));

        g2d.fillPolygon(xPoints, yPoints, 3);
    }
}
