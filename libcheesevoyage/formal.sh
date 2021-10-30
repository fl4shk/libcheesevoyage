#!/bin/bash

python3 "$1" generate -t il > toplevel.il
sby -f "$2"
