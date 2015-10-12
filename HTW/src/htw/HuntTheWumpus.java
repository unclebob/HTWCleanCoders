package htw;

import htw.game.HuntTheWumpusGame;

public interface HuntTheWumpus {
  public enum Direction {NORTH, SOUTH, EAST, WEST;}
  
  HuntTheWumpusGame.Command makeRestCommand();
  HuntTheWumpusGame.Command makeShootCommand(Direction direction);
  HuntTheWumpusGame.Command makeMoveCommand(Direction direction);

  public interface Command {
    void execute();
  }
}
