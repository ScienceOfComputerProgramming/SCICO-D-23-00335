#!/bin/bash
# Cleaning the working folders for the FASE-23 experiment
echo "** Cleaning generated test suites..."
rm -rf ../MCtest/*
rm -rf ../SBTtest/*
echo "** Cleaning the Combinedtest-dir..."
rm -rf ../Combinedtest/*.txt
rm -rf ../Combinedtest/*.ser
rm -rf ../Combinedtest/Model/*
echo "** Cleaning the traces-dir..."
rm -rf ../traces/*
echo "** Cleaning the fixedtraces-dir..."
rm -rf ../fixedtraces/alreadyOk/*.csv
rm -rf ../fixedtraces/fixed/*.csv
rm -rf ../fixedtraces/stuck_unfixed/*.csv
rm -rf ../fixedtraces/timeout_unfixed/*.csv
echo "** Done"
