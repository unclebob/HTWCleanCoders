package htw.game;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;
import htw.HtwMessageReceiver;
import htw.HuntTheWumpus;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HuntTheWumpusGame implements HuntTheWumpus {
  private List<Connection> connections = new ArrayList<>();

  private Set<String> caverns = new HashSet<>();
  private String playerCavern = "NONE";
  private HtwMessageReceiver messageReceiver;
  private Set<String> batCaverns = new HashSet<>();
  private Set<String> pitCaverns = new HashSet<>();
  private String wumpusCavern = "NONE";
  private int quiver = 0;
  private Map<String, Integer> arrowsIn = new HashMap<>();
  private String cavernWithPitCover = "NONE";

  public HuntTheWumpusGame(HtwMessageReceiver receiver) {
    this.messageReceiver = receiver;
  }

  public void initializeArrowsIn() {
    arrowsIn = new HashMap<>();
  }

  public void setPlayerCavern(String playerCavern) {
    this.playerCavern = playerCavern;
  }

  public String getPlayerCavern() {
    return playerCavern;
  }

  private void reportStatus() {
    reportAvailableDirections();
    if (reportNearby(c -> batCaverns.contains(c.to)))
      messageReceiver.hearBats();
    if (reportNearby(c -> pitCaverns.contains(c.to)))
      messageReceiver.hearPit();
    if (reportNearby(c -> wumpusCavern.equals(c.to)))
      messageReceiver.smellWumpus();
  }

  private boolean reportNearby(Predicate<Connection> nearTest) {
    return connections.stream()
            .filter(c -> playerCavern.equals(c.from) && nearTest.test(c))
            .findAny()
            .isPresent();
  }

  private void reportAvailableDirections() {
    connections.stream()
            .filter(isFromPlayerCavern())
            .map(c -> c.direction)
            .forEach(messageReceiver::passage);
  }

  private Predicate<Connection> isFromPlayerCavern() {
    return c -> playerCavern.equals(c.from);
  }

  public void addBatCavern(String cavern) {
    batCaverns.add(cavern);
  }

  public void addPitCavern(String cavern) {
    pitCaverns.add(cavern);
  }

  public void setWumpusCavern(String wumpusCavern) {
    this.wumpusCavern = wumpusCavern;
  }

  public String getWumpusCavern() {
    return wumpusCavern;
  }

  protected void moveWumpus() {
    List<String> wumpusChoices = connections.stream()
            .filter(isFromWumpusCavern())
            .map(c -> c.to)
            .collect(Collectors.toList());
    wumpusChoices.add(wumpusCavern);

    wumpusCavern = wumpusChoices.stream()
            .skip(randomCavernIndex(wumpusChoices.size()))
            .findFirst()
            .get();
  }

  private Predicate<Connection> isFromWumpusCavern() {
    return c -> wumpusCavern.equals(c.from);
  }

  private int randomCavernIndex() {
    return randomCavernIndex(caverns.size() - 1);
  }

  private int randomCavernIndex(int size) {
    return new Random().nextInt(size);
  }

  private void randomlyTransportPlayer() {
    playerCavern = caverns.stream()
            .filter(excludePlayerCavern())
            .skip(randomCavernIndex())
            .findFirst()
            .get();
  }

  private Predicate<String> excludePlayerCavern() {
    return c -> !playerCavern.equals(c);
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
      return integer.intValue();
  }

  public String farthestCavern(String startingCavern, Direction direction) {
    String cavern = startingCavern;
    String furthestCavern = null;
    for (int count = 0; count < 100 && (cavern = nextCavern(cavern, direction)) != null; count++)
      furthestCavern = cavern;
    return furthestCavern;
  }

  private String nextCavern(String cavern, Direction direction) {
    Connection connection = connections.stream()
            .filter(isFromDirection(cavern, direction))
            .findFirst()
            .orElse(null);
    return connection != null ? connection.to : null;
  }

  private Predicate<Connection> isFromDirection(String cavern, Direction direction) {
    return c -> cavern.equals(c.from) && direction.equals(c.direction);
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
    Connection destination = connections.stream()
            .filter(isDestination(cavern, direction))
            .findFirst()
            .orElse(null);
    return destination != null ? destination.to : null;
  }

  private Predicate<Connection> isDestination(String cavern, Direction direction) {
    return c -> c.from.equals(cavern) && c.direction == direction;
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

  public Command makeAddPitCoverCommand(Direction direction) {
    return new AddPitCoverCommand(direction);
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

      private String nextCavern(String cavern, Direction direction) {
        Connection connection = connections.stream()
                .filter(isFromDirection(cavern, direction))
                .findFirst()
                .orElse(null);
        return connection != null ? connection.to : null;
      }

      private Predicate<Connection> isFromDirection(String cavern, Direction direction) {
        return c -> cavern.equals(c.from) && direction.equals(c.direction);
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
      String destination = findDestination(playerCavern, direction);
      if (destination != null) {
        playerCavern = destination;
        return true;
      }
      return false;
    }

    private void checkForPit() {
      if (pitCaverns.contains(playerCavern) && !cavernWithPitCover.equals(playerCavern))
        messageReceiver.fellInPit();
    }

    private void checkForArrows() {
      Integer arrowsFound = getArrowsInCavern(playerCavern);
      if (arrowsFound > 0)
        messageReceiver.arrowsFound(arrowsFound);
      quiver += arrowsFound;
      arrowsIn.put(playerCavern, 0);
    }
  }

  private class AddPitCoverCommand implements Command {
    private Direction direction;

    public AddPitCoverCommand(Direction direction) {
      this.direction = direction;
    }

    public void execute() {
      if (cavernWithPitCover.equals("NONE")) {
        String destination = findDestination(playerCavern, direction);
        if (destination != null) {
          cavernWithPitCover = destination;
          messageReceiver.addPitCoverToAdjacentCavern(direction);
        } else {
          messageReceiver.cavernNotAdjacentForPitCover();
        }
      }
      else {
        messageReceiver.noPitCover();
      }
    }
  }
}
