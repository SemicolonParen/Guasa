# Copyright (c) 2025 GDK Team. All rights reserved.
#
# This software is the proprietary information of GDK Team.
# Use is subject to license terms.

def find_circular_references(reference_graph):
    circular_refs = []
    visited = set()
    rec_stack = set()

    def dfs(node_id, path):
        if node_id in rec_stack:
            cycle_start = path.index(node_id)
            cycle = path[cycle_start:]
            circular_refs.append(cycle)
            return True

        if node_id in visited:
            return False

        visited.add(node_id)
        rec_stack.add(node_id)
        path.append(node_id)

        if node_id in reference_graph:
            for target_id in reference_graph[node_id]:
                dfs(target_id, path[:])

        rec_stack.remove(node_id)
        return False

    for node_id in reference_graph.keys():
        if node_id not in visited:
            dfs(node_id, [])

    return circular_refs


def calculate_object_depth(object_id, reference_graph):
    visited = set()

    def dfs_depth(node_id, depth):
        if node_id in visited:
            return depth

        visited.add(node_id)

        if node_id not in reference_graph or not reference_graph[node_id]:
            return depth

        max_child_depth = depth
        for child_id in reference_graph[node_id]:
            child_depth = dfs_depth(child_id, depth + 1)
            max_child_depth = max(max_child_depth, child_depth)

        return max_child_depth

    return dfs_depth(object_id, 0)


def analyze_reference_patterns(reference_graph):
    in_degree = {}
    out_degree = {}

    for node_id in reference_graph.keys():
        out_degree[node_id] = len(reference_graph[node_id])

        for target_id in reference_graph[node_id]:
            in_degree[target_id] = in_degree.get(target_id, 0) + 1

    roots = [nid for nid in reference_graph.keys() if in_degree.get(nid, 0) == 0]
    leaves = [nid for nid in reference_graph.keys() if out_degree.get(nid, 0) == 0]

    highly_referenced = sorted(
        in_degree.items(),
        key=lambda x: x[1],
        reverse=True
    )[:10]

    highly_referencing = sorted(
        out_degree.items(),
        key=lambda x: x[1],
        reverse=True
    )[:10]

    return {
        'root_objects': roots,
        'leaf_objects': leaves,
        'highly_referenced': highly_referenced,
        'highly_referencing': highly_referencing,
        'total_references': sum(out_degree.values())
    }


def find_shortest_path(reference_graph, start_id, end_id):
    if start_id == end_id:
        return [start_id]

    queue = [(start_id, [start_id])]
    visited = set([start_id])

    while queue:
        current_id, path = queue.pop(0)

        if current_id not in reference_graph:
            continue

        for neighbor_id in reference_graph[current_id]:
            if neighbor_id == end_id:
                return path + [neighbor_id]

            if neighbor_id not in visited:
                visited.add(neighbor_id)
                queue.append((neighbor_id, path + [neighbor_id]))

    return None


def calculate_reference_density(reference_graph):
    total_nodes = len(reference_graph)
    total_edges = sum(len(targets) for targets in reference_graph.values())

    if total_nodes == 0:
        return 0.0

    max_possible_edges = total_nodes * (total_nodes - 1)

    if max_possible_edges == 0:
        return 0.0

    density = (float(total_edges) / max_possible_edges) * 100.0

    return {
        'density_percent': density,
        'total_nodes': total_nodes,
        'total_edges': total_edges,
        'avg_out_degree': float(total_edges) / total_nodes if total_nodes > 0 else 0
    }
