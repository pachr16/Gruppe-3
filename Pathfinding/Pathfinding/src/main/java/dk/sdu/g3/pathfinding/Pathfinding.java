package dk.sdu.g3.pathfinding;

import dk.sdu.g3.common.data.Coordinate;
import dk.sdu.g3.common.data.ITile;
import dk.sdu.g3.common.services.IMap;
import dk.sdu.g3.common.services.IPathfinding;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@ServiceProviders(value = {
    @ServiceProvider(service = IPathfinding.class),})
public class Pathfinding implements IPathfinding {
    
    private int mapLengthX = 0; //Size of the map on the x-axis (Horizontal)
    private int mapLengthY = 0; //Size of the map on the y-axis (Vertical)
    private static final int STEP_COST = 1; //Static variable to illustrate the cost of stepping from Node to Node
    private List<Node> nodes = new ArrayList<>(); //List of all ITiles from the map, converted to Node objects which is the class used for working on path in PathfindingModule
    private List<Node> openList; //List to hold the Nodes that are possible to move to
    private List<Node> closedList; //List to hold the Nodes that are deemed the most promising nodes for the shortest path
    private List<Coordinate> coordinateList; //List to hold the Coordinates generated by the generatePath method. This is essentially a list of every step that a Unit will take through the map
    private Node startNode; //Define a Node as the starting Node. This is the starting position of the Units
    private Node goalNode; //Define a Node as the goal Node. THis is the goal state of the Units

    public Pathfinding() {
    }

    @Override
    public List<Coordinate> generatePath(IMap map, Coordinate start, Coordinate goal) throws Exception{
        mapLengthX = map.getLengthX(); 
        mapLengthY = map.getLengthY(); 
        openList = new ArrayList<>(); 
        closedList = new ArrayList<>(); 
        Node currentNode = null; //Initialize variable to keep track of the Node that is currently being explored throughout the process

        createNodes(map); //Convert all Coordinates received from Enemy to Nodes
        defineStartNode(start); //Define startNode from nodes
        defineGoalNode(goal); // Define goalNode from nodes

        openList.add(startNode); //Add startNode to openList to enable the Node to be currentNode

        while (!openList.isEmpty()) { 
            currentNode = openList.get(0); //currentNode set to the first Node contained in the openList

            for (Node node : openList) { //Iterate through the openList for all Nodes
                calculateHeuristic(node); //Calculate euclidean distance to the goalNode from the startNode
                calculateTotalPathCost(node); //Calculate and set estimated total cost from startNode to goalNode
                if (node.getTotalCost() < currentNode.getTotalCost()) { //If the Node being processed has a lower totalCost than the Node defined as currentNode, change currentNode to said Node
                    currentNode = node;
                }
            }

            if (currentNode.getCenter().equals(goalNode.getCenter())) { //if Node with lowest cost == Goal --> success! 
                closedList.add(currentNode);
                convertNodes(closedList);

                return coordinateList; 
            }

            setAdjacentNodes(currentNode); //find all seccessorNodes

            for (Node successor : currentNode.getNeighbours()) { 
                double successorCurrentCost = currentNode.getAccumulatedStepCost() + STEP_COST; 
                if (openList.contains(successor)) {
                    if (successor.getAccumulatedStepCost() <= successorCurrentCost) { 
                        closedList.add(currentNode); 
                    }
                } else if (closedList.contains(successor)) { 
                    if (successor.getAccumulatedStepCost() <= successorCurrentCost) { 
                        closedList.add(currentNode);
                    } else {
                        openList.add(successor);
                        closedList.remove(successor);
                    }
                } else {
                    openList.add(successor); 
                    calculateHeuristic(successor); 
                }
                successor.setAccumulatedStepCost(successorCurrentCost); 
                successor.setParent(currentNode); 
            }
            closedList.add(currentNode);
        }
            Exception e = new Exception();
            throw e;
    }

    public void defineGoalNode(Coordinate goal) {
        goalNode = null;
        for (Node node : nodes) {
            if (node.getCenter().equals(goal)) {
                setGoalNode(node);
            }
        }
    }

    public void defineStartNode(Coordinate start) {
        startNode = null;
        for (Node node : nodes) {
            if (node.getCenter().equals(start)) {
                setStartNode(node);
            }
        }
    }

    public Node assignNeighbour(int x, int y) { //Look out for null pointer
        for (Node node : nodes) {
            if (node.getCenter().getX() == x) {
                if (node.getCenter().getY() == y) {
                    return node;
                }
            }
        } //Possible null pointer
        return null;
    }

    public void createNodes(IMap map) {
        List<ITile> tiles = map.getTileList();
        for (ITile tile : tiles) {
            Node node = new Node(tile.getCoordinate(), tile.getSize(), tile.isOccupied());
            //If Node has tower placed i.e. isBlocked -> no use for it (No need to add)
            if (!node.isBlocked()) {
                nodes.add(node);
            }
        }
    }

    /**
     * Method used to calculate the value of the heuristic function, used to
     * determine the estimated distance to the goal coordinate in a straight
     * line
     *
     * @param currentNode is the Node that the pathfinding algorithm has reached
     * in its current iteration Variable a is the distance from currentNode's
     * center x Coordinate to the goal Coordinate Variable b is the distance
     * from currentNode's center y Coordinate to the goal Coordinate This forms
     * a triangle between the currentNode and the goal, with the sides being a,
     * b and c By using pythagore, the variable c is calculated, which is the
     * distance from the currentNode to the goal Coordinate
     */
    public void calculateHeuristic(Node currentNode) {
        int sideA = (goalNode.getCenter().getX() - currentNode.getCenter().getX());
        int sideB = (goalNode.getCenter().getY() - currentNode.getCenter().getY());
        double diagonal = Math.sqrt(Math.pow(sideA, 2.0) + Math.pow(sideB, 2.0));
        currentNode.setHeuristic(diagonal);
    }

    public void calculateTotalPathCost(Node currentNode) {
        currentNode.setTotalCost(currentNode.getAccumulatedStepCost() + currentNode.getHeuristic());
    }

    //No test needed as it runs through all neighbour-methods anyway
    private void setAdjacentNodes(Node currentNode) {
        setLeftNeighbour(currentNode);
        setRightNeighbour(currentNode);
        setUpNeighbour(currentNode);
        setDownNeighbour(currentNode);
    }

    public void setLeftNeighbour(Node currentNode) {
        //If (center coordinate of currentNode(x) - size of currentNode(x)) > min(x), set left neighbour
        if ((currentNode.getCenter().getX() - currentNode.getSize()) > 0) {
            if (assignNeighbour((currentNode.getCenter().getX() - (currentNode.getSize() * 2)), currentNode.getCenter().getY()) != null) {
                currentNode.addNeighbour(assignNeighbour((currentNode.getCenter().getX() - (currentNode.getSize() * 2)), currentNode.getCenter().getY()));
            }
        }
    }

    public void setRightNeighbour(Node currentNode) {
        //If (center coordinate of currentNode(x) + size of currentNode(x) < max(x), set right neighbour
        if ((currentNode.getCenter().getX() + currentNode.getSize() < mapLengthX)) {
            if (assignNeighbour((currentNode.getCenter().getX() + (currentNode.getSize() * 2)), currentNode.getCenter().getY()) != null) {
                currentNode.addNeighbour(assignNeighbour((currentNode.getCenter().getX() + (currentNode.getSize() * 2)), currentNode.getCenter().getY()));
            }
        }
    }

    public void setUpNeighbour(Node currentNode) {
        //If (center coordinate of currentNode(y) - size of currentNode(y)) > min(y), set up neighbour  
        if ((currentNode.getCenter().getY() - currentNode.getSize() > 0)) {
            if (assignNeighbour(currentNode.getCenter().getX(), currentNode.getCenter().getY() - (currentNode.getSize() * 2)) != null) {
                currentNode.addNeighbour(assignNeighbour(currentNode.getCenter().getX(), currentNode.getCenter().getY() - (currentNode.getSize() * 2)));
            }
        }
    }

    public void setDownNeighbour(Node currentNode) {
        //If (center coordinate of currentNode(y) + size of currentNode(y)) < max(y), set down neighbour  
        if (currentNode.getCenter().getY() + currentNode.getSize() < mapLengthY) {
            if (assignNeighbour(currentNode.getCenter().getX(), currentNode.getCenter().getY() + (currentNode.getSize() * 2)) != null) {
                currentNode.addNeighbour(assignNeighbour(currentNode.getCenter().getX(), currentNode.getCenter().getY() + (currentNode.getSize() * 2)));
            }
        }
    }

    public List<Coordinate> convertNodes(List<Node> openList) {
        coordinateList = new ArrayList<Coordinate>();
        //Termination clause might have to be -2 as we don't need goalNode when creating Coordinates between Nodes
        for (int i = 0; i < openList.size() - 1; i++) {
            //initializing variables to avoid calling openList too much...
            int currentX = closedList.get(i).getCenter().getX();
            int currentY = closedList.get(i).getCenter().getY();
            int nextX = closedList.get(i + 1).getCenter().getX();
            int nextY = closedList.get(i + 1).getCenter().getY();

            //if X-value is equal in i+1 then i Y-value has changed
            if (currentX == nextX) {
                //Check if target coordinate is higher or lower value than current coordinate
                if (currentY > nextY) {
                    for (int y = currentY; y > nextY; y--) {
                        coordinateList.add(new Coordinate(currentX, y));
                    }
                } else {
                    for (int y = currentY; y < nextY; y++) {
                        coordinateList.add(new Coordinate(currentX, y));
                    }
                }
                //If Y-value is equal in i+1 and i X-value has changed
            } else if (currentY == nextY) {
                //Check if target coordinate is higher or lower value than current coordinate
                if (currentX > nextX) {
                    for (int x = currentX; x > nextX; x--) {
                        coordinateList.add(new Coordinate(x, currentY));
                    }
                } else {
                    for (int x = currentX; x < nextX; x++) {
                        coordinateList.add(new Coordinate(x, currentY));
                    }
                }

            }
        }
        return coordinateList;
    }

    public Node getStartNode() {
        return startNode;
    }

    public void setStartNode(Node startNode) {
        this.startNode = startNode;
    }

    public Node getGoalNode() {
        return goalNode;
    }

    public void setGoalNode(Node goalNode) {
        this.goalNode = goalNode;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }
    
    
    
    


}
