####################################
Java files with Main() entry points:
####################################

 - **tracks.singlePlayer.Test.java** This is the main test file to run single player games in the vgdl framework. It uses methods of the class ArcadeMachine.java. In particular, you can execute the framework in the following ways:
     1. Play a game/level as a human (ArcadeMachine.playOneGame(...)).
     2. Play a game/level with a controller (ArcadeMachine.runOneGame(...)).
     3. Replay a game/level from an action file, obtained in a previous run (ArcadeMachine.replayGame(...)).
     4. Play a game in N levels, M times each (ArcadeMachine.runGames(...)). This execution mode provides statistical information in the games played.
     5. Play a generated level for a certain game that was generated before.

 - **tracks.multiPlayer.TestMultiPlayer.java** This is the main test file to run multi player games in the vgdl framework, using methods from the class ArcadeMachine.java. The following methods could be used:
     1. Play a game/level with two human players (ArcadeMachine.playOneGame(...)).
     2. Play a game/level with two controllers (ArcadeMachine.runOneGame(...)). Note: one of the controllers can be human; if this is the case, the ID of the human player has to be passed to the runOneGame method as the last argument (0 if first player, 1 if second).
     3. Replay a game/level from an action file, obtained in a previous run (ArcadeMachine.replayGame(...)).
     4. Play a game in N levels, M times each (ArcadeMachine.runGames(...)). Statistical information can be obtained from this method.
 
 - **tracks.levelGeneration.TestLevelGeneration.java** 
     1. Generate a level for a certain game using certain level generator, output saved in a file.
     2. Generates X levels with a generator which are saved in certain output files.

 - **tracks.ruleGeneration.TestRuleGeneration.java** 
     1. Generates rules for a game given a fixed level.


##################
Code organization:
##################


This code is organized in Java packages. At the root, these packages are:

 - _core_: Main code for the GVGAI engine, including aspects like game creation, vgdl and competition settings. It is subdivided into other packages:
     - _competition_: Contains some parameters for runs in the competition.
     - _content_: Contains classes involved in creating sprites (entities) for the games.
     - _game_: Classes for the game being played, game description class, forward model and observations of the current state.
     - _generator_: Contains the class that all the generators must inherit from AbstractLevelGenerator.java.
     - _player_: Contains the classes that all agents must inherit from, AbstractPlayer.java (single player agents) and AbstractMultiPlayer.java (multi player agents).
     - _termination_: Classes for game termination conditions, according to VGDL game definitions.
     - _vgdl_: Contains classes for parsing VGDL games, such as VGDLParser.java, VGDLFactory.java and VGDLRegistry.java. It also contains the base class of all existing sprites (VGDLSprite.java) and the class in charge of the graphics (VGDLViewer.java).
 - _ontology_: Ontology for VGDL, which defines all possible types of sprites, effects and physics that can be used.
 - _tools_: Extra classes needed for running the engine and competition.
 - _tracks_: Code for all competition tracks. Each subpackage includes the Main files to run controllers and generators.


The code provided to create controllers and generators is divided into Java packages. There is a package for each competition track, under the package _tracks_:

 - _gameDesign_: This package contains some experimental code for game parameterization.
 - _levelGeneration_: It contains the main Java file for level generation (**TestLevelGeneration.java**) and some sample generators [Sample Level Generators](https://github.com/EssexUniversityMCTS/gvgai/wiki/Creating-Level-Generators) distributed with the starter kit. Each package contains one level generator, and the main file that contains its implementation is called LevelGenerator.java. This package also contains package for different constraints checking.
 - _multiPlayer_: It contains a main entry Java file (**TestMultiPlayer.java**) and sample controllers ([Sample Controllers](https://github.com/EssexUniversityMCTS/gvgai/wiki/Creating-Multi-Player-Controllers)).
 - _ruleGeneration_: It contains the main Java file for rule generation (**TestRuleGeneration.java**), along with some sample Rule Generators.
 - _singlePlayer_: It contains a main entry Java file (**Test.java**) and sample controllers ([Sample Controllers] (https://github.com/EssexUniversityMCTS/gvgai/wiki/Creating-Controllers)).
