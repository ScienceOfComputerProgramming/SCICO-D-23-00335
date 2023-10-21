package eu.iv4xr.ux.pxtestingPipeline;
import static eu.iv4xr.framework.extensions.ltl.LTL.*;
import static eu.iv4xr.framework.extensions.ltl.LTL2Buchi.getBuchi;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fbk.iv4xr.mbt.MBTProperties;
import eu.fbk.iv4xr.mbt.MBTProperties.LR_random_mode;
import eu.fbk.iv4xr.mbt.efsm.EFSM;
import eu.fbk.iv4xr.mbt.efsm.EFSMFactory;
import eu.fbk.iv4xr.mbt.efsm.EFSMPath;
import eu.fbk.iv4xr.mbt.efsm.EFSMState;
import eu.fbk.iv4xr.mbt.efsm.EFSMTransition;
import eu.fbk.iv4xr.mbt.efsm.labRecruits.LabRecruitsRandomEFSM;
import eu.fbk.iv4xr.mbt.efsm.labRecruits.LabRecruitsRandomEFSM.StateType;
import eu.fbk.iv4xr.mbt.efsm.modelcheckingInterface.InterfaceToIv4xrModelCheker;
import eu.fbk.iv4xr.mbt.efsm.modelcheckingInterface.InterfaceToIv4xrModelCheker.EFSMTransitionWrapper;
import eu.fbk.iv4xr.mbt.strategy.GenerationStrategy;
import eu.fbk.iv4xr.mbt.strategy.SearchBasedStrategy;
import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import eu.fbk.iv4xr.mbt.testcase.MBTChromosome;
import eu.fbk.iv4xr.mbt.testsuite.SuiteChromosome;
import eu.fbk.iv4xr.mbt.utils.TestSerializationUtils;
import eu.iv4xr.framework.extensions.ltl.BasicModelChecker;
import eu.iv4xr.framework.extensions.ltl.Buchi;
import eu.iv4xr.framework.extensions.ltl.BuchiModelChecker;
import eu.iv4xr.framework.extensions.ltl.IExplorableState;
import eu.iv4xr.framework.extensions.ltl.ITransition;
import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.ltl.BasicModelChecker.Path;
import eu.iv4xr.ux.pxtesting.mbt.Distance;
import eu.iv4xr.framework.extensions.ltl.*;
import nl.uu.cs.aplib.utils.Pair;
import eu.fbk.iv4xr.mbt.Main;
import static eu.iv4xr.framework.extensions.ltl.LTL.*;
import static org.junit.jupiter.api.Assertions.* ;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import static eu.iv4xr.framework.extensions.ltl.LTL.*;
import static org.junit.jupiter.api.Assertions.* ;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import info.debatty.java.stringsimilarity.*;
import java.util.HashMap;
import java.util.HashSet;

public class test_coverage {
    
	@Test
	public void test() throws FileNotFoundException {
	
	List<String> mclist=new ArrayList<String>();
	List<String> sbtlist=new ArrayList<String>();

	String rootFolder = new File(System.getProperty("user.dir")).getParent();
    String testFolder = rootFolder + File.separator + "Combinedtest";
	String modelFolder = testFolder + File.separator + "Model";
	File mctxtFile = new File( testFolder + File.separator + "MCnotcovered_transitions" + ".txt");
	//File sbttxtFile = new File( testFolder + File.separator + "SBTnotcovered_transitions-59" + ".txt");
	File sbttxtFile = new File( testFolder + File.separator + "SBTnotcovered_transitions-32" + ".txt");

	mclist=readall(mctxtFile); 
	sbtlist=readall(sbttxtFile); 

//	Set<String> dupes = new HashSet<String>(); 
//	for (String x : sbtlist)
//	{
//		if (!dupes.add(x))  System.out.println(x);
//	
//	}
	
		
	
	List<String> mcfinal = new ArrayList<String>();
    List<String> sbtfinal = new ArrayList<String>();

	for (int i = 0; i < mclist.size(); i++){

	    if (!sbtlist.contains(mclist.get(i))){

			mcfinal.add(mclist.get(i));
	    }
	}


	for (int j = 0; j < sbtlist.size(); j++){

	    if (!mclist.contains(sbtlist.get(j))){

	        sbtfinal.add(sbtlist.get(j));
	    }

	}
	System.out.println("original size of MC:" +mclist.size()+ "after duplicate removal: "+ mcfinal.size());
	mcfinal.forEach(c-> System.out.println(c));
	System.out.println("original size of SBT:" +sbtlist.size()+ "after duplicate removal: "+ sbtfinal.size());
	sbtfinal.forEach(c-> System.out.println(c));
}
	

	private List<String> readall(File txtFile) throws FileNotFoundException {
		 Scanner read=new Scanner(txtFile);
	     List<String> lines=new ArrayList<String>();
		 while(read.hasNext())
		   {
			   lines.add(read.useDelimiter(", ").next());
		    }
		return lines;
	}
}
