package htw.game;

import htw.HtwMessageReceiver;
import htw.HuntTheWumpus;

import java.util.*;
import java.util.function.Predicate;

public class HuntTheWumpusGame implements HuntTheWumpus {
  private Set<Cavern> caverns = new HashSet<>();
  private Cavern playerCavern = Cavern.NULL;
  private Cavern wumpusCavern = Cavern.NULL;
  private Set<Cavern> batCaverns = new HashSet<>();
  private Set<Cavern> pitCaverns = new HashSet<>();
  private int quiver = 0;
  private Map<Cavern, Integer> arrowsIn = new HashMap<>();
  private HtwMessageReceiver messageReceiver;
  private RandomChooser randomChooser = new RandomChooser();

  public HuntTheWumpusGame(HtwMessageReceiver receiver) {
    this.messageReceiver = receiver;
  }

  private Cavern cavern(String cavernName) {
    return caverns.stream()
        .filter(c -> c.isNamed(cavernName))
        .findAny()
        .orElse(new Cavern(cavernName));
  }

  public void setPlayerCavern(String playerCavern) {
    this.playerCavern = cavern(playerCavern);
  }

  public String getPlayerCavern() {
    return playerCavern.getName();
  }

  private void reportStatus() {
    reportAvailableDirections();
    if (reportNearby(batCaverns::contains))
      messageReceiver.hearBats();
    if (reportNearby(pitCaverns::contains))
      messageReceiver.hearPit();
    if (reportNearby(wumpusCavern::equals))
      messageReceiver.smellWumpus();
  }

  private boolean reportNearby(Predicate<Cavern> nearTest) {
    return playerCavern.connectedCaverns().stream()
        .anyMatch(nearTest::test);
  }

  private void reportAvailableDirections() {
    playerCavern.availableDirections()
        .forEach(messageReceiver::passage);
  }

  public void addBatCavern(String cavern) {
    batCaverns.add(cavern(cavern));
  }

  public void addPitCavern(String cavern) {
    pitCaverns.add(cavern(cavern));
  }

  public void setWumpusCavern(String wumpusCavern) {
    this.wumpusCavern = cavern(wumpusCavern);
  }

  public String getWumpusCavern() {
    return wumpusCavern.getName();
  }

  protected void moveWumpus() {
    List<Cavern> wumpusChoices = wumpusCavern.connectedCaverns();
    wumpusChoices.add(wumpusCavern);

    wumpusCavern = randomChooser.chooseFrom(wumpusChoices);
  }

  private void randomlyTransportPlayer() {
    List<Cavern> transportChoices = new ArrayList<>(caverns);
    transportChoices.remove(playerCavern);

    playerCavern = randomChooser.chooseFrom(transportChoices);
  }

  public void setQuiver(int arrows) {
    this.quiver = arrows;
  }

  public int getQuiver() {
    return quiver;
  }

  public Integer getArrowsInCavern(String cavern) {
    return zeroIfNull(arrowsIn.get(cavern(cavern)));
  }

  private int zeroIfNull(Integer integer) {
    if (integer == null)
      return 0;
    else
      return integer;
  }

  public void clearMap() {
    playerCavern = Cavern.NULL;
    wumpusCavern = Cavern.NULL;

    batCaverns.clear();
    pitCaverns.clear();
    arrowsIn.clear();
    caverns.clear();
  }

  public void connectCavern(String from, String to, Direction direction) {
    Cavern fromCavern = cavern(from);
    Cavern toCavern = cavern(to);

    fromCavern.addConnection(toCavern, direction);
    caverns.add(fromCavern);
    caverns.add(toCavern);
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
      if (playerCavern.equals(wumpusCavern))
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
      ArrowTracker arrowTracker = new ArrowTracker(playerCavern).trackArrow(direction);
      if (arrowTracker.arrowHitSomething())
        return;
      incrementArrowsInCavern(arrowTracker.getArrowCavern());
    }

    private void incrementArrowsInCavern(Cavern arrowCavern) {
      int arrows = getArrowsInCavern(arrowCavern.getName());
      arrowsIn.put(arrowCavern, arrows + 1);
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
        if (arrowCavern.equals(wumpusCavern)) {
          messageReceiver.playerKillsWumpus();
          return true;
        }
        return false;
      }

      private boolean shotSelfInBack() {
        if (arrowCavern.equals(playerCavern)) {
          messageReceiver.playerShootsSelfInBack();
          return true;
        }
        return false;
      }

      private void checkWallShot() {
        if (arrowCavern.equals(playerCavern))
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
      if (movePlayer(direction))
        checkNewCavern();
      else
        messageReceiver.noPassage();
    }

    private void checkNewCavern() {
      checkForWumpus();
      checkForPit();
      checkForBats();
      checkForArrows();
    }

    private void checkForWumpus() {
      if (wumpusCavern.equals(playerCavern))
        messageReceiver.playerMovesToWumpus();
    }

    private void checkForBats() {
      if (batCaverns.contains(playerCavern)) {
        messageReceiver.batsTransport();
        randomlyTransportPlayer();
      }
    }

    public boolean movePlayer(Direction direction) {
      Cavern destination = playerCavern.findDestination(direction);
      if (!destination.isNull()) {
        playerCavern = destination;
        return true;
      }
      return false;
    }

    private void checkForPit() {
      if (pitCaverns.contains(playerCavern))
        messageReceiver.fellInPit();
    }

    private void checkForArrows() {
      int arrowsFound = getArrowsInCavern(playerCavern.getName());
      if (arrowsFound > 0)
        messageReceiver.arrowsFound(arrowsFound);
      quiver += arrowsFound;
      arrowsIn.put(playerCavern, 0);
    }
  }
}

