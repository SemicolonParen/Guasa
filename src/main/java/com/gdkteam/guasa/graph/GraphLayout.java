/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.graph;

import java.util.*;

public class GraphLayout {
    private final List<Node> nodes;
    private final List<Edge> edges;

    public GraphLayout() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void clear() {
        nodes.clear();
        edges.clear();
    }

    public void applyForceDirectedLayout(int width, int height, int iterations) {
        if (nodes.isEmpty()) {
            return;
        }

        double k = Math.sqrt((width * height) / (double) nodes.size());

        for (int iteration = 0; iteration < iterations; iteration++) {
            Map<Node, Force> forces = new HashMap<>();
            for (Node node : nodes) {
                forces.put(node, new Force());
            }

            for (int i = 0; i < nodes.size(); i++) {
                for (int j = i + 1; j < nodes.size(); j++) {
                    Node n1 = nodes.get(i);
                    Node n2 = nodes.get(j);

                    double dx = n2.x - n1.x;
                    double dy = n2.y - n1.y;
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance < 0.01) {
                        distance = 0.01;
                    }

                    double repulsion = k * k / distance;
                    double fx = (dx / distance) * repulsion;
                    double fy = (dy / distance) * repulsion;

                    forces.get(n1).fx -= fx;
                    forces.get(n1).fy -= fy;
                    forces.get(n2).fx += fx;
                    forces.get(n2).fy += fy;
                }
            }

            for (Edge edge : edges) {
                Node source = edge.source;
                Node target = edge.target;

                double dx = target.x - source.x;
                double dy = target.y - source.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < 0.01) {
                    distance = 0.01;
                }

                double attraction = distance * distance / k;
                double fx = (dx / distance) * attraction;
                double fy = (dy / distance) * attraction;

                forces.get(source).fx += fx;
                forces.get(source).fy += fy;
                forces.get(target).fx -= fx;
                forces.get(target).fy -= fy;
            }

            double temp = 1.0 - (double) iteration / iterations;

            for (Node node : nodes) {
                Force force = forces.get(node);
                node.x += force.fx * temp;
                node.y += force.fy * temp;

                node.x = Math.max(50, Math.min(width - 50, node.x));
                node.y = Math.max(50, Math.min(height - 50, node.y));
            }
        }
    }

    public static class Node {
        private final long id;
        private String label;
        private double x;
        private double y;
        private boolean selected;

        public Node(long id, String label, double x, double y) {
            this.id = id;
            this.label = label;
            this.x = x;
            this.y = y;
            this.selected = false;
        }

        public long getId() { return id; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
    }

    public static class Edge {
        private final Node source;
        private final Node target;
        private String label;

        public Edge(Node source, Node target, String label) {
            this.source = source;
            this.target = target;
            this.label = label;
        }

        public Node getSource() { return source; }
        public Node getTarget() { return target; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    private static class Force {
        double fx = 0;
        double fy = 0;
    }
}
