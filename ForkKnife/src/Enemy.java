/**
 * Enemy.java
 * @author Samarth, Moiz, Avishan
 * 2024/01/23
 * Describes the enemy's attributes and behaviours with the player
 */
import java.awt.Rectangle;

public class Enemy extends Rectangle {

    public PathFinding pathFinder = new PathFinding();

    Weapon weapon = new Weapon();
    String weaponName;

    int size = 64;

    int vx, vy, health;

    int worldXPos;
    int worldYPos;
    int screenXPos;
    int screenYPos;
    Rectangle aggroRect;
    final int aggroSquareSize = 750;

    boolean isAggro = false;

    Enemy(int spawnWorldX, int spawnWorldY) {

        width = height = size;
        vx = vy = 3;
        health = 3;

        this.worldXPos = spawnWorldX;
        this.worldYPos = spawnWorldY;

        int aggroSquareXPos = worldXPos + width / 2 - aggroSquareSize / 2;
        int aggroSquareYPos = worldYPos + height / 2 - aggroSquareSize / 2;

        aggroRect = new Rectangle(aggroSquareXPos, aggroSquareYPos, aggroSquareSize, aggroSquareSize);

        giveWeapon();

    }

    void chasePlayer() {
        if (isAggro) {

            // Moves the player towards the player (set in stone)
            // Also determines direction of fork/knife
            if (worldXPos < ForkKnife.player.worldXPos) {
                weapon.rotation = 90;
                worldXPos += vx;
            } else if (worldXPos > ForkKnife.player.worldXPos) {
                weapon.rotation = 270;
                worldXPos -= vx;
            } 

            if (worldYPos < ForkKnife.player.worldYPos) {
                weapon.rotation = 180;
                worldYPos += vy;
            } else if (worldYPos > ForkKnife.player.worldYPos) {
                weapon.rotation = 0;
                worldYPos -= vy;
            }

            /////////////// PATHFINDING CODE ////////////////

            // // Setting up the pathfinder with the start (enemy) amd target (player)
            // int startCol = (worldXPos) / Map.TILE_SIZE;
            // int startRow = (worldYPos) / Map.TILE_SIZE;
            // int targetCol = (ForkKnife.player.worldXPos) / Map.TILE_SIZE;
            // int targetRow = (ForkKnife.player.worldYPos) / Map.TILE_SIZE;

            // pathFinder.setNodes(startRow, startCol, targetCol, targetRow);

            // System.out.println(pathFinder.findPath());

            // // Moving enemy accordingly if the path is found
            // if (pathFinder.findPath() == true) {

            //     int destinationX = pathFinder.pathList.get(0).col * Map.TILE_SIZE;
            //     int destinationY = pathFinder.pathList.get(0).row * Map.TILE_SIZE;

            //     // FIX THIS CODE! ///////////////////////////////////////////

            //     worldXPos = destinationX;
            //     worldYPos = destinationY;

            //     int enemyTop = worldYPos;
            //     int enemyBottom = worldYPos + height;
            //     int enemyLeft = worldXPos;
            //     int enemyRight = worldXPos + width;
                
            //     if (destinationY > worldYPos) {
            //         worldYPos += vy;
            //         weapon.rotation = 180;
            //     } else if (destinationY < worldYPos) {
            //         worldYPos -= vy;
            //         weapon.rotation = 0;
            //     } else if (destinationX > worldXPos) {
            //         worldXPos += vx;
            //         weapon.rotation = 90;
            //     } else if (destinationX < worldXPos) {
            //         worldXPos -= vx;
            //         weapon.rotation = 270;
            //     }
            // }

            ////////////////////////////////////////////////////

        }
    }

    /**
     * Randomizes which weapon the enemy will have
     */
    void giveWeapon() {
        if(Math.random() < 0.5){
            weaponName="Fork";
        } else weaponName="Knife";
    }

}