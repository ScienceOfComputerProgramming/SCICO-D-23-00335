import sys
import os
from pathlib import Path
import emoGraphLib

#
# Main: to produce aggregate ands all-individual heatmaps. This produce
# heatmaps for each of all six emotions.
# Set the variable makeIndividualMaps to true if individual maps are to be
# generated (may take some time to produce them all!). The default is false.
#
# sys.argv[1] : the directory where the trace files are located.
# sys.argv[2] : the width of the produced heatmaps.
# sys.argv[3] : the height of the produced heatmaps.
#
#dir    = sys.argv[1]
#width  = int(sys.argv[2])
#height = int(sys.argv[3])
#sdir    = 'C:/Users/Gholi002/Documents/Utrecht/iv4XR Research/Implementation/ecoverage/data/CSVs/MC'
#dir    = 'C:/Users/Gholi002/Documents/Utrecht/iv4XR Research/Implementation/ecoverage/data//CSVs/SBT'
#dir    = 'C:/Users/Gholi002/Documents/Utrecht/iv4XR Research/Implementation/ecoverage/data/Shortestdisappointment'
#dir    = 'C:/Users/Gholi002/Documents/Utrecht/iv4XR Research/Implementation/ecoverage/data//CSVs/SBT-Sample'
#dir    = 'C:/Users/Gholi002/Documents/Utrecht/iv4XR Research/Implementation/ecoverage/data//CSVs/Sample'
dir    = '../traces'

width  =100
height = 70
makeIndividualMaps = False

colorscheme = "white"

emoGraphLib.mkAggregateHeatMap(dir,width,height,colorscheme=colorscheme)

if makeIndividualMaps:
    for filename in os.listdir(dir):
        if(filename.endswith(".csv")):
            emoGraphLib.mkHeatMap(dir,filename,width,height,colorscheme=colorscheme)
