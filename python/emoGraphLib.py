#
# Provide functions to produce time-graph and heatmaps from the trace files.
# Three main functions are provided:
#    * mkTimeProgressionGraph(...) to produce a timegraph from a single trace file
#    * mkHeatMap(...) to produce a heatmap of emotion from a single trace file
#    * mkAggregateHeatMap(...) to produde an aggregated heatmap of emotion from
#      all trace files in a given directory.
#
#
saveToFile = True

import sys
import matplotlib
if saveToFile:
   matplotlib.use('Agg')   # to generate png output, must be before importing matplotlib.pyplot
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import numpy as np
import math
import csv
import os
import os.path
from pathlib import Path
#import pprint
#import statistics
#import scipy.stats as scistats


def loadCSV(csvfile):
    """ To load a csv-file. 
    Entries (cells) should be separated by commas. Return a list of rows.
    """
    # need to use a correct character encoding.... latin-1 does it
    with open(csvfile, encoding='latin-1') as file:
      content = csv.DictReader(file, delimiter=',')
      rows = []
      for row in content: rows.append(row)
      return rows

def toFloat(str):
   if str=="" : return 0
   return float(str)

def toInt(str):
   if str=="" : return 0
   return int(str)

def mkTimeProgressionGraph(filename:str,
        selectedProperties,
        timeLabel:str="time",
        outputfile:str="tgraph"
        ):
    """ A function to draw a time-graph.
    
    This plots the values of the properties specified in the selectedProperties over time.

    Parameters
    --------------
    filename  : a csv-file containing input data (comma-separated).
    selectedProperties: a list of property-names (column-names) whole values are to be plotted.
    timeLabel : the name used to denote time. Default is "time".
    outputfile: the name of the outputfile.png. Default "tgraph".
    """
    # read the data from the file:
    dataset = loadCSV(filename)

    plt.ylabel('values', fontsize=12)
    plt.xlabel('time', fontsize=12)
    plt.grid(b=True, axis='y')

    # plot the values of
    for propName in selectedProperties:
       plt.plot([ toInt(r[timeLabel])  for r in dataset ],
                [ toFloat(r[propName]) for r in dataset ],
                  label = propName , )


    plt.rcParams.update({'font.size': 12})
    #fig.suptitle("Emotion time progression")
    #plt.title(f"{propertiesToDisplay} overtime", fontsize=10)
    plt.title("values overtime", fontsize=10)
    plt.legend()
    if saveToFile : plt.savefig(outputfile)
    else : plt.show()

def mkHeatMapWorker(dataset,
        basename,
        pfun,
        maxvalue=1,
        width=64,
        height=64,
        xlabel='x',
        ylabel='y',
        scale=1,
        dir=".",
        graphtitle="heatmap"):
    """ The worker function to construct a visual heatmap from emotion traces.
    Parameters
    -------------
    dataset : a list of rows representing the data obtained from emotion traces. 
    Each row is a disctionary of 
    attribute-name and its value. The values are assumed to be non-negative. 
    Each row is assumed to contain attributes x,y,t; (x,y) represents position,
    and t represents time.

    pfun : is a function (e.g. a lamda expression) that maps each row to a value.
    E.g. it could be the function lambda r : float(r['fear']). This function 
    determines the values that will be plotted onto the produced map.
    
    basename : the map will be saved in a file named basename.png.
    
    dir : the directory where the map will be placed.
    
    maxvalue : the plotted values in the map will be assumed to range in the interval
    of [0...maxvalue]. 0 will be mapped to the color black, and maxvalue to the color
    white.

    scale : positions (x,y) such that round(scale*x),round(sclae*y) have the same value
    are treated as representing the same position. For example scale=0.5 means that
    data from position (0.9,0.9) to be considered as comparable to data from (0,0).

    width : the width of the produced map. 

    height : the height of the produced map.

    graphtitle : a text that will be put as the title of the produced map.
    """
    black = 0
    #white = maxvalue
    map = np.zeros((scale*height,scale*width))
    for x in range(0,scale*height):
      for y in range(0,scale*width):
          map[x][y] = -1001

    for r in dataset:
        xx = round(scale*toFloat(r[xlabel]))
        yy = round(scale*toFloat(r[ylabel]))
        # rotate +90 degree
        x = scale*height - yy
        y = xx
        value = pfun(r)
        #print(f"==== val {value}" )
        if map[x][y] < -1000 :
           map[x][y] = value
        else:
           map[x][y] = max(map[x][y],value)
        #print(f"==== val {map[x][y]}" )
        

    for x in range(0,scale*height):
      for y in range(0,scale*width):
          if map[x][y] < -1000 : map[x][y] = black    
    #map[scale*height-1][scale*width-1] = white  # for imposing the range to go from black to white
       
    ax = plt.gca()
    ax.xaxis.set_visible(False)
    ax.yaxis.set_visible(False)
    plt.imshow(map, cmap='hot', origin='lower', interpolation='nearest', vmin=0, vmax=maxvalue)

    plt.title(graphtitle)
    #plt.legend()
    file_ = Path(dir + "/" +  basename +'.png')
    if saveToFile : plt.savefig(file_)
    else : plt.show()

#
# define here the rescaling, if needed; 1 means no rescaling:
#
rescaling = { 
    "hope" : 1, "joy" : 1, "satisfaction" : 1,
    "fear" : 1 , "distress" : 1, "disappointment" : 1 
}

def getMaxVal(row,propertyPrefixName):
   max = 0
   for prop,val in row.items():
      if prop.lower().startswith(propertyPrefixName):
         v = toFloat(val)
         if v>max : max = v
   return max

def mkHeatMap(dir,filename,width,height,maxvalue=1,xlabel='x',ylabel='y'):
    """ The function to construct visual heatmaps from an emotion trace.

    This will construct heapmaps for six emotions: hope, joy, satisfaction,
    fear, distress, and dissapointment.

    Parameters
    -------------
    dir : the directory where the map will be placed.

    filename: the name of the trace-file whose data will be plotted to a heatmap.
    
    width : the width of the produced map. 

    height : the height of the produced map.

    maxvalue : specify here the maximum value of the emotions. This influences
               the color range of the produced heatmap.

    xlabel : the name/label of x-position. The default is just 'x'
    ylabel : the name/label of x-position. The default is just 'y'             
    """
    basename = os.path.basename(filename) 
    basename = os.path.splitext(basename)[0]
    #file_ = Path(dir + "/" +  filename)
    dataset = data_set1 = loadCSV(filename)
    mkMap = lambda emoType, pfunction: mkHeatMapWorker(dataset=dataset,
            maxvalue=maxvalue,
            width=width,
            height=height,
            xlabel=xlabel,
            ylabel=ylabel,
            pfun = pfunction,
            dir = dir,
            graphtitle = basename + " " + emoType, 
            basename = basename + "_" + emoType)
    mkMap('hope', lambda r: rescaling['hope']*getMaxVal(r,'hope'))
    mkMap('joy',  lambda r: rescaling['joy']*getMaxVal(r,'joy'))
    mkMap('satisfaction', lambda r: rescaling['satisfaction']*getMaxVal(r,'satisfaction'))
    mkMap('fear',     lambda r: rescaling['fear']*getMaxVal(r,'fear'))
    mkMap('distress', lambda r: rescaling['distress']*getMaxVal(r,'distress'))
    mkMap('disappointment', lambda r: rescaling['disappointment']*getMaxVal(r,'disappointment'))

def mkAggregateHeatMap(dir,width,height,maxvalue=1,xlabel='x',ylabel='y'):
    """ The function to construct aggregate visual heatmaps from emotion traces.

    This will construct heapmaps for six emotions: hope, joy, satisfaction,
    fear, distress, and dissapointment. The traces will simply be concatenated
    and treated as one.

    Parameters
    -------------
    dir : the directory where traces are located. They are assume to end with
          .csv (this also means that all csv-files in the dir will be read).
          The resulting heatmaps will be placed in the same directory.
          
    width : the width of the produced map. 

    height : the height of the produced map.

    maxvalue : specify here the maximum value of the emotions. This influences
               the color range of the produced heatmap.

    xlabel : the name/label of x-position. The default is just 'x'
    ylabel : the name/label of x-position. The default is just 'y'             
    """
    dataset = []
    for filename in os.listdir(dir):
        if(filename.endswith(".csv")):
            file_ = Path(dir + "/" + filename)
            datax = loadCSV(file_)
            #print(filename)
            #print(datax)
            dataset.extend(datax)
    mkMap = lambda emoType, pfunction: mkHeatMapWorker(dataset=dataset,
            maxvalue=maxvalue,
            width=width,
            height=height,
            xlabel=xlabel,
            ylabel=ylabel,
            pfun = pfunction,
            dir = dir,
            graphtitle = emoType, 
            basename =  "aggregate_" + emoType)
    mkMap('hope', lambda r: rescaling['hope']*getMaxVal(r,'hope'))
    mkMap('joy',  lambda r: rescaling['joy']*getMaxVal(r,'joy'))
    mkMap('satisfaction', lambda r: rescaling['satisfaction']*getMaxVal(r,'satisfaction'))
    mkMap('fear',     lambda r: rescaling['fear']*getMaxVal(r,'fear'))       
    mkMap('distress', lambda r: rescaling['distress']*getMaxVal(r,'distress'))  
    mkMap('disappointment', lambda r: rescaling['disappointment']*getMaxVal(r,'disappointment'))

#if __name__ == "__main__":
   #print("hello!")
   # sample call to construct a time-graph:
   #mkTimeProgressionGraph("../tmp/tc7.csv",
   #         selectedProperties=["Hope_A shrine is cleansed.","Fear_A shrine is cleansed."],
   #         timeLabel="time",
   #         outputfile="test0TimeGraph.png"
   #         )
   
   # sample call to construct a heat map
   #mkHeatMap("./","../tmp/tc7.csv",19,19,maxvalue=1000,xlabel='x',ylabel='z')
   #mkHeatMap("./","./data_goalQuestCompleted_9.csv",100,70,maxvalue=1,xlabel='x',ylabel='y')
   #mkAggregateHeatMap("./",100,70,maxvalue=1,xlabel='x',ylabel='y')
   #mkAggregateHeatMap("../tmp",19,19,maxvalue=1000,xlabel='x',ylabel='z')