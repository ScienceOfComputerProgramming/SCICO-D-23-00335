#!/bin/bash
# Cleaning the working folders for the FASE-23 experiment
echo "** Cleaning generated test suites..."
rm -r ../MCtest/*
rm -r ../SBTtest/*
echo "** Cleaning the Combinedtest-dir..."
rm -r ../Combinedtest/*.txt
rm -r ../Combinedtest/*.ser
rm -r ../Combinedtest/Model/*
echo "** Cleaning the traces-dir..."
rm ../traces/*
echo "** Done"
