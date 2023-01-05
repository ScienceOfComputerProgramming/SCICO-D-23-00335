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


def mkHeatMapWorker(dataset,
        basename,
        pfun,
        whiteVal=1,
        width=64,
        height=64,
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
    
    whiteVal : the plotted values in the map will be assumed to range in the interval
    of [0...whiteVal]. 0 will be mapped to the color black, and whiteVal to the color
    white.

    scale : positions (x,y) such that round(scale*x),round(sclae*y) have the same value
    are treated as representing the same position. For example scale=0.5 means that
    data from position (0.9,0.9) to be considered as comparable to data from (0,0).

    width : the width of the produced map. 

    height : the height of the produced map.

    graphtitle : a text that will be put as the title of the produced map.
    """
    black = 0
    white = whiteVal
    map = np.zeros((scale*height,scale*width))
    for x in range(0,scale*height):
      for y in range(0,scale*width):
          map[x][y] = -1001

    for r in dataset:
        xx = round(scale*float(r['x']))
        yy = round(scale*float(r['y']))
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
    map[scale*height-1][scale*width-1] = white  # for imposing the range to go from black to white
       
    ax = plt.gca()
    ax.xaxis.set_visible(False)
    ax.yaxis.set_visible(False)
    plt.imshow(map, cmap='hot', origin='lower', interpolation='nearest')

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
    "fear" : 1/1.25 , "distress" : 4, "disappointment" : 1 
}

def mkHeatMap(dir,filename,width,height):
    """ The function to construct a visual heatmap from emotion traces.

    This will construct heapmaps for six emotions: hope, joy, satisfaction,
    fear, distress, and dissapointment.

    Parameters
    -------------
    dir : the directory where the map will be placed.

    filename: the name of the trace-file whose data will be plotted to a heatmap.
    
    width : the width of the produced map. 

    height : the height of the produced map.
    """
    basename = filename.rsplit('.')[0]
    file_ = Path(dir + "/" +  filename)
    dataset = data_set1 = loadCSV(file_)
    mkMap = lambda emoType, pfunction: mkHeatMapWorker(dataset=dataset,
            width=width,
            height=height,
            pfun = pfunction,
            dir = dir,
            graphtitle = basename + " " + emoType, 
            basename = basename + "_" + emoType)
    mkMap('hope', lambda r: rescaling['hope']*float(r['hope']))
    mkMap('joy', lambda r: rescaling['joy']*float(r['joy']))
    mkMap('satisfaction', lambda r: rescaling['satisfaction']*float(r['satisfaction']))
    mkMap('fear', lambda r: rescaling['fear']*float(r['fear']))
    mkMap('distress', lambda r: rescaling['distress']*float(r['distress']))
    mkMap('disappointment', lambda r: rescaling['disappointment']*float(r['disappointment']))

def mkAggregateHeatMap(dir,width,height):
    dataset = []
    for filename in os.listdir(dir):
        if(filename.endswith(".csv")):
            file_ = Path(dir + "/" + filename)
            datax = loadCSV(file_)
            #print(filename)
            #print(datax)
            dataset.extend(datax)
    mkMap = lambda emoType, pfunction: mkHeatMapWorker(dataset=dataset,
            width=width,
            height=height,
            pfun = pfunction,
            dir = dir,
            graphtitle = emoType, 
            basename =  "aggregate_" + emoType)
    mkMap('hope', lambda r: rescaling['hope']*float(r['hope']))
    mkMap('joy', lambda r: rescaling['joy']*float(r['joy']))
    mkMap('satisfaction', lambda r: rescaling['satisfaction']*float(r['satisfaction']))
    mkMap('fear', lambda r: rescaling['fear']*float(r['fear']))       # rescaling fear
    mkMap('distress', lambda r: rescaling['distress']*float(r['distress']))  # rescaling distress
    mkMap('disappointment', lambda r: rescaling['disappointment']*float(r['disappointment']))

