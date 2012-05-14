#!/bin/bash
# runSimulations = runsSimulations from inputs in file1

opath=$PATH
PATH=/bin:/usr/bin
		
java -cp IBEVAC.jar $1 $2

echo "$1 $2 run complete"|mail -s "Run Complete" vaisaghvt@gmail.com
