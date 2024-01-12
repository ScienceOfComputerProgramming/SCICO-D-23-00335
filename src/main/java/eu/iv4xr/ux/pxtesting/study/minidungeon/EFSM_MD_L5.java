package eu.iv4xr.ux.pxtesting.study.minidungeon;

import java.io.IOException;

import eu.fbk.iv4xr.mbt.efsm.EFSM;
import eu.fbk.iv4xr.mbt.efsm.EFSMProvider;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.GameWorldModel;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.MiniDungeon.MiniDungeonConfig;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MiniDungeonModel;

/**
 * Provide an EFSM of a level-5 MiniDungeon.
 * 
 * @author Wish
 */
public class EFSM_MD_L5 implements EFSMProvider{
	
	String mainplayer = "Frodo" ;
	
	public GameWorldModel loadGameWorldModel() throws IOException {
		// L10 is too difficult for the MBT
		// GameWorldModel gwmodel = GameWorldModel.loadGameWorldModelFromFile( "./assets/MD_L10.json") ;
		GameWorldModel gwmodel = GameWorldModel.loadGameWorldModelFromFile( "./assets/MD_L5.json") ;
		gwmodel.alpha = (i,affected) -> S -> { MiniDungeonModel.alphaFunction(mainplayer,i,affected,S) ; return null ; } ;
		gwmodel.additionalInteractionGuard = (i,S) -> MiniDungeonModel.interactionGuard(mainplayer, i, S) ;
		return gwmodel ;
	}
	
	EFSM efsm = null ;
	
	/** 
	 * Get the MD-config used to generate the EFSM MD_L5
	 */
	public MiniDungeonConfig getConfig_MD_L5() {
		MiniDungeonConfig config = new MiniDungeonConfig();
		config.numberOfHealPots = 4;
		config.viewDistance = 4;
		config.enableSmeagol = false ;
		config.numberOfMonsters = 6 ;
		//config.worldSize = 40 ;
		//config.numberOfCorridors = 7 ;
		//config.numberOfMaze = 10 ;
		config.numberOfMaze = 5 ;
		
		config.randomSeed = 1393;	
		return config ;
		// System.out.println(">>> Configuration:\n" + config);
	}
	
	/** 
	 * Get the MD-config used to generate the EFSM MD_L10
	 */
	public MiniDungeonConfig getConfig_MD_L10() {
		MiniDungeonConfig config = new MiniDungeonConfig();
		config.numberOfHealPots = 4;
		config.viewDistance = 4;
		config.enableSmeagol = false ;
		config.numberOfMonsters = 6 ;
		//config.worldSize = 40 ;
		//config.numberOfCorridors = 7 ;
		config.numberOfMaze = 10 ;
		//config.numberOfMaze = 5 ;
		
		config.randomSeed = 1393;	
		return config ;
		// System.out.println(">>> Configuration:\n" + config);
	}
	
	@Override
	// The scheme that FBK-MBT uses to load an EFSM :)
	public EFSM getModel() {
		if (efsm != null) 
			return efsm ;
		
		try {
		GameWorldModel model = loadGameWorldModel() ;
		// convert the above GameWorldModel to FBK-EFSM:
		efsm = MD_FBK_EFSM_Utils.mkEFSM_from_MDModel(model) ;
		//System.out.println(">>> efsm is not null: " + !(efsm == null)) ;
		return efsm ; 
		}
		catch(Exception e) {
			throw new IllegalArgumentException() ;
		}
	}
	
}
