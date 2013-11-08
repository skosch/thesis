with open("bin/outputs/bp50-04.csv.out") as f:
    log = f.readlines()

time = 0.0
bounds = []
justprintedtime = False
for l in log:
    l = l.strip()
    if l and l[0] == "*":
        l = l[5:]
    if justprintedtime and l[-1] == "%":
        bounds.append([time, int(float((l[32:32 + 9].strip()))),
                       float(l[46:46 + 9].strip())])
        justprintedtime = False
        continue
    if not l or not(l[-1] == "\%" or "Elapsed time" in l):
        justprintedtime = False
        continue
    if "Elapsed time" in l:
        time = float(l[15:l.find("sec.")].strip())
        justprintedtime = True
        continue


print bounds
