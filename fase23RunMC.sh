#!/bin/bash
# Run automated PX testing using a Model Checker to
# generate the test cases
echo "** Generate an MC test suite..."
mvn test -Dtest=eu.iv4xr.ux.pxtestingPipeline.MCtest_Generation
echo "** copy all MC test cases ..."
cp ../MCtest/*.ser ../Combinedtest/
cp ../MCtest/*.txt ../Combinedtest/
echo "** Getting the target LR level and EFSM model..."
cp ./FASE23Dataset/Base_LR_Level_and_EFSM/* ../Combinedtest/model/
echo "** Running the test cases..."
mvn test -Dtest=eu.iv4xr.ux.pxtestingPipeline.RunOCC
echo "** Applying trace-fixing..."
mvn test -Dtest=eu.iv4xr.ux.pxtestingPipeline.TestcasesExecRepair
cp ../fixedtraces/fixed/*.csv ../traces/
echo "** Producing heatmaps..."
python3 ./mkHeatmaps.py
echo "** Checking produced emotion traces against PX-requirements..."
mvn test -Dtest=eu.iv4xr.ux.pxtestingPipeline.CheckPXProperties
echo "** Done"
