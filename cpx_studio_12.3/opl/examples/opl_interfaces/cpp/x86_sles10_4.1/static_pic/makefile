SYSTEM = x86_sles10_4.1
LIBFORMAT = static_pic

CCC = /usr/bin/g++

# System/lib-format specific compile/link flags
CSYSFLAGS  = -fPIC -DIL_STD 
LDSYSFLAGS = 

# Compilation and linking flags enabling Multi-threading
CMTFLAGS = -DILOUSEMT -D_REENTRANT
LDMTFLAGS = -lpthread

# Code optimization/debugging options
DEBUG = -O1 -DNDEBUG

# JDK environment
JDK = $(JAVA_HOME)
JDKOPTIONS = 
JDKINCLUDE = linux


OPLDIR=../../../../..
CPLEXDIR=$(OPLDIR)
CPDIR=$(OPLDIR)
ICUDIR=$(OPLDIR)

CPLEXNAME=cplex123	

DEMODIR=$(OPLDIR)/examples/opl_interfaces/cpp
DEMOSRCDIR=$(DEMODIR)/src
SUFFIX=cpp

# OPTIONS and LDOPTIONS provide a way for the user to add his own
# specific compilation and linking options.

CFLAGS = $(CSYSFLAGS) $(DEBUG) \
	-I$(OPLDIR)/include \
	-I$(CPLEXDIR)/include \
	-I$(CPDIR)/include \
	$(CMTFLAGS) \
	$(OPTIONS)  

LDFLAGS = \
	-L$(OPLDIR)/lib/$(SYSTEM)/$(LIBFORMAT) \
		-lopl -liljs -loplnl1\
	-L$(CPLEXDIR)/lib/$(SYSTEM)/$(LIBFORMAT) \
		-lilocplex -lcp -lconcert \
	-L$(CPLEXDIR)/bin/$(SYSTEM) \
		-l$(CPLEXNAME) \
		-ldbkernel -ldblnkdyn -lilog -ldl \
	-L$(ICUDIR)/bin/$(SYSTEM) \
		-licuuc -licui18n -licuio -licudata \
	$(LDSYSFLAGS) \
	$(LDMTFLAGS) \
	$(LDOPTIONS)
#---------------------------------------------------------
# FILES
#---------------------------------------------------------
TEST = carseq customdatasource cutstock cutstock_change \
          iterators mulprod mulprod_main oplrunsample steelmill \
          warehouse 

execute: $(TEST)
	./carseq
	./customdatasource
	./cutstock
	./cutstock_change
	./iterators
	./mulprod
	./mulprod_main
	./oplrunsample
	./steelmill
	./warehouse

build: $(TEST)

clean:
	/bin/rm -rf *.o
	/bin/rm -rf $(TEST)
	touch out.out
	/bin/rm -rf *.out

carseq.o: $(DEMOSRCDIR)/carseq.$(SUFFIX)
	$(CCC) $(CFLAGS) $(DEMOSRCDIR)/carseq.$(SUFFIX) -c
carseq: carseq.o
	$(CCC) $(CFLAGS) carseq.o -o carseq $(LDFLAGS)

customdatasource.o: $(DEMOSRCDIR)/customdatasource.$(SUFFIX)
	$(CCC) $(CFLAGS) $(DEMOSRCDIR)/customdatasource.$(SUFFIX) -c
customdatasource: customdatasource.o
	$(CCC) $(CFLAGS) customdatasource.o -o customdatasource $(LDFLAGS)

cutstock.o: $(DEMOSRCDIR)/cutstock.$(SUFFIX)
	$(CCC) $(CFLAGS) $(DEMOSRCDIR)/cutstock.$(SUFFIX) -c
cutstock: cutstock.o
	$(CCC) $(CFLAGS) cutstock.o -o cutstock $(LDFLAGS)

cutstock_change.o: $(DEMOSRCDIR)/cutstock_change.$(SUFFIX)
	$(CCC) $(CFLAGS) $(DEMOSRCDIR)/cutstock_change.$(SUFFIX) -c
cutstock_change: cutstock_change.o
	$(CCC) $(CFLAGS) cutstock_change.o -o cutstock_change $(LDFLAGS)

iterators.o: $(DEMOSRCDIR)/iterators.$(SUFFIX)
	$(CCC) $(CFLAGS) $(DEMOSRCDIR)/iterators.$(SUFFIX) -c
iterators: iterators.o
	$(CCC) $(CFLAGS) iterators.o -o iterators $(LDFLAGS)

mulprod.o: $(DEMOSRCDIR)/mulprod.$(SUFFIX)
	$(CCC) $(CFLAGS) $(DEMOSRCDIR)/mulprod.$(SUFFIX) -c
mulprod: mulprod.o
	$(CCC) $(CFLAGS) mulprod.o -o mulprod $(LDFLAGS)

mulprod_main.o: $(DEMOSRCDIR)/mulprod_main.$(SUFFIX)
	$(CCC) $(CFLAGS) $(DEMOSRCDIR)/mulprod_main.$(SUFFIX) -c
mulprod_main: mulprod_main.o
	$(CCC) $(CFLAGS) mulprod_main.o -o mulprod_main $(LDFLAGS)

oplrunsample.o: $(DEMOSRCDIR)/oplrunsample.$(SUFFIX)
	$(CCC) $(CFLAGS) $(DEMOSRCDIR)/oplrunsample.$(SUFFIX) -c
oplrunsample: oplrunsample.o
	$(CCC) $(CFLAGS) oplrunsample.o -o oplrunsample $(LDFLAGS)

steelmill.o: $(DEMOSRCDIR)/steelmill.$(SUFFIX)
	$(CCC) $(CFLAGS) $(DEMOSRCDIR)/steelmill.$(SUFFIX) -c
steelmill: steelmill.o
	$(CCC) $(CFLAGS) steelmill.o -o steelmill $(LDFLAGS)

warehouse.o: $(DEMOSRCDIR)/warehouse.$(SUFFIX)
	$(CCC) $(CFLAGS) $(DEMOSRCDIR)/warehouse.$(SUFFIX) -c
warehouse: warehouse.o
	$(CCC) $(CFLAGS) warehouse.o -o warehouse $(LDFLAGS)

