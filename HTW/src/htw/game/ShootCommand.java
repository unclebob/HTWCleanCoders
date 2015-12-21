package htw.game;

import java.util.List;

import static htw.HuntTheWumpus.Direction;

class ShootCommand extends GameCommand {
  private Direction direction;

  public ShootCommand(Direction direction, HuntTheWumpusGame game) {
    super(game);
    this.direction = direction;
  }

  public void processCommand() {
    if (game.quiverEmpty())
      messageReceiver.noArrows();
    else
      shootArrow();
  }

  private void shootArrow() {
    messageReceiver.arrowShot();
    game.decrementQuiverBy(1);
    ArrowTracker arrowTracker = new ArrowTracker(map.getPlayerCavern()).trackArrow(direction);
    if (arrowTracker.arrowHitSomething())
      return;
    incrementArrowsInCavern(arrowTracker.getArrowCavern());
  }

  private void incrementArrowsInCavern(Cavern arrowCavern) {
    map.incrementArrowsIn(arrowCavern, 1);
  }

  private class ArrowTracker {
    private boolean hitSomething = false;
    private Cavern arrowCavern;

    public ArrowTracker(Cavern startingCavern) {
      arrowCavern = startingCavern;
    }

    boolean arrowHitSomething() {
      return hitSomething;
    }

    public Cavern getArrowCavern() {
      return arrowCavern;
    }

    public ArrowTracker trackArrow(Direction direction) {
      checkHitSomething(direction);

      if (!hitSomething)
        checkWallShot();

      return this;
    }

    private void checkHitSomething(Direction direction) {
      for (Cavern c : arrowPath(direction))
        if (updateArrowCavernWith(c) && hitSomething())
          return;
    }

    private List<Cavern> arrowPath(Direction direction) {
      return take(103, arrowCavern.getCavernsGoing(direction));
    }

    private boolean updateArrowCavernWith(Cavern newArrowCavern) {
      arrowCavern = newArrowCavern;
      return true;
    }

    private boolean hitSomething() {
      hitSomething = shotSelfInBack() || shotWumpus();
      return hitSomething;
    }

    private boolean shotWumpus() {
      if (arrowCavern.equals(map.getWumpusCavern())) {
        messageReceiver.playerKillsWumpus();
        return true;
      }
      return false;
    }

    private boolean shotSelfInBack() {
      if (arrowCavern.equals(map.getPlayerCavern())) {
        messageReceiver.playerShootsSelfInBack();
        return true;
      }
      return false;
    }

    private void checkWallShot() {
      if (arrowCavern.equals(map.getPlayerCavern()))
        messageReceiver.playerShootsWall();
    }

    private <T> List<T> take(int nElements, List<T> list) {
      return list.subList(0, Math.min(list.size(), nElements - 1));
    }
  }
}
