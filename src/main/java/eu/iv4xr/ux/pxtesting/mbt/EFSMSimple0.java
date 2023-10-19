package eu.iv4xr.ux.pxtesting.mbt;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.evosuite.shaded.org.apache.commons.lang3.SerializationUtils;

import eu.fbk.iv4xr.mbt.efsm.EFSM;
import eu.fbk.iv4xr.mbt.efsm.EFSMBuilder;
import eu.fbk.iv4xr.mbt.efsm.EFSMContext;
import eu.fbk.iv4xr.mbt.efsm.EFSMDotExporter;
import eu.fbk.iv4xr.mbt.efsm.EFSMFactory;
import eu.fbk.iv4xr.mbt.efsm.EFSMGuard;
import eu.fbk.iv4xr.mbt.efsm.EFSMOperation;
import eu.fbk.iv4xr.mbt.efsm.EFSMParameter;
import eu.fbk.iv4xr.mbt.efsm.EFSMProvider;
import eu.fbk.iv4xr.mbt.efsm.EFSMState;
import eu.fbk.iv4xr.mbt.efsm.EFSMTransition;
import eu.fbk.iv4xr.mbt.efsm.exp.Assign;
import eu.fbk.iv4xr.mbt.efsm.exp.Var;
import eu.fbk.iv4xr.mbt.efsm.exp.bool.BoolNot;
import eu.fbk.iv4xr.mbt.efsm.labRecruits.LRActions;
import eu.fbk.iv4xr.mbt.efsm.labRecruits.LRParameterGenerator;

/**
 * An example of a very simple EFSM. It should implement the interface
 * {@link HaveEFSM} that provides the method {@link HaveEFSM#getModel()},
 * which the MBT test generator uses to obtain the EFSM.
 */
public class EFSMSimple0 implements EFSMProvider {
	
	public EFSM efsm ;
	
	@Override
	public EFSM getModel() {
		return efsm ;
	}
	
	/**
	 * Construct an EFSM modeling a game setup with three rooms:
	 * R0, R1, and R2, connected sequentially: R0 is connected to
	 * R1, which is connected to R2. Door0 connects R0 and R1,
	 * and door1 connects R1 and R2. All doors are initially closed.
	 * 
	 * <p>There are four buttons: b0 and b1 are in the center room R1.
	 * Button b2 is in R0, and b3 is in R2. Button b0 toggles the state
	 * of door0. Button b2 toggles the state of door1. Other buttons
	 * have no effect to any door.
	 * 
	 * <p>The start location/state is b0.
	 * 
	 */
	public EFSMSimple0() {
		
		EFSMBuilder efsmBuilder = new EFSMBuilder(EFSM.class);
		
		// creating EFSM abstract states, we have 8:
		EFSMState b0 = new EFSMState("b0");
		EFSMState b1 = new EFSMState("b1");
		EFSMState b2 = new EFSMState("b2");
		EFSMState b3 = new EFSMState("b3");
		EFSMState d0m = new EFSMState("d0m");
		EFSMState d0p = new EFSMState("d0p");
		EFSMState d1m = new EFSMState("d1m");
		EFSMState d1p = new EFSMState("d1p");
		
		// EFSM's context variables; two vars to represent the state of
		// the two doors we have (open or close).
		Var<Boolean> door0 = new Var<Boolean>("door0", false);
		Var<Boolean> door1 = new Var<Boolean>("door1", false);
		EFSMContext efsmContext = new EFSMContext(door0,door1);

		// transition-labels, we have two label: "explore" (or travel)
		// and "toggle" :
		Var exploreVar = new Var<LRActions>("action", LRActions.EXPLORE);
		EFSMParameter trLabelTravel = new EFSMParameter(exploreVar);
		Var toggleVar = new Var<LRActions>("action", LRActions.TOGGLE);
		EFSMParameter trLabelToggle = new EFSMParameter(toggleVar);
			
		// creating transitions, we have four for toggling b0..b3:
		addToggleTransition(efsmBuilder,b0,trLabelToggle,door0) ;
		addToggleTransition(efsmBuilder,b1,trLabelToggle,null) ;
		addToggleTransition(efsmBuilder,b2,trLabelToggle,door1) ;
		addToggleTransition(efsmBuilder,b3,trLabelToggle,null) ;
		
		// and a bunch of travel-transitions:
		addTravelTransitions(efsmBuilder,b0,b1,trLabelTravel,null) ;
		addTravelTransitions(efsmBuilder,b0,d1m,trLabelTravel,null) ;
		addTravelTransitions(efsmBuilder,b1,d0m,trLabelTravel,null) ;
		addTravelTransitions(efsmBuilder,b2,d0p,trLabelTravel,null) ;
		addTravelTransitions(efsmBuilder,b3,d1p,trLabelTravel,null) ;	
		addTravelTransitions(efsmBuilder,d0m,d0p,trLabelTravel,door0) ;
		addTravelTransitions(efsmBuilder,d1m,d1p,trLabelTravel,door1) ;
		
		// now construct the whole efsm:
		LRParameterGenerator lrParameterGenerator = new LRParameterGenerator();
		EFSMState startingAbsState = b0 ;
		this.efsm = efsmBuilder.build(startingAbsState, efsmContext, lrParameterGenerator);	
	}
	
	
	private static void addToggleTransition(EFSMBuilder efsmBuilder, 
			EFSMState button,
			EFSMParameter transitionLabel,
			Var<Boolean> affectedDoor) {
		EFSMTransition toggle = new EFSMTransition();
		toggle.setInParameter(transitionLabel);
		toggle.setId("TOG" + button.getId());
		if (affectedDoor != null) {
			Assign<Boolean> flipDoor = new Assign(affectedDoor, new BoolNot(affectedDoor));
			toggle.setOp(new EFSMOperation(flipDoor));
		}
		efsmBuilder.withTransition(button, button, toggle) ;
	}
	
	private static void addTravelTransitions(EFSMBuilder efsmBuilder, 
			EFSMState src,
			EFSMState dest,
			EFSMParameter transitionLabel,
			Var<Boolean> passedDoor) {
		
		EFSMTransition travel1 = new EFSMTransition();
		travel1.setInParameter(transitionLabel);
		travel1.setId("t_" + src.getId() + "_" + dest.getId());
		if (passedDoor != null) {
			travel1.setGuard(new EFSMGuard(passedDoor)) ;
		}
		efsmBuilder.withTransition(src,dest,travel1) ;
		EFSMTransition travel2 = new EFSMTransition();
		travel2.setInParameter(transitionLabel);
		travel2.setId("t_" + dest.getId() + "_" + src.getId());
		if (passedDoor != null) {
			travel2.setGuard(new EFSMGuard(passedDoor)) ;
		}
		efsmBuilder.withTransition(dest,src,travel2) ;	
	}
	
	public static void main(String[] args) throws IOException {
		
		EFSMSimple0 efsm = new EFSMSimple0() ;
		efsm.getModel() ;
		
		String modelFileName = "./tmp" + File.separator + "EFSM_model.ser";
		String modelDotFileName = "./tmp" + File.separator + "EFSM_model.dot";
		FileUtils.writeByteArrayToFile(new File(modelFileName), 
				// old MBT 1.1.0b code, upgrading it; it should return a byte-code
				// serialization of the current EFSM:
				// EFSMFactory.getInstance().getOriginalEFSM()
				SerializationUtils.serialize(efsm.getModel())
				);
		var dotExporter = new EFSMDotExporter(efsm.getModel()) ;
				
		dotExporter.writeOut(Paths.get(modelDotFileName));
		

	}

}
