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
import java.awt.*;

public class MemoryStatsPanel extends JPanel {
    private final GuasaCore core;
    private final JLabel heapUsedLabel;
    private final JLabel heapMaxLabel;
    private final JLabel trackedObjectsLabel;
    private final JLabel trackedMemoryLabel;
    private final JProgressBar heapUsageBar;

    public MemoryStatsPanel(GuasaCore core) {
        this.core = core;

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Memory Statistics"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        heapUsedLabel = new JLabel("Heap Used: --");
        heapMaxLabel = new JLabel("Heap Max: --");
        trackedObjectsLabel = new JLabel("Tracked Objects: --");
        trackedMemoryLabel = new JLabel("Tracked Memory: --");

        heapUsageBar = new JProgressBar(0, 100);
        heapUsageBar.setStringPainted(true);
        heapUsageBar.setPreferredSize(new Dimension(300, 25));

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Heap Usage:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        add(heapUsageBar, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(heapUsedLabel, gbc);

        gbc.gridx = 1;
        add(heapMaxLabel, gbc);

        gbc.gridx = 2;
        add(trackedObjectsLabel, gbc);

        gbc.gridx = 3;
        add(trackedMemoryLabel, gbc);
    }

    public void updateStats() {
        HeapAnalyzer.HeapSnapshot snapshot = core.getHeapAnalyzer().getLastSnapshot();

        if (snapshot == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            heapUsedLabel.setText("Heap Used: " + formatBytes(snapshot.getHeapUsed()));
            heapMaxLabel.setText("Heap Max: " + formatBytes(snapshot.getHeapMax()));
            trackedObjectsLabel.setText("Tracked Objects: " + snapshot.getTrackedObjectCount());
            trackedMemoryLabel.setText("Tracked Memory: " + formatBytes(snapshot.getTotalTrackedMemory()));

            int usagePercent = (int) snapshot.getHeapUsagePercentage();
            heapUsageBar.setValue(usagePercent);
            heapUsageBar.setString(usagePercent + "%");

            if (usagePercent > 90) {
                heapUsageBar.setForeground(Color.RED);
            } else if (usagePercent > 75) {
                heapUsageBar.setForeground(Color.ORANGE);
            } else {
                heapUsageBar.setForeground(new Color(100, 150, 255));
            }
        });
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
