/**
 * Player.java
 * @author Avishan, Moiz, Samarth
 * 2024/01/23
 * Sets all attributes for the player
 */
import java.awt.Rectangle;

public class Player extends Rectangle {
    
//variables
    int size = 64;

    int vy, vx;

    int worldXPos;
    int worldYPos;
    int screenXPos;
    int screenYPos;

    boolean isAlive;

    int health;

//constructor
    Player() {

        isAlive = true;

        width = height = size;

        this.screenXPos = ForkKnife.scrW / 2 - this.width / 2;
        this.screenYPos = ForkKnife.scrH / 2 - this.height / 2;

        vx = vy = 5;

        health = 100;

    }
}