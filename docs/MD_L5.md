## Quick Example

We will use a game called [MiniDungeon](https://github.com/iv4xr-project/MiniDungeon). It is a simple game where the player has to go through a number of mazes to get to the 'final shrine' located in the final maze. To get there, the player has to figure out how to open the access from a maze to the next one. Along the way, of course there will be monsters that will try to hurt the player.

A model of a five-level instance of MiniDungeon is included. A visualization of the model is shown below.

![MD_L5 model](./MD_L5.png)

The model of each maze is shown in blue. Each maze is connected to the next one through a shrine that acts as a teleport (brown). The shrines are initially closed. Red lines indicate which in-game object need to brought to a shrine to make it open.

#### Generating test cases from a model

You can invoke the method `test1()` using Maven as shown below, from the project's root. It will generate abstract test cases from the model shown above

```
mvn test -Dtest="eu.iv4xr.ux.pxtesting.study.minidungeon.Test_MD_MBT_Gen#test1"
```
The generated test cases are put in `./tmp`:

  * `tc.ser` : a test-case in binary form.
  * `tc.txt` : a text-representation of the test case.
  * `tc.dot` : a DOT-file containing a visualization of the the test case. See [here](https://graphviz.org/) to get a DOT-visualizer app.

Code snippet that call the generator:

```java
var gen = new TestSuiteGenerator("eu.iv4xr.ux.pxtesting.study.minidungeon.EFSM_MD_L5") ;
gen.idFinalState = "SI4" ;
// generate using MOSA:
gen.generateWithSBT(120,null) ;
gen.printStats();
// generate using a model checker (the produced test suite will be added to the one previously generated)
gen.generateWithMC(false, true, false, 80);
gen.printStats();
// apply sampling to select a subset of 20 test cases:
gen.applySampling(8,20);
gen.printStats();
// save the resulting test cases in files:
gen.save("./tmp","tc");
```

Full source code of the method `test1()`: [Test_MD_MBT_Gen](../src/test/java/eu/iv4xr/ux/pxtesting/study/minidungeon/Test_MD_MBT_Gen.java)

#### Executing test cases

The method `test_generate_and_exec()` will generate some test cases from the model, and immediately execute them:

```
mvn test -Dtest="eu.iv4xr.ux.pxtesting.study.minidungeon.Test_MD_MBT_Exec#test_generate_and_exec"
```

Code snippet that call the executor/runner:

```java
EmotiveTestAgent deployTestAgent() {
   // launch an instance of MiniDungeon, then a test agent to it:
   DungeonApp app = deployApp() ;
   var agent = new EmotiveTestAgent("Frodo","Frodo") ;
   agent. attachState(new MyAgentState())
        . attachEnvironment(new MyAgentEnv(app))  ;
   return agent ;
}

run(testsuite) {
   PXTestAgentRunner runner = new PXTestAgentRunner(
      dummy -> deployTestAgent(),
      new MiniDungeonPlayerCharacterization(),
      new MiniDungeonEventsProducer(),
      // test-case concretizer:
      agent -> tc -> MD_FBK_EFSM_Utils.abstractTestSeqToGoalStructure(agent, tc, gwmodel),
      null,
      mentalGoal_cleanseShrine) ;

   runner.run_(testsuite, "./tmp", 8000, 0);  
}
```

Full source code of the method `test_generate_and_exec()`: [Test_MD_MBT_Exec](../src/test/java/eu/iv4xr/ux/pxtesting/study/minidungeon/Test_MD_MBT_Exec.java). You can also check the method `test_load_and_exec()` that loads a previously generated test cases from files and execute them.
