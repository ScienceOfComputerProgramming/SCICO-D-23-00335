#!/bin/bash
# Run automated PX testing using both MC and SBT test suites.
# For the SBT suite, we use the pre-generated one.
# The target level should be manually copied to Combinedtest/model
# so we can use this script to target a mutated level.
#
echo "** copy MC test suite"
cp ./FASE23Dataset/GeneratedTestSuites/MCtest/*.ser ../Combinedtest/
cp ./FASE23Dataset/GeneratedTestSuites/MCtest/*.txt ../Combinedtest/
echo "** copy SBT test suite"
cp ./FASE23Dataset/GeneratedTestSuites/SBTtest/*.ser ../Combinedtest/
cp ./FASE23Dataset/GeneratedTestSuites/SBTtest/*.txt ../Combinedtest/
echo "** Getting the EFSM model..."
cp ./FASE23Dataset/Base_LR_Level_and_EFSM/*.ser ../Combinedtest/model/
echo "** Running the test cases..."
mvn test -Dtest=eu.iv4xr.ux.pxtestingPipeline.RunOCC
echo "** Applying trace-fixing..."
mvn test -Dtest=eu.iv4xr.ux.pxtestingPipeline.TestcasesExecRepair
cp ../fixedtraces/fixed/*.csv ../traces/
echo "** Checking produced emotion traces against PX-requirements..."
mvn test -Dtest=eu.iv4xr.ux.pxtestingPipeline.CheckPXProperties
echo "** Done"
