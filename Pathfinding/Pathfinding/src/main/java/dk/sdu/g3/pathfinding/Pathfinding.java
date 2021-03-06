package dk.sdu.g3.pathfinding;

import dk.sdu.g3.common.data.Coordinate;
import dk.sdu.g3.common.data.ITile;
import dk.sdu.g3.common.services.IMap;
import dk.sdu.g3.common.services.IPathfinding;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@ServiceProviders(value = {
    @ServiceProvider(service = IPathfinding.class),})
public class Pathfinding implements IPathfinding {

    private int mapLengthX = 0; //Size of the map on the x-axis (Horizontal)
    private int mapLengthY = 0; //Size of the map on the y-axis (Vertical)
    private static final int STEP_COST = 60; //Static variable to illustrate the cost of stepping from Node to Node
    public List<Node> nodes; //List of all ITiles from the map, converted to Node objects which is the class used for working on path in PathfindingModule
    private Node startNode; //Define a Node as the starting Node. This is the starting position of the Units, defined by their starting coordinate
    private Node goalNode; //Define a Node as the goal Node. THis is the goal state of the Units, defined by their goal coordinate

    public Pathfinding() { //Empty constructor for service providing
    }

    /**
     *
     * @param map is the map in its current state including blocked Tiles
     * @param start is the Coordinate on which the Unit given the generated path
     * is spawning
     * @param goal is the Coordinate on which the Unit given the generated path
     * has to stand in order to successfully navigate the map
     * @return the list of Coordinates that the Unit given the generated path
     * has to follow in order to reach goal state
     * @throws Exception if no possible path from start to goal is found
     */
    @Override
    public List<Coordinate> generatePath(IMap map, Coordinate start, Coordinate goal) throws Exception {
        mapLengthX = map.getLengthX();
        mapLengthY = map.getLengthY();
        List<Node> openList = new ArrayList<>(); //List to hold the Nodes that are possible to move to
        List<Node> closedList = new ArrayList<>(); //List to hold the Nodes that are deemed the most promising nodes for the shortest path
        
        createNodes(map); //Convert all Tiles in map received from Enemy to Nodes
        defineStartNode(start); //Define startNode from nodes
        defineGoalNode(goal); // Define goalNode from nodes

        calculateHeuristic(startNode); //Calculate and set the heuristic value of startNode
        startNode.setTotalCost(startNode.getHeuristic());
        openList.add(startNode); //Add startNode to openList to enable the Node to be currentNode
        
        Node currentNode; //Initialize variable to keep track of the Node that is currently being explored throughout the process
        while (!openList.isEmpty()) {
            currentNode = openList.get(0); //Initialize variable to keep track of the Node that is currently being explored throughout the process
            
            for (Node node : openList) { //Iterate through the openList for all Nodes
                calculateHeuristic(node); //Calculate euclidean distance to the goalNode from the startNode
                calculateTotalPathCost(node); //Calculate and set estimated total cost from startNode to goalNode
                if (node.getTotalCost() < currentNode.getTotalCost() && !node.equals(currentNode)) { //If the Node being processed has a lower totalCost than the Node defined as currentNode, change currentNode to said Node
                    currentNode = node;
                }
            }
            openList.remove(currentNode);

            if (currentNode.getCenter().equals(goalNode.getCenter())) { //Rework
                closedList.add(currentNode);
                return convertNodes(currentNode);
            }

            setAdjacentNodes(currentNode); //Find each eligible successor to the currentNode and add them to currentNode's list of neighbours
            
            for (Node successor : currentNode.getNeighbours()) {
                double currentCost = currentNode.getAccumulatedStepCost() + STEP_COST; //Calculate the cost of going from startNode to each successor Node

                //If the successor Node is in openList and the accumulatedStepCost of the successor is less than or equal to the successorCurrentCost variable, add currentNode to closedList
                if (openList.contains(successor)) {
                    if (successor.getAccumulatedStepCost() <= currentCost) {
                        continue;
                    }

                } // Else if successor Node is in closedList and the accumulatedStepCost of the successor is less than or equal to the successorCurrentCost variable, add currentNode to closedList
                else if (closedList.contains(successor)) {
                    if (successor.getAccumulatedStepCost() <= currentCost) {
                        continue;

                    } //Else move successor Node from closedList to openList
                    else {
                        openList.add(successor);
                        closedList.remove(successor);
                    }

                } //Else add the successor Node to openList and calculate the distance from successor Node to goal
                else {
                    openList.add(successor);
                    calculateHeuristic(successor);
                }
                
                //Set accumulatedStepCost for the successor Node to be successorCurrentCost and assign currentNode as its parent
                successor.setAccumulatedStepCost(currentCost);
                successor.setParent(currentNode);
            }
            closedList.add(currentNode);
        }
        //If no path from start to goal has been found, throw an exception to the method calling this method
        Exception e = new Exception();
        throw e;
    }

    public void defineGoalNode(Coordinate goal) {
        for (Node node : nodes) {
            if (node.getCenter().getX() == goal.getX() && node.getCenter().getY() == goal.getY()) {
                goalNode = node;
            }
        }
    }

    public void defineStartNode(Coordinate start) {
        for (Node node : nodes) {
            if (node.getCenter().getX() == start.getX() && node.getCenter().getY() == start.getY()) {
                startNode = node;
            }
        }
    }

    /**
     * Method used to define which Node to assign to a Node
     *
     * @param x is the coordinate of the Node being assigned, on the x-axis
     * @param y is the coordinate of the Node being assigned, on the y-axis
     * @return the Node to be assigned as a neighbour given the parameters
     */
    public Node assignNeighbour(int x, int y) { //Look out for null pointer
        for (Node node : nodes) {
            if (node.getCenter().getX() == x) {
                if (node.getCenter().getY() == y) {
                    return node;
                }
            }
        }
        //Possible null pointer
        return null;
    }

    /**
     * Convert all ITile elements to Node elements (Nodes are used solely for
     * pathfinding purposes
     *
     * @param map is an IMap element received by the class calling the
     * generatePath method, which in turn calls this method
     */
    public void createNodes(IMap map) {
        nodes = new ArrayList<>();
        List<ITile> tiles = map.getTileList();
        for (ITile tile : tiles) {
            //If Node has tower placed i.e. isBlocked -> no use for it (No need to add)
            if (!tile.isOccupied()) {
                Node node = new Node(tile.getCoordinate(), tile.getSize());
                nodes.add(node);
            } 
        }
    }

    /**
     * Method used to calculate the value of the heuristic function, used to
     * determine the estimated distance to the goal coordinate in a straight
     * line This is done using the Pythagorean theorem, as the coordinates of
     * the goal Node and currentNode form a triangle
     *
     * @param node is the Node that the pathfinding algorithm wants the
     * heuristic value of in its current iteration
     */
    public void calculateHeuristic(Node node) {
        int sideA = (goalNode.getCenter().getX() - node.getCenter().getX()); //Horizontal difference between the node and the goal (Distance on the x-axis)
        int sideB = (goalNode.getCenter().getY() - node.getCenter().getY()); //Vertical difference between the node and the goal (Distance on the y-axis)
        double diagonal = Math.sqrt(Math.pow(sideA, 2) + Math.pow(sideB, 2)); //Using the Pythagorean theorem the hypotenuse, i.e. the euclidean distance between the node and the goal is calculated
        
        //Set the heuristic value of currentNode to be 5 * the euclidean distance for more emphasis on the distance to goal than step cost (Weighted A*)
        node.setHeuristic(5 * diagonal);
    }

    /**
     * Calculate and set the estimated total cost from start to goal through
     * this Node
     *
     * @param node is the Node currently being processed by generatePath
     */
    public void calculateTotalPathCost(Node node) {
        node.setTotalCost(node.getAccumulatedStepCost() + node.getHeuristic());
    }

    /**
     * Calls the four setNeighbour functions to add all possible neighbours to
     * the current Node
     *
     * @param currentNode is the Node that generatePath has reached in its
     * current iteration
     */
    //No test needed as it runs through all neighbour-methods anyway
    private void setAdjacentNodes(Node currentNode) {
        currentNode.clearNeighbours();
        setLeftNeighbour(currentNode);
        setRightNeighbour(currentNode);
        setUpNeighbour(currentNode);
        setDownNeighbour(currentNode);
    }

    /**
     * If (center coordinate of currentNode(x) - size of currentNode(x)) >
     * min(x), set left neighbour of currentNode to be the one with a center
     * x-Coordinate of currentNode's center - (2 x size of the Nodes)
     *
     * @param currentNode is the Node that generatePath has reached in its
     * current iteration
     */
    public void setLeftNeighbour(Node currentNode) {
        if ((currentNode.getCenter().getX() - currentNode.getSize()) > 0) {
            if (assignNeighbour((currentNode.getCenter().getX() - (currentNode.getSize() * 2)), currentNode.getCenter().getY()) != null) {
                currentNode.addNeighbour(assignNeighbour((currentNode.getCenter().getX() - (currentNode.getSize() * 2)), currentNode.getCenter().getY()));
            }
        }
    }

    /**
     * If (center coordinate of currentNode(x) + size of currentNode(x) <
     * max(x), set right neighbour of currentNode to be the one with a center
     * x-Coordinate of currentNode + (2 x size of the nodes)
     *
     * @param currentNode is the Node that generatePath has reached in its
     * current iteration
     */
    public void setRightNeighbour(Node currentNode) {
        if ((currentNode.getCenter().getX() + currentNode.getSize() < mapLengthX)) {
            if (assignNeighbour((currentNode.getCenter().getX() + (currentNode.getSize() * 2)), currentNode.getCenter().getY()) != null) {
                currentNode.addNeighbour(assignNeighbour((currentNode.getCenter().getX() + (currentNode.getSize() * 2)), currentNode.getCenter().getY()));
            }
        }
    }

    /**
     * //If (center coordinate of currentNode(y) - size of currentNode(y)) >
     * min(y), set up neighbour of currentNode to be the one with a center
     * y-Coordinate of currentNode - (2 x size of the nodes)
     *
     * @param currentNode is the Node that generatePath has reached in its
     * current iteration
     */
    public void setUpNeighbour(Node currentNode) {
        if ((currentNode.getCenter().getY() - currentNode.getSize() > 0)) {
            if (assignNeighbour(currentNode.getCenter().getX(), currentNode.getCenter().getY() - (currentNode.getSize() * 2)) != null) {
                currentNode.addNeighbour(assignNeighbour(currentNode.getCenter().getX(), currentNode.getCenter().getY() - (currentNode.getSize() * 2)));
            }
        }
    }

    /**
     * //If (center coordinate of currentNode(y) + size of currentNode(y)) <
     * max(y), set down neighbour of currentNode to be the one with a center
     * y-Coordinate of currentNode + (2 x size of the nodes)
     *
     * @param currentNode is the Node that generatePath has reached in its
     * current iteration
     */
    public void setDownNeighbour(Node currentNode) {
        if (currentNode.getCenter().getY() + currentNode.getSize() < mapLengthY) {
            if (assignNeighbour(currentNode.getCenter().getX(), currentNode.getCenter().getY() + (currentNode.getSize() * 2)) != null) {
                currentNode.addNeighbour(assignNeighbour(currentNode.getCenter().getX(), currentNode.getCenter().getY() + (currentNode.getSize() * 2)));
            }
        }
    }

    /**
     * Method used to convert the steps from start to goal from Nodes to
     * Coordinates, making the units walk each step instead of jumping between
     * Nodes
     *
     * @param list is a list of Nodes to convert to Coordinates
     * @return coordinateList, which contains every single coordinate that a
     * Unit will have to follow for the shortest path
     */
    public List<Coordinate> convertNodes(Node goal) {
        List<Coordinate> coordinateList = new ArrayList<>(); //List to hold the Coordinates. This is essentially a list of every step that a Unit will take through the map
        List<Node> list = new ArrayList<>();
        
        Node current = goal;
        
        while (current != null) {
            list.add(current);
            current = current.getParent();
        }
        
        //Termination clause might have to be -2 as we don't need goalNode when creating Coordinates between Nodes
        for (int i = 0; i < list.size() - 1; i++) {
            //Save Coordinates (x and y) in variables to avoid multiple calls to list
            int currentX = list.get(i).getCenter().getX();
            int currentY = list.get(i).getCenter().getY();
            int nextX = list.get(i + 1).getCenter().getX();
            int nextY = list.get(i + 1).getCenter().getY();

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
        //Important to add the centerCoordinate for the last Node as the previous only adds the coordinate before next Node center
        coordinateList.add(list.get(list.size() - 1).getCenter());
        
        Collections.reverse(coordinateList);
        
        for (int i = 0; i < goal.getSize(); i++) {
            coordinateList.add(new Coordinate(goal.getCenter().getX() + i, goal.getCenter().getY()));
        }
        
        return coordinateList;
    }

    //For tests
    public Node getStartNode() {
        return startNode;
    }

    //For tests
    public void setStartNode(Node startNode) {
        this.startNode = startNode;
    }

    //For tests
    public Node getGoalNode() {
        return goalNode;
    }

    //For tests
    public void setGoalNode(Node goalNode) {
        this.goalNode = goalNode;
    }

    //For tests
    public List<Node> getNodes() {
        return nodes;
    }

    //For tests
    public void addNode(Node node) {
        this.nodes.add(node);
    }

    //For tests
    public int getMapLengthX() {
        return mapLengthX;
    }

    //For tests
    public void setMapLengthX(int mapLengthX) {
        this.mapLengthX = mapLengthX;
    }

    //For tests
    public int getMapLengthY() {
        return mapLengthY;
    }

    //For tests
    public void setMapLengthY(int mapLengthY) {
        this.mapLengthY = mapLengthY;
    }

//    //For tests
//    public List<Node> getClosedList() {
//        return closedList;
//    }
//
//    //For tests
//    public void setClosedList(List<Node> closedList) {
//        this.closedList = closedList;
//    }

}
