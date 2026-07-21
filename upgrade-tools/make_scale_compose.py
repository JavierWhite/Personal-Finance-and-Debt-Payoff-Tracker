#!/usr/bin/env python3
"""Create a Compose file that allows debt-service to scale horizontally."""

from __future__ import annotations

import argparse
from pathlib import Path
import re
import sys


def indent_width(line: str) -> int:
    return len(line) - len(line.lstrip(" "))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", default="docker-compose.yml")
    parser.add_argument("--output", default="docker-compose.scale.yml")
    args = parser.parse_args()

    source = Path(args.input)
    target = Path(args.output)
    if not source.exists():
        print(f"ERROR: {source} does not exist", file=sys.stderr)
        return 2

    lines = source.read_text(encoding="utf-8").splitlines()
    service_start = None
    service_end = None

    for index, line in enumerate(lines):
        if re.match(r"^\s{2}debt-service:\s*$", line):
            service_start = index
            break

    if service_start is None:
        print("ERROR: debt-service was not found in docker-compose.yml", file=sys.stderr)
        return 3

    for index in range(service_start + 1, len(lines)):
        line = lines[index]
        if line.strip() and indent_width(line) == 2 and line.rstrip().endswith(":"):
            service_end = index
            break
    if service_end is None:
        service_end = len(lines)

    block = lines[service_start:service_end]
    updated: list[str] = [block[0], "    expose:", '      - "8082"']
    index = 1
    while index < len(block):
        line = block[index]
        stripped = line.strip()
        current_indent = indent_width(line)

        if current_indent == 4 and stripped.startswith("container_name:"):
            index += 1
            continue

        if current_indent == 4 and stripped in {"ports:", "expose:"}:
            index += 1
            while index < len(block):
                next_line = block[index]
                if next_line.strip() and indent_width(next_line) <= 4:
                    break
                index += 1
            continue

        updated.append(line)
        index += 1

    output = lines[:service_start] + updated + lines[service_end:]
    target.write_text("\n".join(output) + "\n", encoding="utf-8")
    print(f"Created {target}")
    print("debt-service container_name and published host port were removed in the generated file.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
