# Copyright (c) 2025 GDK Team. All rights reserved.
#
# This software is the proprietary information of GDK Team.
# Use is subject to license terms.

def topological_sort(graph):
    in_degree = {}
    all_nodes = set(graph.keys())

    for node in graph:
        if node not in in_degree:
            in_degree[node] = 0
        for neighbor in graph[node]:
            all_nodes.add(neighbor)
            in_degree[neighbor] = in_degree.get(neighbor, 0) + 1

    queue = [node for node in all_nodes if in_degree.get(node, 0) == 0]
    result = []

    while queue:
        node = queue.pop(0)
        result.append(node)

        if node in graph:
            for neighbor in graph[node]:
                in_degree[neighbor] -= 1
                if in_degree[neighbor] == 0:
                    queue.append(neighbor)

    if len(result) != len(all_nodes):
        return None

    return result


def strongly_connected_components(graph):
    index_counter = [0]
    stack = []
    lowlink = {}
    index = {}
    on_stack = {}
    components = []

    def strongconnect(node):
        index[node] = index_counter[0]
        lowlink[node] = index_counter[0]
        index_counter[0] += 1
        stack.append(node)
        on_stack[node] = True

        if node in graph:
            for successor in graph[node]:
                if successor not in index:
                    strongconnect(successor)
                    lowlink[node] = min(lowlink[node], lowlink[successor])
                elif on_stack.get(successor, False):
                    lowlink[node] = min(lowlink[node], index[successor])

        if lowlink[node] == index[node]:
            component = []
            while True:
                successor = stack.pop()
                on_stack[successor] = False
                component.append(successor)
                if successor == node:
                    break
            components.append(component)

    for node in graph:
        if node not in index:
            strongconnect(node)

    return components


def find_all_paths(graph, start, end, max_depth=10):
    paths = []

    def dfs(current, target, path, visited, depth):
        if depth > max_depth:
            return

        if current == target:
            paths.append(path[:])
            return

        if current not in graph:
            return

        for neighbor in graph[current]:
            if neighbor not in visited:
                visited.add(neighbor)
                path.append(neighbor)
                dfs(neighbor, target, path, visited, depth + 1)
                path.pop()
                visited.remove(neighbor)

    visited_set = set([start])
    dfs(start, end, [start], visited_set, 0)

    return paths


def calculate_betweenness_centrality(graph):
    centrality = {node: 0.0 for node in graph}

    nodes = list(graph.keys())

    for source in nodes:
        stack = []
        predecessors = {node: [] for node in nodes}
        distances = {node: -1 for node in nodes}
        sigma = {node: 0 for node in nodes}

        distances[source] = 0
        sigma[source] = 1

        queue = [source]

        while queue:
            current = queue.pop(0)
            stack.append(current)

            if current in graph:
                for neighbor in graph[current]:
                    if distances[neighbor] < 0:
                        queue.append(neighbor)
                        distances[neighbor] = distances[current] + 1

                    if distances[neighbor] == distances[current] + 1:
                        sigma[neighbor] += sigma[current]
                        predecessors[neighbor].append(current)

        delta = {node: 0.0 for node in nodes}

        while stack:
            w = stack.pop()
            for v in predecessors[w]:
                delta[v] += (float(sigma[v]) / sigma[w]) * (1 + delta[w])

            if w != source:
                centrality[w] += delta[w]

    return centrality


def detect_communities(graph, iterations=10):
    nodes = list(graph.keys())
    community = {node: i for i, node in enumerate(nodes)}

    for _ in range(iterations):
        for node in nodes:
            if node not in graph:
                continue

            neighbor_communities = {}
            for neighbor in graph[node]:
                comm = community[neighbor]
                neighbor_communities[comm] = neighbor_communities.get(comm, 0) + 1

            if neighbor_communities:
                best_community = max(neighbor_communities.items(), key=lambda x: x[1])[0]
                community[node] = best_community

    communities = {}
    for node, comm in community.items():
        if comm not in communities:
            communities[comm] = []
        communities[comm].append(node)

    return list(communities.values())


def calculate_page_rank(graph, damping=0.85, iterations=100):
    nodes = list(graph.keys())
    n = len(nodes)

    if n == 0:
        return {}

    page_rank = {node: 1.0 / n for node in nodes}

    out_degree = {node: len(graph[node]) if node in graph else 0 for node in nodes}

    for _ in range(iterations):
        new_rank = {}

        for node in nodes:
            rank_sum = 0.0

            for other_node in nodes:
                if other_node in graph and node in graph[other_node]:
                    if out_degree[other_node] > 0:
                        rank_sum += page_rank[other_node] / out_degree[other_node]

            new_rank[node] = (1 - damping) / n + damping * rank_sum

        page_rank = new_rank

    return page_rank
