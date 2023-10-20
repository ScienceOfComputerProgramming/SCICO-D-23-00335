#!/bin/bash
# For quickly checking if the deployment of FASE-23 experiment
# would work.
echo "** Recompile the experiment..."
mvn clean
mvn compile
echo "** Generate an MC test suite..."
mvn test -Dtest=eu.iv4xr.ux.pxtestingPipeline.MCtest_Generation
echo "** Select two test cases for running..."
cp ../MCtest/MCtest_1.ser ../Combinedtest/
cp ../MCtest/MCtest_1.txt ../Combinedtest/
cp ../MCtest/MCtest_8.ser ../Combinedtest/
cp ../MCtest/MCtest_8.txt ../Combinedtest/
echo "** Getting the target LR level and EFSM model..."
cp ./FASE23Dataset/Base_LR_Level_and_EFSM/* ../Combinedtest/model/
echo "** Running selected test cases..."
mvn test -Dtest=eu.iv4xr.ux.pxtestingPipeline.RunOCC
echo "** Producing heatmaps..."
python3 ./mkHeatmaps.py
echo "** Checking produced emotion traces against PX-requirements..."
mvn test -Dtest=eu.iv4xr.ux.pxtestingPipeline.CheckPXProperties
echo "** Done"
