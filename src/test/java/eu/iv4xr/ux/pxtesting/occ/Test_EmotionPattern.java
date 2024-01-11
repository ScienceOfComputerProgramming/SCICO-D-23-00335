package eu.iv4xr.ux.pxtesting.occ;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.extensions.ltl.SATVerdict;

import static eu.iv4xr.ux.pxtesting.occ.EmotionPattern.* ;

public class Test_EmotionPattern {
	
	@Test
	public void test_basic_patterns() throws IOException {
		String file = "./src/test/test0.csv" ;
		assertEquals(SATVerdict.SAT, checkOne("H",',',file)) ;
		assertEquals(SATVerdict.UNSAT, checkOne("P",',',file)) ;
    	assertEquals(SATVerdict.SAT, checkOne("F",',',file)) ;
    	assertEquals(SATVerdict.SAT, checkOne("J",',',file)) ;
	}
	
	@Test
	public void test_seq_patterns() throws IOException {
		String file = "./src/test/test0.csv" ;
    	assertEquals(SATVerdict.SAT, checkOne("NH;F",',',file)) ;
    	assertEquals(SATVerdict.SAT, checkOne("H;J;S",',',file)) ;
    	assertEquals(SATVerdict.UNSAT, checkOne("NJ;S",',',file)) ;
    	assertEquals(SATVerdict.SAT, checkOne("J;NJ;S",',',file)) ;
	}
	
	@Test
	public void test_implicative_patterns() throws IOException {
		String file = "./src/test/test0.csv" ;
    	assertEquals(SATVerdict.SAT, checkOne("H->F",',',file)) ;
    	assertEquals(SATVerdict.SAT, checkOne("P->F",',',file)) ;
    	assertEquals(SATVerdict.SAT, checkOne("H;J;S->F",',',file)) ;
    	assertEquals(SATVerdict.UNSAT, checkOne("H;J;S->P",',',file)) ;
	}
	
	
	@Test
	public void test_checkall() throws IOException {
     	String folder = "./src/test" ;
     	var R = checkAll("H",',',folder,"tc") ;
     	assertTrue(R.valid() && R.sat() && !R.inconclusive() && !R.unsat()) ;
     	assertTrue(R.satN(3)) ;
     	System.out.println(">>> R+" + R) ;
     	R = checkAll("S",',',folder,"tc") ;
     	assertTrue(!R.valid() && !R.sat() && !R.inconclusive() && R.unsat()) ;
     	assertTrue(R.satN(0)) ;
     	System.out.println(">>> R+" + R) ;
	}

}
