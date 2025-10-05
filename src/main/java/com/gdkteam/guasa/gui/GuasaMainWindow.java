/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.gui;

import com.gdkteam.guasa.core.GuasaCore;
import com.gdkteam.guasa.event.GuasaEvent;
import com.gdkteam.guasa.gui.panels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GuasaMainWindow extends JFrame {
    private final GuasaCore core;
    private final ObjectTreePanel objectTreePanel;
    private final ObjectDetailsPanel objectDetailsPanel;
    private final HeapViewPanel heapViewPanel;
    private final ReferenceGraphPanel referenceGraphPanel;
    private final MemoryStatsPanel memoryStatsPanel;
    private final ControlPanel controlPanel;

    public GuasaMainWindow(GuasaCore core) {
        this.core = core;

        setTitle("Guasa Visual Debugger - GDK Team");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1600, 1000);
        setLocationRelativeTo(null);

        objectTreePanel = new ObjectTreePanel(core);
        objectDetailsPanel = new ObjectDetailsPanel(core);
        heapViewPanel = new HeapViewPanel(core);
        referenceGraphPanel = new ReferenceGraphPanel(core);
        memoryStatsPanel = new MemoryStatsPanel(core);
        controlPanel = new ControlPanel(core);

        initializeLayout();
        setupEventListeners();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });
    }

    private void initializeLayout() {
        setLayout(new BorderLayout(5, 5));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.NORTH);
        topPanel.add(memoryStatsPanel, BorderLayout.CENTER);

        JSplitPane leftSplit = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(objectTreePanel),
            new JScrollPane(objectDetailsPanel)
        );
        leftSplit.setResizeWeight(0.6);
        leftSplit.setDividerLocation(0.6);

        JTabbedPane centerTabs = new JTabbedPane();
        centerTabs.addTab("Heap View", new JScrollPane(heapViewPanel));
        centerTabs.addTab("Reference Graph", new JScrollPane(referenceGraphPanel));

        JSplitPane mainSplit = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            leftSplit,
            centerTabs
        );
        mainSplit.setResizeWeight(0.3);
        mainSplit.setDividerLocation(0.3);

        add(topPanel, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        JLabel statusLabel = new JLabel("Ready");
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        core.getEventBus().subscribe(GuasaEvent.ObjectSelected.class, event -> {
            SwingUtilities.invokeLater(() -> {
                objectDetailsPanel.displayObject(event.getObjectId());
                referenceGraphPanel.focusOnObject(event.getObjectId());
            });
        });

        core.getEventBus().subscribe(GuasaEvent.HeapAnalyzed.class, event -> {
            SwingUtilities.invokeLater(() -> {
                heapViewPanel.updateView();
                memoryStatsPanel.updateStats();
            });
        });

        core.getEventBus().subscribe(GuasaEvent.ReferenceGraphUpdated.class, event -> {
            SwingUtilities.invokeLater(() -> {
                referenceGraphPanel.refresh();
            });
        });
    }

    private void shutdown() {
        core.stop();
    }

    public ObjectTreePanel getObjectTreePanel() {
        return objectTreePanel;
    }

    public ObjectDetailsPanel getObjectDetailsPanel() {
        return objectDetailsPanel;
    }

    public HeapViewPanel getHeapViewPanel() {
        return heapViewPanel;
    }

    public ReferenceGraphPanel getReferenceGraphPanel() {
        return referenceGraphPanel;
    }
}
