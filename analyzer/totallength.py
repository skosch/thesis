import csv

for i in range(1, 40):
  # read lines into array
  jobrows = []
  totallength = 0
  with open("/home/sebastian/thesis/data/data_30_" + str(i), "rb") as csvfile:
    for j,row in enumerate(csv.reader(csvfile, delimiter=",")):
      if j==0: continue
      jobrows.append(row)
  illegalpairs = 0 
  for e,c in enumerate(jobrows):
    for f,d in enumerate(jobrows):
      if(int(jobrows[e][0]) + int(jobrows[f][0]) > 10): illegalpairs += 1

  print i, 1.0 * illegalpairs / (30**2)

