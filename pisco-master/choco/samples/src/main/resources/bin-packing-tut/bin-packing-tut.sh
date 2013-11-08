#! /bin/bash
CHOCO_JAR="../../../../../target/choco-2.1.1-SNAPSHOT.jar"
CMD_CLASS="samples.tutorials.to_sort.packing.BinPackingCmd"
#java -ea  -cp $CHOCO_JAR  $CMD_CLASS --properties bp-chart.properties -v SEARCH -f instances/N1C3W1_A.BPP  
#echo -e "\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n"
java -cp $CHOCO_JAR  $CMD_CLASS --light -time 60 --export bp-tut-light.odb -f instances/
echo -e "\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n"
java -cp $CHOCO_JAR  $CMD_CLASS --properties bp-cancel-heuristics.properties --export bp-tut-heavy-noH.odb -f instances/
echo -e "\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n"
java -cp $CHOCO_JAR  $CMD_CLASS -time 60 --export bp-tut-heavy.odb -f instances/
