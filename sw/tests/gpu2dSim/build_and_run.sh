#!/bin/bash
make verilate && make -j18
#numactl -m 0 -C 0,1,2,3 -m 1 -C 4,5,6,7 -- make verilate 
#numactl --physcpubind=+0-4,8-12 -- 
#./build.sh
#make verilate 
retval=$?
if (( $retval == 0 )); then
	#make -j8
	##./gpu2dSim
	#numactl -m 0 -C 0,1,2,3,4,5,6,7 -- ./gpu2dSim
	./gpu2dSim
	#numactl -m 2 -C 0,1,2,3 -- ./gpu2dSim
	#numactl -m 0 -C 0,1,2,3 -- ./gpu2dSim
	#numactl -m 0 -C 0,2,4,6 -- ./gpu2dSim
	#numactl -m 0 -C 0,1,2,3 -m 1 -C 4,5,6,7 -- ./gpu2dSim
fi
