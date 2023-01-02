# OCC4PXTesting 

## Running steps:


### Step 1:
 Run "SBtest_generation.java" (in "src\test\java\eu\iv4xr\ux\pxtestingPipeline\SBtest_generation.java")  (No input parameters needed if the directory of input are scripted)
	
To create the level, its properties are congigured in setPropertiesMBT(), then the level and efsm model and the corresponding testsuite with the goal-coverage criteria  are generated.
			
* preset input: preset configuration of the wave the flag level like number of buttons, door, goal flag and number of rooms in the level.
* output: EFSm model, level.csv and the SBT based test suite will be stored in "Parent directory" (outside the project directory)  +  "SBTtest" folder.
			  
### Step 2:
 Run  "MCtest_generation.java"  (in "src\test\java\eu\iv4xr\ux\pxtestingPipeline\MCtest_generation.java") to create mc based test suite for the same level.   (No input parameters needed if the directory of input are scripted)
	      
* output: EFSm model, level.csv and the SBT based generated test suite will be stored in "Parent directory" + MCtest folder.
		  
### Step 3:
Run "model-basedpxtesting.java"  (No parameters needed if the directory of input are scripted)
		
This loads all test cases of both test suite (SB and MC) to run an gent with emotional model to create the emotion traces by performing actions given in the test cases.
		input: 
		
* test suites  in parent directory of the project + combinedtest folder     ##for simplisity instead of moving test suites, everything can be stored in combined when test suite are generated.
* level name t and the efsm model folder to be assinged to the game configuration called LabRecruitsConfig.
		
		 ```Java
		         LabRecruitsConfig lrCfg = new LabRecruitsConfig(Levelname, modelFolder);

		  ```
* output: emotion traces as "data_goalQuestCompleted_"+ test.getKey()+".csv  inside project directory.
		
### Step 4:
 Run python -- "mkHeatmaps.py" (main directory of the project)Produce aggregated heatmaps of emotion traces for every emotion type.    (has input parameters)
This script gets input parameters as 
		
* "path"  : the path to emotion traces (results of step 3)
* width  the game level width which should be 100.
* height the game level height which needs to be 70.
			
This can be given as input or hard coded in the script. it changed as a hard code now with out the need to set paramters to run.
* Output: 6 heatmaps in total, one for every emotion type.
		
### Step 5:
Run "Emotioncoverage.Java" from ecoverage project "\src\main\java\eu\iv4xr\ux\pxtestingPipeline\Emotion coverage.Java"  (No parameters needed if the directory of input are scripted)
		
* input: path to emotion traces, string patterns you want to check    
* output: Test result as True/False status for SAt(P) or UNSAT(P)
		
		