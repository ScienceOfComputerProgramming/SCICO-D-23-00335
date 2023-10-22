## Plugin: connecting to a game

We use the term 'plugin' to refers to an interface that would allow an external test program to control a game under test (**GUT**) and observe its state. In addition to this we will also need a so-called _concretization function_ that translates abstract test cases generated from a model to be executed on the GUT. Concretization will be discussed separately. Here, we will discuss the plugin part.

The execute test cases PX-MBT uses test agents from the [aplib framework](https://github.com/iv4xr-project/aplib).
The framework is an agent programming framework with focus on game testing. Aplib offers concepts such as _goal structures_ and _tactics_ that make programming game testing automation easier. However, using aplib means that we have to build this plugin in a way that aplib agents can exploit. We will here give an overview of the main building blocks of such a plugin, and will refer the readers to the documentation of  aplib for more detailed examples.

* In aplib, the interface with the GUT is called an _environment_. It is implemented by extending (subclassing) aplib class called [`Environment`](https://github.com/iv4xr-project/aplib/blob/master/src/main/java/nl/uu/cs/aplib/mainConcepts/Environment.java). For a more sophisticated interface, you can choose to extend [`Iv4xrEnvironment`](https://github.com/iv4xr-project/aplib/blob/master/src/main/java/eu/iv4xr/framework/mainConcepts/Iv4xrEnvironment.java) instead, which is also a subclass of `Environment`. The main functionalities to implement are a method to send a command to the GUT and a method to observe its state (and return an observation). The commands send over an Environment are typically primitive commands that the GUT immediately understand, e.g. to move some small distance in a certain direction, or to interact with a nearby game object.
If you choose to extend `Iv4xrEnvironment`, observations are expected to be given in terms of so-called _World Object Model_ (WOM); see [the class WorldModel](https://github.com/iv4xr-project/aplib/blob/master/src/main/java/eu/iv4xr/framework/mainConcepts/WorldModel.java).

* To let an aplib agent do something, we give it a _goal_. To do something more complicated, we can give it a _goal structure_ instead, which consists of subgoals. A goal formulates a state that the agent seeks to establish. The logic/program to achieve a goal is called a _tactic_. In addition to an Enviroment mentioned above, you would want to provide a library of basic goals and tactics. These could be:

   * `at(e)` : we can imagine this to be a goal that is solved when the agent is standing next to an entity `e`. This will need the corresponding tactic, which then need to do path planning, exploration (if `e` is not immediately visible), and possibly also figthing monsters along the way. Aplib provides basic path planning algorithms such as A*, and a programming model that makes the programming of a tactic easier.

   * `interacted(e)` : we can imagine this to be a goal that is solved when the entity `e` is interacted. The tactic to solve this goal would have to guide the agent in performing the interaction. In most cases this can be done with a single interaction with the GUT.

Basic goals like `at(e)` and `interacted(e)` are the building blocks for the concretization function we mentioned before, which is needed to convert abstract test cases from the model to actual executions.

Implementing Environment is usually not difficult. However, providing basic goals and tactics, such as the aforementioned `at(e)` and `interacted(e)` can take quite some effort. However, it is a one off investment. Once we have them, we can keep using them.

##### Further readings

* General documentation on aplib [can be found in its Github](https://github.com/iv4xr-project/aplib). From there you can also find the paper that describes aplib's programming approach.
* [Minimalistic example of defining Environment and goals](https://github.com/iv4xr-project/aplib/blob/master/docs/iv4xr/testagent_tutorial_2.md). This doc is available in aplib.

##### Some examples of plugin projects

* The plugin for the game [MiniDungeon](https://github.com/iv4xr-project/MiniDungeon) is provided in aplib itself, by the following classes: class [MyAgentEnv](https://github.com/iv4xr-project/aplib/blob/master/src/main/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/testAgent/MyAgentEnv.java), [TacticLib](https://github.com/iv4xr-project/aplib/blob/master/src/main/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/testAgent/TacticLib.java),
[GoalLib](https://github.com/iv4xr-project/aplib/blob/master/src/main/java/nl/uu/cs/aplib/exampleUsages/miniDungeon/testAgent/GoalLib.java)
* The plugin for a 3D game called Lab Recruits; [it is a whole project](https://github.com/iv4xr-project/iv4xrDemo).
* [For the game NetHack](https://github.com/iv4xr-project/iv4xr-nethack), it is a part of this project.
* [The plugin for a commercial 3D game Space Enginners](https://github.com/iv4xr-project/iv4xrDemo-space-engineers) (a whole project). If you don't know the game, [check it in YouTube](https://www.youtube.com/watch?v=HAchGUF1RhI) 😀.
