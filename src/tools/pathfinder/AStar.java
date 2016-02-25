package tools.pathfinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * Created by dperez on 13/01/16.
 */
public class AStar
{
    public static PriorityQueue<Node> closedList, openList;
    public HashMap<Integer, ArrayList<Node>> pathCache;
    public PathFinder pathfinder;
    public boolean visited[][];

    public AStar(PathFinder pathfinder)
    {
        this.pathfinder = pathfinder;
        pathCache = new HashMap<Integer, ArrayList<Node>>();
        visited = new boolean[pathfinder.grid.length][pathfinder.grid[0].length];
    }

    public void emptyCache()
    {
        pathCache.clear();

    }

    private static double heuristicEstimatedCost(Node curNode, Node goalNode)
    {
        //4-way: using Manhattan
        double xDiff = Math.abs(curNode.position.x - goalNode.position.x);
        double yDiff = Math.abs(curNode.position.y - goalNode.position.y);
        return xDiff + yDiff;

        //This is Euclidean distance(sub-optimal here).
        //return curNode.position.dist(goalNode.position);
    }


    private ArrayList<Node> calculatePath(Node node)
    {
        ArrayList<Node> path = new ArrayList<Node>();
        while(node != null)
        {
            if(node.parent != null) //to avoid adding the start node.
            {
                node.setMoveDir(node.parent);
                path.add(0,node);
            }
            node = node.parent;
        }
        return path;
    }

    public ArrayList<Node> getPath(Node start, Node goal)
    {
        int pathId = start.id * 10000 + goal.id;
        if(pathCache.containsKey(pathId))
            return pathCache.get(pathId);
        return null;
    }

    public ArrayList<Node> findPath(Node start, Node goal)
    {
        if(goal != null)
        {
            int pathId = start.id * 10000 + goal.id;
            if(pathCache.containsKey(pathId))
                return pathCache.get(pathId);
            ArrayList<Node> path = _findPath(start, goal);

            if(path!=null)
                pathCache.put(pathId, path);

            return path;
        }

        _dijkstraa(start);
        return null;
    }


    private void _dijkstraa(Node start)
    {

        ArrayList<Node> destinationsFromStart = new ArrayList<Node>();
        //All unvisited at the beginning.
        visited = new boolean[pathfinder.grid.length][pathfinder.grid[0].length];
        //...except the starting node
        visited[(int)start.position.x][(int)start.position.y] = true;

        Node node = null;

        openList = new PriorityQueue<Node>();
        start.totalCost = 0.0f;

        openList.add(start);

        while(openList.size() != 0)
        {
            node = openList.poll();
            //System.out.println("Remaining in list: " + openList.size());

            if(!destinationsFromStart.contains(node) && (node != start))
            {
                destinationsFromStart.add(node);
            }

            ArrayList<Node> neighbours = pathfinder.getNeighbours(node);

            for(int i = 0; i < neighbours.size(); ++i)
            {
                Node neighbour = neighbours.get(i);
                double curDistance = neighbour.totalCost;
                if (! visited[(int)neighbour.position.x][(int)neighbour.position.y] )
                {
                    visited[(int)neighbour.position.x][(int)neighbour.position.y] = true;
                    neighbour.totalCost = curDistance + node.totalCost;
                    neighbour.parent = node;
                    openList.add(neighbour);

                }else if(curDistance + node.totalCost < neighbour.totalCost)
                {
                    neighbour.totalCost = curDistance + node.totalCost;
                    neighbour.parent = node;
                }
            }

        }


        for(Node dest : destinationsFromStart)
        {
            int pathid = start.id * 10000 + dest.id;
            pathCache.put(pathid, calculatePath(dest));
        }

    }

    private ArrayList<Node> _findPath(Node start, Node goal)
    {
        Node node = null;
        openList = new PriorityQueue<Node>();
        closedList = new PriorityQueue<Node>();

        start.totalCost = 0.0f;
        start.estimatedCost = heuristicEstimatedCost(start, goal);

        openList.add(start);

        while(openList.size() != 0)
        {
            node = openList.poll();
            closedList.add(node);

            if(node.position.equals(goal.position))
                return calculatePath(node);

            ArrayList<Node> neighbours = pathfinder.getNeighbours(node);

            for(int i = 0; i < neighbours.size(); ++i)
            {
                Node neighbour = neighbours.get(i);
                double curDistance = neighbour.totalCost;

                if(!openList.contains(neighbour) && !closedList.contains(neighbour))
                {
                    neighbour.totalCost = curDistance + node.totalCost;
                    neighbour.estimatedCost = heuristicEstimatedCost(neighbour, goal);
                    neighbour.parent = node;

                    openList.add(neighbour);

                }else if(curDistance + node.totalCost < neighbour.totalCost)
                {
                    neighbour.totalCost = curDistance + node.totalCost;
                    neighbour.parent = node;

                    if(openList.contains(neighbour))
                        openList.remove(neighbour);

                    if(closedList.contains(neighbour))
                        closedList.remove(neighbour);

                    openList.add(neighbour);
                }
            }

        }

        if(! node.position.equals(goal.position))
            return null;

        return calculatePath(node);

    }

    private int[][] uncompressPathId(int pathId)
    {
        int[][] ends = new int[2][2];

        int org =  pathId / 10000;
        int dest = pathId % 10000;

        ends[0] = new int[]{org/100 , org%100};
        ends[1] = new int[]{dest/100 , dest%100};
        return ends;
    }

    public void printPath(int pathId, ArrayList<Node> nodes)
    {
        if(nodes == null)
        {
            System.out.println("No Path");
            return;
        }

        int[][] endsIds = uncompressPathId(pathId);

        String ends = "(" + endsIds[0][0] + "," + endsIds[0][1] + ") -> ("
                + endsIds[1][0] + "," + endsIds[1][1] + ")";


        System.out.print("Path " + ends + "; ("+ nodes.size() + "): ");
        for(Node n : nodes)
        {
            System.out.print(n.position.x + ":" + n.position.y + ", ");
        }
        System.out.println();
    }
}
