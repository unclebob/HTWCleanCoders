package htw.game;

import htw.HtwMessageReceiver;
import htw.HuntTheWumpus;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HuntTheWumpusGame implements HuntTheWumpus {
  private List<Connection> connections = new ArrayList<>();

  private Set<String> caverns = new HashSet<>();
  private Cavern playerCavern = cavern("NONE");
  private HtwMessageReceiver messageReceiver;
  private Set<String> batCaverns = new HashSet<>();
  private Set<String> pitCaverns = new HashSet<>();
  private Cavern wumpusCavern = cavern("NONE");
  private int quiver = 0;
  private Map<String, Integer> arrowsIn = new HashMap<>();

  public HuntTheWumpusGame(HtwMessageReceiver receiver) {
    this.messageReceiver = receiver;
  }

  private Cavern cavern(String cavernName) {
    if (cavernName == null) return null;
    return new Cavern(cavernName);
  }

  public void setPlayerCavern(String playerCavern) {
    this.playerCavern = cavern(playerCavern);
  }

  public String getPlayerCavern() {
    return playerCavern.name;
  }

  private void reportStatus() {
    reportAvailableDirections();
    if (reportNearby(c -> batCaverns.contains(c.to)))
      messageReceiver.hearBats();
    if (reportNearby(c -> pitCaverns.contains(c.to)))
      messageReceiver.hearPit();
    if (reportNearby(c -> wumpusCavern.name.equals(c.to)))
      messageReceiver.smellWumpus();
  }

  private boolean reportNearby(Predicate<Connection> nearTest) {
    return connectionsOf(playerCavern.name).stream()
        .anyMatch(nearTest::test);
  }

  private void reportAvailableDirections() {
    connectionsOf(playerCavern.name).stream()
        .map(c -> c.direction)
        .forEach(messageReceiver::passage);
  }

  private List<Connection> connectionsOf(String cavern) {
    return connections.stream()
        .filter(c -> cavern.equals(c.from))
        .collect(Collectors.toList());
  }

  public void addBatCavern(String cavern) {
    batCaverns.add(cavern);
  }

  public void addPitCavern(String cavern) {
    pitCaverns.add(cavern);
  }

  public void setWumpusCavern(String wumpusCavern) {
    this.wumpusCavern = cavern(wumpusCavern);
  }

  public String getWumpusCavern() {
    return wumpusCavern.name;
  }

  protected void moveWumpus() {
    String cavern = wumpusCavern.name;

    List<String> wumpusChoices = connectionsOf(cavern).stream()
        .map(c -> c.to)
        .collect(Collectors.toList());

    wumpusChoices.add(wumpusCavern.name);

    int nChoices = wumpusChoices.size();
    int choice = randomChoice(nChoices);
    wumpusCavern = cavern(wumpusChoices.get(choice));
  }

  private void randomlyTransportPlayer() {
    Set<String> transportChoices = new HashSet<>(caverns);
    transportChoices.remove(playerCavern.name);
    int nChoices = transportChoices.size();
    int choice = randomChoice(nChoices);
    String[] choices = new String[nChoices];
    playerCavern = cavern(transportChoices.toArray(choices)[choice]);
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
    return zeroIfNull(arrowsIn.get(cavern));
  }

  private int zeroIfNull(Integer integer) {
    if (integer == null)
      return 0;
    else
      return integer;
  }

  public void clearMap() {
    playerCavern = cavern("NONE");
    wumpusCavern = cavern("NONE");

    connections.clear();
    batCaverns.clear();
    pitCaverns.clear();
    arrowsIn.clear();
    caverns.clear();
  }

  private class Connection {
    String from;
    String to;
    Direction direction;

    public Connection(String from, String to, Direction direction) {
      this.from = from;
      this.to = to;
      this.direction = direction;
    }
  }

  public void connectCavern(String from, String to, Direction direction) {
    connections.add(new Connection(from, to, direction));
    caverns.add(from);
    caverns.add(to);
  }

  public String findDestination(String cavern, Direction direction) {
    return connections.stream()
        .filter(c -> c.from.equals(cavern) && c.direction.equals(direction))
        .map(c -> c.to)
        .findAny()
        .orElse(null);
  }

  private class Cavern {
    public String name;

    public Cavern(String name) {
      this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof String)
        return obj.equals(name);

      Cavern c = (Cavern) obj;
      return name.equals(c.name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }
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
        ArrowTracker arrowTracker = new ArrowTracker(playerCavern.name).trackArrow(direction);
        if (arrowTracker.arrowHitSomething())
          return;
        incrementArrowsInCavern(arrowTracker.getArrowCavern());
      }
    }

    private void incrementArrowsInCavern(String arrowCavern) {
      int arrows = getArrowsInCavern(arrowCavern);
      arrowsIn.put(arrowCavern, arrows + 1);
    }

    private class ArrowTracker {
      private boolean hitSomething = false;
      private String arrowCavern;

      public ArrowTracker(String startingCavern) {
        this.arrowCavern = startingCavern;
      }

      boolean arrowHitSomething() {
        return hitSomething;
      }

      public String getArrowCavern() {
        return arrowCavern;
      }

      public ArrowTracker trackArrow(Direction direction) {
        String nextCavern;
        for (int count = 0; (nextCavern = nextCavern(arrowCavern, direction)) != null; count++) {
          arrowCavern = nextCavern;
          if (shotSelfInBack()) return this;
          if (shotWumpus()) return this;
          if (count > 100) return this;
        }
        if (arrowCavern.equals(playerCavern.name))
          messageReceiver.playerShootsWall();
        return this;
      }

      private boolean shotWumpus() {
        if (arrowCavern.equals(wumpusCavern.name)) {
          messageReceiver.playerKillsWumpus();
          hitSomething = true;
          return true;
        }
        return false;
      }

      private boolean shotSelfInBack() {
        if (arrowCavern.equals(playerCavern.name)) {
          messageReceiver.playerShootsSelfInBack();
          hitSomething = true;
          return true;
        }
        return false;
      }

      private String nextCavern(String cavern, Direction direction) {
        return connectionsOf(cavern).stream()
            .filter(c -> c.direction.equals(direction))
            .map(c -> c.to)
            .findAny()
            .orElse(null);
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
      if (batCaverns.contains(playerCavern.name)) {
        messageReceiver.batsTransport();
        randomlyTransportPlayer();
      }
    }

    public boolean movePlayer(Direction direction) {
      String destination = findDestination(playerCavern.name, direction);
      if (destination != null) {
        playerCavern = cavern(destination);
        return true;
      }
      return false;
    }

    private void checkForPit() {
      if (pitCaverns.contains(playerCavern.name))
        messageReceiver.fellInPit();
    }

    private void checkForArrows() {
      Integer arrowsFound = getArrowsInCavern(playerCavern.name);
      if (arrowsFound > 0)
        messageReceiver.arrowsFound(arrowsFound);
      quiver += arrowsFound;
      arrowsIn.put(playerCavern.name, 0);
    }
  }
}
