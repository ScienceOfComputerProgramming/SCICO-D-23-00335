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
