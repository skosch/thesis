import csv
import random


for i in [50]: # range(11, 20) + range(10,100,10):
    for j in range(1, 40):
        with open( "data_" + str(i) + "_" + str(j), "rb") as csvfile:
            csvreader = csv.reader(csvfile, delimiter=",")
            avgsize = 0
            for r,row in enumerate(csvreader):
                if r==0: continue
                avgsize += int(row[0])
            print j, avgsize/50.0
        csvfile.close()

