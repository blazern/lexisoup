#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import subprocess
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Optional


@dataclass(frozen=True)
class VersionInfo:
    version_code: int
    version_name: str


def run_capture(cmd: list[str], *, cwd: Optional[Path] = None) -> str:
    """Run a command and return stdout (raises on non-zero exit)."""
    proc = subprocess.run(
        cmd,
        cwd=str(cwd) if cwd else None,
        capture_output=True,
        text=True,
        check=False,
    )
    if proc.returncode != 0:
        stderr = (proc.stderr or "").strip()
        raise SystemExit(f"Command failed ({proc.returncode}): {' '.join(cmd)}\n{stderr}")
    return (proc.stdout or "").strip()


def git_short_sha(*, cwd: Optional[Path] = None) -> str:
    return run_capture(["git", "rev-parse", "--short", "HEAD"], cwd=cwd)


def compute_version_code(ts_local: datetime) -> int:
    # year(2) + dayOfYear(3) + HHmm(4) => 9 digits
    year_2 = f"{ts_local.year % 100:02d}"
    day_3 = f"{ts_local.timetuple().tm_yday:03d}"
    hhmm = ts_local.strftime("%H%M")
    return int(f"{year_2}{day_3}{hhmm}")


def compute_version_name(ts_local: datetime, sha: str) -> str:
    ts_str = ts_local.strftime("%Y-%m-%d-%H:%M")
    return f"{ts_str} {sha}"


def parse_epoch_seconds(raw: str) -> datetime:
    """
    Parse a numeric unix timestamp (seconds).
    Interpreted as UTC epoch seconds, then converted to local time and made naive.
    """
    s = raw.strip()
    if not s:
        raise SystemExit("--timestamp must be a number (unix seconds)")

    try:
        # allow "1700000000" or "1700000000.123"
        epoch = float(s)
    except ValueError as e:
        raise SystemExit("--timestamp must be a number (unix seconds)") from e

    # Convert epoch -> local time
    return datetime.fromtimestamp(epoch)


def main() -> None:
    parser = argparse.ArgumentParser(
        description=(
            "Generate version_code + version_name."
        ),
    )
    parser.add_argument(
        "--timestamp",
        default=None,
        help="Optional unix timestamp in seconds (a number). Default: current time.",
    )
    parser.add_argument(
        "--repo-dir",
        type=Path,
        default=Path.cwd(),
        help="Repo directory to run git in (default: current directory).",
    )
    parser.add_argument(
        "--sha",
        default=None,
        help="Optional git short SHA override (default: computed via git).",
    )
    args = parser.parse_args()

    ts_local = parse_epoch_seconds(args.timestamp) if args.timestamp is not None else datetime.now()
    sha = args.sha if args.sha else git_short_sha(cwd=args.repo_dir)

    info = VersionInfo(
        version_code=compute_version_code(ts_local),
        version_name=compute_version_name(ts_local, sha),
    )

    print(json.dumps({"version_code": info.version_code, "version_name": info.version_name}, indent=4))


if __name__ == "__main__":
    main()
