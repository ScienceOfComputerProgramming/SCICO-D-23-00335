#!/bin/bash
# Run automated PX testing using a test suite freshly
# generated using a Search-based Testing algorithm (MOSA).
#
echo "** Generate a fresh SBT test suite..."
> mvn test -Dtest=eu.iv4xr.ux.pxtestingPipeline.SBtest_Generation
echo "** copy all SBT test cases ..."
cp ../SBTtest/*.ser ../Combinedtest/
cp ../SBTtest/*.txt ../Combinedtest/
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
