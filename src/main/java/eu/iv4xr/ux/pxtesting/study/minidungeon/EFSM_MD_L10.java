package eu.iv4xr.ux.pxtesting.study.minidungeon;

import java.io.IOException;

import eu.fbk.iv4xr.mbt.efsm.EFSM;
import eu.fbk.iv4xr.mbt.efsm.EFSMProvider;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.GameWorldModel;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MiniDungeonModel;

public class EFSM_MD_L10 implements EFSMProvider{
	
	String mainplayer = "Frodo" ;
	
	GameWorldModel loadGameWorldModel() throws IOException {
		// well... L10 is too difficult for the MBT
		GameWorldModel gwmodel = GameWorldModel.loadGameWorldModelFromFile( "./assets/MD_L10.json") ;
		//GameWorldModel gwmodel = GameWorldModel.loadGameWorldModelFromFile( "./assets/MD_L10.json") ;
		gwmodel.alpha = (i,affected) -> S -> { MiniDungeonModel.alphaFunction(mainplayer,i,affected,S) ; return null ; } ;
		gwmodel.additionalInteractionGuard = (i,S) -> MiniDungeonModel.interactionGuard(mainplayer, i, S) ;
		return gwmodel ;
	}
	
	EFSM efsm = null ;
	
	
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
