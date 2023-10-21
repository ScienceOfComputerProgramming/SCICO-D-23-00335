package eu.iv4xr.ux.pxtestingPipeline;

import org.junit.jupiter.api.* ;

import eu.iv4xr.ux.pxtesting.study.labrecruits.EmotionCoverage;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

public class CheckPXProperties {
	
	@Test
	public void checkPX() throws IOException {
		
		String traceDir = ".." + File.separator + "traces" ;
		EmotionCoverage ecov = EmotionCoverage.readFromCSVs(traceDir) ;
		
		String[] pxPropertiesToCheck = { 
				"nD;S",  // distress is never triggered then satisfaction 
				"nF;S",  // distress is never triggered then satisfaction
				"J;nS",  // joy is triggered, but then satisfaction is never triggered
				"J;D",   // joy is triggered, and then distress is triggered
				"J;F;S", // joy is triggered, and then fear is triggered, then satisfaction is triggered
				"D;H;P", // distress is triggered, and then hope is triggered, then disappointment is triggered
				"D;H;S",
				"D;H;nD;S",
				"F;D;H;F;J",
				"H;F;D;D;D;H;F;J",
				"F;D;D;H;F;P" } ;
		ecov.addTargets(pxPropertiesToCheck);
		ecov.calculate();
		ecov.printCoverage(0);		
	}

}
