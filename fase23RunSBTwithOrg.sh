#!/bin/bash
# Run automated PX testing using a pre-generated test suite.
# The test suite was generated using a Search-based Testing
# algorithm (MOSA).
#
echo "** copy SBT test suite"
cp ./FASE23Dataset/GeneratedTestSuites/SBTtest/*.ser ../Combinedtest/
cp ./FASE23Dataset/GeneratedTestSuites/SBTtest/*.txt ../Combinedtest/
echo "** Getting the target LR level and EFSM model..."
cp ./FASE23Dataset/Base_LR_Level_and_EFSM/* ../Combinedtest/model/
echo "** Running the test cases..."
mvn test -Dtest=eu.iv4xr.ux.pxtestingPipeline.RunOCC
echo "** Applying trace-fixing..."
mvn test -Dtest=eu.iv4xr.ux.pxtestingPipeline.TestcasesExecRepair
echo "** Producing heatmaps..."
python3 ./mkHeatmaps.py
echo "** Checking produced emotion traces against PX-requirements..."
mvn test -Dtest=eu.iv4xr.ux.pxtestingPipeline.CheckPXProperties
echo "** Done"
