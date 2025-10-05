/*
 * Copyright (c) 2025 GDK Team. All rights reserved.
 *
 * This software is the proprietary information of GDK Team.
 * Use is subject to license terms.
 */

package com.gdkteam.guasa.python;

import org.python.core.*;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PythonBridge {
    private static final Logger logger = LoggerFactory.getLogger(PythonBridge.class);

    private PythonInterpreter interpreter;
    private final Map<String, PyObject> loadedModules;
    private volatile boolean initialized = false;

    public PythonBridge() {
        this.loadedModules = new ConcurrentHashMap<>();
    }

    public void initialize() {
        if (initialized) {
            logger.warn("PythonBridge already initialized");
            return;
        }

        try {
            logger.info("Initializing Python bridge");

            Properties props = new Properties();
            props.setProperty("python.import.site", "false");
            PythonInterpreter.initialize(System.getProperties(), props, new String[0]);

            interpreter = new PythonInterpreter();

            loadPythonModules();

            initialized = true;
            logger.info("Python bridge initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize Python bridge", e);
            throw new RuntimeException("Python bridge initialization failed", e);
        }
    }

    private void loadPythonModules() {
        String[] modules = {
            "heap_stats_processor",
            "reference_analyzer",
            "data_serializer",
            "graph_algorithms",
            "visualization_helper"
        };

        for (String moduleName : modules) {
            try {
                loadModule(moduleName);
            } catch (Exception e) {
                logger.error("Failed to load Python module: {}", moduleName, e);
            }
        }
    }

    private void loadModule(String moduleName) {
        try {
            String resourcePath = "/python/" + moduleName + ".py";
            InputStream is = getClass().getResourceAsStream(resourcePath);

            if (is == null) {
                logger.warn("Python module not found: {}", resourcePath);
                return;
            }

            InputStreamReader reader = new InputStreamReader(is);
            interpreter.execfile(reader);

            PySystemState sys = Py.getSystemState();
            PyObject module = sys.modules.__finditem__(moduleName);

            if (module == null) {
                module = new PyModule(moduleName, null);
            }

            loadedModules.put(moduleName, module);
            logger.info("Loaded Python module: {}", moduleName);

        } catch (Exception e) {
            logger.error("Error loading Python module: {}", moduleName, e);
        }
    }

    public PyObject callFunction(String functionName, Object... args) {
        if (!initialized) {
            throw new IllegalStateException("Python bridge not initialized");
        }

        try {
            PyObject function = interpreter.get(functionName);

            if (function == null) {
                logger.warn("Python function not found: {}", functionName);
                return Py.None;
            }

            PyObject[] pyArgs = new PyObject[args.length];
            for (int i = 0; i < args.length; i++) {
                pyArgs[i] = convertToPython(args[i]);
            }

            return function.__call__(pyArgs);

        } catch (Exception e) {
            logger.error("Error calling Python function: {}", functionName, e);
            return Py.None;
        }
    }

    public Map<String, Object> calculateMemoryStatistics(List<Map<String, Object>> objectData) {
        try {
            PyList pyList = new PyList();
            for (Map<String, Object> obj : objectData) {
                pyList.add(convertToPython(obj));
            }

            PyObject result = callFunction("calculate_memory_statistics", pyList);
            return convertToJava(result);

        } catch (Exception e) {
            logger.error("Error calculating memory statistics", e);
            return Collections.emptyMap();
        }
    }

    public List<Long> findCircularReferences(Map<Long, List<Long>> referenceGraph) {
        try {
            PyObject pyGraph = convertToPython(referenceGraph);
            PyObject result = callFunction("find_circular_references", pyGraph);

            if (result instanceof PyList) {
                List<Long> circular = new ArrayList<>();
                PyList pyResult = (PyList) result;
                for (Object item : pyResult) {
                    if (item instanceof PyList) {
                        PyList cycle = (PyList) item;
                        for (Object nodeObj : cycle) {
                            if (nodeObj instanceof PyLong || nodeObj instanceof PyInteger) {
                                circular.add(((Number) nodeObj).longValue());
                            }
                        }
                    }
                }
                return circular;
            }

            return Collections.emptyList();

        } catch (Exception e) {
            logger.error("Error finding circular references", e);
            return Collections.emptyList();
        }
    }

    public Map<String, Object> analyzeReferencePatterns(Map<Long, List<Long>> referenceGraph) {
        try {
            PyObject pyGraph = convertToPython(referenceGraph);
            PyObject result = callFunction("analyze_reference_patterns", pyGraph);
            return convertToJava(result);

        } catch (Exception e) {
            logger.error("Error analyzing reference patterns", e);
            return Collections.emptyMap();
        }
    }

    public String serializeHeapSnapshot(Map<String, Object> snapshotData) {
        try {
            PyObject pySnapshot = convertToPython(snapshotData);
            PyObject result = callFunction("serialize_heap_snapshot", pySnapshot);

            if (result instanceof PyString) {
                return result.toString();
            }

            return "";

        } catch (Exception e) {
            logger.error("Error serializing heap snapshot", e);
            return "";
        }
    }

    private PyObject convertToPython(Object obj) {
        if (obj == null) {
            return Py.None;
        }

        if (obj instanceof PyObject) {
            return (PyObject) obj;
        }

        if (obj instanceof String) {
            return new PyString((String) obj);
        }

        if (obj instanceof Integer) {
            return new PyInteger((Integer) obj);
        }

        if (obj instanceof Long) {
            return new PyLong((Long) obj);
        }

        if (obj instanceof Double || obj instanceof Float) {
            return new PyFloat(((Number) obj).doubleValue());
        }

        if (obj instanceof Boolean) {
            return new PyBoolean((Boolean) obj);
        }

        if (obj instanceof List) {
            PyList pyList = new PyList();
            for (Object item : (List<?>) obj) {
                pyList.add(convertToPython(item));
            }
            return pyList;
        }

        if (obj instanceof Map) {
            PyDictionary pyDict = new PyDictionary();
            Map<?, ?> map = (Map<?, ?>) obj;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                pyDict.put(convertToPython(entry.getKey()), convertToPython(entry.getValue()));
            }
            return pyDict;
        }

        return Py.java2py(obj);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToJava(PyObject pyObj) {
        if (pyObj == null || pyObj == Py.None) {
            return Collections.emptyMap();
        }

        if (pyObj instanceof PyDictionary) {
            Map<String, Object> map = new HashMap<>();
            PyDictionary pyDict = (PyDictionary) pyObj;

            for (Object keyObj : pyDict.keys()) {
                String key = keyObj.toString();
                PyObject value = pyDict.get(new PyString(key));

                if (value instanceof PyInteger || value instanceof PyLong) {
                    map.put(key, ((Number) value).longValue());
                } else if (value instanceof PyFloat) {
                    map.put(key, ((PyFloat) value).getValue());
                } else if (value instanceof PyString) {
                    map.put(key, value.toString());
                } else if (value instanceof PyList) {
                    map.put(key, convertListToJava((PyList) value));
                } else if (value instanceof PyDictionary) {
                    map.put(key, convertToJava(value));
                } else {
                    map.put(key, value.toString());
                }
            }

            return map;
        }

        return Collections.emptyMap();
    }

    private List<Object> convertListToJava(PyList pyList) {
        List<Object> list = new ArrayList<>();

        for (Object item : pyList) {
            if (item instanceof PyInteger || item instanceof PyLong) {
                list.add(((Number) item).longValue());
            } else if (item instanceof PyFloat) {
                list.add(((PyFloat) item).getValue());
            } else if (item instanceof PyString) {
                list.add(item.toString());
            } else if (item instanceof PyList) {
                list.add(convertListToJava((PyList) item));
            } else if (item instanceof PyDictionary) {
                list.add(convertToJava((PyObject) item));
            } else {
                list.add(item.toString());
            }
        }

        return list;
    }

    public void executeScript(String script) {
        if (!initialized) {
            throw new IllegalStateException("Python bridge not initialized");
        }

        try {
            interpreter.exec(script);
        } catch (Exception e) {
            logger.error("Error executing Python script", e);
        }
    }

    public void shutdown() {
        if (!initialized) {
            return;
        }

        logger.info("Shutting down Python bridge");

        if (interpreter != null) {
            interpreter.close();
        }

        loadedModules.clear();
        initialized = false;

        logger.info("Python bridge shut down");
    }

    public boolean isInitialized() {
        return initialized;
    }

    public Set<String> getLoadedModules() {
        return new HashSet<>(loadedModules.keySet());
    }
}
