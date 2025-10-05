/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.gui.panels;

import com.gdkteam.guasa.core.GuasaCore;
import com.gdkteam.guasa.event.GuasaEvent;
import com.gdkteam.guasa.memory.ObjectTracker;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ObjectTreePanel extends JPanel {
    private final GuasaCore core;
    private final JTree objectTree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;
    private final JTextField filterField;
    private final Timer refreshTimer;

    public ObjectTreePanel(GuasaCore core) {
        this.core = core;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Tracked Objects"));

        rootNode = new DefaultMutableTreeNode("Objects");
        treeModel = new DefaultTreeModel(rootNode);
        objectTree = new JTree(treeModel);
        objectTree.setRootVisible(true);

        filterField = new JTextField();
        filterField.setToolTipText("Filter objects by class name");

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(new JLabel("Filter:"), BorderLayout.WEST);
        topPanel.add(filterField, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(objectTree), BorderLayout.CENTER);

        setupListeners();

        refreshTimer = new Timer(core.getConfiguration().getGuiRefreshRateMs(), e -> refreshTree());
        refreshTimer.start();
    }

    private void setupListeners() {
        objectTree.addTreeSelectionListener(e -> {
            TreePath path = e.getNewLeadSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObject = node.getUserObject();
                if (userObject instanceof ObjectNode) {
                    ObjectNode objectNode = (ObjectNode) userObject;
                    core.getEventBus().publish(new GuasaEvent.ObjectSelected(objectNode.getId()));
                }
            }
        });

        filterField.addActionListener(e -> {
            core.getEventBus().publish(new GuasaEvent.FilterChanged(filterField.getText()));
            refreshTree();
        });
    }

    private void refreshTree() {
        String filterText = filterField.getText().toLowerCase();
        Collection<ObjectTracker.TrackedObject> trackedObjects = core.getObjectTracker().getAllTrackedObjects();

        Map<String, List<ObjectNode>> objectsByClass = new HashMap<>();

        for (ObjectTracker.TrackedObject tracked : trackedObjects) {
            if (tracked.isAlive()) {
                String className = tracked.getClassName();

                if (filterText.isEmpty() || className.toLowerCase().contains(filterText)) {
                    objectsByClass.computeIfAbsent(className, k -> new ArrayList<>())
                        .add(new ObjectNode(tracked.getId(), className, tracked.getIdentityHashCode()));
                }
            }
        }

        SwingUtilities.invokeLater(() -> {
            rootNode.removeAllChildren();

            List<String> sortedClasses = new ArrayList<>(objectsByClass.keySet());
            Collections.sort(sortedClasses);

            for (String className : sortedClasses) {
                List<ObjectNode> objects = objectsByClass.get(className);
                DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(
                    className + " (" + objects.size() + ")"
                );

                for (ObjectNode obj : objects) {
                    classNode.add(new DefaultMutableTreeNode(obj));
                }

                rootNode.add(classNode);
            }

            treeModel.reload();

            for (int i = 0; i < objectTree.getRowCount() && i < 100; i++) {
                objectTree.expandRow(i);
            }
        });
    }

    public void stopRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    private static class ObjectNode {
        private final long id;
        private final String className;
        private final int hashCode;

        public ObjectNode(long id, String className, int hashCode) {
            this.id = id;
            this.className = className;
            this.hashCode = hashCode;
        }

        public long getId() {
            return id;
        }

        @Override
        public String toString() {
            String shortClassName = className.substring(className.lastIndexOf('.') + 1);
            return String.format("%s@%x [ID: %d]", shortClassName, hashCode, id);
        }
    }
}
