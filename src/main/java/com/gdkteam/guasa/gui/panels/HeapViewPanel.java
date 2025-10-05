/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.gui.panels;

import com.gdkteam.guasa.core.GuasaCore;
import com.gdkteam.guasa.memory.HeapAnalyzer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class HeapViewPanel extends JPanel {
    private final GuasaCore core;
    private final JTable classStatsTable;
    private final DefaultTableModel tableModel;
    private final HeapVisualizationPanel visualizationPanel;

    public HeapViewPanel(GuasaCore core) {
        this.core = core;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Heap View"));

        tableModel = new DefaultTableModel(
            new String[]{"Class", "Instances", "Total Memory", "Avg Size", "% of Tracked"},
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        classStatsTable = new JTable(tableModel);
        classStatsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        visualizationPanel = new HeapVisualizationPanel();
        visualizationPanel.setPreferredSize(new Dimension(0, 200));

        JSplitPane splitPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            visualizationPanel,
            new JScrollPane(classStatsTable)
        );
        splitPane.setResizeWeight(0.3);

        add(splitPane, BorderLayout.CENTER);
    }

    public void updateView() {
        HeapAnalyzer.HeapSnapshot snapshot = core.getHeapAnalyzer().getLastSnapshot();

        if (snapshot == null) {
            return;
        }

        updateTable(snapshot);
        visualizationPanel.updateVisualization(snapshot);
    }

    private void updateTable(HeapAnalyzer.HeapSnapshot snapshot) {
        tableModel.setRowCount(0);

        Map<String, Integer> objectCounts = snapshot.getObjectCountByClass();
        Map<String, Long> memorySizes = snapshot.getMemorySizeByClass();

        List<Map.Entry<String, Long>> sortedEntries = new ArrayList<>(memorySizes.entrySet());
        sortedEntries.sort((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()));

        long totalMemory = snapshot.getTotalTrackedMemory();

        for (Map.Entry<String, Long> entry : sortedEntries) {
            String className = entry.getKey();
            String shortName = className.substring(className.lastIndexOf('.') + 1);
            int count = objectCounts.getOrDefault(className, 0);
            long memory = entry.getValue();
            long avgSize = count > 0 ? memory / count : 0;
            double percentage = totalMemory > 0 ? (memory * 100.0 / totalMemory) : 0;

            tableModel.addRow(new Object[]{
                shortName,
                count,
                formatBytes(memory),
                formatBytes(avgSize),
                String.format("%.2f%%", percentage)
            });
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private class HeapVisualizationPanel extends JPanel {
        private HeapAnalyzer.HeapSnapshot currentSnapshot;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (currentSnapshot == null) {
                return;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            drawHeapUsageBar(g2d, width, height);
        }

        private void drawHeapUsageBar(Graphics2D g2d, int width, int height) {
            int barHeight = 60;
            int barY = 20;
            int barX = 50;
            int barWidth = width - 100;

            long heapUsed = currentSnapshot.getHeapUsed();
            long heapMax = currentSnapshot.getHeapMax();
            long heapCommitted = currentSnapshot.getHeapCommitted();

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(barX, barY, barWidth, barHeight);

            if (heapMax > 0) {
                int committedWidth = (int) ((heapCommitted * barWidth) / heapMax);
                g2d.setColor(new Color(200, 200, 255));
                g2d.fillRect(barX, barY, committedWidth, barHeight);

                int usedWidth = (int) ((heapUsed * barWidth) / heapMax);
                g2d.setColor(new Color(100, 150, 255));
                g2d.fillRect(barX, barY, usedWidth, barHeight);
            }

            g2d.setColor(Color.BLACK);
            g2d.drawRect(barX, barY, barWidth, barHeight);

            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            String usedText = String.format("Used: %s / %s (%.1f%%)",
                formatBytes(heapUsed),
                formatBytes(heapMax),
                currentSnapshot.getHeapUsagePercentage());
            g2d.drawString(usedText, barX, barY + barHeight + 20);

            String trackedText = String.format("Tracked Objects: %d (Memory: %s)",
                currentSnapshot.getTrackedObjectCount(),
                formatBytes(currentSnapshot.getTotalTrackedMemory()));
            g2d.drawString(trackedText, barX, barY + barHeight + 40);
        }

        public void updateVisualization(HeapAnalyzer.HeapSnapshot snapshot) {
            this.currentSnapshot = snapshot;
            repaint();
        }

        private String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
