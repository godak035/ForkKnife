/**
 * ForkKnife.java
 * @author Avishan, Moiz, Samarth
 * 2024/01/23
 * Running the ForkKnife game and checking all actions/events + drawing graphics
 */

// General imports
import java.awt.*;
import hsa2.GraphicsConsole;
import java.util.*;

//Image imports
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.File;

//Timer imports
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ForkKnife implements ActionListener {

    // Running the game (actually)
    public static void main(String[] args) {
        new ForkKnife();
    }

    // Variables
    final static int scrW = 800, scrH = 800;
    final static int introW = 600, introH = 600;
    final static int helpW = 600, helpH = 600;
    final int maxPixel = 3200;
    final int stormStartingSize = 4525;
    final int stormClosingSpeed = 2;
    final int delayBetweenStormPhases = 20000;
    final int minimumStormWidth = 20;
    double gunShotTimerCounter;

    boolean removeBullet, removeEnemy, removeBulletForEnemy, removeBulletForObstacle, reducePlayerHealthInStorm, closeStorm;
    int markedBulletIndex, markedEnemyIndex, markedBulletForEnemyIndex, removeBulletForObstacleIndex;

    int secondsBetweenShots;
    int stormPhase;


    // Lists
    public static ArrayList<Enemy> enemies = new ArrayList<Enemy>();
    public static ArrayList<Rectangle> obstaclesForCollision = new ArrayList<Rectangle>();
    ArrayList<Bullet> bullets = new ArrayList<Bullet>();

    // Graphic consoles
    GraphicsConsole gcINTRO = new GraphicsConsole(introW, introH);
    GraphicsConsole gc = new GraphicsConsole(scrW, scrH);
    GraphicsConsole gcHelp = new GraphicsConsole(helpW, helpH);

    // Objects (of created classes)
    public static Player player = new Player();
    Map map = new Map();
    Weapon gun = new Weapon();
    Sound sound = new Sound();


    // RECTANGLES
    Rectangle logoRect = new Rectangle(introW / 2 - 250, 20, 500, 300);
    Rectangle storm = new Rectangle((Map.TILE_SIZE * Map.maxMapRow/2) - (stormStartingSize/2) ,(Map.TILE_SIZE * Map.maxMapCol/2) - (stormStartingSize/2), stormStartingSize, stormStartingSize);

    // TIMERS
    Timer gunShotDelayTimer = new Timer(100, this);
    Timer playerInStormTimer = new Timer(500, this);
    Timer stormDelayTimer = new Timer(delayBetweenStormPhases, this);

    // BUTTONS
    Rectangle clickPlay = new Rectangle(introW / 2 - 75, introH / 6 * 3, 150, 50);
    Rectangle clickHelp = new Rectangle(introW / 2 - 75, introH / 6 * 4, 150, 50);
    Rectangle clickQuit = new Rectangle(introW / 2 - 75, introH / 6 * 5, 150, 50);
    Rectangle returnToIntro = new Rectangle(20, 20, 50, 50);

    // IMAGES + RECTS
    BufferedImage logo;
    BufferedImage playerImg;
    BufferedImage enemyImg;
    BufferedImage gunImg;
    BufferedImage bulletImg;
    BufferedImage helpScreen;
    BufferedImage backgroundWater;
    BufferedImage knife;
    BufferedImage fork;
    static BufferedImage rock;
    static BufferedImage bush;
    static BufferedImage grass;

    // Constructor
    ForkKnife() {

        // Setup functions
        playMusic(0);
        loadImages();
        gcSetup();
        gcHelpSetup();
        gcIntroSetup();

        gcIntroDraw();

        // MAIN GAME LOOP
        while (player.isAlive && enemies.size() > 0) {

            // UPDATING ALL RECTANGLE'S X AND Y POSITIONS
            setXAndYValues();
            setStormPosition();

            // Updating storm
            closeStorm();
            checkPlayerInStorm();

            // Updating everything related to the player
            playerEnemyCollision();
            checkPlayerAlive();
            movePlayer();

            // Everything related to enemy
            checkEnemyHealth();
            updateEnemyScreenPosition();
            updateEnemyAggroSquare();
            updateWeaponCoordinates();
            checkEnemyAggro();
            enemyChasePlayer();
            checkEnemyInStorm();

            // Updating everything related to the gun
            updateGunCoordinates();
            setGunRotation();

            // Updating everything related to the bullet(s)
            removeBullets();
            updateBulletScreenPosition();
            bulletCollisionWithEnemy();
            bulletCollisionWithObstacle();
            addAndSetBullet();
            moveBullets();
            drawGraphics();

        }

        if (player.isAlive) {
            gc.showDialog("You won! All the enemies are defeated!", "ROYALE VICTORY");
        } else if (!player.isAlive) {
            gc.showDialog("You lost. The enemy defeated you.", "Better Luck Next Time!");
        }

        gc.setVisible(false);
        gc.dispose();
        System.exit(0);

    }// End ForkKnife Constructor

    /**
     * Setting up the main game screen
     */
    void gcSetup() {
        gc.setFont(new Font("Impact", Font.PLAIN, 30));
        gc.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        gc.setVisible(false);
        gc.setTitle("ForkKnife");
        gc.setAntiAlias(true);
        gc.setLocationRelativeTo(null);
        gc.setBackgroundColor(new Color(0, 210, 255));
        gc.clear();
        gc.enableMouse();
        gc.enableMouseMotion();

        secondsBetweenShots = 1;
        gunShotTimerCounter = 1;
        closeStorm = false;
        reducePlayerHealthInStorm = false;
        removeBullet = false;
        removeEnemy = false;

        map.getSpawns();
        map.setSpawns();
        map.getObstacles();

    }// gcSetup end

    /**
     * Setting up the intro screen
     */
    void gcIntroSetup() {
        gcINTRO.setFont(new Font("Impact", Font.PLAIN, 30));
        gcINTRO.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        gcINTRO.setVisible(true);
        gcINTRO.setTitle("How to Play");
        gcINTRO.setAntiAlias(true);
        gcINTRO.setLocationRelativeTo(null);
        gcINTRO.setBackgroundColor(new Color(22, 143, 230));
        gcINTRO.clear();
        gcINTRO.enableMouse();
        gcINTRO.enableMouseMotion();
        
    }// IntroSetup

    /**
     * Setting up the help screen
     */
    void gcHelpSetup() {
        gcHelp.setFont(new Font("Impact", Font.PLAIN, 30));
        gcHelp.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        gcHelp.setVisible(false);
        gcHelp.setTitle("Help");
        gcHelp.setAntiAlias(true);
        gcHelp.setLocationRelativeTo(null);
        gcHelp.setBackgroundColor(new Color(22, 143, 230));
        gcHelp.clear();
        gcHelp.enableMouse();
        gcHelp.enableMouseMotion();
    }

    /**
     * Drawing everything in the intro screen
     * Also continously checking if any "button" (rectangle) is clicked
     */
    void gcIntroDraw() {
        gcINTRO.setFont(new Font("Impact", Font.PLAIN, 30));
        gcINTRO.drawImage(logo, logoRect.x, logoRect.y);
        gcINTRO.setColor(Color.BLACK);
        gcINTRO.fillRect(clickPlay.x, clickPlay.y, clickPlay.width, clickPlay.height);
        gcINTRO.fillRect(clickHelp.x, clickHelp.y, clickHelp.width, clickHelp.height);
        gcINTRO.fillRect(clickQuit.x, clickQuit.y, clickQuit.width, clickQuit.height);
        gcINTRO.setColor(Color.CYAN);
        gcINTRO.drawString("PLAY", clickPlay.x + 45, clickPlay.y + 37);
        gcINTRO.drawString("HELP", clickHelp.x + 45, clickHelp.y + 37);
        gcINTRO.drawString("QUIT", clickQuit.x + 45, clickQuit.y + 37);

        while (true) {

            // DO NOT DELETE THIS. IT MAKES EVERYTHING WORK!
            System.out.print("");

            boolean checkClickPlay = checkButtonClick(clickPlay, gcINTRO);
            boolean checkClickHelp = checkButtonClick(clickHelp, gcINTRO);
            boolean checkClickQuit = checkButtonClick(clickQuit, gcINTRO);

            if (checkClickPlay) {

                // Starting timers
                gunShotDelayTimer.start();
                stormDelayTimer.start();

                // Switching sounds
                sound.stop();    
                playMusic(1);

                gc.setVisible(true);
                gcINTRO.setVisible(false);
                gcHelp.setVisible(false);
                break;

            }

            if (checkClickHelp) {
                gc.setVisible(false);
                gcHelp.setVisible(true);
                gcINTRO.setVisible(false);
                gcHelpDraw();
                break;
            }

            if (checkClickQuit) {
                gc.setVisible(false);
                gcINTRO.setVisible(false);
                gcHelp.setVisible(false);
                gc.dispose();
                gcINTRO.dispose();
                gcHelp.dispose();
                break;
            }

        }

    }

    /**
     * Drawing everything in the help menu
     * Also continously checking if the back button is clicked
     */
    void gcHelpDraw() {
        gcHelp.setFont(new Font("Impact", Font.PLAIN, 50));
        gcHelp.drawImage(helpScreen, 0, 0);
        gcHelp.setColor(Color.BLACK);
        gcHelp.fillOval(returnToIntro.x, returnToIntro.y, returnToIntro.width, returnToIntro.height);
        gcHelp.setColor(Color.CYAN);
        gcHelp.drawString("<", returnToIntro.x + 10, returnToIntro.y + 45);

        while (true) {

            // DO NOT DELETE THIS. IT MAKES EVERYTHING WORK!
            System.out.print("");

            if (checkButtonClick(returnToIntro, gcHelp)) {

                gcINTRO.setVisible(true);
                gc.setVisible(false);
                gcHelp.setVisible(false);
                gcIntroDraw();
                break;

            }

        }

    }

    /**
     * Checks if a "button" (rectangle) is being clicked
     * 
     * @param rect : the rectangle to check for collision
     * @param gc   : the graphics console in which the button is located
     * @return : true if button is clicked, false otherwise
     */
    boolean checkButtonClick(Rectangle rect, GraphicsConsole gc) {
        if (rect.contains(gc.getMouseX(), gc.getMouseY())) {
            if (gc.getMouseClick() > 0) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Starts the music
     * @param i : The song to be played (1 for intro screen, 0 for main game)
     */
    public void playMusic(int i) {
        sound.setFile(i);
        sound.play();
        sound.loop();
    }
	
    /**
     * Stops the music
     */
    public void stopMusic() {
	    sound.stop();
    }

    /**
     * Setting the RECTANGLE x and y values for all entities 
     * Needed for .contains and .intersects methods
     */
    void setXAndYValues() {
        player.x = player.worldXPos;
        player.y = player.worldYPos;

        for (Enemy enemy : enemies) {
            enemy.x = enemy.worldXPos;
            enemy.y = enemy.worldYPos;
        }

        for (Bullet bullet : bullets) {
            bullet.x = bullet.worldXPos;
            bullet.y = bullet.worldYPos;
        }
    }

    /**
     * Set's a diagonal speed for the player based on vx and vy
     */
    private int getDiagonalSpeed() {
        return (int) (Math.hypot(player.vx, player.vy) / 1.75);
    }

    /**
     * Checks for keyboard input for player movement, and moves player accordingly
     * Also checks to make sure the direction in which the player is moving is valid
     */
    void movePlayer() {
        if (gc.isKeyDown(87) && gc.isKeyDown(65) && player.worldYPos > 0 && player.worldXPos > 0
                && !obstacleAbovePlayer() && !obstacleLeftOfPlayer()) { // UP AND LEFT
            player.worldYPos -= getDiagonalSpeed();
            player.worldXPos -= getDiagonalSpeed();
        } else if (gc.isKeyDown(87) && gc.isKeyDown(68) && (player.worldYPos >= 0)
                && (player.worldXPos < (maxPixel - player.width)) && !obstacleAbovePlayer()
                && !obstacleRightOfPlayer()) { // UP
            // AND
            // RIGHT
            player.worldYPos -= getDiagonalSpeed();
            player.worldXPos += getDiagonalSpeed();
        } else if (gc.isKeyDown(83) && gc.isKeyDown(65) && (player.worldYPos <= (maxPixel - player.height))
                && player.worldXPos >= 0 && !obstacleBelowPlayer() && !obstacleLeftOfPlayer()) { // DOWN
            // AND
            // LEFT
            player.worldYPos += getDiagonalSpeed();
            player.worldXPos -= getDiagonalSpeed();
        } else if (gc.isKeyDown(83) && gc.isKeyDown(68) && (player.worldYPos <= (maxPixel - player.height))
                && (player.worldXPos <= (maxPixel - player.width)) && !obstacleBelowPlayer()
                && !obstacleRightOfPlayer()) { // DOWN
            // AND
            // RIGHT
            player.worldYPos += getDiagonalSpeed();
            player.worldXPos += getDiagonalSpeed();
        } else {
            // W (up)
            if (gc.isKeyDown(87) && player.worldYPos >= 0 && !obstacleAbovePlayer()) {
                player.worldYPos -= player.vy;
            }

            // A (left)
            if (gc.isKeyDown(65) && player.worldXPos >= 0 && !obstacleLeftOfPlayer()) {
                player.worldXPos -= player.vx;
            }

            // S (Down)
            if (gc.isKeyDown(83) && (player.worldYPos <= (maxPixel - player.height)) && !obstacleBelowPlayer()) {
                player.worldYPos += player.vy;
            }

            // D (Right)
            if (gc.isKeyDown(68) && (player.worldXPos <= (maxPixel - player.width)) && !obstacleRightOfPlayer()) {
                player.worldXPos += player.vx;
            }
        }

        // Setting player to collision point to make sure player doesn't glitch out of
        // map [BOUNDRIES ONLY]
        if (player.worldXPos < 0) {
            player.worldXPos = 0;
        }
        if (player.worldXPos > (maxPixel - player.width)) {
            player.worldXPos = (maxPixel - player.width);
        }

        if (player.worldYPos < 0) {
            player.worldYPos = 0;
        }
        if (player.worldYPos > (maxPixel - player.height)) {
            player.worldYPos = (maxPixel - player.height);
        }

    }// end move player

    /**
     * Checks player collisions with obstacle (ABOVE)
     * 
     * @return : true if there is an obstacle above player, false otherwise
     */
    private boolean obstacleAbovePlayer() {
        for (Rectangle r : obstaclesForCollision) {
            // if obstacle contains TOP MIDDLE of player
            if (r.contains(player.worldXPos + player.width / 2, player.worldYPos)) {
                // player.worldYPos = (r.y + player.height);
                return true;
            }
        }
        return false;
    }//end obstacle AbovePlayer

    /**
     * Checks player collisions with obstacle (BELOW)
     * 
     * @return : true if there is an obstacle below player, false otherwise
     */
    private boolean obstacleBelowPlayer() {
        for (Rectangle r : obstaclesForCollision) {
            // if obstacle contains BOTTOM MIDDLE of player
            if (r.contains(player.worldXPos + player.width / 2, player.worldYPos + player.height)) {
                // player.worldYPos = (r.y - player.height);
                return true;
            }
        }
        return false;
    }//end obstacleBelowPlayer

    /**
     * Checks player collisions with obstacle (LEFT)
     * 
     * @return : true if there is an obstacle to the left of player, false otherwise
     */
    private boolean obstacleLeftOfPlayer() {
        for (Rectangle r : obstaclesForCollision) {
            // if obstacle contains LEFT MIDDLE of player
            if (r.contains(player.worldXPos, player.worldYPos + player.height / 2)) {
                // player.worldXPos = (r.x + player.width);
                return true;
            }
        }
        return false;
    }//end obstacleLeftOfPlayer

    /**
     * Checks player collisions with obstacle (RIGHT)
     * 
     * @return : true if there is an obstacle to the right of player, false
     *         otherwise
     */
    private boolean obstacleRightOfPlayer() {
        for (Rectangle r : obstaclesForCollision) {
            // if obstacle contains RIGHT MIDDLE of player
            if (r.contains((player.worldXPos + player.width), (player.worldYPos + player.height / 2))) {
                // player.worldXPos = (r.x - player.width);
                return true;
            }
        }
        return false;
    }//end obstacleRightOfPlayer

    /**
     * Checks player collision with enemy
     * @return : true if the player collides with enemy (game over if this happens), false otherwise
     */
    void playerEnemyCollision() {
        for (Enemy e : enemies) {
            if (e.intersects(player)) {
                player.health -= 100;
            }
        }
    }

    void checkPlayerAlive() {
        if(player.health <= 0) {
            player.isAlive = false;
        }
    } 

    /**
     * Sets the rotation at which the gun image will be rotated, depending on where
     * the cursor is
     */
    void setGunRotation() {

        gun.rotation = (int) Math
                .toDegrees(Math.atan((double) Math.abs(gc.getMouseY() - (player.screenYPos + player.height / 2))
                        / (double) Math.abs(gc.getMouseX() - (player.screenXPos + player.width / 2))));

        if (gc.getMouseX() <= (player.screenXPos + player.width / 2)) {
            if (gc.getMouseY() > (player.screenYPos + player.height / 2)) {
                gun.rotation = 270 - gun.rotation;
            }
            if (gc.getMouseY() <= (player.screenYPos + player.height / 2)) {
                gun.rotation += 270;
            }
        }

        if (gc.getMouseX() > (player.screenXPos + player.width / 2)) {
            if (gc.getMouseY() < (player.screenYPos + player.height / 2)) {
                gun.rotation = 90 - gun.rotation;
            }
            if (gc.getMouseY() >= (player.screenYPos + player.height / 2)) {
                gun.rotation += 90;
            }
        }

    }

    /**
     * Sets the gun's x and y position relative to the center of the player
     */
    void updateGunCoordinates() {
        gun.x = player.screenXPos + player.width / 2 - gun.width / 2;
        gun.y = player.screenYPos - gun.height + player.height / 2;
    }

    void updateWeaponCoordinates() {
        for(Enemy enemy : enemies) {
            enemy.weapon.x = enemy.screenXPos + enemy.width / 2 - enemy.weapon.width / 2;
            enemy.weapon.y = enemy.screenYPos - enemy.weapon.height + enemy.height / 2;
        }
    }

    /**
     * Determines if a gun is shot after the interval of time since the last gunshot
     * 
     * @return : true if gun is shot at a correct time, false otherwise
     */
    private boolean gunIsShot() {
        if (gunShotTimerCounter > secondsBetweenShots) { //
            if (gc.getMouseButton(0)) {
                gunShotTimerCounter = 0;
                return true;
            }
        }
        return false;
    }

    /**
     * Determines the distance from the player to mouse (using pythagorean theorem)
     * 
     * @return : the distance from player to mouse
     */
    private double getDistancePlayerToMouse() {
        return (Math.hypot(Math.abs(gc.getMouseX() - (player.screenXPos + player.width / 2)),
                Math.abs(gc.getMouseY() - (player.screenYPos + player.height / 2))));
    }

    /**
     * If a bullet is shot, then adds a new bullet to the bullets list, setting up
     * all orig values (world, screen, speeds)
     */
    void addAndSetBullet() {
        if (gunIsShot()) {
            Bullet bullet = new Bullet(gun.rotation);

            // Setting ORIGINAL SCREEN POSITION
            // The following 2 "temp" variables are calculated by normalizing the vector
            // through an equation found oneline!
            double tempBulletScreenX = (double) (player.screenXPos + player.width / 2)
                    + ((double) gun.height / getDistancePlayerToMouse()
                            * ((double) this.gc.getMouseX() - (double) (player.screenXPos + player.width / 2)));
            double tempBulletScreenY = (double) (player.screenYPos + player.height / 2)
                    + ((double) gun.height / getDistancePlayerToMouse()
                            * ((double) this.gc.getMouseY() - (double) (player.screenYPos + player.height / 2)));
            bullet.screenXPos = (int) tempBulletScreenX;
            bullet.screenYPos = (int) tempBulletScreenY;

            // Setting ORIGINAL WORLD POSITION
            bullet.worldXPos = bullet.screenXPos + player.worldXPos - player.screenXPos;
            bullet.worldYPos = bullet.screenYPos + player.worldYPos - player.screenYPos;

            // Setting bullet origin x and y
            bullet.xAtShot = bullet.worldXPos;
            bullet.yAtShot = bullet.worldYPos;

            // SETTING BULLET SPEED (AND DIRECTION -- using trig!)
            bullet.vx = Math.abs(Bullet.SPEED * Math.sin(Math.toRadians(gun.rotation)));
            bullet.vy = Math.abs(Bullet.SPEED * Math.cos(Math.toRadians(gun.rotation)));

            if (gc.getMouseX() > (player.screenXPos + player.width / 2)
                    && gc.getMouseY() < (player.screenYPos + player.height / 2)) {
                bullet.vy *= -1;
            }

            if (gc.getMouseX() < (player.screenXPos + player.width / 2)
                    && gc.getMouseY() < (player.screenYPos + player.height / 2)) {
                bullet.vx *= -1;
                bullet.vy *= -1;
            }

            if (gc.getMouseX() < (player.screenXPos + player.width / 2)
                    && gc.getMouseY() > (player.screenYPos + player.height / 2)) {
                bullet.vx *= -1;
            }

            // ADDING BULLET TO LIST OF BULLETS
            bullets.add(bullet);

        }
    }

    /**
     * Moves the bullets based on their worldPos and velocities
     */
    void moveBullets() {
        for (Bullet bullet : bullets) {
            bullet.worldXPos += (int) Math.round(bullet.vx);
            bullet.worldYPos += (int) Math.round(bullet.vy);
        }
    }

    /**
     * Updates the screen position of each bullet to allow for easier drawing
     */
    void updateBulletScreenPosition() {
        for (Bullet bullet : bullets) {
            bullet.screenXPos = bullet.worldXPos - player.worldXPos + player.screenXPos;
            bullet.screenYPos = bullet.worldYPos - player.worldYPos + player.screenYPos;
        }
    }

    /**
     * Removes any bullets that have travelled any longer than the max x value
     */
    void removeBullets() {
        for (Bullet bullet : bullets) {
            bullet.xFromShot = Math.abs(bullet.worldXPos - bullet.xAtShot);
            bullet.yFromShot = Math.abs(bullet.worldYPos - bullet.yAtShot);
            if (bullet.xFromShot >= Bullet.MAX_X || bullet.yFromShot >= Bullet.MAX_Y) {
                removeBullet = true;
                markedBulletIndex = bullets.indexOf(bullet);
            }
        }
        if (removeBullet) {
            bullets.remove(markedBulletIndex);
            markedBulletIndex = -1;
            removeBullet = false;
        }

    }

    /**
     * Checks to see if enemy died and then removes the enemy 
     */
    void checkEnemyHealth() {
        for (Enemy e : enemies) {
            if (e.health <= 0) {
                removeEnemy = true;
                markedEnemyIndex = enemies.indexOf(e);

            }
        }
        if (removeEnemy) {
            enemies.remove(markedEnemyIndex);
            markedEnemyIndex = -1;
            removeEnemy = false;
        }
    }

    /**
     * Check if bullets collide with enemies, if they do, lower health
     */
    void bulletCollisionWithEnemy() {
        for (Enemy e : enemies) {
            for (Bullet b : bullets) {
                if (e.intersects(b)) {
                    e.health -= 1;
                    removeBulletForEnemy = true;
                    markedBulletForEnemyIndex = bullets.indexOf(b);
                }
            }
        }

        if (removeBulletForEnemy) {
            bullets.remove(markedBulletForEnemyIndex);
            markedBulletForEnemyIndex = -1;
            removeBulletForEnemy = false;
        }
    }

    /**
     * Checks to see if the bullet collided with any obstacles
    */
    void bulletCollisionWithObstacle() {
        for (Rectangle r : obstaclesForCollision) {
            for (Bullet b : bullets) {
                if (r.intersects(b)) {
                    removeBulletForObstacle = true;
                    removeBulletForObstacleIndex = bullets.indexOf(b);
                }
            }
        }

        if (removeBulletForObstacle) {
            bullets.remove(removeBulletForObstacleIndex);
            removeBulletForObstacleIndex = -1;
            removeBulletForObstacle = false;
        }
    }

    /**
     * Updating each enemy's position on the screen (referencing world position)
     */
    void updateEnemyScreenPosition() {
        for (Enemy enemy : enemies) {
            enemy.screenXPos = enemy.worldXPos - player.worldXPos + player.screenXPos;
            enemy.screenYPos = enemy.worldYPos - player.worldYPos + player.screenYPos;
        }
    }

    /**
     * Updating the square in which an enemy is going to attack the player
     */
    void updateEnemyAggroSquare() {
        for (Enemy enemy : enemies) {
            enemy.aggroRect.x = enemy.worldXPos + enemy.width / 2 - enemy.aggroSquareSize / 2;
            enemy.aggroRect.y = enemy.worldYPos + enemy.height / 2 - enemy.aggroSquareSize / 2;
        }
    }

    /**
     * Determines if an enemy is going to attack the player or not
     */
    void checkEnemyAggro() {
        for (Enemy enemy : enemies) {
            if (enemy.aggroRect.contains(player.worldXPos + player.width / 2, player.worldYPos + player.height / 2)) {
                enemy.isAggro = true;
            } else {
                double enemyDistanceFromCenter = Math.sqrt(Math.abs(Math.pow(enemy.worldXPos - (Map.TILE_SIZE*Map.maxMapCol/2), 2.0))
                        + Math.abs(Math.pow(enemy.worldYPos - (Map.TILE_SIZE*Map.maxMapRow/2), 2.0)));
                if(enemyDistanceFromCenter < storm.width/2) {
                    enemy.isAggro = false;
                }
            }

            if(enemy.health < 3) {
                enemy.isAggro = true;
            }

        }
    }

    /**
     * Enemies will chase player (ONLY IF AGGRO == true)
     */
    void enemyChasePlayer() {
        for (Enemy enemy : enemies) {
            enemy.chasePlayer();
        }
    }

    /**
     * Setting the position of the storm in relation to the middle of the map
     */
    void setStormPosition() {
        int middleOfMapX = Map.TILE_SIZE * Map.maxMapRow/2;
        int middleOfMapY = Map.TILE_SIZE * Map.maxMapCol/2;

        storm.x = middleOfMapX - storm.width/2;
        storm.y = middleOfMapY - storm.height/2;
    }

    /**
     * @return : the storm's SCREEN x position
     */
    int getStormScreenPosX() {
        return (storm.x - player.worldXPos + player.screenXPos);
    }

    /**
     * @return : the storm's SCREEN y position
     */
    int getStormScreenPosY() {
        return (storm.y - player.worldYPos + player.screenYPos);
    }

    /**
     * Lower's the size of the storm's circle
     */
    void closeStorm() {
        if(closeStorm) {
            if(storm.width > minimumStormWidth) {
                storm.width -= stormClosingSpeed;
                storm.height -= stormClosingSpeed;
            }
        }
    }
    /**
     * Checks to see if player is in the storm (and allows for damage reduction)
     */
    void checkPlayerInStorm() {
        double playerDistanceFromCenter = Math.sqrt(Math.abs(Math.pow(player.worldXPos - (Map.TILE_SIZE*Map.maxMapCol/2), 2.0)) + Math.abs(Math.pow(player.worldYPos - (Map.TILE_SIZE*Map.maxMapRow/2), 2.0)));

        // Checking if player is outside the circle
        if(playerDistanceFromCenter > storm.width/2) {
            reducePlayerHealthInStorm = true;
            playerInStormTimer.start();
        } else {
            reducePlayerHealthInStorm = false;
            playerInStormTimer.stop();
        }
    }

    /**
     * Checks to see if enemy is in the storm
     */
    void checkEnemyInStorm() {
        for(Enemy enemy : enemies) {
            double enemyDistanceFromCenter = Math.sqrt(Math.abs(Math.pow(enemy.worldXPos - (Map.TILE_SIZE*Map.maxMapCol/2), 2.0)) + Math.abs(Math.pow(enemy.worldYPos - (Map.TILE_SIZE*Map.maxMapRow/2), 2.0)));
            
            if(enemyDistanceFromCenter > storm.width/2) {
                enemy.isAggro = true;
            }
        }
    }

    /**
     * Display important information relating to the storm
     */
    void showInformation() {
        gc.drawString("ENEMIES: " + enemies.size(), 20, 40);
        gc.drawString("HEALTH: " + player.health, 20, 100);
        gc.drawString("STORM PHASE: " + stormPhase, 20, 160);
    }

    /**
     * Loads in all the images
     * Try/catch statement provided by Mr. Campbell
     */
    void loadImages() {

        try {
            // Images load here
            playerImg = ImageIO.read(new File("Assets/Images/ForkKnifePlayer.png"));
            enemyImg = ImageIO.read(new File("Assets/Images/ForkKnifeEnemy.png"));
            gunImg = ImageIO.read(new File("Assets/Images/ForkKnifeGun.png"));
            bulletImg = ImageIO.read(new File("Assets/Images/ForkKnifeBullet.png"));
            logo = ImageIO.read(new File("Assets/Images/Forknife.png"));
            helpScreen = ImageIO.read(new File("Assets/Images/helpScreen.png"));
            backgroundWater = ImageIO.read(new File("Assets/Images/ForkKnifeBackgroundWater.png"));
            rock = ImageIO.read(new File("Assets/Images/ForkKnifeRock.png"));
            bush = ImageIO.read(new File("Assets/Images/ForkKnifeBush.png"));
            grass = ImageIO.read(new File("Assets/Images/ForkKnifeGrass.png"));
            knife = ImageIO.read(new File("Assets/Images/Knife.png"));
            fork = ImageIO.read(new File("Assets/Images/Fork.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Draws ALL the graphics, including the map
     */
    void drawGraphics() {

        synchronized (gc) {
            gc.clear();

            gc.setRotation(0, 0, 0);

            // Drawing map
            map.drawMap(gc);

            //Drawing storm
            gc.setStroke(20);
            gc.setColor(new Color(157,77,187));
            gc.drawOval(getStormScreenPosX(), getStormScreenPosY(), storm.width, storm.height);

            // Drawing enemy WEAPONS
            for (Enemy enemy : enemies) {
                gc.setRotation(enemy.weapon.rotation, enemy.screenXPos + (enemy.width / 2), enemy.screenYPos + (player.height / 2));
                if(enemy.weaponName.equals("Fork")){
                    gc.drawImage(fork, enemy.weapon.x, enemy.weapon.y);
                }
                if(enemy.weaponName.equals("Knife")){
                    gc.drawImage(knife, enemy.weapon.x, enemy.weapon.y);
                }
            }

            // Drawing enemies
            gc.setRotation(0, 0, 0);
            for (Enemy enemy : enemies) {
                gc.drawImage(enemyImg, enemy.screenXPos, enemy.screenYPos);
            }

            for (Bullet bullet : bullets) {
                // Setting rotation as per bullet rotation, in relation to the midpoint of the
                // bullet
                gc.setRotation(bullet.rotation, bullet.screenXPos + bullet.width / 2,
                        bullet.screenYPos + bullet.height / 2);
                gc.drawImage(bulletImg, bullet.screenXPos, bullet.screenYPos);
            }

            // Setting rotation for player + gun
            gc.setRotation(gun.rotation, player.screenXPos + player.width / 2, player.screenYPos + player.height / 2);

            // Drawing gun
            gc.drawImage(gunImg, gun.x, gun.y);

            // Drawing player
            gc.drawImage(playerImg, player.screenXPos, player.screenYPos);

            // TEXT
            gc.setRotation(0, 0, 0);
            gc.setColor(Color.BLACK);
            showInformation();
        }

        gc.sleep(10);

    }

    //Override comment for timers
    @Override
    public void actionPerformed(ActionEvent e) {
        // Checking if gunShotDelayTimer "ticked"
        if (e.getSource() == gunShotDelayTimer) {
            gunShotTimerCounter += 0.1;
        }

        // Checking if playerInStormTimer "ticked"
        if(e.getSource() == playerInStormTimer) {
            if(reducePlayerHealthInStorm) {
                player.health -= 2;
            }
        }

        //
        if(e.getSource() == stormDelayTimer) {
            if(closeStorm == false) {
                stormPhase++;
            }
            closeStorm = !closeStorm;
        }
    }
}