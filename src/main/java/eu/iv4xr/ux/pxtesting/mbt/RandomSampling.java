package eu.iv4xr.ux.pxtesting.mbt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import info.debatty.java.stringsimilarity.JaroWinkler;

/**
 * For making a random selection from a set of test cases.
 * 
 * @author sansari
 */
public class RandomSampling {
	
	public int seed = 1371 ;
	Random rnd = new Random(seed) ;
	
	public RandomSampling() { } 
	
	public RandomSampling(int seed) { 
		this.seed = seed ;
		rnd = new Random(seed) ;
	}

	/**
	 * Return a random subset of a given size from a test suite.
	 */
	public List<AbstractTestSequence> randomSampling(List<AbstractTestSequence> testsuite, int size) {
		
		List<AbstractTestSequence> testsuite_new= new ArrayList<AbstractTestSequence>();
	
		if (size >= testsuite.size()) {
			testsuite_new.addAll(testsuite) ;
			return testsuite_new ;
		}
		
		int[] unique = rnd.ints(0, testsuite.size()).distinct().limit(size).toArray();
		for(var u: unique)
		{
			testsuite_new.add(testsuite.get(u));
		}	
		return testsuite_new;	
	}

	/**
	 * Return a subset of a given size from a test suite, chosen through an adaptive random
	 * sampling method. It works as follows:
	 * 
	 * <ol>
	 * <li>Given a suite S, we start we an empty selected-set Z. We fill Z one at a time.
	 * until get get the desired number N of test cases.
	 * At the beginning we move one tc from S to Z.
	 * <li>We make a random sample of k test-cases from S. We call these candidates.
	 * We choose candidate c, whose total distance to members of Z is maximum. Then we
	 * move c from S to Z.
	 * <li>We keep doing this until #Z=N.
	 * </ol>
	 * 
	 * <p>Below, the parameter "size" is N, and k is sampling size in the above algoritm.
	 * 
	 * <p>Note: this is a variation of
	 * Hemmati et al, Achieving Scalable Model-Based Testing Through Test Case Diversity.
	 */
	public List<AbstractTestSequence> adaptiveRandomSampling(List<AbstractTestSequence> testsuite, 
			int k,
			int size) {
		
		if (size <= 0 || k<=0)
			throw new IllegalArgumentException() ;
		
		List<AbstractTestSequence> remaining = new ArrayList<>() ;
		remaining.addAll(testsuite) ;
						
		if (size >= testsuite.size()) {
			return remaining ;
		}
		
		if (k==1) {
			return randomSampling(testsuite,size) ;
		}
		
		var D = new Distance("jaro") ;
		
		List<AbstractTestSequence> testsuite_new= new ArrayList<AbstractTestSequence>();

		int rand1=rnd.nextInt(remaining.size());
		testsuite_new.add(remaining.remove(rand1));

		while(testsuite_new.size()!=size)
		{
			
			int[] candidates = rnd.ints(0, remaining.size()).distinct().limit(k).toArray();
			
			double bestDistance = -1 ;
			int bestCandidate = -1 ;
			
			// will choose for the candidate whose total-distance to the current
			// selected-set is the greatest:
			for(var u : candidates)
			{
				double totaldistance = 0 ;
				String tcu = Distance.toMinString(remaining.get(u)) ;
				for (var l : testsuite_new) {
					String tcl = Distance.toMinString(l) ;
					totaldistance += D.distance(tcu,tcl);
				}
				if (totaldistance > bestDistance) {
					bestCandidate = u ;
				}
			}
			AbstractTestSequence best = remaining.remove(bestCandidate) ;
			testsuite_new.add(best) ;
		}	    
		
		return testsuite_new;
	}

}
