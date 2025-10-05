/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.gui.panels;

import com.gdkteam.guasa.core.GuasaCore;
import com.gdkteam.guasa.memory.ObjectTracker;
import com.gdkteam.guasa.memory.ReferenceGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class ReferenceGraphPanel extends JPanel {
    private final GuasaCore core;
    private final GraphCanvas graphCanvas;
    private long focusedObjectId = -1;

    public ReferenceGraphPanel(GuasaCore core) {
        this.core = core;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Reference Graph"));

        graphCanvas = new GraphCanvas();

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clear());

        controlPanel.add(refreshButton);
        controlPanel.add(clearButton);

        add(controlPanel, BorderLayout.NORTH);
        add(graphCanvas, BorderLayout.CENTER);
    }

    public void focusOnObject(long objectId) {
        this.focusedObjectId = objectId;
        refresh();
    }

    public void refresh() {
        graphCanvas.buildGraph(focusedObjectId);
        graphCanvas.repaint();
    }

    public void clear() {
        focusedObjectId = -1;
        graphCanvas.clear();
    }

    private class GraphCanvas extends JPanel {
        private final Map<Long, GraphNode> nodes = new HashMap<>();
        private final List<GraphEdge> edges = new ArrayList<>();
        private GraphNode selectedNode = null;
        private Point dragOffset = null;

        public GraphCanvas() {
            setBackground(Color.WHITE);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    selectedNode = findNodeAt(e.getPoint());
                    if (selectedNode != null) {
                        dragOffset = new Point(
                            e.getX() - selectedNode.x,
                            e.getY() - selectedNode.y
                        );
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    selectedNode = null;
                    dragOffset = null;
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (selectedNode != null && dragOffset != null) {
                        selectedNode.x = e.getX() - dragOffset.x;
                        selectedNode.y = e.getY() - dragOffset.y;
                        repaint();
                    }
                }
            });
        }

        public void buildGraph(long focusId) {
            nodes.clear();
            edges.clear();

            if (focusId < 0) {
                return;
            }

            ObjectTracker.TrackedObject focusObject = core.getObjectTracker().getTrackedObject(focusId);
            if (focusObject == null || !focusObject.isAlive()) {
                return;
            }

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            GraphNode focusNode = new GraphNode(focusId, focusObject.getClassName(), centerX, centerY);
            nodes.put(focusId, focusNode);

            Set<ReferenceGraph.ObjectReference> outgoing = core.getReferenceGraph().getOutgoingReferences(focusId);
            Set<Long> incoming = core.getReferenceGraph().getIncomingReferences(focusId);

            int angleStep = 360 / Math.max(outgoing.size(), 1);
            int radius = 150;
            int angle = 0;

            for (ReferenceGraph.ObjectReference ref : outgoing) {
                long targetId = ref.getTargetId();
                ObjectTracker.TrackedObject target = core.getObjectTracker().getTrackedObject(targetId);

                if (target != null && target.isAlive()) {
                    double rad = Math.toRadians(angle);
                    int x = centerX + (int) (radius * Math.cos(rad));
                    int y = centerY + (int) (radius * Math.sin(rad));

                    GraphNode targetNode = new GraphNode(targetId, target.getClassName(), x, y);
                    nodes.put(targetId, targetNode);

                    edges.add(new GraphEdge(focusNode, targetNode, ref.getFieldName()));
                    angle += angleStep;
                }
            }

            angleStep = 360 / Math.max(incoming.size(), 1);
            radius = 250;
            angle = 0;

            for (Long sourceId : incoming) {
                if (!nodes.containsKey(sourceId)) {
                    ObjectTracker.TrackedObject source = core.getObjectTracker().getTrackedObject(sourceId);

                    if (source != null && source.isAlive()) {
                        double rad = Math.toRadians(angle);
                        int x = centerX + (int) (radius * Math.cos(rad));
                        int y = centerY + (int) (radius * Math.sin(rad));

                        GraphNode sourceNode = new GraphNode(sourceId, source.getClassName(), x, y);
                        nodes.put(sourceId, sourceNode);

                        edges.add(new GraphEdge(sourceNode, focusNode, "ref"));
                        angle += angleStep;
                    }
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (GraphEdge edge : edges) {
                drawEdge(g2d, edge);
            }

            for (GraphNode node : nodes.values()) {
                drawNode(g2d, node);
            }
        }

        private void drawNode(Graphics2D g2d, GraphNode node) {
            int size = 80;
            int halfSize = size / 2;

            Color nodeColor = (node.id == focusedObjectId) ? new Color(100, 150, 255) : new Color(200, 220, 255);
            g2d.setColor(nodeColor);
            g2d.fillOval(node.x - halfSize, node.y - halfSize, size, size);

            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(node.x - halfSize, node.y - halfSize, size, size);

            String shortName = node.className.substring(node.className.lastIndexOf('.') + 1);
            if (shortName.length() > 12) {
                shortName = shortName.substring(0, 10) + "..";
            }

            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(shortName);
            g2d.drawString(shortName, node.x - textWidth / 2, node.y);

            String idText = "#" + node.id;
            int idWidth = fm.stringWidth(idText);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString(idText, node.x - idWidth / 2, node.y + 12);
        }

        private void drawEdge(Graphics2D g2d, GraphEdge edge) {
            g2d.setColor(new Color(100, 100, 100));
            g2d.setStroke(new BasicStroke(1.5f));

            int x1 = edge.from.x;
            int y1 = edge.from.y;
            int x2 = edge.to.x;
            int y2 = edge.to.y;

            g2d.drawLine(x1, y1, x2, y2);

            drawArrowHead(g2d, x1, y1, x2, y2);
        }

        private void drawArrowHead(Graphics2D g2d, int x1, int y1, int x2, int y2) {
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

        private GraphNode findNodeAt(Point point) {
            for (GraphNode node : nodes.values()) {
                int dx = point.x - node.x;
                int dy = point.y - node.y;
                if (Math.sqrt(dx * dx + dy * dy) <= 40) {
                    return node;
                }
            }
            return null;
        }

        public void clear() {
            nodes.clear();
            edges.clear();
            repaint();
        }
    }

    private static class GraphNode {
        long id;
        String className;
        int x, y;

        GraphNode(long id, String className, int x, int y) {
            this.id = id;
            this.className = className;
            this.x = x;
            this.y = y;
        }
    }

    private static class GraphEdge {
        GraphNode from;
        GraphNode to;
        String label;

        GraphEdge(GraphNode from, GraphNode to, String label) {
            this.from = from;
            this.to = to;
            this.label = label;
        }
    }
}
