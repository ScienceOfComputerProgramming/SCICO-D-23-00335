## Running emotive test agent

This assumes you have read [the documentation on how on preparation steps towards running an agent](preppx.md).

The easiest way to run an emotive agent is by running it through an [PX-agent runner](../src/main/java/eu/iv4xr/ux/pxtesting/PXTestAgentRunner.java). This runner will configure the agent. For example, an instance of the JOCC model of emotion will be created and hooked to the agent.

Once we have a runner, then given a bunch of abstract test cases (e.g. [generated from an EFSM model](efsm.md)), we can do `runner.run(testsuite,...)` to run the test cases on the actual game under test.

The constructor:


```java
PXTestAgentRunner(Function<Void,EmotiveTestAgent> agentConstructor,
   XUserCharacterization playerCharacterization, // input-2
   SyntheticEventsProducer eventsProducer, // input-1
   Function<EmotiveTestAgent,Function<AbstractTestSequence,GoalStructure>> concretizationFunction,  // input-3
   Function<EmotiveTestAgent,Function<SimpleState,Pair<String,Number>[]>> customStateInstrumenter, // input-4
   Pair<Goal,Integer> ... goals )
```

Parameters marked as [input-1 .. input-4 were explained before](preppx.md). The parameter `goals` specify which playing goals are to be added for the purpose of emotion simulation. E.g. it could be the goal to win the game. We will come back to this later.

#### The parameter `agentConstructor`

It is a function that creates an instance of aplib's `EmotiveTestAgent`. Additionally, the agent is expected to already equipped with a state and holds a reference to an instance of `Environment` that interfaces it with the game under test. Here is an example of this construction function for the game MiniDungeon:

```java
var agentConstructor = dummy -> {
   // creating an instance of MiniDungeon
   DungeonApp app = new DungeonApp(new MiniDungeonConfig());
   // creating an Env that interfaces with that instance of MD:
   var env = new MyAgentEnv(app) ;
   // create an emotive-agent:
   var agent = new EmotiveTestAgent("Frodo","Frodo") ;
   // attach the env and a fresh state to the agent:
   agent. attachState(new MyAgentState())
        . attachEnvironment(env)  ;
   return agent ;  
}
```

#### The parameter `goals`

It is just one or more goals. This is probably a bit vague 😀. What is meant by a goal here? Aplib also has a concept of 'goal' and 'goal-structure', but that is not what is meant here. Technically a goal here is an instance of JOCC class `Goal`. It can be constructed with a constructor `Goal(name,sig)` where `name` is just a string-name of the goal, to identify it, and `sig` is a value in [0..1] representing the significance of the goal.

Now, the runner (PXTestAgentRunner) expects actually one or more pairs of the form (G,i) where is an instance of JOCC `Goal`, and `i` is the initial precieved likelihood of eventually achieving this goal `G`.

An example of creating such a pair:

```java
Pair<Goal,Integer> finishingCurrentMaze = new Pair<>(
    new Goal("A shrine is cleansed.").withSignificance(0.8),
    50) ;
```

#### Running the test agent

After creating the runner, we can invoke it through methods such as:

   * `run(testsuite, saveDir, ...)`: this will translate a bunch of abstract test-case to goal-structures (concretization), which will be executed by the test agent. Generated traces will be saved in the specified `saveDir`.
   * `run(testsuiteDir, saveDir, ...)`: this will load saved abstract test-cases from `testsuiteDir`, then run them as above.

As an example, see the test-class [`Test_MD_MBT_Exec`](../src/test/java/eu/iv4xr/ux/pxtesting/study/minidungeon/Test_MD_MBT_Exec.java).
