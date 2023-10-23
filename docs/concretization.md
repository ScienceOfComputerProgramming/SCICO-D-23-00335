## Concretization

Assuming you have read:
  * [_Plugin: connecting to a game_](plugin.md)

Test cases generated from a model is _abstract_. In particular, they cannot be executed as is on the game under test (**GUT**).
To execute an abstract test case we need a so-called _concretization function_, which can turn the abstract test case into concrete interactions with the GUT. This concretization function can be non-trivial to build. Since PX-MBT makes use of aplib agents to control the GUT, the concretization function is expected to produce so-called _goal structures_ to be executed by an aplib agent, which in turn will translate goal structures to primitive interactions with the GUT.
To explain how this works, we will use an example.

We will take the game [MiniDungeon discussed here](MD_L5.md) as the example.
