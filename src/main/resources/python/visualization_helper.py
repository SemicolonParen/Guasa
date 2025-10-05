# Copyright (c) 2025 GDK Team. All rights reserved.
#
# This software is the proprietary information of GDK Team.
# Use is subject to license terms.

def calculate_graph_layout(nodes, edges, width, height, algorithm='force'):
    if algorithm == 'force':
        return force_directed_layout(nodes, edges, width, height)
    elif algorithm == 'circular':
        return circular_layout(nodes, width, height)
    elif algorithm == 'hierarchical':
        return hierarchical_layout(nodes, edges, width, height)
    else:
        return random_layout(nodes, width, height)


def force_directed_layout(nodes, edges, width, height, iterations=50):
    import random
    import math

    positions = {}
    for node in nodes:
        positions[node] = {
            'x': random.uniform(width * 0.1, width * 0.9),
            'y': random.uniform(height * 0.1, height * 0.9)
        }

    edge_set = set()
    for edge in edges:
        edge_set.add((edge['source'], edge['target']))

    k = math.sqrt((width * height) / len(nodes)) if nodes else 1

    for iteration in range(iterations):
        forces = {node: {'x': 0, 'y': 0} for node in nodes}

        for i, node1 in enumerate(nodes):
            for node2 in nodes[i+1:]:
                dx = positions[node2]['x'] - positions[node1]['x']
                dy = positions[node2]['y'] - positions[node1]['y']
                distance = math.sqrt(dx * dx + dy * dy) or 0.01

                repulsion = k * k / distance
                fx = (dx / distance) * repulsion
                fy = (dy / distance) * repulsion

                forces[node1]['x'] -= fx
                forces[node1]['y'] -= fy
                forces[node2]['x'] += fx
                forces[node2]['y'] += fy

        for source, target in edge_set:
            if source in positions and target in positions:
                dx = positions[target]['x'] - positions[source]['x']
                dy = positions[target]['y'] - positions[source]['y']
                distance = math.sqrt(dx * dx + dy * dy) or 0.01

                attraction = distance * distance / k
                fx = (dx / distance) * attraction
                fy = (dy / distance) * attraction

                forces[source]['x'] += fx
                forces[source]['y'] += fy
                forces[target]['x'] -= fx
                forces[target]['y'] -= fy

        temperature = 1.0 - (float(iteration) / iterations)

        for node in nodes:
            dx = forces[node]['x'] * temperature
            dy = forces[node]['y'] * temperature

            positions[node]['x'] += dx
            positions[node]['y'] += dy

            positions[node]['x'] = max(50, min(width - 50, positions[node]['x']))
            positions[node]['y'] = max(50, min(height - 50, positions[node]['y']))

    return positions


def circular_layout(nodes, width, height):
    import math

    n = len(nodes)
    if n == 0:
        return {}

    center_x = width / 2.0
    center_y = height / 2.0
    radius = min(width, height) * 0.4

    positions = {}
    for i, node in enumerate(nodes):
        angle = 2 * math.pi * i / n
        positions[node] = {
            'x': center_x + radius * math.cos(angle),
            'y': center_y + radius * math.sin(angle)
        }

    return positions


def hierarchical_layout(nodes, edges, width, height):
    levels = {}
    visited = set()

    edge_dict = {}
    for edge in edges:
        source = edge['source']
        if source not in edge_dict:
            edge_dict[source] = []
        edge_dict[source].append(edge['target'])

    roots = [node for node in nodes if node not in [e['target'] for e in edges]]

    if not roots:
        roots = [nodes[0]] if nodes else []

    def assign_level(node, level):
        if node in visited:
            return
        visited.add(node)

        if level not in levels:
            levels[level] = []
        levels[level].append(node)

        if node in edge_dict:
            for child in edge_dict[node]:
                assign_level(child, level + 1)

    for root in roots:
        assign_level(root, 0)

    for node in nodes:
        if node not in visited:
            levels[0].append(node)

    positions = {}
    max_level = max(levels.keys()) if levels else 0
    level_height = height / (max_level + 1) if max_level > 0 else height / 2

    for level, level_nodes in levels.items():
        y = level_height * (level + 0.5)
        node_width = width / (len(level_nodes) + 1)

        for i, node in enumerate(level_nodes):
            x = node_width * (i + 1)
            positions[node] = {'x': x, 'y': y}

    return positions


def random_layout(nodes, width, height):
    import random

    positions = {}
    for node in nodes:
        positions[node] = {
            'x': random.uniform(50, width - 50),
            'y': random.uniform(50, height - 50)
        }

    return positions


def calculate_color_gradient(value, min_value, max_value):
    if max_value == min_value:
        ratio = 0.5
    else:
        ratio = (value - min_value) / float(max_value - min_value)

    if ratio < 0.5:
        r = int(255 * (ratio * 2))
        g = 255
        b = 0
    else:
        r = 255
        g = int(255 * (1 - (ratio - 0.5) * 2))
        b = 0

    return {'r': r, 'g': g, 'b': b}


def generate_heatmap_data(values, bins=10):
    if not values:
        return []

    min_val = min(values)
    max_val = max(values)

    if max_val == min_val:
        return [{'range': [min_val, max_val], 'count': len(values)}]

    bin_size = (max_val - min_val) / float(bins)
    histogram = [0] * bins

    for value in values:
        bin_index = int((value - min_val) / bin_size)
        if bin_index >= bins:
            bin_index = bins - 1
        histogram[bin_index] += 1

    result = []
    for i in range(bins):
        result.append({
            'range': [min_val + i * bin_size, min_val + (i + 1) * bin_size],
            'count': histogram[i]
        })

    return result
