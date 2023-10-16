package eu.iv4xr.ux.pxmbt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;

/**
 * For making a random selection from a set of test cases.
 * 
 * @author sansari
 */
public class RandomSampling {

	static List<AbstractTestSequence> RandomSampling(List<AbstractTestSequence> Testsuite, int size) {
		
		Random r = new Random();
		List<AbstractTestSequence> Testsuite_New= new ArrayList<AbstractTestSequence>();
	
		int[] unique = r.ints(0, Testsuite.size()-1).distinct().limit(size).toArray();
		for(var u: unique)
		{
			Testsuite_New.add(Testsuite.get(u));
		}	
		return Testsuite_New;
		
	}

}
