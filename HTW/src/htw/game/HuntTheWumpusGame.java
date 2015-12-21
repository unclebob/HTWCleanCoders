package htw.game;

import htw.HtwMessageReceiver;
import htw.HuntTheWumpus;

import java.util.*;
import java.util.function.Predicate;

public class HuntTheWumpusGame implements HuntTheWumpus {
  private Set<Cavern> caverns = new HashSet<>();
  private Cavern playerCavern = Cavern.NULL;
  private HtwMessageReceiver messageReceiver;
  private Set<Cavern> batCaverns = new HashSet<>();
  private Set<Cavern> pitCaverns = new HashSet<>();
  private Cavern wumpusCavern = Cavern.NULL;
  private int quiver = 0;
  private Map<Cavern, Integer> arrowsIn = new HashMap<>();

  public HuntTheWumpusGame(HtwMessageReceiver receiver) {
    this.messageReceiver = receiver;
  }

  private Cavern cavern(String cavernName) {
    Cavern cavern = new Cavern(cavernName);
    for (Cavern c : caverns) {
      if (c.equals(cavern)) {
        return c;
      }
    }

    return cavern;
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
    playerCavern.availableDirections().forEach(messageReceiver::passage);
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

    wumpusCavern = randomChoiceFrom(wumpusChoices);
  }

  private void randomlyTransportPlayer() {
    List<Cavern> transportChoices = new ArrayList<>(caverns);
    transportChoices.remove(playerCavern);

    playerCavern = randomChoiceFrom(transportChoices);
  }

  private Cavern randomChoiceFrom(List<Cavern> choices) {
    int nChoices = choices.size();
    int choice = randomChoice(nChoices);
    return choices.get(choice);
  }

  private int randomChoice(int numberOfPossibleChoices) {
    return (int) (Math.random() * numberOfPossibleChoices);
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
      else {
        messageReceiver.arrowShot();
        quiver--;
        ArrowTracker arrowTracker = new ArrowTracker(playerCavern).trackArrow(direction);
        if (arrowTracker.arrowHitSomething())
          return;
        incrementArrowsInCavern(arrowTracker.getArrowCavern());
      }
    }

    private void incrementArrowsInCavern(Cavern arrowCavern) {
      int arrows = getArrowsInCavern(arrowCavern.getName());
      arrowsIn.put(arrowCavern, arrows + 1);
    }

    private class ArrowTracker {
      private boolean hitSomething = false;
      private Cavern arrowCavern;

      public ArrowTracker(Cavern startingCavern) {
        this.arrowCavern = startingCavern;
      }

      boolean arrowHitSomething() {
        return hitSomething;
      }

      public Cavern getArrowCavern() {
        return arrowCavern;
      }

      public ArrowTracker trackArrow(Direction direction) {
        int count = 0;
        while (arrowCavern.hasConnectionGoing(direction)) {
          arrowCavern = arrowCavern.findDestination(direction);
          if (shotSelfInBack()) return this;
          if (shotWumpus()) return this;
          if (count > 100) return this;
          count++;
        }
        if (arrowCavern.equals(playerCavern))
          messageReceiver.playerShootsWall();
        return this;
      }

      private boolean shotWumpus() {
        if (arrowCavern.equals(wumpusCavern)) {
          messageReceiver.playerKillsWumpus();
          hitSomething = true;
          return true;
        }
        return false;
      }

      private boolean shotSelfInBack() {
        if (arrowCavern.equals(playerCavern)) {
          messageReceiver.playerShootsSelfInBack();
          hitSomething = true;
          return true;
        }
        return false;
      }

    }
  }

  private class MoveCommand extends GameCommand {
    private Direction direction;

    public MoveCommand(Direction direction) {
      this.direction = direction;
    }

    public void processCommand() {
      if (movePlayer(direction)) {
        checkForWumpus();
        checkForPit();
        checkForBats();
        checkForArrows();
      } else
        messageReceiver.noPassage();
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
      Integer arrowsFound = getArrowsInCavern(playerCavern.getName());
      if (arrowsFound > 0)
        messageReceiver.arrowsFound(arrowsFound);
      quiver += arrowsFound;
      arrowsIn.put(playerCavern, 0);
    }
  }
}
