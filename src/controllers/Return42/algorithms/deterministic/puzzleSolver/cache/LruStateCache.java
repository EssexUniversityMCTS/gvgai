package controllers.Return42.algorithms.deterministic.puzzleSolver.cache;

import core.game.StateObservation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import controllers.Return42.algorithms.deterministic.puzzleSolver.AStarNode;

/**
 * Created by Oliver on 03.05.2015.
 */
public class LruStateCache implements StateCache {

    private final Map<AStarNode, StateObservation> cache;
    private final Queue<AStarNode> insertionOrder;
    private final Runtime runtime;
    
    private boolean isMemoryMaxedOut;
	private int cacheSize;

    public LruStateCache() {
        this.cache = new HashMap<>();
        this.runtime = Runtime.getRuntime();
        insertionOrder = new LinkedList<>();
        cacheSize = 1000;
        isMemoryMaxedOut = false;
    }


    @Override
    public void storeState(AStarNode node, StateObservation state) {
        cache.put(node, state);
        insertionOrder.add(node);

        if (insertionOrder.size() > cacheSize) {
        	handleCacheFull();
        }
    }

    private void handleCacheFull() {
    	if (!isMemoryMaxedOut && canIncreaseCacheSize()) {
    		cacheSize += 100;
    	} else {
    		isMemoryMaxedOut = true;
            AStarNode entryToDelete = insertionOrder.poll();
            cache.remove( entryToDelete );
    	}
	}

	private boolean canIncreaseCacheSize() {
    	long usedMemory = runtime.totalMemory() - runtime.freeMemory();
    	return usedMemory < 0.5 * runtime.maxMemory();
	}


	@Override
    public boolean containsStateForNode(AStarNode node) {
        return cache.containsKey( node );
    }

    @Override
    public StateObservation getState(AStarNode node) {
        if (cache.containsKey( node )) {
            StateObservation state = cache.get( node );

            return state;
        } else {
            return null;
        }
    }
}
