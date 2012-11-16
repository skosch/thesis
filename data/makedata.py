import csv
import random

random.seed(None)

for i in range(11, 20) + range(10,100,10):
    for j in range(1, 10):
        with open( "data_" + str(i) + "_" + str(j), "wb") as csvfile:
            csvwriter = csv.writer(csvfile, delimiter=",")
            csvwriter.writerow(['s_j', 'p_j', 'd_j'])
            for job in range(i):
                csvwriter.writerow([random.randrange(1,20), random.randrange(1,20), random.randrange(1,5*i)])
        csvfile.close()

