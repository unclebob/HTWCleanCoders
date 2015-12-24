package htw.fixtures;

import htw.HuntTheWumpus.Direction;

import static htw.fixtures.TestContext.game;

public class DoArrowsAccumulateInFarthestCavern {

    private String playerCavern;
    private int arrowCount;
    private Direction shootingDirection;
    private int NUM_SHOTS = 3;
    private int[] shotCount = new int[NUM_SHOTS];

    public void setPlayerCavern(String playerCavern) {
        this.playerCavern = playerCavern;
    }

    public void setArrowCount(int arrowCount) {
        this.arrowCount = arrowCount;
    }

    public void setShootingDirection(Direction shootingDirection) {
        this.shootingDirection = shootingDirection;
    }

    public String farthestCavern() {
        return game.farthestCavern(playerCavern, shootingDirection);
    }

    public void reset() {
        TestContext.messages.clear();
        TestContext.game.initializeArrowsIn();
    }

    public void execute() {
        game.setPlayerCavern(playerCavern);
        game.setQuiver(arrowCount);
        for (int i = 0; i < NUM_SHOTS; i++)
            recordShotCountAfterShooting(i, farthestCavern());
    }

    private void recordShotCountAfterShooting(int i, String farthestCavern) {
        game.makeShootCommand(shootingDirection).execute();
        shotCount[i] = game.getArrowsInCavern(farthestCavern);
    }

    public int countAfterFirstShot() {
        return shotCount[0];
    }

    public int countAfterSecondShot() {
        return shotCount[1];
    }

    public int countAfterThirdShot() {
        return shotCount[2];
    }
}
