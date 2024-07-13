/**
 * PathFinding.java
 * @author Moiz
 * 2024/01/23
 * Implements the A* pathfinding algorithm 
 * Allows enemy to chase player and avoid obstacles
 */

import java.util.ArrayList;

public class PathFinding {

    // Creating lists used for pathfinding
    Node[][] nodes;
    ArrayList<Node> openList = new ArrayList<Node>();
    ArrayList<Node> pathList = new ArrayList<Node>();

    Node start, target, current;

    boolean targetFound;

    int iterations;

    PathFinding() {

        initializeNodes();

        this.targetFound = false;
        this.iterations = 0;

    }

    /**
     * Filling the list of nodes, and determining solid value
     */
    void initializeNodes() {

        // Instantiating node list
        nodes = new Node[Map.maxMapCol][Map.maxMapRow];

        // Looping through all nodes to determine if they are solid or not
        for (int i = 0; i < Map.maxMapRow; i++) {
            for (int j = 0; j < Map.maxMapCol; j++) {
                nodes[j][i] = new Node(j,i);
            }
        }

    }

    /**
     * Resets all nodes in the node list
     * This is done since the algorithm runs over and over again
     */
    private void resetNodes() {

        // Looping through all nodes and reseting open/closed values
        for (int i = 0; i < Map.maxMapRow; i++) {
            for (int j = 0; j < Map.maxMapCol; j++) {
                nodes[j][i].open = false;
                nodes[j][i].checked = false;
            }
        }

        // Clearing all lists
        openList.clear();
        pathList.clear();

        // Resetting other things
        targetFound = false;
        iterations = 0;
    }

    /**
     * Setting the gCost, hCost and fCost of all the nodes
     * 
     * @param node : To set the costs of all nodes
     */
    private void setCost(Node node) {

        int xDistance;
        int yDistance;

        // G COST --> Distance from node to start
        xDistance = Math.abs(node.col - start.col);
        yDistance = Math.abs(node.row - start.row);
        node.gCost = xDistance + yDistance;

        // H COST --> Distance from node to target
        xDistance = Math.abs(node.col - target.col);
        yDistance = Math.abs(node.row - target.row);
        node.hCost = xDistance + yDistance;

        // F COST --> Sum of H and G costs
        node.fCost = node.gCost + node.hCost;
    }

    /**
     * Getting everything set up to allow for the pathfinding to occur
     * @param startRow  : the start node's row
     * @param startCol  : the start node's column
     * @param targetRow : the target/end node's row
     * @param targetCol : the target/end node's column
     */
    void setNodes(int startCol, int startRow, int targetCol, int targetRow) {

        // RESETTING nodes to make sure there is no interference with previous pathfinding results
        resetNodes();

        // Setting node to solid (if applicable)
        for (int i = 0; i < Map.maxMapRow; i++) {
            for (int j = 0; j < Map.maxMapCol; j++) {
                if (Map.mapOutline[j][i] == 1 || Map.mapOutline[j][i] == 2) {
                    nodes[j][i].isSolid = true;
                } else {
                    nodes[j][i].isSolid = false;
                }
            }
        }

        // Setting start/target/current node
        start = nodes[startCol][startRow];
        current = start;
        target = nodes[targetCol][targetRow];

        openList.add(current);

        for (int i = 0; i < Map.maxMapRow; i++) {
            for (int j = 0; j < Map.maxMapCol; j++) {
                setCost(nodes[j][i]);
            }
        }

    }

    /**
     * Opening the neighbours of a certain node, if they are valid
     * 
     * @param node : The node to check the neighbours of
     */
    private void openNeighbourNodes(Node node) {

        Node nodeToCheck;

        // CHECKING ABOVE NODE
        if (node.row - 1 >= 0) {
            nodeToCheck = nodes[node.col][(node.row - 1)];
            if (nodeToCheck.isSolid == false && nodeToCheck.open == false && nodeToCheck.checked == false) {
                nodeToCheck.open = true;
                nodeToCheck.parent = node;
                openList.add(nodeToCheck);
            }

        }

        // CHECKING BELOW NODE
        if (node.row + 1 < Map.maxMapRow) {
            nodeToCheck = nodes[node.col][(node.row + 1)];
            if (nodeToCheck.isSolid == false && nodeToCheck.open == false && nodeToCheck.checked == false) {
                nodeToCheck.open = true;
                nodeToCheck.parent = node;
                openList.add(nodeToCheck);
            }

        }

        // CHECKING LEFT NODE
        if (node.col - 1 >= 0) {
            nodeToCheck = nodes[(node.col - 1)][node.row];
            if (nodeToCheck.isSolid == false && nodeToCheck.open == false && nodeToCheck.checked == false) {
                nodeToCheck.open = true;
                nodeToCheck.parent = node;
                openList.add(nodeToCheck);
            }

        }

        // CHECKING RIGHT NODE
        if (node.col + 1 < Map.maxMapCol) {
            nodeToCheck = nodes[(node.col + 1)][node.row];
            if (nodeToCheck.isSolid == false && nodeToCheck.open == false && nodeToCheck.checked == false) {
                nodeToCheck.open = true;
                nodeToCheck.parent = node;
                openList.add(nodeToCheck);
            }

        }

    }

    /**
     * Finding the path from start to target
     * @return : true if path is found, false otherwise
     */
    public boolean findPath() {

        while (targetFound == false && iterations < 999) {

            // Marking current node as checked
            current.checked = true;

            // Removing current node from openlist, and finding valid neighbours to put into
            // openlist
            openList.remove(current);
            openNeighbourNodes(current);

            // Determining best node in openlist to check
            int bestIndex = 0;
            int bestFCost = 999;

            for (int i = 0; i < openList.size(); i++) {
                if (openList.get(i).fCost < bestFCost) {
                    bestIndex = i;
                    bestFCost = openList.get(i).fCost;
                } 
                else if (openList.get(i).fCost == bestFCost) {
                    if (openList.get(i).gCost < openList.get(bestIndex).gCost) {
                        bestIndex = i;
                    }
                }
            }

            // BREAKING THE LOOP IF THERE IS NO MORE NODES TO SEARCH!
            if (openList.size() == 0) break;

            // Setting current to the best node in openList
            current = openList.get(bestIndex);

            // If the target node is found, then retrace the path
            if (current == target) {
                targetFound = true;
                retracePath();
            }

            // Increasing iterations to avoid infinite loop
            iterations++;

        }

        return targetFound;

    }

    /**
     * Determining the path if the target has been found
     */
    void retracePath() {

        // Setting the "current" node to the TARGET
        Node currentForPath = target;

        // Retracing each parent from the target node so we eventually reach the start
        while (currentForPath != start) {
            // Putting each node at index 0 so we "reverse" the list, giving us start to finish!
            pathList.add(0, currentForPath);
            currentForPath = currentForPath.parent;
        }

    }

}