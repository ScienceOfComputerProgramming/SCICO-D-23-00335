package eu.iv4xr.ux.pxmbt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

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
	 * The metric is either "jaro", or "levenshtein", or "jaccard".
	 * 
	 * <p>NOTE: not supporting jaro-winkler! Just Jaro. 
	 */
	public Distance(String metric) {
	
		switch (metric){
			case "jaro":  mtr=new JaroWinkler(-1);
				break;
				
			case "levenshtein":   mtr=new Levenshtein();
				break;
				
			case "jaccard":       mtr= new Jaccard();
				break; 
				
			default:  mtr=new JaroWinkler(-1);
		}
	}
	
	/**
	 * Consruct a minimal string representation of an abstract test case. This is a concatenation
	 * of all the id of the states passed by the test case, and the id of the transitions that
	 * connect them. This representation is not meant to be readable, and is mainly used to calculate
	 * edit "distance" or dissimilarity  between test cases.
	 */
	public static String toMinString(AbstractTestSequence tc) {
		var z = new StringBuffer() ;
		var transitions = tc.getPath().getTransitions() ;
		int k = 0 ;
		for (var t : transitions) {
			if (k==0) 
				z.append(t.getSrc().getId()) ;
			z.append(t.getId()) ;
			z.append(t.getTgt().getId()) ;
			k++ ;
		}
		return z.toString() ;
	}
	
	/**
	 * Given a test suite S, this return the total distance between every pair of
	 * distinct test-cases (t1,t2) in S. If (t1,t2) has been counted, we don't
	 * count (t2,t1) again.
	 * 
	 * <p>The current implementation convert the test cases to strings, and then 
	 * apply the distance metric on the resulting strings.
	 */
	public double totDistance(List<AbstractTestSequence> absTestsuite) {
		
		return distances(absTestsuite).getSum() ;
	}
	
	
	public double distance(String seq1, String seq2) {
		return mtr.distance(seq1,seq2) ;
	}

	public double distance(AbstractTestSequence seq1, AbstractTestSequence seq2) {
		return mtr.distance(toMinString(seq1), toMinString(seq2)) ;
	}
	
	/**
	 * Given a test suite S, this method first collects the distance between 
	 * every pair of distinct test-cases (t1,t2) in S. Here the pairs (t1,t2)
	 * and (t2,t1) are considered as the same pair. We then return a statistics
	 * over these distances. From this stats, we can query the total of the distances,
	 * the minimum, the mean, etc.
	 * 
	 * <p>The current implementation convert the test cases to strings, and then 
	 * apply the distance metric on the resulting strings.
	 */
	
	public SummaryStatistics distances(List<AbstractTestSequence> absTestsuite) {
		
		var stats = new SummaryStatistics() ;
		
		for(int i=0; i<absTestsuite.size();i++ ){
			//String tci = absTestsuite.get(i).toString() ;
			// Don't just use toString() :)  it is more verbose and has some duplicity, so
			// the string-diff will appear to be lower
			String tci = toMinString(absTestsuite.get(i)) ;
			//System.out.println(">>> tcix: " + tci) ;
			//System.out.println(">>> tciorg: " + absTestsuite.get(i)) ;
			
			
			for(int j=i+1 ;j<absTestsuite.size();j++ ) {			
				//String tcj = absTestsuite.get(j).toString() ;

				String tcj = toMinString(absTestsuite.get(j)) ;
				
				stats.addValue(mtr.distance(tci,tcj));
			}
		}
		return stats;
	}
	
	/**
	 * The same as {@link #distance(List), but where the test-suite is represented as EvoMMBT's
	 * SuiteChromosome.
	 */
	@Deprecated
	public double totDistance(SuiteChromosome absTestSuite) {
		
		List<AbstractTestSequence> S = absTestSuite.getTestChromosomes().stream()
				.map(ch -> (AbstractTestSequence) ch.getTestcase())
				.collect(Collectors.toList()) ;
		
		return totDistance(S) ;
	}
	/*
	 * I don't know what this compute .... :D 
	 */
	public int pairs_distance(List<AbstractTestSequence> absTestsuite) {
		
		double distance = 0;

		HashMap<Integer,Double> dist_map=new HashMap<>();
		for(int i=0; i<absTestsuite.size();i++ ){
			for(int j=i+1 ;j<absTestsuite.size();j++ ) {
				distance+= mtr.distance(
						toMinString(absTestsuite.get(i)),
						toMinString(absTestsuite.get(j)));
				/*
				 * if(distance> max) { max=distance; indexes[0][0]=i; indexes[1][0]=j; }
				 */
			}
			double avg=distance/(absTestsuite.size()-i-1);
			dist_map.put(i,avg);
		}
		return dist_map.entrySet().stream()
				.max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
				.get().getKey();
	}
	
	@Deprecated
	public int pairs_distance(SuiteChromosome absTestSuite) {
		
		List<AbstractTestSequence> S = absTestSuite.getTestChromosomes().stream()
				.map(ch -> (AbstractTestSequence) ch.getTestcase())
				.collect(Collectors.toList()) ;
		
		return pairs_distance(S) ;
	}


}
