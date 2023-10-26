## Concretization

Assuming you have read:
  * [_Plugin: connecting to a game_](plugin.md)

Test cases generated from a model is _abstract_. In particular, they cannot be executed as is on the game under test (**GUT**).
To execute an abstract test case we need a so-called _concretization function_, which can turn the abstract test case into concrete interactions with the GUT. This concretization function can be non-trivial to build. Since PX-MBT makes use of aplib agents to control the GUT, the concretization function is expected to produce so-called _goal structures_ to be executed by an aplib agent, which in turn will translate goal structures to primitive interactions with the GUT.
To explain how this works, we will use an example.

We will take the game [MiniDungeon discussed here](MD_L5.md) as the example. The class class [MyAgentEnv](https://github.com/iv4xr-project/aplib/blob/master/src/main/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/testAgent/MyAgentEnv.java), extends `Iv4xrEnvironment`, provides the interface between aplib agents and the game. Its main methods are:

   * `WorldModel observe(String agentId)`: it returns an observation on the current game state, e.g. the current location and health of the player character, and game objects that are visible to the player. Observation is structured generically as an instance of [`WorldModel`](https://github.com/iv4xr-project/aplib/blob/master/src/main/java/eu/iv4xr/framework/mainConcepts/WorldModel.java).

   * `WorldModel action(String agentId, Command cmd)`: this executes a command on the game. The command is primitive, e.g. to move-up and move-down, or use-item (of an item in the player's bag).

Any program (no necessarily an aplib agent) can use this interface to control a running instance of MiniDungeon. To provide a higher level control to aplib agents, we also the following classes [TacticLib](https://github.com/iv4xr-project/aplib/blob/master/src/main/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/testAgent/TacticLib.java) and
[GoalLib](https://github.com/iv4xr-project/aplib/blob/master/src/main/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/testAgent/GoalLib.java).

### GoalLib

To do its work, an aplib agent needs to be given a so-called _goal_. Goals can be structured to form _goal structures_. A goal is essentially a pair of (p,T) where p is a predicate specifying a state that the agent should establish, and T is a so-called _tactic_ that the agent then executes in order to achieve p. An example of a goal structure is `SEQ(G1,G2,G3)`, specifying a goal that is achieved by achieving the sub-goals G1, G2, and G3, in that order. Another example is `FIRSTof(G1,G2)` that is achieved if either G1 or G2 is achieved, which are attempted in that specified order.

The class `GoalLib` of MiniDungeon provides a set of common goal structures. They represent common tasks that the agent knows how to do them:

   * `entityInCloseRange(id)`: when executed will bring the agent to a location next to an in-game object with the given id. The tactic behind this goal structure uses a path finding algorithm to guide itself to the object. If the object has not been seen yet, the tactic will cause the agent to explore the current maze until it sees the object.
   * `entityInteracted(id)`: when executed will interact with the game object with the given id, provided the agent is located next to the object. E.g. interacting with a potion will cause the potion to be picked up and put in the player's bag, if it has space left.

### TacticLib

The actual work horse of aplib agents are the tactics. A simple tactic could call one of the basic actions provided by `MyAgenEnv` mentioned above. This might work as the tactic for the goal `entityInteracted()`, but `entityInCloseRange()` would require a whole series of actions to be executed, and some logic to be built into the tactic as well. To make tactics reactive (able to switch behavior e.g. when at the next turn a monster attack), they have a different programming model than the traditional procedural model. We are not going into that here; the readers are recommended to check out [aplib github site](https://github.com/iv4xr-project/aplib), that have documentations on this and a paper that explains tactics and goals.

Anyway, to illustrate, the main tactic used for `entityInteracted()` is this:

```java
FIRSTof(tacticLib.useHealingPotAction()
           .on_(tacticLib.hasHealPot_and_HpLow)
           .lift(),
        tacticLib.useRagePotAction()
           .on_(tacticLib.hasRagePot_and_inCombat)
           .lift(),
        tacticLib.attackMonsterAction()
           .on_(tacticLib.inCombat_and_hpNotCritical)
           .lift(),
        tacticLib.navigateToTac(targetId),  // (1)
        tacticLib.explore(null),            // (2)
        ABORT())                            // (3)
```

The `FIRSTof` means that it will execute the first sub-tactic that is currently enabled. E.g. it could be to use the navigation sub-tactic (1), that will guide the agent to the target game object. Else, if navigation is not enabled (e.g. because the object has not been seen), we fall back to exploration sub-tactic (2). If the agent also has not area left to explore, we fall back to ABORT (3), that will then declare the current goal as unachievable. The example above also includes _survival_ subtactic, which takes precedence over navigation and exploration. E.g. the sub-tactic to attack a monster, if its condition is enabled (e.g. that there is a monster next to the agent). The code of the sub-tactics are not shown, but they can be found in the class [`TacticLib`](https://github.com/iv4xr-project/aplib/blob/master/src/main/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/testAgent/TacticLib.java).

### Finally ... how to do concretization?

The concretization function can be found in the class [`MiniDungeonModel`](https://github.com/iv4xr-project/aplib/blob/master/src/main/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/testAgent/MiniDungeonModel.java). It is implemented in the method `GoalStructure convertToGoalStructure(agent,sequence)` where `sequence` is an abstract test case. An abstract test case for MiniDungeon is essentially just a sequence of _travel_ and _interaction_ transitions. A travel transition represents a physical travel from the location of one object to another. An interaction transition is as the name says 😀. The concretization function essentially translate a _travel_ transition to the goal `entityInCloseRange(id)`, and an _interact_ transition to the goal `entityInteracted(id)`, and that's it!
