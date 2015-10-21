package controllers.alxio;

import core.game.Observation;
import core.game.StateObservation;
import java.util.ArrayList;
import ontology.Types;
import tools.Vector2d;

/**
 *
 * @author ALX
 */
public class Z {
    public static long hash(StateObservation so){
        return instance.internalHash(so);
    }
    
    public static void init(int x, int y){
        instance = new Z(new MersenneTwisterFast(), x, y);
    }
    
    private Z(MersenneTwisterFast r, int x, int y){
        this.x = x;
        this.y = y;
        this.random = r;
        orientationsHashes = new long[orientations.length];
        for(int i=0;i<orientationsHashes.length;++i){
            orientationsHashes[i] = random.nextLong();
        }
    }
    
    private static Z instance = null;
    
    private Vector2d[] orientations = {Types.UP, Types.LEFT, Types.RIGHT, Types.DOWN, Types.NIL, Types.NONE};
    private long[] orientationsHashes;
    
    private long internalHash(StateObservation so){
        long hash = Double.doubleToLongBits(so.getGameScore());
//        Vector2d ori = so.getAvatarOrientation();
//        for(int i=0;i<orientations.length;++i){
//            if(orientations[i].equals(ori)){
//                hash^=orientationsHashes[i];
//                break;
//            }
//        }
        //so.getFromAvatarSpritesPositions();

        ArrayList<Observation>[][] grid = so.getObservationGrid();
        if(grid.length != y || grid[0].length != x){
            throw new RuntimeException("Bad array size");
        }
      
        double radius = (2 + Strategus.SIGTH_RADIUS * Math.max(y, x)) * so.getBlockSize();
        
        for(int i=0;i<y;++i){
            for(int j=0;j<x;++j){
                ArrayList<Observation>obs = grid[i][j];
                for(Observation o : obs){
                    initType(o.itype);
                    if(o.category == TYPE_NPC){
                        if(o.position.dist(so.getAvatarPosition()) < radius)
                            hash ^= values[o.itype][i][j];
                    }else{
                        hash ^= values[o.itype][i][j]; 
                    }
                }
            }
        }
        //Debug.log(6, "NPC: " + count);
        return hash;
    }
    
    private void initType(int id){
        if(values[id] == null){
            values[id] = new long[y][x];
            for(int i=0;i<y;++i)for(int j=0;j<x;++j){
                values[id][i][j] = random.nextLong();
            }
        }
    }
    
    int y;
    int x;
    MersenneTwisterFast random;
    long[][][] values = new long[1001][][];
    
    public static final int TYPE_AVATAR = 0;
    public static final int TYPE_RESOURCE = 1;
    public static final int TYPE_PORTAL = 2;
    public static final int TYPE_NPC = 3;
    public static final int TYPE_STATIC = 4;
    public static final int TYPE_FROMAVATAR = 5;
    public static final int TYPE_MOVABLE = 6;
}
