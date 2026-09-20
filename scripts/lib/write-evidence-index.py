#!/usr/bin/env python3
"""Gera índice markdown de evidências backend + Android."""
from __future__ import annotations

import argparse
import json
from pathlib import Path


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument("--backend-summary", type=Path)
    p.add_argument("--android-dir", type=Path)
    p.add_argument("--out", type=Path, required=True)
    p.add_argument("--instrument-exit", type=int, default=0)
    args = p.parse_args()

    lines = ["# Índice de evidências Sprint 2", ""]

    if args.backend_summary and args.backend_summary.is_file():
        data = json.loads(args.backend_summary.read_text(encoding="utf-8"))
        lines.append("## Backend (TRX)")
        lines.append(
            f"- passed={data.get('passed')} failed={data.get('failed')} "
            f"skipped={data.get('skipped')} total={data.get('total')} trx_files={data.get('trx_files')}"
        )
        for proj in data.get("projects", []):
            lines.append(f"  - `{Path(proj['file']).name}`: passed={proj.get('passed')} failed={proj.get('failed')}")
        lines.append("")

    lines.append("## Android (instrumentação)")
    lines.append(f"- exit_code={args.instrument_exit}")
    idx = args.android_dir / "screenshots" / "INDEX.json"
    if idx.is_file():
        aj = json.loads(idx.read_text(encoding="utf-8"))
        for case in aj.get("cases", []):
            lines.append(
                f"- **{case.get('caseId')}** / {case.get('screen')}: {case.get('status')} — `{case.get('file')}`"
            )
    else:
        lines.append("- (sem INDEX.json — jornada não executada ou pull vazio)")
    lines.append("")

    args.out.parent.mkdir(parents=True, exist_ok=True)
    args.out.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Índice: {args.out}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
