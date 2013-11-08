import csv

for i in range(1, 41):
    # read lines into array
    jobrows = []
    nj = 50
    totallength = 0
    with open("/home/sebastian/thesis/daste/data/bp" + str(nj) + "-" +
              str(i).zfill(2) + ".csv", "rb") as csvfile:
        for j, row in enumerate(csv.reader(csvfile, delimiter=",")):
            if j == 0:
                continue
            jobrows.append(row)
    illegalpairs = 0.0
    illegaltriples = 0.0
    totalheight = 0.0
    for e, c in enumerate(jobrows):
        totalheight += int(jobrows[e][0])
        for f, d in enumerate(jobrows):
            if(e < f and int(jobrows[e][0]) + int(jobrows[f][0]) > 10):
                illegalpairs += 1
                continue
            for g, h in enumerate(jobrows):
                if(g > f and int(jobrows[e][0]) + int(jobrows[f][0]) + int(jobrows[g][0]) > 10):
                   illegaltriples += 1

    print 1.0 * (illegalpairs + illegaltriples)

