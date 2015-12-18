package htw.fixtures;

import htw.HuntTheWumpus.Direction;

import static htw.fixtures.TestContext.game;

public class CanPlayerShootWithoutArrows {

    private String playerCavern;
    private Direction shootingDirection;

    public void setPlayerCavern(String playerCavern) {
        this.playerCavern = playerCavern;
    }

    public void setShootingDirection(Direction shootingDirection) {
        this.shootingDirection = shootingDirection;
    }

    public void reset() {
        TestContext.messages.clear();
    }

    public void execute() {
        game.setPlayerCavern(playerCavern);
        game.setQuiver(0);
        game.makeShootCommand(shootingDirection).execute();
    }

    public String noArrowsMessageGiven() {
        return TestContext.messages.contains("NO_ARROWS") ? "Yes" : "No";
    }
}
