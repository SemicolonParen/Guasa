# Copyright (c) 2025 GDK Team. All rights reserved.
#
# This software is the proprietary information of GDK Team.
# Use is subject to license terms.

import json


def serialize_heap_snapshot(snapshot_data):
    try:
        serialized = {
            'timestamp': snapshot_data.get('timestamp', 0),
            'heap_used': snapshot_data.get('heap_used', 0),
            'heap_max': snapshot_data.get('heap_max', 0),
            'heap_committed': snapshot_data.get('heap_committed', 0),
            'tracked_objects': snapshot_data.get('tracked_objects', []),
            'class_statistics': snapshot_data.get('class_statistics', {}),
            'reference_count': snapshot_data.get('reference_count', 0)
        }

        return json.dumps(serialized, indent=2)
    except Exception as e:
        return json.dumps({'error': str(e)})


def deserialize_heap_snapshot(json_data):
    try:
        return json.loads(json_data)
    except Exception as e:
        return {'error': str(e)}


def export_to_csv(object_list, fields):
    csv_lines = [','.join(fields)]

    for obj in object_list:
        values = []
        for field in fields:
            value = obj.get(field, '')
            value_str = str(value).replace(',', ';').replace('\n', ' ')
            values.append(value_str)
        csv_lines.append(','.join(values))

    return '\n'.join(csv_lines)


def parse_csv(csv_text):
    lines = csv_text.strip().split('\n')
    if not lines:
        return []

    headers = lines[0].split(',')
    data = []

    for line in lines[1:]:
        values = line.split(',')
        obj = {}
        for i, header in enumerate(headers):
            if i < len(values):
                obj[header] = values[i]
        data.append(obj)

    return data


def format_memory_report(statistics):
    report_lines = []
    report_lines.append("=" * 60)
    report_lines.append("GUASA MEMORY REPORT")
    report_lines.append("=" * 60)
    report_lines.append("")

    if 'timestamp' in statistics:
        report_lines.append("Timestamp: {}".format(statistics['timestamp']))
        report_lines.append("")

    if 'total_memory' in statistics:
        report_lines.append("Total Memory: {} bytes".format(statistics['total_memory']))

    if 'object_count' in statistics:
        report_lines.append("Object Count: {}".format(statistics['object_count']))

    if 'average_memory' in statistics:
        report_lines.append("Average Object Size: {:.2f} bytes".format(statistics['average_memory']))

    report_lines.append("")
    report_lines.append("-" * 60)
    report_lines.append("TOP MEMORY CONSUMERS")
    report_lines.append("-" * 60)

    if 'top_memory_consumers' in statistics:
        for i, (class_name, data) in enumerate(statistics['top_memory_consumers'], 1):
            report_lines.append("{}. {} - {} instances, {} bytes".format(
                i, class_name, data['count'], data['total_size']
            ))

    report_lines.append("")
    report_lines.append("=" * 60)

    return '\n'.join(report_lines)


def compress_snapshot_data(snapshot_data):
    compressed = {
        't': snapshot_data.get('timestamp', 0),
        'hu': snapshot_data.get('heap_used', 0),
        'hm': snapshot_data.get('heap_max', 0),
        'oc': len(snapshot_data.get('tracked_objects', []))
    }

    class_summary = {}
    for obj in snapshot_data.get('tracked_objects', []):
        cn = obj.get('className', 'Unknown')
        if cn not in class_summary:
            class_summary[cn] = 0
        class_summary[cn] += 1

    compressed['cs'] = class_summary

    return compressed
