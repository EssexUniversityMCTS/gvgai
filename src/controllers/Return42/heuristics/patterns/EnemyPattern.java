package controllers.Return42.heuristics.patterns;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Vector2d;

import java.util.HashMap;
import java.util.List;

import controllers.Return42.GameStateCache;
import controllers.Return42.util.Util;

public class EnemyPattern implements Pattern {

    private Observation first;
    private Observation second;

    private Types.ACTIONS actionToTake = Types.ACTIONS.ACTION_NIL;

    private boolean debug = false;
    private boolean canKillEnemies = true;

    public EnemyPattern() {
        actDirMap.put(Types.ACTIONS.ACTION_DOWN, new Vector2d(0, 1));
        actDirMap.put(Types.ACTIONS.ACTION_UP, new Vector2d(0, -1));
        actDirMap.put(Types.ACTIONS.ACTION_LEFT, new Vector2d(-1, 0));
        actDirMap.put(Types.ACTIONS.ACTION_RIGHT, new Vector2d(1, 0));
    }

    @Override
    public boolean appliesToGame(GameStateCache state) {
        return state.getAvailableActions().size() > 4 && state.getNPCPositions() != null;
    }

    @Override
    public double applies(GameStateCache state) {
        if(!canKillEnemies) {
            return -1;
        }
        List<Observation>[][] map = state.getState().getObservationGrid();
        first = null;
        second = null;
        List<Observation>[] npcs = state.getNPCPositions();
        if(npcs == null) {
            return -1;
        }
        for(List<Observation> l : npcs) {
            if(l.size() > 0) {
                Observation next = l.get(0);
                if(first == null) {
                    first = next;
                    if(l.size() > 1) {
                        next = l.get(1);
                        if(second == null) {
                            second = next;
                        } else if(second.sqDist > next.sqDist) {
                            first = next;
                        }
                    }
                } else if(first.sqDist > next.sqDist) {
                    first = next;
                    if(l.size() > 1) {
                        next = l.get(1);
                        if(second == null) {
                            second = next;
                        } else if(second.sqDist > next.sqDist) {
                            first = next;
                        }
                    }
                } else if(second == null) {
                    second = next;
                } else if(second.sqDist > next.sqDist) {
                    second = next;
                }
            }
        }
        if(first != null) {
            Vector2d dirF = new Vector2d(first.position).subtract(state.getAvatarPosition()).mul(1 / state.getBlockSize());
            double mag = dirF.mag();
            dirF.normalise();
            if(mag == 2 || mag == 1) {
                if(second != null) {
                    Vector2d dirS = new Vector2d(second.position).subtract(state.getAvatarPosition()).mul(1/state.getBlockSize());
                    if(dirS.mag() < 3) {
                        if(debug) System.out.println("Two enemies close...");
                        dirS.normalise();
                        if(dirF.x >= 0 && dirS.x >= 0) {
                            if(canMoveto(map, state, Types.ACTIONS.ACTION_LEFT))
                            actionToTake = Types.ACTIONS.ACTION_LEFT;
                        } else if(dirF.x <= 0 && dirS.x <= 0) {
                            if(canMoveto(map, state, Types.ACTIONS.ACTION_RIGHT))
                            actionToTake = Types.ACTIONS.ACTION_RIGHT;
                        } else if(dirF.y >= 0 && dirS.y >= 0) {
                            if(canMoveto(map, state, Types.ACTIONS.ACTION_UP))
                            actionToTake = Types.ACTIONS.ACTION_UP;
                        } else if(dirF.y <= 0 && dirS.y <= 0) {
                            if(canMoveto(map, state, Types.ACTIONS.ACTION_DOWN))
                            actionToTake = Types.ACTIONS.ACTION_DOWN;
                        } else {
                            if(debug) System.out.println("No clear way out. Pray!");
                            return 1; //enemy too close;
                        }
                        if(debug) System.out.println(".. but found a way out");
                        return -1;
                    }
                }

                if(dirF.equals(state.getState().getAvatarOrientation())) {
                    if(canMoveto(map, state, state.getState().getAvatarOrientation())) {
                        if(debug) System.out.println("Trying to squash it");
                        actionToTake = Types.ACTIONS.ACTION_USE;
                        if(mag == 1) {
                            checkKillable(state);
                        }
                    }
                } else if(mag == 2) {
                    if(debug) System.out.println("Not facing correctly. Turning around");
                    if(dirF.x > 0) {
                        if(canMoveto(map, state, Types.ACTIONS.ACTION_RIGHT))
                        actionToTake = Types.ACTIONS.ACTION_RIGHT;
                    } else if(dirF.x < 0) {
                        if(canMoveto(map, state, Types.ACTIONS.ACTION_LEFT))
                        actionToTake = Types.ACTIONS.ACTION_LEFT;
                    } else if(dirF.y > 0) {
                        if(canMoveto(map, state, Types.ACTIONS.ACTION_DOWN))
                        actionToTake = Types.ACTIONS.ACTION_DOWN;
                    } else if(dirF.y < 0) {
                        if(canMoveto(map, state, Types.ACTIONS.ACTION_UP))
                        actionToTake = Types.ACTIONS.ACTION_UP;
                    }
                } else {
                    if(debug) System.out.println("Fucker got close behind us. RUN!");
                    if(dirF.x > 0) {
                        if(canMoveto(map, state, Types.ACTIONS.ACTION_LEFT))
                        actionToTake = Types.ACTIONS.ACTION_LEFT;
                    } else if(dirF.x < 0) {
                        if(canMoveto(map, state, Types.ACTIONS.ACTION_RIGHT))
                        actionToTake = Types.ACTIONS.ACTION_RIGHT;
                    } else if(dirF.y > 0) {
                        if(canMoveto(map, state, Types.ACTIONS.ACTION_UP))
                        actionToTake = Types.ACTIONS.ACTION_UP;
                    } else if(dirF.y < 0) {
                        if(canMoveto(map, state, Types.ACTIONS.ACTION_DOWN))
                        actionToTake = Types.ACTIONS.ACTION_DOWN;
                    }
                }
                return 1;
            } else if(mag > 2) {
                if(debug) System.out.println("Going for a hunt");
                if(dirF.x > 0) {
                    if(canMoveto(map, state, Types.ACTIONS.ACTION_RIGHT))
                    actionToTake = Types.ACTIONS.ACTION_RIGHT;
                } else if(dirF.x < 0) {
                    if(canMoveto(map, state, Types.ACTIONS.ACTION_LEFT))
                    actionToTake = Types.ACTIONS.ACTION_LEFT;
                } else if(dirF.y > 0) {
                    if(canMoveto(map, state, Types.ACTIONS.ACTION_DOWN))
                    actionToTake = Types.ACTIONS.ACTION_DOWN;
                } else if(dirF.y < 0) {
                    if(canMoveto(map, state, Types.ACTIONS.ACTION_UP))
                    actionToTake = Types.ACTIONS.ACTION_UP;
                }
                return 0;
            } else {
                if(debug) System.out.println("Fucker is diagonal. RUN! " + mag);
                if(dirF.x > 0) {
                    if(canMoveto(map, state, Types.ACTIONS.ACTION_LEFT))
                    actionToTake = Types.ACTIONS.ACTION_LEFT;
                } else if(dirF.x < 0) {
                    if(canMoveto(map, state, Types.ACTIONS.ACTION_RIGHT))
                    actionToTake = Types.ACTIONS.ACTION_RIGHT;
                } else if(dirF.y > 0) {
                    if(canMoveto(map, state, Types.ACTIONS.ACTION_UP))
                    actionToTake = Types.ACTIONS.ACTION_UP;
                } else if(dirF.y < 0) {
                    if(canMoveto(map, state, Types.ACTIONS.ACTION_DOWN))
                    actionToTake = Types.ACTIONS.ACTION_DOWN;
                }
                return 0.5;
            }
        }
        return -1;
    }

    @Override
    public Types.ACTIONS getAction() {
        return actionToTake;
    }

    HashMap<Types.ACTIONS, Vector2d> actDirMap = new HashMap<>();


	public boolean canMoveto(List<Observation>[][] map, GameStateCache state, Types.ACTIONS action) {
		return canMoveto(map, state, actDirMap.get(action));
	}

	public boolean canMoveto(List<Observation>[][] map, GameStateCache state, Vector2d dir) {
		Vector2d pp = Util.gamePositionToGridPosition(state.getState(), state.getAvatarPosition());
		Vector2d newPosition = new Vector2d(pp.x + dir.x, pp.y + dir.y);
		if (Util.isGridPositionValid(state.getState(), newPosition)) {
			List<Observation> dest = map[(int) (newPosition.x)][(int) (newPosition.y)];
			for (Observation o : dest) {
				if (o.category == Types.TYPE_STATIC || o.category == Types.TYPE_PORTAL || o.category == Types.TYPE_MOVABLE) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

    private void checkKillable(GameStateCache state) {
        List<Observation>[] npcs = state.getNPCPositions();
        int amount = 0;
        for(List<Observation> o : npcs) {
            amount += o.size();
        }
        StateObservation next = state.getState().copy();
        next.advance(actionToTake);

        npcs = next.getNPCPositions();
        int namount = 0;
        for(List<Observation> o : npcs) {
            namount += o.size();
        }

        if(namount == amount) {
            //TODO usable for knowledge base / controller
            System.out.println("CANNOT KILL ENEMIES. FLEE!");
            canKillEnemies = false;
        }
    }
}
