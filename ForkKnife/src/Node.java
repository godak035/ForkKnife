/**
 * Node.java
 * @author Moiz
 * 2024/01/23
 * Used with the PathFinding class to characterize each tile on the map
 */

public class Node {

    // Will be used to trace the path from end to start
    Node parent;

    // Values used to determine best nodes to search
    int gCost;
    int hCost;
    int fCost;

    int row, col;

    // Values to determine attributes of a certain nod
    boolean isSolid, open, checked;

    Node(int row, int col) {

        this.row = row;
        this.col = col;

    }

}