package htw.fixtures;

import htw.HuntTheWumpus;

import static htw.fixtures.TestContext.game;

public class DoesArrowKillWumpus {

    private String playerCavern;
    private String wumpusCavern;
    private int arrowsForPlayer;
    private HuntTheWumpus.Direction shootingDirection;

    public void setPlayerCavern(String playerCavern) {
        this.playerCavern = playerCavern;
    }

    public void setWumpusCavern(String wumpusCavern) {
        this.wumpusCavern = wumpusCavern;
    }

    public void setArrowsForPlayer(int arrowsForPlayer) {
        this.arrowsForPlayer = arrowsForPlayer;
    }

    public void setShootingDirection(HuntTheWumpus.Direction shootingDirection) {
        this.shootingDirection = shootingDirection;
    }

    public void reset() {
        TestContext.messages.clear();
        TestContext.game.initializeArrowsIn();
    }

    public void execute() {
        game.setPlayerCavern(playerCavern);
        game.setWumpusCavern(wumpusCavern);
        game.setQuiver(arrowsForPlayer);
        game.makeShootCommand(shootingDirection).execute();
    }

    public String farthestCavern() {
        return game.farthestCavern(playerCavern, shootingDirection);
    }

    public int arrowsInFarthestCavern() {
        return game.getArrowsInCavern(farthestCavern());
    }

    public String wumpusKilledMessage() {
        return TestContext.messages.contains("WUMPUS_KILLED") ? "Yes" : "No";
    }

}
