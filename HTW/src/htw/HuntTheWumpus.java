package htw;

import htw.game.HuntTheWumpusGame;

public interface HuntTheWumpus {
  public enum Direction {
    NORTH {
      public Direction opposite() {
        return SOUTH;
      }
    },
    SOUTH {
      public Direction opposite() {
        return NORTH;
      }
    },
    EAST {
      public Direction opposite() {
        return WEST;
      }
    },
    WEST {
      public Direction opposite() {
        return EAST;
      }
    };
    public abstract Direction opposite();

  }
  void setPlayerCavern(String playerCavern);

  String getPlayerCavern();
  void addBatCavern(String cavern);
  void addPitCavern(String cavern);
  void setWumpusCavern(String wumpusCavern);
  String getWumpusCavern();
  void setQuiver(int arrows);
  int getQuiver();
  Integer getArrowsInCavern(String cavern);
  void connectCavern(String from, String to, Direction direction);
  String findDestination(String cavern, Direction direction);
  HuntTheWumpusGame.Command makeRestCommand();
  HuntTheWumpusGame.Command makeShootCommand(Direction direction);
  HuntTheWumpusGame.Command makeMoveCommand(Direction direction);

  public interface Command {
    void execute();
  }
}
