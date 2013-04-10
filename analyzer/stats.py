import csv
import os
from subprocess import Popen, PIPE

print "Hi. This script will run the CP *or* MIP binary on several data sets" + \
" and record their performance."
print "Input model type ('cp' or 'mip' or 'bender')"

modelType = ""
maxNj = 0
timeout = 0

while(not(modelType=="cp" or modelType=="mip" or modelType=="bender" or
modelType == "nkbender")):
    modelType = raw_input('>')
print "Enter max number of jobs"
while(not(maxNj > 10 and maxNj < 100)):
    maxNj = int(raw_input('>'))
print "Enter timeout in seconds"
while(not(timeout > 2 and timeout < 2400)):
    timeout = int(raw_input('>'))

basedir = "/home/sebastian/thesis/"

#for nj in [10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 30, 40, 50]:
    #for nsample in range(1,11):
for nj in [30]:
    for nsample in range(24, 40):
        if(modelType=="cp"):
            filename = basedir + "data/data_" + str(nj) + "_" + str(nsample)
            try:
                process = Popen([basedir + "cp/bin/thesis_cp", str(nj), "n",
                filename], stdout=PIPE, stderr=PIPE)
                exit_code = os.waitpid(process.pid, 0)
                output = process.communicate()[0]
                elapsedTime = output.split("\n")[len(output.split("\n"))-2]
                print str(nj) + ", " + str(nsample) + ", " + elapsedTime
            except IOError as e:
                pass
        if(modelType=="mip"):
            filename = basedir + "data/data_" + str(nj) + "_" + str(nsample)
            try:
                process = Popen([basedir + "mip/bin/thesis_mip", str(nj), "n",
                filename], stdout=PIPE, stderr=PIPE)
                exit_code = os.waitpid(process.pid, 0)
                output = process.communicate()[0]
                elapsedTime = output.split("\n")[len(output.split("\n"))-3]
                print str(nj) + ", " + str(nsample) + ", " + elapsedTime
            except IOError as e:
                pass
        if(modelType=="bender"):
            filename = basedir + "data/data_" + str(nj) + "_" + str(nsample)
            try:
                process = Popen([basedir + "bender/bin/thesis_bender", str(nj), "n",
                filename], stdout=PIPE, stderr=PIPE)
                exit_code = os.waitpid(process.pid, 0)
                output = process.communicate()[0]
                elapsedTime = output.split("\n")[len(output.split("\n"))-3]
                print str(nj) + ", " + str(nsample) + ", " + elapsedTime
            except IOError as e:
                pass
        if(modelType=="nkbender"):
            filename = basedir + "data/data_" + str(nj) + "_" + str(nsample)
            try:
                process = Popen([basedir + "nkbender1/bin/thesis_nkbender1",
                str(nj), "0", "0", "2000", 
                filename], stdout=PIPE, stderr=PIPE)
                exit_code = os.waitpid(process.pid, 0)
                output = process.communicate()[0]
                elapsedTime = output.split("\n")[len(output.split("\n"))-2]
                avgperbatch = output.split("\n")[len(output.split("\n"))-4]
                print (str(nj) + ", " + str(nsample) + ", " + elapsedTime + ", "
                + avgperbatch)
            except IOError as e:
                pass

print "done"
