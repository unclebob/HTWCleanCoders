package htw;

import htw.game.HuntTheWumpusGame;

public interface HuntTheWumpus {
  public enum Direction {NORTH, SOUTH, EAST, WEST;}

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
  HuntTheWumpusGame.Command makeRestCommand();
  HuntTheWumpusGame.Command makeShootCommand(Direction direction);
  HuntTheWumpusGame.Command makeMoveCommand(Direction direction);

  public interface Command {
    void execute();
  }
}
