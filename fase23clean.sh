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
echo "** Done"
