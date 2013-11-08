import csv
import glob

for fn in glob.glob("*.txt"):
    newrows = []
    with open(fn, "r") as csvf:
        csvr = csv.reader(csvf, delimiter=" ")
        for i in range(6):
            csvr.next()
        for row in csvr:
            try:
                s, p, d = row[1], row[0], row[3]
                newrows.append([s, p, d])
            except:
                pass  # ignore #End row

    with open("data/" + fn, "w") as csvf:
        csvw = csv.writer(csvf)
        csvw.writerow(["s", "p", "d"])
        for row in newrows:
            csvw.writerow(row)

