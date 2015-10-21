/*
 * BFSNodeo change this license header, choose License Headers in Project Properties.
 * BFSNodeo change this template file, choose BFSNodeools | BFSNodeemplates
 * and open the template in the editor.
 */
package controllers.alxio;

import core.game.StateObservation;
import java.util.ArrayList;
import java.util.Iterator;

public class ChildNodes implements Iterable<Pair<Double, BFSNode>>{
    public ChildNodes(BFSNode node){
        nodes.add(node);
        count.add(1);
        typesCount = 1;
        totalCount = 1;
    }
    ArrayList<BFSNode> nodes = new ArrayList<>();
    ArrayList<Integer> count = new ArrayList<>();
    int typesCount = 0;
    int totalCount = 0;
    int a = 0, b = 0;
    
    BFSNode findByHashCode(long hash){
        for(int i=0;i<typesCount;++i){
            if(nodes.get(i).getHash() == hash){
                count.set(i, count.get(i) + 1);
                ++totalCount;
                //Debug.log(4, "MOVE: "+count.get(i)+"/"+totalCount);
                return nodes.get(i);
            }
        }
        //Debug.log(4, "MOVE: 0/"+typesCount);
        return null;
    }
    
    public static int sameCount = 0;
    public static int otherCount = 0;
    
    final BFSNode addNode(StateObservation state, BFSNode parent){
        long h = Z.hash(state);
        for(int i=0;i<typesCount;++i){
            if(nodes.get(i).getHash() == h){
                count.set(i, count.get(i) + 1);
                ++sameCount;
                ++totalCount;
                return nodes.get(i);
            }
        }
        if(typesCount > 0){
            otherCount++;
        }
        BFSNode node = new BFSNode(state, h , parent);
        nodes.add(node);
        count.add(1);
        typesCount++;
        totalCount++;
        return node;
    }
    
    boolean isSure(){
        return totalCount > Strategus.SAME_COUNT_TO_BE_SURE && typesCount == 1 || totalCount > Strategus.TOTAL_COUNT_TO_BE_SURE;  
    }
    
    double contains(BFSNode best) {
        double k = 0;
        long h = best.getHash();
        for(int i=0;i<typesCount;++i){
            if(nodes.get(i).getHash() == h){
                k = count.get(i);
                break;
            }
        }
        return k == 0 ? 0 : (k / totalCount); 
    }
    
    BFSNode get() {
        if(typesCount == 0) return null;
        if(b++ > count.get(a)){
            b = 0;
            a = (a+1) % typesCount;
        }
        return nodes.get(a);
    }

    @Override
    public Iterator<Pair<Double, BFSNode>> iterator() {
        return new ChildIterator();
    }

    int mostProbableId(){
        if(typesCount == 0) return -1;
        if(typesCount == 1) return 0;
        else{
            int best = 0;
            for(int i=1;i<typesCount;++i){
                if(count.get(i) > count.get(best)){
                    best = i;
                }
            }
            return best;
        }    
    }
    
    BFSNode mostProbableNode() {
        int best = mostProbableId();
        if(best == -1) return null;
        return nodes.get(best);
    }
    
    double greatestProbability() {
        if(typesCount == 0) return 0;
        int best = mostProbableId();
        return ((double)count.get(best)) / totalCount;
    }

    double expectedValue() {
        if(typesCount == 0) return -Double.MAX_VALUE;
        if(typesCount == 1 && isSure()) return nodes.get(0).expectedValue;
        double value = 0;
        for(int i=0;i<typesCount;++i){
            value += nodes.get(i).expectedValue * count.get(i) / (totalCount);
        }
        return value;
    }
    
    class ChildIterator implements Iterator<Pair<Double, BFSNode>>{
        int next = 0;
        @Override
        public boolean hasNext() {
            return next < typesCount;
        }
        @Override
        public Pair<Double, BFSNode> next() {
            return new Pair<>(
                    ((double)count.get(next))/totalCount,
                    nodes.get(next++)
            );
        }
        @Override
        public void remove(){
            throw new UnsupportedOperationException();
        }
    }
}
