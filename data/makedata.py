import csv
import random

random.seed(None)

for i in range(10,100,20):
    with open( "data_" + str(i), "wb") as csvfile:
        csvwriter = csv.writer(csvfile, delimiter=",")
        csvwriter.writerow(['s_j', 'p_j', 'd_j'])
        for job in range(i):
            csvwriter.writerow([random.randrange(1,20), random.randrange(1,20), random.randrange(1,5*i)])
    csvfile.close()

