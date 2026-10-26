#!/usr/bin/env python3
"""Show which TV audio outputs are playing right now and whether the leveler sits on them."""
import re
import subprocess

dump = subprocess.run(["adb", "shell", "dumpsys", "media.audio_flinger"],
                      capture_output=True, text=True).stdout

blocks = re.split(r"\n(?=Output thread )", dump)
for block in blocks:
    head = re.match(r"Output thread \S+, name (\S+), tid \d+, type \d+ \((\w+)\)", block)
    if not head:
        continue
    name, kind = head.groups()
    active = re.search(r"(\d+) Tracks of which (\d+) are active", block)
    fmt = re.search(r"HAL format: \S+ \((\S+)\)", block)
    flags = re.search(r"AudioStreamOut: \S+ flags \S+ \((\S+)\)", block)
    standby = re.search(r"Standby: (\w+)", block)
    leveler = "Dynamics Processing" in block
    print(f"{name:12} {kind:8} {flags.group(1) if flags else '?':42} "
          f"{fmt.group(1).replace('AUDIO_FORMAT_', '') if fmt else '?':10} "
          f"playing={'no' if standby and standby.group(1) == 'yes' else 'YES':4} "
          f"leveler={'YES' if leveler else 'no'}")
