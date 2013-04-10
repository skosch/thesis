import csv
import random

random.seed(None)
capacity = 10
for i in range(11, 20) + range(10,100,10):
    for j in range(1, 40):
        with open( "data_" + str(i) + "_" + str(j), "wb") as csvfile:
            csvwriter = csv.writer(csvfile, delimiter=",")
            csvwriter.writerow(['s_j', 'p_j', 'd_j'])
            s = []
            p = []
            d = []
            for job in range(i):
                s.append(random.randrange(1, capacity))
                p.append(random.randrange(1, 99))
                d.append(random.randrange(1, 99))
                csvwriter.writerow([s[job], p[job], d[job]])
        csvfile.close()

