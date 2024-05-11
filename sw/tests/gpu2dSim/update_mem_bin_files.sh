#!/bin/bash
for file in ../../../hw/gen/*.bin; do
	rm $(basename $file)
	ln -s $file
done
