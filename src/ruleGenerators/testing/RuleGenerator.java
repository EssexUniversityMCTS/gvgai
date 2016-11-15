package ruleGenerators.testing;

import core.game.SLDescription;
import core.generator.AbstractRuleGenerator;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class RuleGenerator extends AbstractRuleGenerator{

    public RuleGenerator(SLDescription sl, ElapsedCpuTimer time){
	
    }
    
    @Override
    public String[][] generateRules(SLDescription sl, ElapsedCpuTimer time) {
	sl.testRules(new String[]{
		"sprite_1  EOS  > stepBack",
	        "sprite_2   EOS  > turnAround",
	        "sprite_1 EOS  > killSprite"}, 
		new String[]{"SpriteCounter stype=sprite_1 limit=0 win=False",
	                "MultiSpriteCounter stype1=sprite_2 stype2=sprite_3 limit=0 win=True"}).advance(ACTIONS.ACTION_LEFT);;
	return new String[][]{new String[]{
		"sprite_1  EOS  > stepBack",
	        "sprite_2   EOS  > turnAround",
	        "sprite_1 EOS  > killSprite"}, 
	    new String[]{"SpriteCounter      stype=sprite_1               limit=0 win=False",
	                "MultiSpriteCounter stype1=sprite_2 stype2=sprite_3 limit=0 win=True"}};
    }

}
