/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.gui.panels;

import com.gdkteam.guasa.core.GuasaCore;
import com.gdkteam.guasa.memory.ObjectTracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ObjectDetailsPanel extends JPanel {
    private final GuasaCore core;
    private final JTable fieldsTable;
    private final DefaultTableModel tableModel;
    private final JTextArea metadataArea;
    private long currentObjectId = -1;

    public ObjectDetailsPanel(GuasaCore core) {
        this.core = core;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Object Details"));

        tableModel = new DefaultTableModel(new String[]{"Field", "Type", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        fieldsTable = new JTable(tableModel);
        fieldsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        metadataArea = new JTextArea(5, 20);
        metadataArea.setEditable(false);
        metadataArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JSplitPane splitPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(metadataArea),
            new JScrollPane(fieldsTable)
        );
        splitPane.setResizeWeight(0.3);

        add(splitPane, BorderLayout.CENTER);
    }

    public void displayObject(long objectId) {
        this.currentObjectId = objectId;

        ObjectTracker.TrackedObject tracked = core.getObjectTracker().getTrackedObject(objectId);

        if (tracked == null || !tracked.isAlive()) {
            clearDisplay();
            metadataArea.setText("Object not found or has been garbage collected");
            return;
        }

        Object obj = tracked.getObject();
        displayObjectMetadata(tracked, obj);
        displayObjectFields(obj);
    }

    private void displayObjectMetadata(ObjectTracker.TrackedObject tracked, Object obj) {
        StringBuilder sb = new StringBuilder();
        sb.append("Object ID: ").append(tracked.getId()).append("\n");
        sb.append("Class: ").append(tracked.getClassName()).append("\n");
        sb.append("Identity Hash: ").append(String.format("0x%x", tracked.getIdentityHashCode())).append("\n");
        sb.append("Tracking Time: ").append(new java.util.Date(tracked.getTrackingTimestamp())).append("\n");
        sb.append("Access Count: ").append(tracked.getAccessCount()).append("\n");

        long estimatedSize = core.getHeapAnalyzer().estimateObjectSize(obj);
        sb.append("Estimated Size: ").append(formatBytes(estimatedSize)).append("\n");

        int refCount = core.getReferenceGraph().getReferenceCount(tracked.getId());
        sb.append("Incoming References: ").append(refCount).append("\n");

        metadataArea.setText(sb.toString());
    }

    private void displayObjectFields(Object obj) {
        tableModel.setRowCount(0);

        if (obj == null) {
            return;
        }

        Class<?> clazz = obj.getClass();

        if (clazz.isArray()) {
            displayArrayElements(obj);
        } else {
            displayClassFields(obj, clazz);
        }
    }

    private void displayArrayElements(Object array) {
        int length = Array.getLength(array);

        for (int i = 0; i < length && i < 1000; i++) {
            try {
                Object element = Array.get(array, i);
                String value = formatValue(element);
                tableModel.addRow(new Object[]{"[" + i + "]", element != null ? element.getClass().getSimpleName() : "null", value});
            } catch (Exception e) {
                tableModel.addRow(new Object[]{"[" + i + "]", "Error", e.getMessage()});
            }
        }

        if (length > 1000) {
            tableModel.addRow(new Object[]{"...", "...", "... " + (length - 1000) + " more elements"});
        }
    }

    private void displayClassFields(Object obj, Class<?> clazz) {
        List<Field> allFields = new ArrayList<>();

        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    allFields.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }

        for (Field field : allFields) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                String valueStr = formatValue(value);

                tableModel.addRow(new Object[]{
                    field.getName(),
                    field.getType().getSimpleName(),
                    valueStr
                });
            } catch (Exception e) {
                tableModel.addRow(new Object[]{field.getName(), field.getType().getSimpleName(), "Error: " + e.getMessage()});
            }
        }
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.length() > 100) {
                return "\"" + str.substring(0, 97) + "...\"";
            }
            return "\"" + str + "\"";
        }

        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            return value.getClass().getComponentType().getSimpleName() + "[" + length + "]";
        }

        if (value instanceof Number || value instanceof Boolean || value instanceof Character) {
            return value.toString();
        }

        Long objectId = core.getObjectTracker().getObjectId(value);
        if (objectId != null) {
            return String.format("%s@%x [ID: %d]",
                value.getClass().getSimpleName(),
                System.identityHashCode(value),
                objectId);
        }

        return value.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(value));
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private void clearDisplay() {
        tableModel.setRowCount(0);
        metadataArea.setText("");
    }
}
