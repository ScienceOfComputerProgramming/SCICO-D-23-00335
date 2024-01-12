package eu.iv4xr.ux.pxtesting.study.labrecruits;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.ltl.SATVerdict; 
import eu.iv4xr.framework.extensions.occ.Emotion;
import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;
import eu.iv4xr.framework.mainConcepts.IEmotion;
import eu.iv4xr.ux.pxtesting.study.labrecruits.LRState.LREmotion;
import nl.uu.cs.aplib.utils.CSVUtility;

public class EmotionCoverage {
	
	
	Map<String,List<LRState>> executionData  = new HashMap<>() ;
	List<String> targetNames = new LinkedList<>() ;
	Map<String,LTL<LRState>> coverageTargets = new HashMap<>();
	
	/**
	 * Map for each LTL property, the executions that cover them.
	 */
	Map<String,List<String>> coverageData = new HashMap<>() ;
	
	public void printGeneralStatistics() {
		int numOfExecutions = executionData.size() ;
		int numOfCoverageTargets = coverageTargets.size() ;
		int covered = 0 ;
		List<String> uniquelyCovered = new LinkedList<>() ;
		for(var cd : coverageData.entrySet()) {
			if (cd.getValue().size() > 0) covered++ ;
			if (cd.getValue().size() == 1) uniquelyCovered.add(cd.getKey()) ;
		}
		System.out.println("** #test-cases: " + numOfExecutions) ;
		System.out.println("** #emotion patterns investigated: " + numOfCoverageTargets) ;
		System.out.println("** #patterns witnessed: " + covered) ;
		System.out.println("** #patterns not appearing: " + (numOfCoverageTargets - covered)) ;
		System.out.println("** #unique patterns: " + uniquelyCovered.size()) ;
		  if (uniquelyCovered.size() > 0) {
			  System.out.println("    " + uniquelyCovered) ;
		  }
	}
	
	public void printCoverage(int verbosity) {
		int numOfExecutions = executionData.size() ;
		int numOfCoverageTargets = coverageTargets.size() ;
		int covered = 0 ;
		for(var targetName :  targetNames) {
			var cov = coverageData.get(targetName) ;
			if (cov.size() > 0) covered++ ; 
			String targetStat = "" + targetName + ": " + cov.size() ;
			if (cov.size() == numOfExecutions) {
				targetStat += " (VALID)" ;
			}
			else if (cov.isEmpty()) {
				targetStat += " (UNSAT)" ;
			}
			else {
				targetStat += " (SAT)" ;
			}
			System.out.println("** " + targetStat) ;
			if(verbosity>0) {
				for(var exec : cov) {
					System.out.println("      " + exec) ;
				}
			}
		}
		System.out.println("** #test-cases: " + numOfExecutions) ;
		System.out.println("** #emotion patterns investigated: " + numOfCoverageTargets) ;
		System.out.println("** #patterns witnessed: " + covered) ;
		System.out.println("** #patterns not appearing: " + (numOfCoverageTargets - covered)) ;
	}
	
	public void addTargets(String ... patterns ) {
		for(String pat : patterns) {
			coverageTargets.put(pat, translate(pat)) ;
			targetNames.add(pat) ;
		}
	}
	
	public void calculate() {
		for(var target : coverageTargets.entrySet()) {
			String pattern = target.getKey() ;
			LTL<LRState> ltl = target.getValue() ;
			List<String> coveringExecutions = new LinkedList<>() ;
			//System.out.println("=== " + pattern) ;
			for(var exec : executionData.entrySet()) {
				//System.out.println("    " + exec.getKey() + "/" + exec.getValue().size()) ;
				if(ltl.sat(exec.getValue()) == SATVerdict.SAT) {
					coveringExecutions.add(exec.getKey()) ;
				}
			}
			coverageData.put(pattern, coveringExecutions) ;
		}
	}
	
	public void clearTarget() {
		coverageTargets.clear();
		coverageData.clear();
		targetNames.clear() ;
	}
	
	static float raiseThreshold = 0f ;
	
	static Predicate<LRState> raising(Emotion.EmotionType etype, float threshold) {
		return state -> state.getEmotionDif(etype.toString()) > threshold ;
	}
	
	static Predicate<LRState> raising(Emotion.EmotionType etype) {
		return raising(etype,raiseThreshold) ;
	}
	
	static Predicate<LRState> notRaising(Emotion.EmotionType etype) {
		return state -> ! raising(etype,raiseThreshold).test(state) ;
	}
	
	static final String IMP_SYMBOL = "->" ;
	
	/**
	 * Translate a pattern like "F;H;F" (unconditioned pattern), or conditioned
	 * pattern like "S -> F;H;J;S" to the corresponding LTL formula. 
	 * More implications are allowed, as in
	 * pat1 -> pat2 -> pat3, which is interpreted right-associatively.
	 */
	static LTL<LRState> translate(String pattern) {
		if (pattern.contains(IMP_SYMBOL)) {
			
			String[] parts = pattern.split(IMP_SYMBOL) ;
			int N = parts.length ;
			LTL<LRState> q = translateNonConditionalPattern(parts[N-1].trim())  ;
			for (int k = N-2; 0<=k; k--) {
				String subPattern = parts[k].strip() ;
				LTL<LRState> p = translateNonConditionalPattern(subPattern) ;
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
    static LTL<LRState> translateNonConditionalPattern(String pattern) {
    	pattern = pattern.toUpperCase() ;
    	String[] pattern_ = pattern.split(";") ;
    	LTL<LRState> ltl = null ;
    	for(int n = pattern_.length-1 ; 0<=n; n--) {
    		String p = pattern_[n] ;
    		//System.out.println(">>>> " + p) ;
    		if(p.startsWith("N") ) {
    			p = p.substring(1) ;
    			LTL<LRState> not_p = null ;
    			switch(p) {
    			   case "H" : not_p = LTL.now("not"+p, notRaising(Emotion.EmotionType.Hope)) ; break ;
    			   case "J" : not_p = LTL.now("not"+p, notRaising(Emotion.EmotionType.Joy)) ; break ;
    			   case "S" : not_p = LTL.now("not"+p, notRaising(Emotion.EmotionType.Satisfaction)) ; break ;
    			   case "F" : not_p = LTL.now("not"+p, notRaising(Emotion.EmotionType.Fear)) ; break ;
    			   case "D" : not_p = LTL.now("not"+p, notRaising(Emotion.EmotionType.Distress)) ; break ;
    			   case "P" : not_p = LTL.now("not"+p, notRaising(Emotion.EmotionType.Disappointment)) ; break ;
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
    			LTL<LRState> p_now = null ;
    			switch(p) {
    			   case "H" : p_now = LTL.now(p, raising(Emotion.EmotionType.Hope)) ; break ;
    			   case "J" : p_now = LTL.now(p, raising(Emotion.EmotionType.Joy)) ; break ;
    			   case "S" : p_now = LTL.now(p, raising(Emotion.EmotionType.Satisfaction)) ; break ;
    			   case "F" : p_now = LTL.now(p, raising(Emotion.EmotionType.Fear)) ; break ;
    			   case "D" : p_now = LTL.now(p, raising(Emotion.EmotionType.Distress)) ; break ;
    			   case "P" : p_now = LTL.now(p, raising(Emotion.EmotionType.Disappointment)) ; break ;
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
	
	
	static public EmotionCoverage readFromCSVs(String dir) throws IOException {
		DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir)) ;
		EmotionCoverage data = new EmotionCoverage() ;
		for (Path path : stream) {
            if (!Files.isDirectory(path)) {
            	String fname = path.getFileName().toString() ;
            	if (! fname.toLowerCase().endsWith(".csv")) continue ;
            	// parsing the content of the file:
            	List<LRState> execution = new LinkedList<>() ;
            	var content = CSVUtility.readCSV(',',path.toString()) ;
            	int r = 0 ;
            	LRState previous = null ;
            	for (String[] row : content) {
            		// the first row is just the header (column-names); skip it:
            		if (r==0) {
            			r++ ;
            			continue ;            			
            		}
            		LRState st = new LRState() ;
            		st.updateTurn = Integer.parseInt(row[0]) ;
            		st.x = Float.parseFloat(row[1]) ;
            		st.y = Float.parseFloat(row[2]) ;
            		
            		List<LRState.LREmotion> emotions = new LinkedList<>() ;
            		LRState.LREmotion hope = new LRState.LREmotion() ;
            		hope.type = Emotion.EmotionType.Hope ;
            		hope.intensity = Float.parseFloat(row[3]) ;
            		emotions.add(hope) ;
            		
            		LRState.LREmotion joy = new LRState.LREmotion() ;
            		joy.type = Emotion.EmotionType.Joy ;
            		joy.intensity = Float.parseFloat(row[4]) ;
            		emotions.add(joy) ;
            		
            		LRState.LREmotion satisfaction = new LRState.LREmotion() ;
            		satisfaction.type = Emotion.EmotionType.Satisfaction ;
            		satisfaction.intensity = Float.parseFloat(row[5]) ;
            		emotions.add(satisfaction) ;
            		
            		LRState.LREmotion fear = new LRState.LREmotion() ;
            		fear.type = Emotion.EmotionType.Fear ;
            		fear.intensity = Float.parseFloat(row[6]) ;
            		emotions.add(fear) ;
            		
            		LRState.LREmotion distress = new LRState.LREmotion() ;
            		distress.type = Emotion.EmotionType.Distress ;
            		distress.intensity = Float.parseFloat(row[7]) ;
            		emotions.add(distress) ;
            		
            		LRState.LREmotion disappointment = new LRState.LREmotion() ;
            		disappointment.type = Emotion.EmotionType.Disappointment ;
            		disappointment.intensity = Float.parseFloat(row[8]) ; 
            		emotions.add(disappointment) ;
            		
            		st.emotions = emotions ;
            		st.previous = previous ;
            		// don't add the first real state:
            		if (r>1) 
            			execution.add(st) ;
            		previous = st ;
            		r++ ;
            	}
            	data.executionData.put(fname, execution) ;
            }
        }
		System.out.println(">> reading " + data.executionData.size() + " files from: " + Paths.get(dir).toString()) ;
		
		return data ;
	}
	
	// just for testing
	public static void main(String[] args) throws IOException {
		System.out.println(translate("S")) ;
		System.out.println(translate("P")) ;
		System.out.println(translate("nP")) ;
		System.out.println(translate("H;H;S")) ;
		System.out.println(translate("H;nH;S")) ;
		System.out.println(translate("nF;H")) ;
		System.out.println(translate("S -> F;F;S")) ;
		
		String dirMC = "./data/CSVs/MC" ;
		String dirSBT = "./data/CSVs/SBT" ;
		EmotionCoverage ecovMC = readFromCSVs(dirMC) ;
		EmotionCoverage ecovSBT = readFromCSVs(dirSBT) ;
		String[] targets0 = {
				  "H", "J", "S", "F", "D", "P",
				  "nH", "nJ", "nS", "nF", "nD", "nP"
				}  ;
		ecovMC.addTargets(targets0);
		ecovSBT.addTargets(targets0);
		ecovMC.calculate();
		ecovSBT.calculate();
		System.out.println("====== MC:") ;
		ecovMC.printCoverage(0);
		System.out.println("====== SBT:") ;
		ecovSBT.printCoverage(0);
		ecovMC.clearTarget();
		ecovSBT.clearTarget();
		
		String[] targets1 = { 
				"nH;J", "nF;J", "nH;D", "nF;D", 
				"nJ;H", "nD;F", "nJ;S", "nD;P",
				"H;J", "H;S", "H;F", "H;D", "H;P", "S -> H;S", 
				"D;H","D;J","D;F", "D;S",
				"H;F;H", "F;H;F", "J;F;S", "D;H;P"
		} ;
		ecovMC.addTargets(targets1);
		ecovSBT.addTargets(targets1);
		ecovMC.calculate();
		ecovSBT.calculate();
		System.out.println("====== MC:") ;
		ecovMC.printCoverage(0);
		System.out.println("====== SBT:") ;
		ecovSBT.printCoverage(0);
		ecovMC.clearTarget();
		ecovSBT.clearTarget();
		/*
		int nsat = 0 ;
		int ndisp = 0 ;
		for(var exec : ecov.executionData.entrySet()) {
			for(var st : exec.getValue()) {
				if(st.getEmotionIntensity(EmotionType.Satisfaction.toString()) > 0) {
					nsat++ ;
					System.out.println("=== sat " + exec.getKey()) ;
					break ;
				}
			}
			for(var st : exec.getValue()) {
				if(st.getEmotionIntensity(EmotionType.Disappointment.toString()) > 0) {
					System.out.println("=== disp " + exec.getKey()) ;
					ndisp++ ;
					break ;
				}
			}
		}
		System.out.println(" #satisfaction: " + nsat) ;
		System.out.println(" #disappointment: " + ndisp) ;
		*/
		//var exec = ecov.executionData.get("data_goalQuestCompleted_7.csv") ;
		//LRState.printLRStateSequence(exec);
	}
	
	
}
