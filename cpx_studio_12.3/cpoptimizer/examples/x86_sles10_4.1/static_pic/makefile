CCC = g++
CONCERTDIR = ../../../../concert
CPLEXDIR = ../../../../cplex
CFLAGS = -DIL_STD -O -DNDEBUG -I../../../include -I$(CONCERTDIR)/include -fPIC -fstrict-aliasing -pedantic -Wall -fexceptions -Wno-long-long -ffloat-store -m32 -march=i686 -DILOUSEMT -D_REENTRANT -DILM_REENTRANT
LDFLAGS = -L../../../lib/x86_sles10_4.1/static_pic -lcp -L$(CPLEXDIR)/lib/x86_sles10_4.1/static_pic -lcplex -L$(CONCERTDIR)/lib/x86_sles10_4.1/static_pic -lconcert  -lpthread 
JAVA = $(JAVA_HOME)/bin/java
JAVAC = $(JAVA_HOME)/bin/javac
JAVAFLAGS = 
CPCLASSPATH = ../../../lib/ILOG.CP.jar
LIBRARYPATH = ../../../bin/x86_sles10_4.1:$(CPLEXDIR)/bin/x86_sles10_4.1
CPPEXDIR = ../../../examples/src/cpp
JAVAEXDIR = ../../../examples/src/java

all: cpp java

cpp: sched_flowshop steelmill sched_calendar color_ex3 ppp facility plantlocation truckfleet sched_learningeffect sports smooth sched_goalprog schedsearch_settimes sched_time teambuilding facility_ex3 sched_setup searchphases sched_state schedsearch_greedyandrelax sched_jobshopflex sched_square sched_intro sched_sequence color_ex4 sched_tcost schedsearch_permutation sched_jobshop schedsearch_optionalsettimes sched_rcpsp schedsearch_sequencing intro steelmill_ex3 sched_optional sched_openshop color facility_ex4 sched_pflowshop sched_cumul talent sched_rcpspmm networkcfg

java: SchedJobShop.class Facility.class SchedSetup.class SchedState.class SchedSquare.class SchedRCPSP.class SchedCalendar.class Color.class Ppp.class SchedSequence.class SchedTime.class Sports.class Steelmill.class Truckfleet.class SchedJobShopFlex.class SchedLearningEffect.class SchedTCost.class SchedGoalProg.class SchedPFlowShop.class SchedCumul.class Teambuilding.class PlantLocation.class Talent.class SchedFlowShop.class SchedOptional.class SchedOpenShop.class SchedIntro.class SearchPhases.class SchedRCPSPMM.class

execute: execute_cpp execute_java

clean:
	rm -f sched_flowshop steelmill sched_calendar color_ex3 ppp facility plantlocation truckfleet sched_learningeffect sports smooth sched_goalprog schedsearch_settimes sched_time teambuilding facility_ex3 sched_setup searchphases sched_state schedsearch_greedyandrelax sched_jobshopflex sched_square sched_intro sched_sequence color_ex4 sched_tcost schedsearch_permutation sched_jobshop schedsearch_optionalsettimes sched_rcpsp schedsearch_sequencing intro steelmill_ex3 sched_optional sched_openshop color facility_ex4 sched_pflowshop sched_cumul talent sched_rcpspmm networkcfg *.class

FORCE:

$(JAVAC):
	@echo "####################################"
	@echo "Error: cannot find the javac command"
	@echo "Please ensure that JAVA_HOME is set correctly"
	@echo "Currently, JAVA_HOME = $(JAVA_HOME)"
	@exit 1
sched_flowshop:
	$(CCC) -o sched_flowshop $(CFLAGS) $(CPPEXDIR)/sched_flowshop.cpp $(LDFLAGS)
sched_flowshop.run: sched_flowshop FORCE
	./sched_flowshop
steelmill:
	$(CCC) -o steelmill $(CFLAGS) $(CPPEXDIR)/steelmill.cpp $(LDFLAGS)
steelmill.run: steelmill FORCE
	./steelmill
sched_calendar:
	$(CCC) -o sched_calendar $(CFLAGS) $(CPPEXDIR)/sched_calendar.cpp $(LDFLAGS)
sched_calendar.run: sched_calendar FORCE
	./sched_calendar
color_ex3:
	$(CCC) -o color_ex3 $(CFLAGS) $(CPPEXDIR)/color_ex3.cpp $(LDFLAGS)
color_ex3.run: color_ex3 FORCE
	./color_ex3
ppp:
	$(CCC) -o ppp $(CFLAGS) $(CPPEXDIR)/ppp.cpp $(LDFLAGS)
ppp.run: ppp FORCE
	./ppp
facility:
	$(CCC) -o facility $(CFLAGS) $(CPPEXDIR)/facility.cpp $(LDFLAGS)
facility.run: facility FORCE
	./facility
plantlocation:
	$(CCC) -o plantlocation $(CFLAGS) $(CPPEXDIR)/plantlocation.cpp $(LDFLAGS)
plantlocation.run: plantlocation FORCE
	./plantlocation
truckfleet:
	$(CCC) -o truckfleet $(CFLAGS) $(CPPEXDIR)/truckfleet.cpp $(LDFLAGS)
truckfleet.run: truckfleet FORCE
	./truckfleet
sched_learningeffect:
	$(CCC) -o sched_learningeffect $(CFLAGS) $(CPPEXDIR)/sched_learningeffect.cpp $(LDFLAGS)
sched_learningeffect.run: sched_learningeffect FORCE
	./sched_learningeffect
sports:
	$(CCC) -o sports $(CFLAGS) $(CPPEXDIR)/sports.cpp $(LDFLAGS)
sports.run: sports FORCE
	./sports
smooth:
	$(CCC) -o smooth $(CFLAGS) $(CPPEXDIR)/smooth.cpp $(LDFLAGS)
smooth.run: smooth FORCE
	./smooth
sched_goalprog:
	$(CCC) -o sched_goalprog $(CFLAGS) $(CPPEXDIR)/sched_goalprog.cpp $(LDFLAGS)
sched_goalprog.run: sched_goalprog FORCE
	./sched_goalprog
schedsearch_settimes:
	$(CCC) -o schedsearch_settimes $(CFLAGS) $(CPPEXDIR)/schedsearch_settimes.cpp $(LDFLAGS)
schedsearch_settimes.run: schedsearch_settimes FORCE
	./schedsearch_settimes
sched_time:
	$(CCC) -o sched_time $(CFLAGS) $(CPPEXDIR)/sched_time.cpp $(LDFLAGS)
sched_time.run: sched_time FORCE
	./sched_time
teambuilding:
	$(CCC) -o teambuilding $(CFLAGS) $(CPPEXDIR)/teambuilding.cpp $(LDFLAGS)
teambuilding.run: teambuilding FORCE
	./teambuilding
facility_ex3:
	$(CCC) -o facility_ex3 $(CFLAGS) $(CPPEXDIR)/facility_ex3.cpp $(LDFLAGS)
facility_ex3.run: facility_ex3 FORCE
	./facility_ex3
sched_setup:
	$(CCC) -o sched_setup $(CFLAGS) $(CPPEXDIR)/sched_setup.cpp $(LDFLAGS)
sched_setup.run: sched_setup FORCE
	./sched_setup
searchphases:
	$(CCC) -o searchphases $(CFLAGS) $(CPPEXDIR)/searchphases.cpp $(LDFLAGS)
searchphases.run: searchphases FORCE
	./searchphases
sched_state:
	$(CCC) -o sched_state $(CFLAGS) $(CPPEXDIR)/sched_state.cpp $(LDFLAGS)
sched_state.run: sched_state FORCE
	./sched_state
schedsearch_greedyandrelax:
	$(CCC) -o schedsearch_greedyandrelax $(CFLAGS) $(CPPEXDIR)/schedsearch_greedyandrelax.cpp $(LDFLAGS)
schedsearch_greedyandrelax.run: schedsearch_greedyandrelax FORCE
	./schedsearch_greedyandrelax
sched_jobshopflex:
	$(CCC) -o sched_jobshopflex $(CFLAGS) $(CPPEXDIR)/sched_jobshopflex.cpp $(LDFLAGS)
sched_jobshopflex.run: sched_jobshopflex FORCE
	./sched_jobshopflex
sched_square:
	$(CCC) -o sched_square $(CFLAGS) $(CPPEXDIR)/sched_square.cpp $(LDFLAGS)
sched_square.run: sched_square FORCE
	./sched_square
sched_intro:
	$(CCC) -o sched_intro $(CFLAGS) $(CPPEXDIR)/sched_intro.cpp $(LDFLAGS)
sched_intro.run: sched_intro FORCE
	./sched_intro
sched_sequence:
	$(CCC) -o sched_sequence $(CFLAGS) $(CPPEXDIR)/sched_sequence.cpp $(LDFLAGS)
sched_sequence.run: sched_sequence FORCE
	./sched_sequence
color_ex4:
	$(CCC) -o color_ex4 $(CFLAGS) $(CPPEXDIR)/color_ex4.cpp $(LDFLAGS)
color_ex4.run: color_ex4 FORCE
	./color_ex4
sched_tcost:
	$(CCC) -o sched_tcost $(CFLAGS) $(CPPEXDIR)/sched_tcost.cpp $(LDFLAGS)
sched_tcost.run: sched_tcost FORCE
	./sched_tcost
schedsearch_permutation:
	$(CCC) -o schedsearch_permutation $(CFLAGS) $(CPPEXDIR)/schedsearch_permutation.cpp $(LDFLAGS)
schedsearch_permutation.run: schedsearch_permutation FORCE
	./schedsearch_permutation
sched_jobshop:
	$(CCC) -o sched_jobshop $(CFLAGS) $(CPPEXDIR)/sched_jobshop.cpp $(LDFLAGS)
sched_jobshop.run: sched_jobshop FORCE
	./sched_jobshop
schedsearch_optionalsettimes:
	$(CCC) -o schedsearch_optionalsettimes $(CFLAGS) $(CPPEXDIR)/schedsearch_optionalsettimes.cpp $(LDFLAGS)
schedsearch_optionalsettimes.run: schedsearch_optionalsettimes FORCE
	./schedsearch_optionalsettimes
sched_rcpsp:
	$(CCC) -o sched_rcpsp $(CFLAGS) $(CPPEXDIR)/sched_rcpsp.cpp $(LDFLAGS)
sched_rcpsp.run: sched_rcpsp FORCE
	./sched_rcpsp
schedsearch_sequencing:
	$(CCC) -o schedsearch_sequencing $(CFLAGS) $(CPPEXDIR)/schedsearch_sequencing.cpp $(LDFLAGS)
schedsearch_sequencing.run: schedsearch_sequencing FORCE
	./schedsearch_sequencing
intro:
	$(CCC) -o intro $(CFLAGS) $(CPPEXDIR)/intro.cpp $(LDFLAGS)
intro.run: intro FORCE
	./intro
steelmill_ex3:
	$(CCC) -o steelmill_ex3 $(CFLAGS) $(CPPEXDIR)/steelmill_ex3.cpp $(LDFLAGS)
steelmill_ex3.run: steelmill_ex3 FORCE
	./steelmill_ex3
sched_optional:
	$(CCC) -o sched_optional $(CFLAGS) $(CPPEXDIR)/sched_optional.cpp $(LDFLAGS)
sched_optional.run: sched_optional FORCE
	./sched_optional
sched_openshop:
	$(CCC) -o sched_openshop $(CFLAGS) $(CPPEXDIR)/sched_openshop.cpp $(LDFLAGS)
sched_openshop.run: sched_openshop FORCE
	./sched_openshop
color:
	$(CCC) -o color $(CFLAGS) $(CPPEXDIR)/color.cpp $(LDFLAGS)
color.run: color FORCE
	./color
facility_ex4:
	$(CCC) -o facility_ex4 $(CFLAGS) $(CPPEXDIR)/facility_ex4.cpp $(LDFLAGS)
facility_ex4.run: facility_ex4 FORCE
	./facility_ex4
sched_pflowshop:
	$(CCC) -o sched_pflowshop $(CFLAGS) $(CPPEXDIR)/sched_pflowshop.cpp $(LDFLAGS)
sched_pflowshop.run: sched_pflowshop FORCE
	./sched_pflowshop
sched_cumul:
	$(CCC) -o sched_cumul $(CFLAGS) $(CPPEXDIR)/sched_cumul.cpp $(LDFLAGS)
sched_cumul.run: sched_cumul FORCE
	./sched_cumul
talent:
	$(CCC) -o talent $(CFLAGS) $(CPPEXDIR)/talent.cpp $(LDFLAGS)
talent.run: talent FORCE
	./talent
sched_rcpspmm:
	$(CCC) -o sched_rcpspmm $(CFLAGS) $(CPPEXDIR)/sched_rcpspmm.cpp $(LDFLAGS)
sched_rcpspmm.run: sched_rcpspmm FORCE
	./sched_rcpspmm
networkcfg:
	$(CCC) -o networkcfg $(CFLAGS) $(CPPEXDIR)/networkcfg.cpp $(LDFLAGS)
networkcfg.run: networkcfg FORCE
	./networkcfg
execute_cpp: sched_flowshop.run steelmill.run sched_calendar.run color_ex3.run ppp.run facility.run plantlocation.run truckfleet.run sched_learningeffect.run sports.run smooth.run sched_goalprog.run schedsearch_settimes.run sched_time.run teambuilding.run facility_ex3.run sched_setup.run searchphases.run sched_state.run schedsearch_greedyandrelax.run sched_jobshopflex.run sched_square.run sched_intro.run sched_sequence.run color_ex4.run sched_tcost.run schedsearch_permutation.run sched_jobshop.run schedsearch_optionalsettimes.run sched_rcpsp.run schedsearch_sequencing.run intro.run steelmill_ex3.run sched_optional.run sched_openshop.run color.run facility_ex4.run sched_pflowshop.run sched_cumul.run talent.run sched_rcpspmm.run networkcfg.run
SchedJobShop.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedJobShop.java
SchedJobShop.run: SchedJobShop.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedJobShop
Facility.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/Facility.java
Facility.run: Facility.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. Facility
SchedSetup.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedSetup.java
SchedSetup.run: SchedSetup.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedSetup
SchedState.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedState.java
SchedState.run: SchedState.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedState
SchedSquare.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedSquare.java
SchedSquare.run: SchedSquare.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedSquare
SchedRCPSP.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedRCPSP.java
SchedRCPSP.run: SchedRCPSP.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedRCPSP
SchedCalendar.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedCalendar.java
SchedCalendar.run: SchedCalendar.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedCalendar
Color.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/Color.java
Color.run: Color.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. Color
Ppp.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/Ppp.java
Ppp.run: Ppp.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. Ppp
SchedSequence.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedSequence.java
SchedSequence.run: SchedSequence.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedSequence
SchedTime.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedTime.java
SchedTime.run: SchedTime.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedTime
Sports.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/Sports.java
Sports.run: Sports.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. Sports
Steelmill.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/Steelmill.java
Steelmill.run: Steelmill.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. Steelmill
Truckfleet.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/Truckfleet.java
Truckfleet.run: Truckfleet.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. Truckfleet
SchedJobShopFlex.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedJobShopFlex.java
SchedJobShopFlex.run: SchedJobShopFlex.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedJobShopFlex
SchedLearningEffect.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedLearningEffect.java
SchedLearningEffect.run: SchedLearningEffect.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedLearningEffect
SchedTCost.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedTCost.java
SchedTCost.run: SchedTCost.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedTCost
SchedGoalProg.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedGoalProg.java
SchedGoalProg.run: SchedGoalProg.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedGoalProg
SchedPFlowShop.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedPFlowShop.java
SchedPFlowShop.run: SchedPFlowShop.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedPFlowShop
SchedCumul.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedCumul.java
SchedCumul.run: SchedCumul.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedCumul
Teambuilding.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/Teambuilding.java
Teambuilding.run: Teambuilding.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. Teambuilding
PlantLocation.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/PlantLocation.java
PlantLocation.run: PlantLocation.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. PlantLocation
Talent.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/Talent.java
Talent.run: Talent.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. Talent
SchedFlowShop.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedFlowShop.java
SchedFlowShop.run: SchedFlowShop.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedFlowShop
SchedOptional.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedOptional.java
SchedOptional.run: SchedOptional.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedOptional
SchedOpenShop.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedOpenShop.java
SchedOpenShop.run: SchedOpenShop.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedOpenShop
SchedIntro.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedIntro.java
SchedIntro.run: SchedIntro.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedIntro
SearchPhases.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SearchPhases.java
SearchPhases.run: SearchPhases.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SearchPhases
SchedRCPSPMM.class: $(JAVAC)
	$(JAVAC) -d . -classpath $(CPCLASSPATH) $(JAVAEXDIR)/SchedRCPSPMM.java
SchedRCPSPMM.run: SchedRCPSPMM.class FORCE
	LD_LIBRARY_PATH=$(LIBRARYPATH):$$LD_LIBRARY_PATH $(JAVA) $(JAVAFLAGS) -classpath $(CPCLASSPATH):. SchedRCPSPMM
execute_java: SchedJobShop.run Facility.run SchedSetup.run SchedState.run SchedSquare.run SchedRCPSP.run SchedCalendar.run Color.run Ppp.run SchedSequence.run SchedTime.run Sports.run Steelmill.run Truckfleet.run SchedJobShopFlex.run SchedLearningEffect.run SchedTCost.run SchedGoalProg.run SchedPFlowShop.run SchedCumul.run Teambuilding.run PlantLocation.run Talent.run SchedFlowShop.run SchedOptional.run SchedOpenShop.run SchedIntro.run SearchPhases.run SchedRCPSPMM.run
