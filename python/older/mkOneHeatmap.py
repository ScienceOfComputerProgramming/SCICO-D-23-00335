import sys
import os
from pathlib import Path
import emoGraphLib 

#
# Main: to produce aggregate the heat heatmaps from a specific trace-file. 
# This produces heatmaps for each of all six emotions. The produced map
# will be put in the same directory as the trace-file.
#
# sys.argv[1] : the trace-file whose data are to be read.
# sys.argv[2] : the width of the produced heatmaps.
# sys.argv[3] : the height of the produced heatmaps.
#
#dir    = sys.argv[1]
file = Path(sys.argv[1])
width  = int(sys.argv[2])
height = int(sys.argv[3])

colorscheme = "white"

emoGraphLib.mkHeatMap(str(file.parent),file.name,width,height,colorscheme=colorscheme)
