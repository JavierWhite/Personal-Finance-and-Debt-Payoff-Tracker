#!/usr/bin/env python3
"""Patch the existing Maven reactor for Eureka, Gateway, and load-balanced calls."""

from __future__ import annotations

import argparse
import datetime as dt
import re
import shutil
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

CLOUD_VERSION = "2025.0.3"

SERVICE_DEPENDENCIES: dict[str, list[tuple[str, str]]] = {
    "config-service": [
        ("org.springframework.cloud", "spring-cloud-starter-netflix-eureka-client"),
    ],
    "user-account-service": [
        ("org.springframework.cloud", "spring-cloud-starter-netflix-eureka-client"),
    ],
    "debt-service": [
        ("org.springframework.cloud", "spring-cloud-starter-netflix-eureka-client"),
    ],
    "savings-service": [
        ("org.springframework.cloud", "spring-cloud-starter-netflix-eureka-client"),
    ],
    "retirement-service": [
        ("org.springframework.cloud", "spring-cloud-starter-netflix-eureka-client"),
    ],
    "analytics-service": [
        ("org.springframework.cloud", "spring-cloud-starter-netflix-eureka-client"),
        ("org.springframework.cloud", "spring-cloud-starter-loadbalancer"),
        ("org.springframework.cloud", "spring-cloud-starter-circuitbreaker-resilience4j"),
        ("org.springframework.boot", "spring-boot-starter-aop"),
    ],
    "frontend-service": [
        ("org.springframework.cloud", "spring-cloud-starter-netflix-eureka-client"),
    ],
}


def dependency_xml(group_id: str, artifact_id: str, indent: str = "        ") -> str:
    return (
        f"\n{indent}<dependency>\n"
        f"{indent}    <groupId>{group_id}</groupId>\n"
        f"{indent}    <artifactId>{artifact_id}</artifactId>\n"
        f"{indent}</dependency>"
    )


def add_modules(text: str) -> str:
    required = ["eureka-server", "api-gateway"]
    if "<modules>" in text:
        additions = "".join(
            f"\n        <module>{module}</module>"
            for module in required
            if f"<module>{module}</module>" not in text
        )
        if additions:
            text = text.replace("</modules>", additions + "\n    </modules>", 1)
        return text

    module_block = (
        "\n    <modules>\n"
        "        <module>security-common</module>\n"
        "        <module>config-service</module>\n"
        "        <module>user-account-service</module>\n"
        "        <module>debt-service</module>\n"
        "        <module>savings-service</module>\n"
        "        <module>retirement-service</module>\n"
        "        <module>analytics-service</module>\n"
        "        <module>frontend-service</module>\n"
        "        <module>eureka-server</module>\n"
        "        <module>api-gateway</module>\n"
        "    </modules>\n"
    )
    return text.replace("</project>", module_block + "\n</project>", 1)


def add_cloud_property(text: str) -> str:
    property_pattern = re.compile(
        r"<spring-cloud\.version>.*?</spring-cloud\.version>", re.DOTALL
    )
    replacement = f"<spring-cloud.version>{CLOUD_VERSION}</spring-cloud.version>"
    if property_pattern.search(text):
        return property_pattern.sub(replacement, text, count=1)

    if "</properties>" in text:
        return text.replace(
            "</properties>",
            f"        <spring-cloud.version>{CLOUD_VERSION}</spring-cloud.version>\n    </properties>",
            1,
        )

    property_block = (
        "\n    <properties>\n"
        f"        <spring-cloud.version>{CLOUD_VERSION}</spring-cloud.version>\n"
        "    </properties>\n"
    )
    return text.replace("</project>", property_block + "\n</project>", 1)


def add_cloud_bom(text: str) -> str:
    if "spring-cloud-dependencies" in text:
        return text

    bom_dependency = (
        "\n            <dependency>\n"
        "                <groupId>org.springframework.cloud</groupId>\n"
        "                <artifactId>spring-cloud-dependencies</artifactId>\n"
        "                <version>${spring-cloud.version}</version>\n"
        "                <type>pom</type>\n"
        "                <scope>import</scope>\n"
        "            </dependency>"
    )

    dm_match = re.search(
        r"(<dependencyManagement>\s*<dependencies>)(.*?)(</dependencies>\s*</dependencyManagement>)",
        text,
        re.DOTALL,
    )
    if dm_match:
        start, body, end = dm_match.groups()
        replacement = start + body.rstrip() + bom_dependency + "\n        " + end
        return text[: dm_match.start()] + replacement + text[dm_match.end() :]

    block = (
        "\n    <dependencyManagement>\n"
        "        <dependencies>"
        + bom_dependency
        + "\n        </dependencies>\n"
        "    </dependencyManagement>\n"
    )

    if "<build>" in text:
        return text.replace("<build>", block + "\n    <build>", 1)
    return text.replace("</project>", block + "\n</project>", 1)


def add_dependencies(text: str, dependencies: list[tuple[str, str]]) -> str:
    missing = [dep for dep in dependencies if f"<artifactId>{dep[1]}</artifactId>" not in text]
    if not missing:
        return text

    additions = "".join(dependency_xml(group, artifact) for group, artifact in missing)

    dep_match = re.search(r"<dependencies>(.*?)</dependencies>", text, re.DOTALL)
    if dep_match:
        body = dep_match.group(1).rstrip()
        replacement = "<dependencies>" + body + additions + "\n    </dependencies>"
        return text[: dep_match.start()] + replacement + text[dep_match.end() :]

    block = "\n    <dependencies>" + additions + "\n    </dependencies>\n"
    if "<build>" in text:
        return text.replace("<build>", block + "\n    <build>", 1)
    return text.replace("</project>", block + "\n</project>", 1)


def validate_xml(path: Path) -> None:
    try:
        ET.parse(path)
    except ET.ParseError as exception:
        raise RuntimeError(f"Patched XML is invalid: {path}: {exception}") from exception


def backup_files(repo: Path, files: list[Path]) -> Path:
    timestamp = dt.datetime.now().strftime("%Y%m%d-%H%M%S")
    backup_root = repo / ".microservices-upgrade-backup" / timestamp
    for path in files:
        if not path.exists():
            continue
        destination = backup_root / path.relative_to(repo)
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(path, destination)
    return backup_root


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("repo", type=Path, help="Path to the project repository")
    args = parser.parse_args()

    repo = args.repo.expanduser().resolve()
    root_pom = repo / "pom.xml"
    if not root_pom.exists():
        print(f"ERROR: No pom.xml found in {repo}", file=sys.stderr)
        return 2

    pom_files = [root_pom]
    for module in SERVICE_DEPENDENCIES:
        path = repo / module / "pom.xml"
        if path.exists():
            pom_files.append(path)

    backup_root = backup_files(repo, pom_files + [repo / "docker-compose.yml"])

    root_text = root_pom.read_text(encoding="utf-8")
    root_text = add_modules(root_text)
    root_text = add_cloud_property(root_text)
    root_text = add_cloud_bom(root_text)
    root_pom.write_text(root_text, encoding="utf-8")
    validate_xml(root_pom)

    for module, dependencies in SERVICE_DEPENDENCIES.items():
        pom_path = repo / module / "pom.xml"
        if not pom_path.exists():
            print(f"WARNING: {module}/pom.xml not found; skipped")
            continue
        text = pom_path.read_text(encoding="utf-8")
        updated = add_dependencies(text, dependencies)
        pom_path.write_text(updated, encoding="utf-8")
        validate_xml(pom_path)
        print(f"Patched {pom_path.relative_to(repo)}")

    print(f"Patched {root_pom.relative_to(repo)}")
    print(f"Backups: {backup_root}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
