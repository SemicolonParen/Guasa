# Guasa Visual Debugger

Copyright (c) 2025 GDK Team. All rights reserved.

## Overview

Guasa is a production-ready Java library that provides real-time visual debugging and heap inspection capabilities. It acts as a live debugger/inspector that pops up a GUI showing running objects, their states, and references - essentially providing a visual map of your heap in real-time, similar to IntelliJ's debugger but embedded directly in your application.

## Features

- **Real-time Object Tracking**: Track objects as they're created and monitor their lifecycle
- **Heap Analysis**: Comprehensive heap memory analysis with detailed statistics
- **Reference Graph Visualization**: Visual representation of object references and relationships
- **Memory Profiling**: Track memory usage patterns and detect potential leaks
- **Python Integration**: Advanced data processing using Jython for analytics
- **Java Agent Support**: Automatic instrumentation via Java agent
- **Production-Ready**: Designed with production-level code structure and error handling

## Architecture

### Core Components

- **GuasaCore**: Central coordinator managing all subsystems
- **ObjectTracker**: Tracks object instances and their lifecycle
- **HeapAnalyzer**: Analyzes heap memory usage and patterns
- **ReferenceGraph**: Builds and analyzes object reference relationships
- **EventBus**: Event-driven architecture for component communication
- **PythonBridge**: Integration layer for Python utilities via Jython

### GUI Components

- **ObjectTreePanel**: Hierarchical view of tracked objects
- **ObjectDetailsPanel**: Detailed inspection of individual objects
- **HeapViewPanel**: Visual representation of heap usage
- **ReferenceGraphPanel**: Interactive graph visualization
- **MemoryStatsPanel**: Real-time memory statistics

### Python Utilities

- `heap_stats_processor.py`: Memory statistics calculation and leak detection
- `reference_analyzer.py`: Reference pattern analysis and circular reference detection
- `data_serializer.py`: Data serialization and export utilities
- `graph_algorithms.py`: Graph analysis algorithms (topological sort, SCC, PageRank)
- `visualization_helper.py`: Layout algorithms for graph visualization

## Building

### Using Maven

```bash
mvn clean package
```

### Using Gradle

```bash
gradle clean build
```

## Usage

### As a Library

```java
import com.gdkteam.guasa.Guasa;
import com.gdkteam.guasa.api.GuasaAPI;

public class MyApplication {
    public static void main(String[] args) {
        // Initialize Guasa
        Guasa.initialize();

        // Track objects
        MyObject obj = new MyObject();
        GuasaAPI.trackObject(obj);

        // Get object info
        GuasaAPI.ObjectInfo info = GuasaAPI.getObjectInfo(obj);
        System.out.println("Object size: " + info.getEstimatedSize());

        // Take snapshot
        GuasaAPI.takeSnapshot();
    }
}
```

### As a Java Agent

```bash
java -javaagent:guasa-1.0.0.jar=autostart=true,transform=true -jar your-app.jar
```

### Agent Configuration Options

- `autostart=true/false`: Auto-start GUI on initialization
- `transform=true/false`: Enable bytecode transformation
- `include=com.myapp:com.other`: Include packages for tracking
- `exclude=com.exclude:org.skip`: Exclude packages from tracking
- `trackAll=true/false`: Track all objects
- `sampling=100`: Sampling rate (0-100)

## Configuration

Edit `src/main/resources/guasa.properties`:

```properties
scheduler.threads=2
worker.threads=4
update.interval.ms=1000
max.tracked.objects=100000
enable.auto.tracking=true
enable.python.integration=true
gui.refresh.rate.ms=500
enable.memory.profiling=true
enable.reference.tracking=true
```

## API Reference

### GuasaAPI Methods

- `trackObject(Object obj)`: Track an object
- `untrackObject(Object obj)`: Stop tracking an object
- `getObjectInfo(Object obj)`: Get detailed object information
- `getTrackedObjectsByClass(Class<?> clazz)`: Get all tracked instances of a class
- `getTrackedObjectCount()`: Get total count of tracked objects
- `clearAllTracking()`: Clear all tracking data
- `takeSnapshot()`: Force analysis and snapshot
- `getMemoryStats()`: Get current memory statistics

## Dependencies

- Java 17+
- Jython 2.7.3 (for Python integration)
- ASM 9.6 (for bytecode manipulation)
- SLF4J + Logback (for logging)
- Gson (for JSON serialization)
- Swing (for GUI)

## Use Cases

- **Development Tools**: Live debugging during development
- **Teaching**: Visualize object-oriented concepts and memory management
- **Game Engines**: Monitor game object hierarchies and memory usage
- **Performance Tuning**: Identify memory leaks and optimization opportunities
- **System Monitoring**: Real-time application state inspection

## License

This software is the proprietary information of GDK Team. Use is subject to license terms.

## Author

GDK Team

---

For more information and updates, visit our website or contact GDK Team.
