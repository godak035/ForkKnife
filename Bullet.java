/**
 * Bullet.java
 * @author Avishan, Moiz, Samarth
 * 2024/01/23
 * Sets all attributes for the bullets
 */
import java.awt.Rectangle;

public class Bullet extends Rectangle {

    //Variables
    double vx, vy;
    int size = 20; //size of the bullet

    final static int SPEED = 10;
    final static int MAX_X = 300;
    final static int MAX_Y = 300;

    int xAtShot, yAtShot, xFromShot, yFromShot;

    int worldXPos;
    int worldYPos;
    int screenXPos;
    int screenYPos;

    int rotation;
    
    //Constructor 
    Bullet(int rotation) {
        width = height = size;

        this.rotation = rotation;

    }

}