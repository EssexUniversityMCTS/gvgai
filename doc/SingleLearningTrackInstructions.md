# Instructions for GVGAI Single-Player Learning track

## Useful links
[GVGAI competition](http://www.gvgai.net/)

[GVGAI framework](https://github.com/EssexUniversityMCTS/gvgai)

[GVGAI wiki](https://github.com/EssexUniversityMCTS/gvgai/wiki) (planning tracks and level generation track)

**Contact:** Jialin Liu, University of Essex, UK

Email: *jialin.liu@essex.ac.uk* or *jialin.liu.cn@gmail.com*

## Overview
The Single-Player Learning track is based on the GVGAI framework. Different from the planning tracks, no forward model is given to the agent, thus, no simulation of games is possible. The agent does still have access to the current game state (objects in the current game state), as in the planning tracks.

## Main steps
For a given game, each agent will have **5 minutes** for training on levels 0,1,2 of the game, then levels 3 and 4 will be used for validation. The communication time is not included by Timer.
### Main steps during training
1. **Training phase 1:** Playing once levels 0, 1 and 2 in a sequence: Firstly, the agent plays once levels 0,1,2 sequentially. At the end of each level, whether the game has terminated normally or the agent forces to terminate the game, the server will send the results of the (possibly unfinished) game to the agent.
2. **Training phase 2:** (Repeat until time up) Level selection: After having finished step 1, the agent is free to select the next level to play (from levels 0, 1 and 2) by calling the method `int result()` (detailed later). If the selected level id $$$\not\in \\{0,1,2\\}$$$, then a random level id $$$\in \\{0,1,2\\}$$$ will be passed and a new game will start. This step is repeated until **5 minutes** has been used.

In case that 5 minutes has been used up, the results and observation of the game will still be sent to the agent and the agent will have no more than **1 second** before the validation.

### Main steps during validation
During the validation, the agent plays once levels 3 and 4 sequentially.

*Remark: Playing each level once or several times is to be decided.*

## Methods to be implemented and time control
This section details the methods to be implemented in Agent.
A sample random agent is given in the framework.
### Constructor of the agent class
    public Agent(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer){...}
The constructor receives two parameters:

* `SerializableStateObservation sso`: The `StateObservation` is the observation of the current state of the game, which can be used in deciding the next action to take by the agent (see [doc for planning track](https://github.com/EssexUniversityMCTS/gvgai/wiki/Creating-Controllers) for detailed information). The `SerializableStateObservation` is the serialised `StateObservation` **without forward model**, which is a `String`.
* `ElapsedCpuTimer elapsedTimer`: The `ElapsedCpuTimer` is a class that allows querying for the remaining CPU time the agent has to return an action. You can query for the number of milliseconds passed since the method was called (`elapsedMillis()`) or the remaining time until the timer runs out (`remainingTimeMillis()`).
The constructor has **1 second**. If `remainingTimeMillis()` ≤ 0, this agent is **disqualified** in the game being played.

### Initialise the agent
    public Types.ACTIONS init(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer){...}
The `init` method is called once after the constructor, before selecting any action to play. It receives two parameters:

* `SerializableStateObservation sso`.
* `ElapsedCpuTimer elapsedTimer`: (see previous section) The `act` has to finish in **40ms**, otherwise, the `NIL_ACTION` will be played.

### Select an action to play
    public Types.ACTIONS act(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer){...}
The `act` method selects an action to play at every game tick. It receives two parameters:

* `SerializableStateObservation sso`.
* `ElapsedCpuTimer elapsedTimer`: The timer with maximal time **40ms** for the whole training. The `act` has to finish in **40 ms**, otherwise, this agent is **disqualified** in the game being played.

### Abort the current game
The agent can abort the current game by returning the action `ACTION_ESCAPE`. The agent will receive the results and state observation `sso` of the unfinished game and returns the next level to play using the method `int result(sso)`.

### Select the next level to play
    public int result(SerializableStateObservation sso, ElapsedCpuTimer elapsedTimer) {...}
During the step 2 of training, after terminating a game and receiving the results and final game state, the agent is supposed to select the next level to play. If the return level id $$$\not\in \\{0,1,2\\}$$$, then a random level id $$$\in \\{0,1,2\\}$$$ will be passed and a new game will start. The `result` method receives two parameters:

* `SerializableStateObservation sso`: the serialised observation of final game stat at termination.
* `ElapsedCpuTimer elapsedTimer`: The global timer with maximal time 10 mins for the whole training. If there is no time left (`remainingTimeMillis()` ≤ 0), an extract timer with maximal time=1 second will be passed.

## Structure of framework
The framework can be downloaded [here](https://github.com/EssexUniversityMCTS/gvgai) (master). There are two main packages to work on:

* `gvgai/clients/GVGAI-JavaClient`: This should be imported as a separate project. It contains the all the necessary classes for the client, including the communication and a sample random agent. You can create an agent for the Single-Player Learning Track of the GVG-AI competition just by creating a Java class that inherits from `utils.AbstractPlayer.java`. This class must be named `Agent.java` and its package must be the same as the username you used to register on the website (this is in order to allow the server to run your controller when you submit). 
During the testing phase, the package can be located in `gvgai/clients/GVGAI-JavaClient/src/agents`.
* `gvgai/src/tracks/SingleLearning`: This package contains `LearningMachine.java`, a test `TestSingleLearning.java`, `ServerComm.java` and the necessary .sh/.bat files for communication.

## How to test
### Step 1: (Optional) Set the port for communication
If you don't want to use the default setting with port 8000, please edit `TestSingleLearning.java`.
### Step 2: Set your agent
If you want to use your own agent, you can pass the name of agent as a parameter to `JavaClient.java`. Otherwise, the sample random agent `gvgai/clients/GVGAI-JavaClient/src/agents/random/Agent.java` will be used.
### Step 3: Launch the client
Run `JavaClient.java` of project `gvgai/clients/GVGAI-JavaClient`.
### Step 4: Launch the server
Run `TestSingleLearning.java` in `gvgai/src/tracks/SingleLearning`.

## Possible issues and actions
* You may need to edit `gvgai/src/tracks/SingleLearning/runClient_nocompile.bat` to specify the path to the sdk.
