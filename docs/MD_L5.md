## Quick Example

Run the method `test_generate_and_exec`. This will generate test cases from a model and execute them on a game under test. The generated test cases, and the resulting emotion traces are placen in `./tmp` :

```
mvn test -Dtest="eu.iv4xr.ux.pxtesting.minidungeon.Test_MD_MBT_Exec#test_generate_and_exec"
```

## So, what did we just do....?

The game under test is a game called [MiniDungeon](https://github.com/iv4xr-project/MiniDungeon). It is a simple game where the player has to go through a number of mazes to get to the 'final shrine' located in the final maze. To get there, the player has to figure out how to open the access from a maze to the next one. Along the way, of course there will be monsters that will try to hurt the player.

A model of a five-level instance of MiniDungeon is included. A visualization of the model is shown below.

| a maze in MiniDungeon | a model of a 5-mazes level |
|---|---|
| ![MiniDungeon](./ss1.png) |  ![MD_L5 model](./MD_L5.png) |



The model of each maze is shown in blue. Each maze is connected to the next one through a shrine that acts as a teleport (brown). The shrines are initially closed. Red lines indicate which in-game object need to brought to a shrine to make it open.

#### Generating test cases from a model

You can invoke the method `test1()` using Maven as shown below, from the project's root. It will generate abstract test cases from the model shown above

```
mvn test -Dtest="eu.iv4xr.ux.pxtesting.minidungeon.Test_MD_MBT_Gen#test1"
```
The generated test cases are put in `./tmp`:

  * `tc.ser` : a test-case in binary form.
  * `tc.txt` : a text-representation of the test case.
  * `tc.dot` : a DOT-file containing a visualization of the the test case. See [here](https://graphviz.org/) to get a DOT-visualizer app.

Code snippet that call the generator:

```java
var gen = new TestSuiteGenerator("eu.iv4xr.ux.pxtesting.minidungeon.EFSM_MD_L5") ;
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

Full source code of the method `test1()`: [Test_MD_MBT_Gen](../src/test/java/eu/iv4xr/ux/pxtesting/minidungeon/Test_MD_MBT_Gen.java)

#### Executing test cases

The method `test_generate_and_exec()` will generate some test cases from the model, and immediately execute them:

```
mvn test -Dtest="eu.iv4xr.ux.pxtesting.minidungeon.Test_MD_MBT_Exec#test_generate_and_exec"
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

Full source code of the method `test_generate_and_exec()`: [Test_MD_MBT_Exec](../src/test/java/eu/iv4xr/ux/pxtesting/minidungeon/Test_MD_MBT_Exec.java). You can also check the method `test_load_and_exec()` that loads a previously generated test cases from files and execute them.

#### Emotion traces and verifying PX properties

In the above runs, the test agent was equipped with an emotion module, so the runs produced emotion traces. Each trace is a sequence of emotion state of the agent, sampled at each update cycle of the agent. The produces traces were put in `./tmp`.

We can verify PX properties on these traces. In PX-MBT we can express a PX property using a pattern. For example the pattern `H;nF;H` specifies an execution/trace where eventually the intensity of the emotion hope rises, and then after some time the we have the second rise in hope, while between the first and the second rises in hope, the intensity of fear never rises. This can be verified using the method  `checkAll`(_pattern_,_separator_,_dir_,_prefix_), where _separator_ is the used separator in the trace-files (comma), _dir_ is the directory where the traces are located, and _prefix_ is a prefix of their names, as a means to filter them.

As a demo you can run this:

```
mvn test -Dtest="eu.iv4xr.ux.pxtesting.minidungeon.Test_MD_MBT_Exec#test_verify_pattern"
```

which performs the following test:

```Java
@Test
public void test_verify_pattern() throws IOException {
		var result = EmotionPattern.checkAll("H;nF;H", ',', "./tmp","tc") ;
		System.out.println(">>> " + result);
		assertTrue(! result.valid() && result.sat()) ;
	}
```

For more documentation about emotion patterns and other means of PX analyses (e.g by producing time graphs), see [here](analyses.md).

#### Components

If you want to know what were the main components to have PX Testing working on the game MiniDungeon, you first need to read [few other documentations](../README.md#docs), at least up to the section about _"running test cases"_. The section that specifically explains the main components is [here](./preppx.md).

These main components need to be prepared/implemented, before we can have PX testing running. These components are then passed as parameters, in one form or another, to the constructor of `PXTestAgentRunner`. For MiniDungeon, these components are:

   1. A class defining events that matter for PX evaluation: [`MiniDungeonEventsProducer`.](../src/main/java/eu/iv4xr/ux/pxtesting/study/minidungeon/MiniDungeonEventsProducer.java)
   1. [A class defining a Player Characterizaiton.](../src/main/java/eu/iv4xr/ux/pxtesting/study/minidungeon/MiniDungeonPlayerCharacterization.java)
   1. A concretization function. [The most essential part of this concretization is here, in the function `convertToGoalStructure()`](https://github.com/iv4xr-project/aplib/blob/master/src/main/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/testAgent/MiniDungeonModel.java), which provides a translation from an EFSM abstract test case to an aplib-agent goal structure. This concretization is borrowed from the project aplib.
