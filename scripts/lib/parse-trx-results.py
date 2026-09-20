#!/usr/bin/env python3
"""Agrega resultados de arquivos .trx (VSTest). Modo --check falha se failed>0 ou sem TRX."""
from __future__ import annotations

import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

NS = {"t": "http://microsoft.com/schemas/VisualStudio/TeamTest/2010"}


def parse_trx(path: Path) -> dict:
    root = ET.parse(path).getroot()
    counters = root.find(".//t:ResultSummary/t:Counters", NS)
    if counters is None:
        counters = root.find(".//{*}Counters")
    if counters is None:
        return {
            "file": str(path),
            "error": "Counters not found",
            "passed": 0,
            "failed": 1,
            "skipped": 0,
            "total": 1,
        }

    def attr(name: str) -> int:
        val = counters.attrib.get(name)
        if val is None:
            val = counters.attrib.get(name.capitalize(), "0")
        return int(val or 0)

    passed = attr("passed")
    failed = attr("failed")
    total = attr("total")
    executed = attr("executed")
    if total == 0:
        total = max(executed, passed + failed)
    skipped = max(0, total - passed - failed) if total else attr("notExecuted")
    return {
        "file": str(path),
        "passed": passed,
        "failed": failed,
        "skipped": skipped,
        "total": total,
    }


def aggregate(results_dir: Path) -> dict:
    files = sorted(results_dir.rglob("*.trx"))
    per_file = [parse_trx(f) for f in files]
    return {
        "trx_files": len(files),
        "passed": sum(x.get("passed", 0) for x in per_file),
        "failed": sum(x.get("failed", 0) for x in per_file),
        "skipped": sum(x.get("skipped", 0) for x in per_file),
        "total": sum(x.get("total", 0) for x in per_file),
        "projects": per_file,
    }


def write_summary(results_dir: Path, json_path: Path | None) -> dict:
    summary = aggregate(results_dir)
    line = (
        f"trx_files={summary['trx_files']} "
        f"passed={summary['passed']} failed={summary['failed']} "
        f"skipped={summary['skipped']} total={summary['total']}"
    )
    print(line)
    if json_path:
        json_path.parent.mkdir(parents=True, exist_ok=True)
        json_path.write_text(json.dumps(summary, indent=2), encoding="utf-8")
        json_path.with_suffix(".txt").write_text(line + "\n", encoding="utf-8")
    return summary


def check(results_dir: Path) -> int:
    summary = aggregate(results_dir)
    if summary["trx_files"] == 0:
        print("no TRX files found", file=sys.stderr)
        return 1
    if summary["failed"] > 0:
        print(f"failed tests: {summary['failed']}", file=sys.stderr)
        return 1
    if summary["total"] == 0:
        print("total tests is 0", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    if len(sys.argv) >= 3 and sys.argv[1] == "--check":
        raise SystemExit(check(Path(sys.argv[2])))
    if len(sys.argv) < 2:
        print("usage: parse-trx-results.py <results-dir> [summary.json]", file=sys.stderr)
        raise SystemExit(2)
    results_dir = Path(sys.argv[1])
    json_path = Path(sys.argv[2]) if len(sys.argv) >= 3 else None
    if not results_dir.is_dir():
        print(f"directory not found: {results_dir}", file=sys.stderr)
        raise SystemExit(2)
    write_summary(results_dir, json_path)
    raise SystemExit(0)
