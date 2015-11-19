package htw.game;

import htw.HtwMessageReceiver;
import htw.HuntTheWumpus;

import java.util.*;
import java.util.function.Predicate;

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

  public HuntTheWumpusGame(HtwMessageReceiver receiver) {
    this.messageReceiver = receiver;
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
    for (Connection c : connections)
      if (playerCavern.equals(c.from) && nearTest.test(c))
        return true;
    return false;
  }

  private void reportAvailableDirections() {
    for (Connection c : connections) {
      if (playerCavern.equals(c.from))
        messageReceiver.passage(c.direction);
    }
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
    List<String> wumpusChoices = new ArrayList<>();
    for (Connection c : connections)
      if (wumpusCavern.equals(c.from))
        wumpusChoices.add(c.to);
    wumpusChoices.add(wumpusCavern);

    int nChoices = wumpusChoices.size();
    int choice = (int) (Math.random() * nChoices);
    wumpusCavern = wumpusChoices.get(choice);
  }

  private void randomlyTransportPlayer() {
    Set<String> transportChoices = new HashSet<>(caverns);
    transportChoices.remove(playerCavern);
    int nChoices = transportChoices.size();
    int choice = (int) (Math.random() * nChoices);
    String[] choices = new String[nChoices];
    playerCavern = transportChoices.toArray(choices)[choice];
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
    for (Connection c : connections)
      if (c.from.equals(cavern) && c.direction == direction)
        return c.to;
    return null;
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
        for (Connection c : connections)
          if (cavern.equals(c.from) && direction.equals(c.direction))
            return c.to;
        return null;
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
      if (pitCaverns.contains(playerCavern))
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
}
