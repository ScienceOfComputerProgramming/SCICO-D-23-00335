package eu.iv4xr.ux.pxtesting.occ;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.ltl.SATVerdict;
import eu.iv4xr.framework.extensions.occ.Emotion;
import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;
import nl.uu.cs.aplib.utils.Pair;

public class EmotionPattern {
	
	public static float getEmotionIntensity(EmotionType ty, String goal, Set<OCCEmotion> emo) {
		
		List<OCCEmotion> z = null ;
		
		if (goal == null) {
			z = emo.stream().filter(E -> E.em.etype.equals(ty)).collect(Collectors.toList());
		}
		else {
			z = emo.stream().filter(E -> E.goalName.equals(goal) && E.em.etype.equals(ty))
					.collect(Collectors.toList());
		}
		if (z.size() == 0) 
			return 0 ;
		
		if (z.size() == 1)
			return z.get(0).getIntensity() ;
		
		return z.stream().max((x,y) -> Float.compare(x.getIntensity(), y.getIntensity())).get().getIntensity() ;
	}
	
	static float raiseThreshold = 0f ;
	
	
	static Predicate<Pair<Set<OCCEmotion>, Set<OCCEmotion>>> raising(EmotionType etype, float threshold) {
		return state -> {
			if (state.snd == null) // no previous state
				return false ;
			float current = getEmotionIntensity(etype,null,state.fst) ;
			float prev = getEmotionIntensity(etype,null,state.snd) ;
			//if (etype == EmotionType.Joy && current > 0) {
			//	System.out.println(">>> Joy: " + current + ", prev:" + prev) ;
			//}
			return current - prev > threshold ;
		} ;
	}
	
	static Predicate<Pair<Set<OCCEmotion>, Set<OCCEmotion>>> raising(Emotion.EmotionType etype) {
		return raising(etype,raiseThreshold) ;
	}
	
	static Predicate<Pair<Set<OCCEmotion>, Set<OCCEmotion>>> notRaising(Emotion.EmotionType etype) {
		return state -> ! raising(etype,raiseThreshold).test(state) ;
	}
	
	static final String IMP_SYMBOL = "->" ;
	
	/**
	 * Translate a pattern like "F;H;F" (unconditioned pattern), or conditioned
	 * pattern like "S -> F;H;J;S" to the corresponding LTL formula. 
	 * More implications are allowed, as in
	 * pat1 -> pat2 -> pat3, which is interpreted right-associatively.
	 */
	static LTL<Pair<Set<OCCEmotion>, Set<OCCEmotion>>> translate(String pattern) {
		if (pattern.contains(IMP_SYMBOL)) {
			
			String[] parts = pattern.split(IMP_SYMBOL) ;
			int N = parts.length ;
			LTL<Pair<Set<OCCEmotion>, Set<OCCEmotion>>> q = translateNonConditionalPattern(parts[N-1].trim())  ;
			for (int k = N-2; 0<=k; k--) {
				String subPattern = parts[k].strip() ;
				LTL<Pair<Set<OCCEmotion>, Set<OCCEmotion>>> p = translateNonConditionalPattern(subPattern) ;
				q = p.implies(q) ;
			}
			return q ;
		}
		else {
			return translateNonConditionalPattern(pattern) ;
		}
	}
	
	/**
	 * Translate unconditioned pattern to the corresponding LTL formula.
	 */
    @SuppressWarnings("unchecked")
	static LTL<Pair<Set<OCCEmotion>, Set<OCCEmotion>>> translateNonConditionalPattern(String pattern) {
    	pattern = pattern.toUpperCase() ;
    	String[] pattern_ = pattern.split(";") ;
    	LTL<Pair<Set<OCCEmotion>, Set<OCCEmotion>>> ltl = null ;
    	for(int n = pattern_.length-1 ; 0<=n; n--) {
    		String p = pattern_[n] ;
    		//System.out.println(">>>> " + p) ;
    		if(p.startsWith("N") ) {
    			p = p.substring(1) ;
    			LTL<Pair<Set<OCCEmotion>, Set<OCCEmotion>>> not_p = null ;
    			switch(p) {
    			   case "H" : not_p = LTL.now("not"+p, notRaising(EmotionType.Hope)) ; break ;
    			   case "J" : not_p = LTL.now("not"+p, notRaising(EmotionType.Joy)) ; break ;
    			   case "S" : not_p = LTL.now("not"+p, notRaising(EmotionType.Satisfaction)) ; break ;
    			   case "F" : not_p = LTL.now("not"+p, notRaising(EmotionType.Fear)) ; break ;
    			   case "D" : not_p = LTL.now("not"+p, notRaising(EmotionType.Distress)) ; break ;
    			   case "P" : not_p = LTL.now("not"+p, notRaising(EmotionType.Disappointment)) ; break ;
    			}
    			if (n == pattern_.length-1) {
    				// p is the last atom in the pattern:
    				ltl = LTL.always(not_p) ;
    			}
    			else {
        			ltl = not_p.until(ltl) ;
    			}	
    		}
    		else {
    			LTL<Pair<Set<OCCEmotion>, Set<OCCEmotion>>> p_now = null ;
    			switch(p) {
    			   case "H" : p_now = LTL.now(p, raising(EmotionType.Hope)) ; break ;
    			   case "J" : p_now = LTL.now(p, raising(EmotionType.Joy)) ; break ;
    			   case "S" : p_now = LTL.now(p, raising(EmotionType.Satisfaction)) ; break ;
    			   case "F" : p_now = LTL.now(p, raising(EmotionType.Fear)) ; break ;
    			   case "D" : p_now = LTL.now(p, raising(EmotionType.Distress)) ; break ;
    			   case "P" : p_now = LTL.now(p, raising(EmotionType.Disappointment)) ; break ;
    			}
    			if (n == pattern_.length-1) {
    				// p is the last atom in the pattern:
    				ltl = p_now ;
    			}
    			else {
    				// p is not the last atom;
    				// we need to look at the next atom:
    				String q = pattern_[n+1] ;
    				if(q.startsWith("N")) {
    					ltl = LTL.ltlAnd(p_now, LTL.next(ltl)) ;
    				}
    				else {
    					ltl = LTL.ltlAnd(p_now, LTL.next(LTL.eventually(ltl))) ;
    				}
    			}
    		}
    	}
    	String firstAtom = pattern_[0] ;
    	if (!firstAtom.startsWith("N")) {
    		// if the first atom is not a "N" we need to add a top-level eventually:
    		ltl = LTL.eventually(ltl) ;
    	}
    	return ltl ;
    }
    
    public static SATVerdict checkOne( List<Pair<Set<OCCEmotion>, Set<OCCEmotion>>> trace, String pattern) {
    	LTL<Pair<Set<OCCEmotion>, Set<OCCEmotion>>> ltl = translate(pattern) ;
    	return ltl.sat(trace) ;
    }
    
    public static SATVerdict checkOne(String pattern, Character separator, String fname) throws IOException {
    	var trace = TraceReader.getEmotionTrace(separator,fname) ;
    	//System.out.println(">>> #tr=" + trace.size()) ;
    	return checkOne(trace,pattern) ;
    }
    
    public static class CheckingResult {
    	public List<List<Pair<Set<OCCEmotion>, Set<OCCEmotion>>>> traces ;
    	public List<Integer> sat = new LinkedList<>() ;
    	public List<Integer> violating  = new LinkedList<>() ;
    	public List<Integer> unknown  = new LinkedList<>() ;
    	public boolean valid() {
    		return (violating.isEmpty()) ;
    	}
    	public boolean sat() {
    		return sat.size() > 0 ;
    	}
    	public boolean satN(int n) {
    		return sat.size() >= n ;
    	}
    	public boolean unsat() {
    		return sat.isEmpty() && violating.size() > 0 ;
    	}
    	public boolean inconclusive() {
    		return unknown.size() == traces.size() ;
    	}
    	@Override
    	public String toString() {
    		String z = "** N=" + traces.size() ;
    		if (valid()) {
        		z += ", VALID" ;    			
    		}
    		else if (unsat()) {
    			z += ", UNSAT" ;
    		}
    		else if (sat()) {
    			z += ", SAT, but not VALID" ;
    		}
    		else if (inconclusive()) {
    			z += ", inconclusive" ;
    		}
    		else {
    			throw new IllegalArgumentException("Problem with verdict calculation! This should not happen.") ;
    		}
    		z += "\n** #sat    =" + sat.size() ;
    		z += "\n** #unsat  =" + violating.size() ;
    		z += "\n** #unknown=" + unknown.size() ;   
        	return z ;
    	}
    }
    
    public static CheckingResult checkAll(List<List<Pair<Set<OCCEmotion>, Set<OCCEmotion>>>> traces, String pattern) {
    	
    	LTL<Pair<Set<OCCEmotion>, Set<OCCEmotion>>> ltl = translate(pattern) ;
    	var result = new CheckingResult() ;
    	result.traces = traces ;
    	Integer k = 0 ;
    	for (var tr : traces) {
    		var verdict = checkOne(tr,pattern) ;
    		switch (verdict) {
    			case SAT   : result.sat.add(k) ; break ;
    			case UNSAT : result.violating.add(k) ; break ;
    			default : result.unknown.add(k) ;
    		}
    		k++ ;
    	}
    	return result ;
    }
    
    public static CheckingResult checkAll(String pattern, Character separator, String tracesDir, String prefixName) 
    		throws IOException {
    	var traces = TraceReader.getAllEmotionTrace(separator,tracesDir,prefixName) ;
    	return checkAll(traces,pattern) ;
    }
    
    // just a test...
    public static void main(String[] args) throws IOException {
    	
    	System.out.println(">>> " + checkOne("H",',',"./tmp/test0.csv")) ;
    	System.out.println(">>> " + checkOne("F",',',"./tmp/test0.csv")) ;
    	System.out.println(">>> " + checkOne("NH;F",',',"./tmp/test0.csv")) ;
    	System.out.println(">>> " + checkOne("J",',',"./tmp/test0.csv")) ;
    	System.out.println(">>> " + checkOne("H;J;S",',',"./tmp/test0.csv")) ;
    	System.out.println(">>> " + checkOne("NJ;S",',',"./tmp/test0.csv")) ;
     	System.out.println(">>> " + checkOne("J;NJ;S",',',"./tmp/test0.csv")) ;
      
    }
    
}
	
