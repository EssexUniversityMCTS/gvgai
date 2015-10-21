package controllers.Return42.algorithms.deterministic.puzzleSolver;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ontology.Types;
import ontology.Types.ACTIONS;

public class AStarUtils {

    public static List<Types.ACTIONS> extractActions( AStarNode node ) {
        List<Types.ACTIONS> result = new LinkedList<>();

        while (node.getPrev() != null) {
        	ACTIONS[] actionsForStep = node.getLastActions();
        	
        	for( int i = actionsForStep.length -1; i >= 0; i-- ) {
                result.add( actionsForStep[i] );
        	}

        	node = node.getPrev();
        }

        Collections.reverse(result);
        return result;
    }
	
}
