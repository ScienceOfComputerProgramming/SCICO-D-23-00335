package eu.iv4xr.ux.pxmbt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import alice.tuprolog.Var;
import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import eu.fbk.iv4xr.mbt.testcase.MBTChromosome;
import eu.fbk.iv4xr.mbt.testsuite.SuiteChromosome;
import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.Levenshtein;
import info.debatty.java.stringsimilarity.MetricLCS;
import info.debatty.java.stringsimilarity.interfaces.StringDistance;

/**
 * For measuring the distance/difference-metric between two test-cases. Three
 * distance-metrics are supported: Jaro-Winker, Levenshtein, and Jaccard.
 */
public class Distance {

	public StringDistance  mtr;

	Distance(){}
	
	/**
	 * The metric is either "jaro-winkler", or "levenshtein", or "jaccard".
	 */
	public Distance(String metric) {
	
		switch (metric){
			case "jaro-winkler":  mtr=new JaroWinkler(-1);
				break;
				
			case "levenshtein":   mtr=new Levenshtein();
				break;
				
			case "jaccard":       mtr= new Jaccard();
				break; 
				
			default:  mtr=new JaroWinkler();
		}
	}
	
	/**
	 * Given a test suite S, this return the total distance between every pair of
	 * distinct test-cases (t1,t2) in S. If (t1,t2) has been counted, we don't
	 * count (t2,t1) again.
	 * 
	 * <p>The current implementation convert the test cases to strings, and then 
	 * apply the distance metric on the resulting strings.
	 */
	public double distance(List<AbstractTestSequence> absTestsuite) {
		
		double total=0;
		int count=0;
		double[][] testsuite_distance= new double[absTestsuite.size()][absTestsuite.size()];
		for(int i=0; i<absTestsuite.size();i++ ){
		
			for(int j=i+1 ;j<absTestsuite.size();j++ ) {
				
				testsuite_distance[i][j]= mtr.distance(absTestsuite.get(i).toString(),absTestsuite.get(j).toString());
			 
				total += mtr.distance(absTestsuite.get(i).toString(),absTestsuite.get(j).toString());
					count++; 
			}
		}
		System.out.print("#pairs: "+ count);
		return total;
	}
	
	/**
	 * The same as {@link #distance(List), but where the test-suite is represented as EvoMMBT's
	 * SuiteChromosome.
	 */
	public double distance(SuiteChromosome absTestSuite) {
		
		MBTChromosome ch = absTestSuite.getTestChromosome(0) ; 
		double total=0;
		int count=0;
		double[][] testsuite_distance= new double[absTestSuite.size()][absTestSuite.size()];

		for(int i=0; i<absTestSuite.size();i++ ){
		
			for(int j=i+1 ;j<absTestSuite.size();j++ ) {
				
				testsuite_distance[i][j]= mtr.distance(absTestSuite.getTestChromosome(i).toString(),absTestSuite.getTestChromosome(j).toString());
			 
				total+=mtr.distance(absTestSuite.getTestChromosome(i).toString(),absTestSuite.getTestChromosome(j).toString());
				count++;
			}
		}
		System.out.println("#pairs: "+ count);
		return total;
	}
	
	public int pairs_distance(SuiteChromosome loadedSolution) {
		
		double distance = 0;
		//double max=0;
		//int indexes [][]=new int [2][1];
		HashMap<Integer , Double> dist_map=new HashMap<>();
		for(int i=0; i<loadedSolution.size();i++ ){
		
			for(int j=i+1 ;j<loadedSolution.size();j++ ) {
				
				distance+= mtr.distance(loadedSolution.getTestChromosome(i).toString(),loadedSolution.getTestChromosome(j).toString());
			 
				/*
				 * if(distance> max) { max=distance; indexes[0][0]=i; indexes[1][0]=j; }
				 */
			}
			double avg=distance/(loadedSolution.size()-i-1);
			dist_map.put(i,avg);
			
			
		}
		return dist_map.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
		
	}


}
