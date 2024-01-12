package eu.iv4xr.ux.pxtesting.mbt;

import java.util.*;
import java.util.stream.Collectors;

import eu.fbk.iv4xr.mbt.efsm.*;
import eu.fbk.iv4xr.mbt.efsm.exp.*;
import eu.fbk.iv4xr.mbt.efsm.exp.bool.*;
import eu.fbk.iv4xr.mbt.efsm.exp.integer.*;
import eu.fbk.iv4xr.mbt.efsm.labRecruits.ButtonDoors1;

/**
 * Experimental, for future work.
 * 
 * <p>Provide a translator from EFSM models from eu.fbk.iv4xr.mbt.efsm to Prism models. This allows
 * the model to be extended in Prism, to model the probabilities of taking different transitions,
 * and then analyze the resulting probabilistic model in Prism.
 */
public class EFSM2Prism {
	
	static String bracket(String p) {
		return "(" + p + ")" ; 
	}
	
	static String prime(String x) {
		return x + "'" ;
	}
	
	static String line(String s) {
		return s + "\n" ;
	}
	
	static String indent(int k, String s) {
		String z = "" ;
		while(k>0) {
			z += " " ; k-- ;
		}
		return z + s ;
	}
	
	static String concat(List<String> xs) {
		StringBuffer buf = new StringBuffer() ;
		for(String s : xs) buf.append(s) ;
		return buf.toString() ;
	}
	
	static List<String> indent(int k, List<String> xs) {
		return xs.stream().map(s -> indent(k,s)).collect(Collectors.toList()) ;
	}
	
	static List<String> withTerminator(List<String> xs, String terminator) {
		return xs.stream().map(s -> s + terminator).collect(Collectors.toList()) ;
	}
	
	
	
	static String withSeparator(List<String> xs, String sep) {
		String out = "" ;
		int k = 0 ;
		for (var z : xs) {
			if (k>0) out += sep ;
			out += z ;
			k++ ;
		}
		return out ;
	}
	
	static String translateExpr(Exp e) {
		if (e instanceof Const) {
			
			return ((Const) e).getVal().toString();
		}
		if (e instanceof Var ) {
			return ((Var) e).getId() ;
		}
		if (e instanceof BoolNot ) {
			return "!" + bracket(translateExpr(( (BoolNot)e).getParameter())) ;
		}
		if (e instanceof BoolAnd) {
			return bracket(translateExpr(( (BoolAnd)e).getParameter1()))
			+ " & "
			+ bracket(translateExpr(( (BoolAnd)e).getParameter2())) ;
		}
		if (e instanceof BoolOr ) {
			return bracket(translateExpr(( (BoolOr)e).getParameter1()))
			+ " | "
			+ bracket(translateExpr(( (BoolOr)e).getParameter2())) ;
		}
		if (e instanceof BoolEq ) {
			return bracket(translateExpr(( (BoolEq)e).getParameter1()))
			+ " = "
			+ bracket(translateExpr(( (BoolEq)e).getParameter2())) ;
		}
		if (e instanceof IntSum ) {
			return bracket(translateExpr(( (IntSum)e).getParameter1()))
			+ " + "
			+ bracket(translateExpr(( (IntSum)e).getParameter2())) ;
		}
		if (e instanceof IntSubt) {
			return bracket(translateExpr(( (IntSubt)e).getParameter1()))
			+ " - "
			+ bracket(translateExpr(( (IntSubt)e).getParameter2())) ;
		}
		if (e instanceof IntGreat ) {
			return bracket(translateExpr(( (IntGreat)e).getParameter1()))
			+ " > "
			+ bracket(translateExpr(( (IntGreat)e).getParameter2())) ;
		}
		if (e instanceof IntEq ) {
			return bracket(translateExpr(((IntEq)e).getParameter1()))
			+ " = "
			+ bracket(translateExpr(((IntEq)e).getParameter2())) ;
		}
		throw new UnsupportedOperationException("Cannot translate " + e) ;	
	}
	
	static String translateAssignment(Assign asg) {
		String s = prime(translateExpr(asg.getVariable())) + " = " + translateExpr(asg.getExpression()) ;
		return bracket(s) ;		
	}
	
	static String translateAssignment(AssignSet asgs) {
		var asgs_ = 
		    asgs.getHash().values().stream()
		      .map(asg_ -> translateAssignment((Assign) asg_)) 
		      .collect(Collectors.toList());
		   
		return withSeparator((List<String>) asgs_, " & ") ;
	}
	
	static final String mystate_ = "mystate_" ;
		
	
	static String translateTransition(EFSMTransition tr) {
		String guard = mystate_ + "=" + tr.getSrc().getId() ;
		var g = tr.getGuard() ;
		if(g != null) {
			String guard2 = translateExpr(g.getGuard()) ;
			guard = bracket(guard) + " & " + bracket(guard2) ;
		}
		String action = bracket(prime(mystate_) + "=" + tr.getTgt().getId()) ;
		var a = tr.getOp() ;
		if(a!=null) {
			action = action + " & " + translateAssignment(a.getAssignments()) ;
		}
 		return "[] " + guard + " -> " + action ;
	}
		
	
	/**
	 * Translate the transitions in the given EFSM. This will translate each transition
	 * in the EFSM to its own Prims-transition.
	 */
	private static String translateTransitionsInEFSM(EFSM efsm)  {
		Set transitions = efsm.getTransitons() ;
		List<String> lines = new LinkedList<>() ;
		for(var tr_ : transitions) {
			EFSMTransition tr = (EFSMTransition) tr_ ;
			String trStr = translateTransition(tr) ;
			lines.add(trStr) ;
		}
		lines = indent(4, withTerminator(lines, " ;\n")) ;
		return concat(lines) ;
	}
	
	/**
	 * Translate for the Probabilistic-mode.
	 * 
	 * <p>Let s be a source (abstract) state in the EFSM. Let V be all arrows that leave s. For now
	 * we assume that at most one of the transition in V, say t, is guarded by some g, the rest, V',
	 * not. We map V to two Prism-transitions: (1) representing t, and (2) representing V/{t}, 
	 * guarded by ~g. If V' has two or more transitions, the choice is made probabilistic, set with
	 * equal chance of 1/|V'|.
	 */
	private static String translateTransitionsIn_LR_EFSM(EFSM efsm)  {
		Set transitions = efsm.getTransitons() ;
		Map<String,List<EFSMTransition>> transitionsMap = new HashMap<>() ;
		for(var tr_ : transitions) {
			EFSMTransition tr = (EFSMTransition) tr_ ;
			String srcId = tr.getSrc().getId() ;
			var trs = transitionsMap.get(srcId) ;
			if(trs == null) {
				trs = new LinkedList<EFSMTransition>() ;
				transitionsMap.put(srcId,trs) ;
			}
			trs.add(tr) ;
		}
		
		List<String> lines = new LinkedList<>() ;
		
		for(var trGroup : transitionsMap.entrySet()) {
			String srcState = trGroup.getKey() ;
			List<EFSMTransition> outgoings = trGroup.getValue() ;
			
			if(outgoings.isEmpty()) continue ;
			
			EFSMTransition guarded = null ;
			for(var tr : outgoings) {
				if(tr.getGuard() != null) {
					guarded = tr ;
					break ;
				}
			}
				
			// translate the guarded scenario:
			if (guarded != null) {
				String guard_ = "" + mystate_ + "=" + srcState ;
				guard_ = bracket(guard_) + " & " + bracket(translateExpr(guarded.getGuard().getGuard())) ;
				guard_ = "[] " + guard_ + " ->" ;
				lines.add(guard_) ;
				var assignments = indent(4,combineAndTranslateTheAsssignments(outgoings)) ;
				lines.addAll(assignments) ;
				// remove the guarded-tr from the outgoing:
				outgoings.remove(guarded) ;
			}
			
			// translate the negation of the guarded scenario:
			if(outgoings.size() > 0) {
				String otherguard = "" + mystate_ + "=" + srcState ;
				if(guarded != null) {
					otherguard = bracket(otherguard) + " & " + "!" + bracket(translateExpr(guarded.getGuard().getGuard())) ;
				}
				otherguard = "[] " +  otherguard + " -> " ;
				lines.add(otherguard) ;
				var assignments = indent(4,combineAndTranslateTheAsssignments(outgoings)) ;
				lines.addAll(assignments) ;
			}	
		}
		lines = indent(4,lines);
		lines = withTerminator(lines,"\n") ;
		return concat(lines) ;		
	}
	
	/**
	 * Combine the assignments of a bunch of transitions originating from the same state-node
	 * to form a combined probabilistic arm of a Prim-transition.
	 * 
	 * <p>So e.g. [a1,a2,a3] is combined to 1/3:a1 + 1/3:a2 + 1/3:a3.
	 */
	private static List<String> combineAndTranslateTheAsssignments(List<EFSMTransition> outgoingTransitions) {
		List<String> lines = new LinkedList<>() ;
		int N = outgoingTransitions.size() ;		
		int k = 0 ;
		for(EFSMTransition tr : outgoingTransitions) {
			String action = bracket(prime(mystate_) + "=" + tr.getTgt().getId()) ;
			var a = tr.getOp() ;
			if(a!=null) {
				action = action + " & " + translateAssignment(a.getAssignments()) ;
			}
			action = "1/" + N + ": " + action ;
			if(k>0) {
				action = "+ " + action ;
			}
			if(k==N-1) {
				action += " ;" ;
			}
			lines.add(action) ;
			k++ ;
		}
		return lines ;
	}
	
	public enum PrismModelType { mdp, dtmc }
	public enum TranslationMode { ND, Probabilistic }

	/**
	 * Translates an EFSM model from eu.fbk.iv4xr.mbt.efsm to a Prism model. The resulting model
	 * is returned as a string, which subsequently can be saved e.g. in a file. 
	 * 
	 * <p>Two translation modes is provided:
	 * 
	 *    <ol>
	 *    <li> TranslationMode.ND (non-deterministic): this translates each transition in the input EFSM to
	 *    a Prism transition. This produces a non-deterministic Prism model, which is also non-probabilistic.
	 *    
	 *    <li> TranslationMode.Probabilistic: this groups outgoing transitions in the input EFSM into two 
	 *    groups. Let s be an (abstract) state in the EFSM. Let T be the set of all transitions that leaves
	 *    the state s. For now we assume that at most one of the transitions in T is guarded. We split T
	 *    in two groups: the single guarded transition {g->a}, if we have one, and T' = T/{g->a}.
	 *    
	 *    <p>We translate g->a to a single transition in Prism. We merge the transitions in T' into a group
	 *    of probabilistic transitions, guarded by !g. The probability of each transition u in T' is set
	 *    to be 1/|T'|. You can always adjust this in the resulting Prism model.1
	 *    
	 *    <p>This mode of translation produces a probabilistic model (so called DTMC).
	 *    </ol>
	 * 
	 * @param efsm  The EFSM to translate.
	 * @param moduleName  The EFSM will be translated to a module-structure in Prism. 
	 *                    This parameter specifies the name to be given to the module.
	 * @param modeltype   Either dtmc or mdp. Currently only dtmc should be used.
	 * @param translationMode See above.
	 * @return
	 */
	public static String translateEFSM(EFSM efsm,
			String moduleName, 
			
			TranslationMode translationMode) {
		StringBuilder out = new StringBuilder() ;
		// Specifying Prims-model-type:
		PrismModelType modeltype = PrismModelType.dtmc ;
		if (translationMode == TranslationMode.ND) 
			modeltype = PrismModelType.mdp ;
		out.append(line(modeltype.toString())) ;
		out.append(line("")) ;
		// state-names declarations
		out.append(line("// fsm-state-ids abbreviations:")) ;
		int stateId = 0 ;
		var states = efsm.getStates() ;
		for(var st_ : states) {
			EFSMState st = (EFSMState) st_ ;
			out.append(line("const int " + st.getId() + " = " + stateId + " ;")) ;
			stateId ++ ;
		}
		int lastStateId = stateId - 1 ;
		out.append(line("")) ;
		// declaring state-cursor as a global var:
		out.append(line("global " + mystate_ 
				+ " : [0.." + lastStateId + "] init "  // range of possible my-states
				+ efsm.getInitialConfiguration().getState().getId()
				+ " ;")) ; // initial state
		out.append(line("")) ;
		
        // module name:
		out.append(line("module " + moduleName)) ;
		out.append(line("")) ;
		
		// variables of the efsm:
		out.append(line(indent(4,"// efsm's variables:"))) ;
		Collection variables = efsm.getInitialConfiguration().getContext().getContext().getHash().values() ;
		List<String> vars = new LinkedList<>() ;
		for(var x_ : variables) {
			Var x = (Var) x_ ;
			String xstr = x.getId() + " : ";
			if(x.getValue() instanceof Integer) {
				xstr += "int" ;
			}
			else if (x.getValue() instanceof Boolean) {
				xstr += "bool" ;
			}
			xstr += " init " + x.getValue().toString() ;  // the var's initial value
			vars.add(xstr) ;
		}
		vars = indent(4,withTerminator(vars," ;\n")) ;
		out.append(concat(vars)) ;
		out.append(line("")) ;
		
		// re-arrange the transitions by its source-state:
		
		
		// translating the transitions:
		out.append(line(indent(4,"// the transitions:"))) ;
		switch(translationMode) {
		   case ND : out.append(translateTransitionsInEFSM(efsm)) ; break;
		   case Probabilistic : out.append(translateTransitionsIn_LR_EFSM(efsm)) ;
		}
		out.append(line("")) ;
		// module-end:
		out.append(line("endmodule")) ;
		return out.toString() ;
	}
	
	
	// just for testing the translator
	public static void main(String[] args) {
		// get an EFSM model:
		EFSM example = new ButtonDoors1().getModel() ;
		// Translate to Prism model:
		String prismModel = translateEFSM(example, "buttonDoors1", TranslationMode.Probabilistic) ;
		System.out.println(prismModel) ;
		
	}
		

}
