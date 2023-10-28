import emoGraphLib

# sample call to construct a time-graph:
emoGraphLib.mkTimeProgressionGraph("./sampletrace1.csv",
           selectedProperties=["hope","fear","distress"],
           timeLabel="t",
           outputfile="sampletrace1_timeGraph.png")
