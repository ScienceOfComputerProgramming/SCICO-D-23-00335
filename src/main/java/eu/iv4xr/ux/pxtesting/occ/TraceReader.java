package eu.iv4xr.ux.pxtesting.occ;

import java.io.File;
import java.io.IOException;
import java.util.*;

import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import eu.fbk.iv4xr.mbt.utils.TestSerializationUtils;
import eu.iv4xr.framework.extensions.occ.Emotion;
import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;
import eu.iv4xr.framework.extensions.occ.Goal;
import nl.uu.cs.aplib.utils.CSVUtility;
import nl.uu.cs.aplib.utils.Pair;

public class TraceReader {
	
	public static List<Map<String,String>> readTraceFile(Character separator, String fname) throws IOException {
		
		var rawrows = CSVUtility.readCSV(separator, fname) ;
		
		List<Map<String,String>> rows = new LinkedList<>() ;
		
		if (rows.size()<=1)
			return rows ;
		
		Map<String,Integer> propertyNames = new HashMap<>() ;
		String[] columnNames = rawrows.remove(0) ;
		for (int k=0; k<columnNames.length; k++) {
			propertyNames.put(columnNames[k], k) ;
		}
		
		for (var R : rawrows) {
			Map<String,String> Rmap = new HashMap<>() ;
			for (var prop : propertyNames.entrySet()) {
				int k = prop.getValue() ;
				if (k < R.length) {
					Rmap.put(prop.getKey(), R[k]) ;
				}
			}
			rows.add(Rmap) ;
		}
		
		return rows ;
	}
	
	static private String getGoalName(String z) {
		int k = z.indexOf('_') ;
		if (k<0) return null ;
		return z.substring(k+1) ;
	}
	
	public static Set<OCCEmotion> getEmotionState(Map<String,String> state) {
		
		Set<OCCEmotion> emo = new HashSet<>() ;
		
		var HOPE = EmotionType.Hope.toString() ;
		var JOY = EmotionType.Joy.toString() ;
		var SATIS = EmotionType.Satisfaction.toString() ;
		var FEAR = EmotionType.Fear.toString() ;
		var DISTRESS = EmotionType.Distress.toString() ;
		var DISAP = EmotionType.Disappointment.toString() ;
		
		for (var X : state.entrySet()) {
			String pname = X.getKey() ;
			if (pname.startsWith(HOPE) || pname.startsWith(JOY) || pname.startsWith(SATIS)
					|| pname.startsWith(FEAR) || pname.startsWith(DISTRESS) || pname.startsWith(DISAP)) {
				String gname = getGoalName(pname) ;
				Goal g = new Goal(gname) ;
				int intensity = Integer.parseInt(X.getValue()) ;
				EmotionType ty = null ;
				if (pname.startsWith(HOPE)) {
					ty = EmotionType.Hope ;
				} 
				else if (pname.startsWith(JOY)) {
					ty = EmotionType.Joy ;
				} 
				else if (pname.startsWith(SATIS)) {
					ty = EmotionType.Satisfaction ;
				} 
				else if (pname.startsWith(FEAR)) {
					ty = EmotionType.Fear ;
				} 
				else if (pname.startsWith(DISTRESS)) {
					ty = EmotionType.Distress ;
				} 
				else if (pname.startsWith(DISAP)) {
					ty = EmotionType.Disappointment ;
				} 
				var E_ = new Emotion(ty,g,0,intensity) ;
				OCCEmotion E = new OCCEmotion(gname,E_) ;
				emo.add(E) ;
			}
		}
		return emo ;
	}
	
	public static List<Pair<Set<OCCEmotion>, Set<OCCEmotion>>> getEmotionTrace(Character separator, String fname) throws IOException {
		var rawrows = readTraceFile(separator,fname) ;
		List<Pair<Set<OCCEmotion>, Set<OCCEmotion>>> etrace = new LinkedList<>() ;
		Set<OCCEmotion> previous = null ;
		for (var R : rawrows) {
			Set<OCCEmotion> current = getEmotionState(R) ;
			Pair<Set<OCCEmotion>,Set<OCCEmotion>> emo = new Pair<>(current,previous) ;
			etrace.add(emo) ;
			previous = current ;
		}
		return etrace ;
	}
	
	public static List<List<Pair<Set<OCCEmotion>, Set<OCCEmotion>>>> getAllEmotionTrace(Character separator, 
			String dirName,
			String prefixName) throws IOException {
		List<List<Pair<Set<OCCEmotion>, Set<OCCEmotion>>>> traces = new LinkedList<>() ;
		List<File> files = org.apache.maven.shared.utils.io.FileUtils.getFiles(new File(dirName), "*.csv", "");
		for (File file : files) {
			
			String fname = file.getName() ;
			// dropping the extension .ser
			fname = fname.substring(0, fname.length() - 4) ;
			
			if (! fname.startsWith(prefixName))
				continue ;
			
			var trace = getEmotionTrace(separator,file.getAbsolutePath()) ;
			traces.add(trace) ;
			
		}
		return traces ;
	}
	

}
