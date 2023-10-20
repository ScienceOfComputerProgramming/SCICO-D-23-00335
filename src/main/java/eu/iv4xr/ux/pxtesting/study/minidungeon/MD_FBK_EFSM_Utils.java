package eu.iv4xr.ux.pxtesting.study.minidungeon;

import java.util.*;
import java.util.stream.Collectors;

import org.evosuite.utils.Randomness;

import eu.fbk.iv4xr.mbt.efsm.EFSM;
import eu.fbk.iv4xr.mbt.efsm.EFSMBuilder;
import eu.fbk.iv4xr.mbt.efsm.EFSMContext;
import eu.fbk.iv4xr.mbt.efsm.EFSMGuard;
import eu.fbk.iv4xr.mbt.efsm.EFSMOperation;
import eu.fbk.iv4xr.mbt.efsm.EFSMParameter;
import eu.fbk.iv4xr.mbt.efsm.EFSMParameterGenerator;
import eu.fbk.iv4xr.mbt.efsm.EFSMState;
import eu.fbk.iv4xr.mbt.efsm.EFSMTransition;
import eu.fbk.iv4xr.mbt.efsm.exp.Assign;
import eu.fbk.iv4xr.mbt.efsm.exp.Const;
import eu.fbk.iv4xr.mbt.efsm.exp.Exp;
import eu.fbk.iv4xr.mbt.efsm.exp.Var;
import eu.fbk.iv4xr.mbt.efsm.exp.bool.BoolAnd;
import eu.fbk.iv4xr.mbt.efsm.exp.bool.BoolNot;
import eu.fbk.iv4xr.mbt.efsm.exp.integer.IntEq;
import eu.fbk.iv4xr.mbt.efsm.exp.integer.IntGreat;
import eu.fbk.iv4xr.mbt.efsm.labRecruits.ButtonDoors1;
import eu.fbk.iv4xr.mbt.efsm.labRecruits.LRActions;
import eu.fbk.iv4xr.mbt.efsm.labRecruits.LRParameterGenerator;
import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import eu.fbk.iv4xr.mbt.testcase.MBTChromosome;
import eu.fbk.iv4xr.mbt.testcase.Testcase;
import eu.fbk.iv4xr.mbt.testsuite.SuiteChromosome;
import eu.iv4xr.framework.extensions.ltl.IExplorableState;
import eu.iv4xr.framework.extensions.ltl.BasicModelChecker.Path;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.GWObject;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.GWState;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.GWTransition;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.GameWorldModel;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.LabRecruitsModel;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MiniDungeonModel;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.GWTransition.GWTransitionType;

/**
 * Containing functions to convert a {@link GameWorldModel} modelling
 * a level from the Lab Recruits of MiniDungeon games to FBK's
 * EFSM representation, and functions to translate test suites from
 * FBK's model based testing (MBT) algorithms to test cases over
 * the original GameWorldModel. 
 * 
 * @author Wish
 */
public class MD_FBK_EFSM_Utils {
	
	private static int scrollCode(String scrollId) {
		int k = scrollId.indexOf('_') ;
		int mazeNr = Integer.parseInt(scrollId.substring(1,k)) ;
		int scrollNr = Integer.parseInt(scrollId.substring(k+1,scrollId.length())) ;
		int scrollCode = 1000*(mazeNr+1) + scrollNr ;
		return scrollCode ;
	}
	
	static class MDParameterGenerator extends EFSMParameterGenerator  {

		private static final long serialVersionUID = 1 ;

		@Override
		public EFSMParameter getRandom() {
			Float f =  Randomness.nextFloat();
			if (f > 0.7) {
				return new EFSMParameter(new Var("tmp","TRAVEL"));
			}else {
				return new EFSMParameter(new Var("tmp","INTERACT"));
			}
		}

	}
	
	/**
	 * Construct an FBK-ESFM model from a MiniDungeon GameWorldModel.
	 */
	public static EFSM mkEFSM_from_MDModel(GameWorldModel model) {
		model.reset(); 
		GWState init = model.getCurrentState() ;
		
		// due to a limitation in FBK-EFSM we will represent the player's
		// bag as if it only has capacity one, and each scroll has a 
		// corresponding int-code, which is just 1000*maze-nr + scroll-nr.
		// Calculate first, the opener-scroll for every shrine:
		Map<String,Integer> shrineOpeners = new HashMap<>() ;
		
		
		for(var link : model.objectlinks.entrySet()) {
			String key = link.getKey() ;
			if (key.startsWith("SM") || key.startsWith("SI")) {
				String shrine = link.getKey() ;
				String holyscrollId =  null ;
				if (link.getValue() == null || link.getValue().isEmpty())
					continue ;
				for (var solution : link.getValue()) {
					holyscrollId = solution ;
					break ;
				}
				System.out.println(">>> " + shrine + ", opener: " + holyscrollId + ", code="
						+ scrollCode(holyscrollId)) ;
				shrineOpeners.put(shrine, scrollCode(holyscrollId)) ;
			}
		}
			
		Map<String,EFSMState> efsmStates = new HashMap<>() ;
		Map<String,Var<Boolean>> scrollPickedUpStateVars = new HashMap<>() ;
		Map<String,Var<Boolean>> shrineStateVars = new HashMap<>() ;
		
		// System.out.println(">>> ") ;
		
		// creating abstract states, one for every obj in the model, and vars
		// associated with them:
		for(GWObject o : init.objects.values()) {
			
			if (o.type.equals("DUMMY") || o.type.equals("FRODO") || o.type.equals("SMEAGOL")) {
				continue ;
			}
			efsmStates.put(o.id, new EFSMState(o.id)) ;
			if (o.type.equals("SHRINE") 
					&&
					(o.properties.get("shrinetype").equals("MoonShrine")
					 || o.properties.get("shrinetype").equals("ShrineOfImmortals"))) {
				
				shrineStateVars.put(o.id,new Var<Boolean>(o.id + "_clean", false)) ;
			}
			if (o.type.equals("SCROLL")) {
				
				scrollPickedUpStateVars.put(o.id, new Var<Boolean>(o.id + "_pickedUp", false)) ;
			}
			
		}
		
		
		// create an efsm-context and add the vars to it:
		
		Var<Boolean> gameOver = new Var<Boolean>("gameover", false);
		Var<Integer> bag      = new Var<Integer>("bag",0);
		Var[] vars = new Var[shrineStateVars.size() + scrollPickedUpStateVars.size() + 2] ;
		vars[0] = gameOver ;
		vars[1] = bag ;
		int k=2 ;
		for (var v : shrineStateVars.values()) {
			vars[k] = v ; k++ ;
		}
		for (var v : scrollPickedUpStateVars.values()) {
			vars[k] = v ; k++ ;
		}
		
		EFSMContext efsmContext = new EFSMContext(vars);
		
		
		// interact-action, represented as a transition input parameter
		EFSMParameter inputParInteract = new EFSMParameter(new Var<String>("action", "INTERACT")) ;			
		// travel-action, represented as a transition input parameter ;
		EFSMParameter inputParTravel = new EFSMParameter(new Var<String>("action", "TRAVEL"));

		EFSMBuilder efsmBuilder = new EFSMBuilder(EFSM.class);
			
		// create the transitions:

		
		for (String o1_id : efsmStates.keySet()) {
			
			GWObject o1 = init.objects.get(o1_id) ;
			
			//System.out.println(">>> " + o1.id + ", " + o1.type) ;
			
			// adding interact-transitions on scrolls:
			if (o1.type.equals("SCROLL")) {
				// o1 is a button, create a toggle-transition:
				EFSMTransition interact = new EFSMTransition();
				interact.setInParameter(inputParInteract);
				interact.setId("INTERACT_" + o1.id);
				
				Var<Boolean> hasBeenPicked = scrollPickedUpStateVars.get(o1.id) ;
				
				//System.out.println(">>> " + (hasBeenPicked != null)) ;

				interact.setGuard(new EFSMGuard(
						new BoolAnd(
							  new BoolNot(gameOver),
							  new BoolAnd(
									new BoolNot(hasBeenPicked),
									new IntEq(bag, new Const<Integer>(0))))
						)) ;
				
				EFSMOperation effect = new EFSMOperation(
					    new Assign<Boolean>(hasBeenPicked,new Const<Boolean>(true)),
					    new Assign<Integer>(bag,new Const<Integer>(scrollCode(o1.id)))
					    ) ;
					
				interact.setOp(effect);
				// System.out.println(">>> xx3") ;
				
				// add the transition to the efsm:
				efsmBuilder.withTransition(
						efsmStates.get(o1.id), 
						efsmStates.get(o1.id), 
						interact) ;		
				
				//System.out.println(">>> xx4") ;
			}
			
			//System.out.println(">>> xx") ;
			
			if (o1.type.equals("SHRINE")
				&&
				(o1.properties.get("shrinetype").equals("MoonShrine")
				 || o1.properties.get("shrinetype").equals("ShrineOfImmortals"))){
				
				EFSMTransition interact = new EFSMTransition();
				interact.setInParameter(inputParInteract);
				interact.setId("INTERACT_" + o1.id);
				
				Var<Boolean> isClean = shrineStateVars.get(o1.id) ;
				//System.out.println(">>> " + (isClean != null)) ;
				
				interact.setGuard(new EFSMGuard(
						new BoolAnd(
							  new BoolNot(gameOver),
							  new BoolAnd(
									new BoolNot(isClean),
									new IntGreat(bag, new Const<Integer>(0))))
						)) ;
				
				int shrineOpener = shrineOpeners.get(o1.id) ;
				EFSMOperation effect = new EFSMOperation(
						new Assign<Boolean>(isClean, new IntEq(bag, new Const<Integer>(shrineOpener))),
						new Assign<Integer>(bag,new Const<Integer>(0))
						) ;
				
				// for immortal shrine, also check if it would set gameOver to true:
				if (o1.properties.get("shrinetype").equals("ShrineOfImmortals")) {
					effect = new EFSMOperation(
						new Assign<Boolean>(isClean, new IntEq(bag, new Const<Integer>(shrineOpener))),
						new Assign<Boolean>(gameOver, new IntEq(bag, new Const<Integer>(shrineOpener))),
						new Assign<Integer>(bag,new Const<Integer>(0))
						) ;
				}
				
				interact.setOp(effect);
				// add the transition to the efsm:
				efsmBuilder.withTransition(
						efsmStates.get(o1.id), 
						efsmStates.get(o1.id), 
						interact) ;	
				System.out.println(">>> adding interact " + o1.id + ", opener:" + shrineOpener) ;
			}
			
			
			// below we add travel-transiitions from o1 to other objects o2, which is not
			// o1 itself, and if it is in the same zone:
			for (String o2_id : efsmStates.keySet()) {
				GWObject o2 = init.objects.get(o2_id) ;
				if (o2.type.equals("STARTLOC")) continue ; // forbid travel back to the start-loc
				if (! o1.id.equals(o2.id) 
						&& model.inTheSameZone(o1.id,o2.id)) {
					
					EFSMTransition travel = new EFSMTransition();
					travel.setInParameter(inputParTravel);
					travel.setId("TRAVEL_" + o1.id + "_" + o2.id);
					if (o2.type.equals("SCROLL")) {
						// if o2 is a scroll, travel to it is only possible if the scroll
						// is still there (has not been picked up):
						Var<Boolean> hasBeenPicked = scrollPickedUpStateVars.get(o2.id) ;
						travel.setGuard(new EFSMGuard(
								new BoolAnd(
									new BoolNot(gameOver),
									new BoolNot(hasBeenPicked)))) ;
									
					}
					else if (o1.type.equals("SHRINE") && o2.type.equals("SHRINE")
						  && (o1.properties.get("shrinetype").equals("MoonShrine"))
						  && (o2.properties.get("shrinetype").equals("SunShrine"))) {
						// teleport, from moon to sun, only possible if the moon is clean:
						
						Var<Boolean> moonIsClean = shrineStateVars.get(o1.id) ;				
						travel.setGuard(new EFSMGuard(
								new BoolAnd(
									new BoolNot(gameOver),
									moonIsClean))) ;			

					}
					else {
						travel.setGuard(new EFSMGuard(new BoolNot(gameOver))) ;
					}
	
					efsmBuilder.withTransition(
							efsmStates.get(o1.id), 
							efsmStates.get(o2.id), 
							travel) ;
					}
			}
		}
		//System.out.println(">>> here") ;
		
		MDParameterGenerator parameterGenerator = new MDParameterGenerator();
		EFSMState startingAbsState =  efsmStates.get(init.currentAgentLocation) ;
		
		// construct the efsm:
		EFSM efsm = efsmBuilder.build(startingAbsState, efsmContext, parameterGenerator);
		return efsm ;	

	}
		
	/**
	 * Convert of an abstract test-case (coming from MBT) for MiniDungeon game to a 
	 * sequence of GW-transitions.
	 */
	private static List<GWTransition> abstractTestSeqToGWTransitionSeq(AbstractTestSequence tc) {
		
		List<GWTransition> gwSeq = new LinkedList<>() ;
		
		for (var step : tc.getPath().getTransitions()) {
			String src = step.getSrc().getId() ;
			String dest = step.getTgt().getId() ;
			var v = step.getInParameter().getParameter().getVariable("action") ;
			String v_ = v.getValue().toString() ;
			//System.out.print("# " + src + "->" + dest + " by " + v_);		
			
			GWTransitionType trType = null ;
			if (v_.equals("TRAVEL")) {
				trType = GWTransitionType.TRAVEL ;
			}
			else if (v_.equals("INTERACT")) {
				trType = GWTransitionType.INTERACT ;
			}
			else {
				System.out.println("#### " + v_) ;
				throw new IllegalArgumentException() ;
			}

			GWTransition gwTr = new GWTransition(trType,dest) ;
			gwSeq.add(gwTr) ;
		}
		//System.out.println("") ;
		return gwSeq ;
	}
	
	
	private static Path<IExplorableState> abstractTestSeqToPath(AbstractTestSequence tc, GameWorldModel model) {
		model.reset(); 
		model.unsafelyIgnoreTransitionCondition = true ;
		Path<IExplorableState> sigma = new Path<>() ;
		var seq = abstractTestSeqToGWTransitionSeq(tc) ;
		//System.out.println("### " + seq) ;
		sigma.addInitialState(model.getCurrentState());
		for (GWTransition step : seq) {
			//System.out.println(">>>" + step) ;
			model.execute(step);
			sigma.addTransition(step, model.getCurrentState());
		}
		model.unsafelyIgnoreTransitionCondition = false ;
		return sigma ;
	}
	
	public static GoalStructure abstractTestSeqToGoalStructure(
			TestAgent agent,
			AbstractTestSequence tc, 
			GameWorldModel model) {
		
		var path = abstractTestSeqToPath(tc,model) ;
		
		var G = MiniDungeonModel.convertToGoalStructure(agent,path) ;
		
		return G ;
	}

}
