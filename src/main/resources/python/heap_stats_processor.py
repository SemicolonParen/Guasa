# Copyright (c) 2025 GDK Team. All rights reserved.
#
# This software is the proprietary information of GDK Team.
# Use is subject to license terms.

def calculate_memory_statistics(object_data):
    total_memory = sum(obj['size'] for obj in object_data)
    avg_memory = total_memory / len(object_data) if object_data else 0

    class_distribution = {}
    for obj in object_data:
        class_name = obj['className']
        if class_name not in class_distribution:
            class_distribution[class_name] = {'count': 0, 'total_size': 0}
        class_distribution[class_name]['count'] += 1
        class_distribution[class_name]['total_size'] += obj['size']

    sorted_classes = sorted(
        class_distribution.items(),
        key=lambda x: x[1]['total_size'],
        reverse=True
    )

    return {
        'total_memory': total_memory,
        'average_memory': avg_memory,
        'object_count': len(object_data),
        'class_distribution': sorted_classes,
        'top_memory_consumers': sorted_classes[:10]
    }


def calculate_percentiles(sizes):
    if not sizes:
        return {}

    sorted_sizes = sorted(sizes)
    n = len(sorted_sizes)

    def get_percentile(p):
        index = int(n * p / 100.0)
        if index >= n:
            index = n - 1
        return sorted_sizes[index]

    return {
        'p50': get_percentile(50),
        'p75': get_percentile(75),
        'p90': get_percentile(90),
        'p95': get_percentile(95),
        'p99': get_percentile(99)
    }


def analyze_memory_growth(snapshots):
    if len(snapshots) < 2:
        return None

    growth_rates = []
    for i in range(1, len(snapshots)):
        prev_memory = snapshots[i-1]['total_memory']
        curr_memory = snapshots[i]['total_memory']

        if prev_memory > 0:
            growth_rate = ((curr_memory - prev_memory) / float(prev_memory)) * 100.0
            growth_rates.append(growth_rate)

    avg_growth = sum(growth_rates) / len(growth_rates) if growth_rates else 0

    return {
        'average_growth_rate': avg_growth,
        'max_growth': max(growth_rates) if growth_rates else 0,
        'min_growth': min(growth_rates) if growth_rates else 0,
        'total_snapshots': len(snapshots)
    }


def detect_memory_leaks(class_stats_history):
    potential_leaks = []

    for class_name, history in class_stats_history.items():
        if len(history) < 5:
            continue

        counts = [h['count'] for h in history]

        constantly_growing = True
        for i in range(1, len(counts)):
            if counts[i] <= counts[i-1]:
                constantly_growing = False
                break

        if constantly_growing:
            growth = counts[-1] - counts[0]
            growth_percent = (growth / float(counts[0])) * 100 if counts[0] > 0 else 0

            if growth_percent > 50:
                potential_leaks.append({
                    'class_name': class_name,
                    'initial_count': counts[0],
                    'current_count': counts[-1],
                    'growth': growth,
                    'growth_percent': growth_percent
                })

    return sorted(potential_leaks, key=lambda x: x['growth_percent'], reverse=True)
