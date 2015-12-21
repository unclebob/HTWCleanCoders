package htw.game;

import htw.HtwMessageReceiver;
import htw.HuntTheWumpus;

import java.util.*;

public class HuntTheWumpusGame implements HuntTheWumpus {
  private HuntTheWumpusMap map = new HuntTheWumpusMap();
  private HtwMessageReceiver messageReceiver;

  private int quiver = 0;

  public HuntTheWumpusGame(HtwMessageReceiver receiver) {
    this.messageReceiver = receiver;
  }

  private Cavern cavern(String cavernName) {
    return map.getCavernNamed(cavernName);
  }

  public void setPlayerCavern(String playerCavern) {
    map.setPlayerCavern(cavern(playerCavern));
  }

  public String getPlayerCavern() {
    return map.getPlayerCavern().getName();
  }

  private void reportStatus() {
    reportAvailableDirections();
    reportSpecialLocations();
  }

  private void reportAvailableDirections() {
    map.availableDirections().forEach(messageReceiver::passage);
  }

  private void reportSpecialLocations() {
    if (map.batCavernIsNear())
      messageReceiver.hearBats();
    if (map.pitCavernIsNear())
      messageReceiver.hearPit();
    if (map.wumpusCavernIsNear())
      messageReceiver.smellWumpus();
  }

  public void addBatCavern(String cavern) {
    map.addBatCavern(cavern(cavern));
  }

  public void addPitCavern(String cavern) {
    map.addPitCavern(cavern(cavern));
  }

  public void setWumpusCavern(String wumpusCavern) {
    map.setWumpusCavern(cavern(wumpusCavern));
  }

  public String getWumpusCavern() {
    return map.getWumpusCavern().getName();
  }

  protected void moveWumpus() {
    map.moveWumpus();
  }

  public void setQuiver(int arrows) {
    this.quiver = arrows;
  }

  public int getQuiver() {
    return quiver;
  }

  public Integer getArrowsInCavern(String cavern) {
    return map.getArrowsInCavern(cavern(cavern));
  }

  public void clearMap() {
    map.clear();
  }

  public void connectCavern(String from, String to, Direction direction) {
    map.connectCavern(cavern(from), cavern(to), direction);
  }

  public String findDestination(String cavern, Direction direction) {
    return cavern(cavern).findDestination(direction).getName();
  }

  public Command makeRestCommand() {
    return new RestCommand();
  }

  public Command makeShootCommand(Direction direction) {
    return new ShootCommand(direction);
  }

  public Command makeMoveCommand(Direction direction) {
    return new MoveCommand(direction);
  }

  public abstract class GameCommand implements Command {
    public void execute() {
      processCommand();
      moveWumpus();
      checkWumpusMovedToPlayer();
      reportStatus();
    }

    protected void checkWumpusMovedToPlayer() {
      if (map.getPlayerCavern().equals(map.getWumpusCavern()))
        messageReceiver.wumpusMovesToPlayer();
    }

    protected abstract void processCommand();

  }

  private class RestCommand extends GameCommand {
    public void processCommand() {
    }
  }

  private class ShootCommand extends GameCommand {
    private Direction direction;

    public ShootCommand(Direction direction) {
      this.direction = direction;
    }

    public void processCommand() {
      if (quiver == 0)
        messageReceiver.noArrows();
      else
        shootArrow();
    }

    private void shootArrow() {
      messageReceiver.arrowShot();
      quiver--;
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

  private class MoveCommand extends GameCommand {
    private Direction direction;

    public MoveCommand(Direction direction) {
      this.direction = direction;
    }

    public void processCommand() {
      try {
        movePlayer(direction);
        checkNewCavern();
      } catch (NoPassage exception) {
        messageReceiver.noPassage();
      }
    }

    private void checkNewCavern() {
      checkForWumpus();
      checkForPit();
      checkForBats();
      checkForArrows();
    }

    private void checkForWumpus() {
      if (map.getWumpusCavern().equals(map.getPlayerCavern()))
        messageReceiver.playerMovesToWumpus();
    }

    private void checkForBats() {
      if (map.playerIsInBatsCavern()) {
        messageReceiver.batsTransport();
        map.randomlyTransportPlayer();
      }
    }

    public void movePlayer(Direction direction) {
      Cavern destination = map.getPlayerCavern().findDestination(direction);
      if (destination.isNull()) {
        throw new NoPassage();
      }

      map.setPlayerCavern(destination);
    }

    private void checkForPit() {
      if (map.playerIsInPitCavern())
        messageReceiver.fellInPit();
    }

    private void checkForArrows() {
      int arrowsFound = getArrowsInCavern(map.getPlayerCavern().getName());
      if (arrowsFound > 0)
        messageReceiver.arrowsFound(arrowsFound);
      quiver += arrowsFound;
      map.clearArrowsInPlayerCavern();
    }

    private class NoPassage extends RuntimeException {}
  }
}

