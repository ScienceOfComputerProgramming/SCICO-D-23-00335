package eu.iv4xr.ux.pxtestingPipeline;

import java.io.File;
import java.io.FileNotFoundException;

import cex.CexPathStates;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLangException;
import prism.PrismLog;
import prism.Result;

public class cobaPrismAPIs_1 {
	
	
	public static void main(String[] args) throws FileNotFoundException, PrismException {
		
		String projectRoot = "/Users/iswbprasetya/workshop/projects/iv4xr/formalFatima/mcutils" ;
		
		//PrismLog mainLog = new PrismDevNullLog();
		PrismLog mainLog = new PrismFileLog("stdout");
		// Initialise PRISM engine 
		Prism prism = new Prism(mainLog);
		prism.initialise();
		// Parse and load a PRISM model from a file
		ModulesFile modulesFile = prism.parseModelFile(new File(projectRoot + "/prism/samplemodels/dice/dice1.pm"));
		prism.loadPRISMModel(modulesFile);
		// Parse and load a properties model for the model
		PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile, new File(projectRoot + "/prism/samplemodels/dice/queries.pctl"));
		
		// Model checking properties from the loaded property-files:
		for (int k=0; k<5; k++) {
			System.out.println("====== Property to check: " + propertiesFile.getPropertyObject(k));
			Result result = prism.modelCheck(propertiesFile, propertiesFile.getPropertyObject(k));
			System.out.println(result.getResult());
			System.out.println(result.getExplanation());
			var counterExample = (CexPathStates) result.getCounterexample() ;
			if (counterExample == null) {
				System.out.println(">> Prism does not seem to produce a witness...") ;
			}
			else {
				System.out.println(">> Witness execution: " + counterExample);
			}
		}
		
		
		

	}
	


}