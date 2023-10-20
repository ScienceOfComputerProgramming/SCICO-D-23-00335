import sys
import os
import emoGraphLib
from pathlib import Path

def prinStats(dir):
    """ 
    Printing some stats of emotion traces.
    dir: is the directory where the trace-files are located.
    """
    countfile = 0
    minVal = { 'hope' : 0 , 
      'joy' : 0,
      'satisfaction' : 0,
      'fear' : 0,
      'distress' : 0,
      'disappointment' : 0
    }
    maxVal = { 'hope' : 0 , 
      'joy' : 0,
      'satisfaction' : 0,
      'fear' : 0,
      'distress' : 0,
      'disappointment' : 0,
      't' : 0 # time
    }
    dataset = []
    for filename in os.listdir(dir):
        if(filename.endswith(".csv")):
            countfile = countfile + 1
            file_ = Path(dir + "/" + filename)
            datax = emoGraphLib.loadCSV(file_)
            #print(filename)
            #print(datax)
            dataset.extend(datax)
    if len(dataset) == 0:
        raise Exception("No data to read")
    minVal['hope'] = dataset[0]['hope']    
    minVal['joy'] = dataset[0]['joy']    
    minVal['satisfaction'] = dataset[0]['satisfaction']    
    minVal['fear'] = dataset[0]['fear']    
    minVal['distress'] = dataset[0]['distress']    
    minVal['disappointment'] = dataset[0]['disappointment']    
    maxVal['hope'] = dataset[0]['hope']    
    maxVal['joy'] = dataset[0]['joy']    
    maxVal['satisfaction'] = dataset[0]['satisfaction']    
    maxVal['fear'] = dataset[0]['fear']    
    maxVal['distress'] = dataset[0]['distress']    
    maxVal['disappointment'] = dataset[0]['disappointment']  
    maxVal['t'] = dataset[0]['t']   
    for row in dataset:
        minVal['hope'] = min(minVal['hope'],row['hope'])   
        minVal['joy'] = min(minVal['joy'],row['joy'])   
        minVal['satisfaction'] = min(minVal['satisfaction'],row['satisfaction'])   
        minVal['fear'] = min(minVal['fear'],row['fear'])   
        minVal['distress'] = min(minVal['distress'],row['distress'])   
        minVal['disappointment'] = min(minVal['disappointment'],row['disappointment'])   
        maxVal['hope'] = max(maxVal['hope'],row['hope'])   
        maxVal['joy'] = max(maxVal['joy'],row['joy'])   
        maxVal['satisfaction'] = max(maxVal['satisfaction'],row['satisfaction'])   
        maxVal['fear'] = max(maxVal['fear'],row['fear'])   
        maxVal['distress'] = max(maxVal['distress'],row['distress'])   
        maxVal['disappointment'] = max(maxVal['disappointment'],row['disappointment'])   
        maxVal['t'] = max(maxVal['t'],row['t'])   

    print(f"*** #traces : {countfile}")   
    print(f"*** min-hope: {minVal['hope']}")   
    print(f"*** max-hope: {maxVal['hope']}")   
    print(f"*** min-joy : {minVal['joy']}")   
    print(f"*** max-joy : {maxVal['joy']}")   
    print(f"*** min-satisfaction : {minVal['satisfaction']}")   
    print(f"*** max-satisfaction : {maxVal['satisfaction']}")   
    print(f"*** min-fear: {minVal['fear']}")   
    print(f"*** max-fear: {maxVal['fear']}")   
    print(f"*** min-distress      : {minVal['distress']}")   
    print(f"*** max-distress      : {maxVal['distress']}")   
    print(f"*** min-disappointment: {minVal['disappointment']}")   
    print(f"*** max-disappointment: {maxVal['disappointment']}")   
    print(f"*** max-num-of-cycles : {maxVal['t']}")   

    
#
# Main: print some statistics of trace files. 
# 
# argv[1] : the directory where the trace-files are located.
#
#dir = sys.argv[1]
dir    = 'C:/Users/Gholi002/Documents/Utrecht/iv4XR Research/Implementation/ecoverage/data/CSVs/x'
#prinStats(dir)
emoGraphLib.findExtremeCases(dir)
