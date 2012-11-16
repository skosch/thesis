import csv
import operator
import itertools
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle
from matplotlib.lines import Line2D

# read lines into array
jobrows = []
with open("../cp/bin/output/output_10", "rb") as csvfile:
  for row in csv.reader(csvfile, delimiter=","):
    jobrows.append(row)

jobrows.pop(0) # remove header line

# sort array by batch
jobs = sorted(jobrows, key=operator.itemgetter(4)) 

# loop through batches, sort by p_j
batches = []
for key, group in itertools.groupby(jobs, operator.itemgetter(4)):
  batches.append(sorted(list(group), key=operator.itemgetter(2), reverse=True))

print "Batches:"
print batches

cur_x = 0
cur_y = 0

# now go through batches and draw things.
# first, create a figure

fig = plt.figure(figsize = [4,3])
ax = fig.add_subplot(111)

# then, create the axes
capacity = 20

for batch in batches:
  for job in batch:
    print "Job " + job[0] + ": s=" + job[1] + " p=" + job[2]
    rect = Rectangle( [cur_x, cur_y], int(job[2]), int(job[1]),
    facecolor="white", hatch="/", edgecolor="black")
    ax.add_patch(rect)
    cur_y += int(job[1])
  Pk = int(batch[0][2])
  Dk = min(map(int,map(operator.itemgetter(3),batch))) # min dj out of all the jobs
  print "Shortest due date: " + str(Dk)
  print "Batch done. Longest job: " + str(Pk)

  # the following line is added after each batch to show its due date
  dueline = Line2D([Dk, cur_x+Pk], [0, capacity], color="darkred")
  ax.add_line(dueline)

  # the following line is added to show the capacity
  capline = Line2D([cur_x, cur_x+Pk], [capacity, capacity], linestyle="--",
  color="black")
  ax.add_line(capline)

  # the following line is added to show the end of the batch
  endline = Line2D([cur_x+Pk, cur_x+Pk], [0, capacity], linestyle="--",
  color="black")
  ax.add_line(endline)

  cur_y = 0
  cur_x = cur_x + Pk

ax.autoscale_view(True, True, True)
plt.show()

