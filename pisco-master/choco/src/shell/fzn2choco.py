#!/usr/bin/python
# CONSTANTS
import sys, subprocess, shlex, time, threading, os, signal, re, logging, tempfile
from threading import Thread
from os.path import join

## file name containing list of command to run
listfilename = './runner.list'


# ENVIRONMENT VARIABLES

MINIZINC_DIR = '/Users/cprudhom/Documents/Projects/_Librairies/minizinc-1.2.2'

MY_DIR = './../../'
CHOCO_LIB = join(MY_DIR,'choco-parsers/src/main/resources/std_lib')

CHOCO_HOME = 'target/choco-2.1.3-SNAPSHOT'
CHOCO_SOLVER = join(CHOCO_HOME, 'choco-solver-2.1.3-SNAPSHOT.jar')
CHOCO_CLI = join(CHOCO_HOME, 'extra', 'choco-cli-2.1.3-SNAPSHOT.jar')
CHOCO_DB = join(CHOCO_HOME, 'extra', 'choco-db-2.1.3-SNAPSHOT.jar')
CHOCO_PARSERS = join(CHOCO_HOME, 'extra', 'choco-parsers-2.1.3-SNAPSHOT.jar')

JAVA_OPTS = CMD = '-Xmx256m -Xms256m -XX:+AggressiveOpts'

MZN_FILE = ''
DZN_FILE = ''

## time limit for a process
TIMELIMIT = 180 #in seconds
## number threads used
THREAD = 1


#####################################################################
################### FUNCTIONS #######################################
#####################################################################
def readParameters(paramlist):
    global MZN_FILE
    global DZN_FILE
    global TIMELIMIT
    if len(paramlist) > 0:
        if paramlist[0] == "-f": # file name
            MZN_FILE = paramlist[1]
        elif paramlist[0] == "-d":
            DZN_FILE = paramlist[1] # data file
        elif paramlist[0] == "-tl":
            TIMELIMIT = int(paramlist[1]) # process time limit
        readParameters(paramlist[2:])

def buildLog(name, ext, level):
    hdlr = logging.FileHandler('./' + name + ext)
    formatter = logging.Formatter('%(message)s')
    hdlr.setFormatter(formatter)
    logger = logging.getLogger(name)
    logger.addHandler(hdlr)
    logger.setLevel(level)
    return logger

def kill( process ):
    if process.poll() is None:
        os.kill(process.pid, signal.SIGKILL)

def limit( process, cutoff ):
    t = threading.Timer(cutoff, kill, [process])
    t.start()
    return t


class runit(Thread):
    def __init__ (self, args):
        Thread.__init__(self)
        self.args = args
        self.time = 0

    def run(self):
        process = subprocess.Popen(self.args, bufsize=0, stderr=subprocess.PIPE)
        start = time.time()
        clock = limit(process, TIMELIMIT)
        process.wait()
        end = time.time()
        output, error = process.communicate()
        clock.cancel()
        self.time = end - start
        print ("..."+str(current.time)+'s')
        line = ''
        if len(error) > 0:
            print("error - see log file")
            err.error(self.args)
            for char in error:
                if char == '\n':
                    err.error(line)
                    line = ''
                else:
                    line += char
            err.error('\n')
            #process.stdout.close()
        process.stderr.close()

#####################################################################
################### FUNCTIONS #######################################
#####################################################################
err = buildLog('fzn2choco', '.log', logging.ERROR)

## FIRST, READ PARAMETERS
readParameters(sys.argv[1:])
if not MZN_FILE:
    print ("ERR>> \"-f mzn_file\" is required")
    err.error("ERR>> \"-f mzn_file\" is required")
    sys.exit(0)

EXTENSION = MZN_FILE[len(MZN_FILE) - 3:].lower()
FZN_FILE=None
OUTPUT=None

if EXTENSION == 'mzn':
    ## PARSE THE MINIZINC FILE IN FLATZINC FILE USING CHOCO LIBRARIES
    OUTPUT = tempfile.NamedTemporaryFile(suffix='.fzn', prefix='tmp', delete=False)
    #print OUTPUT.name

    COMMAND = join(MINIZINC_DIR, 'bin', 'mzn2fzn')
    COMMAND+=' --stdlib-dir '+ CHOCO_LIB
    COMMAND+=' -G choco_std'
    COMMAND += ' ' + MZN_FILE
    COMMAND += ' -o ' + OUTPUT.name
    if DZN_FILE != '':
        COMMAND += ' -d ' + DZN_FILE

    print ('PARSE MZN FILE'),
    ARGS = shlex.split(COMMAND)
    current = runit(ARGS)
    current.start()
    current.join()
    FZN_FILE=OUTPUT.name

elif EXTENSION == 'fzn':
    FZN_FILE=MZN_FILE

else:
    print ("ERR>> Unexpected extension")
    err.error("ERR>> Unexpected extensio")
    sys.exit(0)

COMMAND = 'java -cp .:' + CHOCO_SOLVER + ':' + CHOCO_PARSERS + ':' + CHOCO_CLI + ':' + CHOCO_DB + ' cli.FcspCmd -f ' + FZN_FILE
print ('SOLVE PROBLEM USING CHOCO')
ARGS = shlex.split(COMMAND)
current = runit(ARGS)
current.start()
current.join()

if OUTPUT is not None:
    os.unlink(OUTPUT.name)




