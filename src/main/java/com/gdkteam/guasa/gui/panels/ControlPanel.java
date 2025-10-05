/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.gui.panels;

import com.gdkteam.guasa.core.GuasaCore;

import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
    private final GuasaCore core;
    private final JButton pauseButton;
    private final JButton resumeButton;
    private final JButton gcButton;
    private final JButton clearButton;
    private final JButton snapshotButton;

    public ControlPanel(GuasaCore core) {
        this.core = core;

        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        setBorder(BorderFactory.createEtchedBorder());

        pauseButton = new JButton("Pause");
        resumeButton = new JButton("Resume");
        gcButton = new JButton("Force GC");
        clearButton = new JButton("Clear Tracking");
        snapshotButton = new JButton("Take Snapshot");

        resumeButton.setEnabled(false);

        pauseButton.addActionListener(e -> pauseTracking());
        resumeButton.addActionListener(e -> resumeTracking());
        gcButton.addActionListener(e -> forceGarbageCollection());
        clearButton.addActionListener(e -> clearTracking());
        snapshotButton.addActionListener(e -> takeSnapshot());

        add(pauseButton);
        add(resumeButton);
        add(gcButton);
        add(clearButton);
        add(snapshotButton);
    }

    private void pauseTracking() {
        core.getObjectTracker().stop();
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(true);
    }

    private void resumeTracking() {
        core.getObjectTracker().start();
        pauseButton.setEnabled(true);
        resumeButton.setEnabled(false);
    }

    private void forceGarbageCollection() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Force garbage collection? This may cause a temporary pause.",
            "Confirm GC",
            JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            System.gc();
            JOptionPane.showMessageDialog(this, "Garbage collection requested");
        }
    }

    private void clearTracking() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Clear all tracked objects? This cannot be undone.",
            "Confirm Clear",
            JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            core.getObjectTracker().clearAllTracking();
            core.getReferenceGraph().clear();
            JOptionPane.showMessageDialog(this, "Tracking data cleared");
        }
    }

    private void takeSnapshot() {
        core.getHeapAnalyzer().analyze();
        core.getReferenceGraph().update();
        JOptionPane.showMessageDialog(this, "Snapshot taken successfully");
    }
}
