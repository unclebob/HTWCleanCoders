package htw.game;

import htw.HtwMessageReceiver;
import htw.HuntTheWumpus;

public class HuntTheWumpusFacade implements HuntTheWumpus {
  protected Game game = new Game();
  private HtwMessageReceiver messageReceiver;

  public HuntTheWumpusFacade(HtwMessageReceiver receiver) {
    this.messageReceiver = receiver;
  }

  private Cavern cavern(String cavernName) {
    return game.getCavernNamed(cavernName);
  }

  public void setPlayerCavern(String playerCavern) {
    game.setPlayerCavern(cavern(playerCavern));
  }

  public String getPlayerCavern() {
    return game.getPlayerCavern().getName();
  }

  public void addBatCavern(String cavern) {
    game.addBatCavern(cavern(cavern));
  }

  public void addPitCavern(String cavern) {
    game.addPitCavern(cavern(cavern));
  }

  public void setWumpusCavern(String wumpusCavern) {
    game.setWumpusCavern(cavern(wumpusCavern));
  }

  public String getWumpusCavern() {
    return game.getWumpusCavern().getName();
  }

  public void setQuiver(int arrows) {
    game.setQuiver(arrows);
  }

  public int getQuiver() {
    return game.getQuiver();
  }

  public Integer getArrowsInCavern(String cavern) {
    return game.getArrowsInCavern(cavern(cavern));
  }

  public void clearMap() {
    game.clearMap();
  }

  public void connectCavern(String from, String to, Direction direction) {
    game.connectCavern(cavern(from), cavern(to), direction);
  }

  public String findDestination(String cavern, Direction direction) {
    return cavern(cavern).findDestination(direction).getName();
  }

  public Command makeRestCommand() {
    return new RestCommand(game, messageReceiver);
  }

  public Command makeShootCommand(Direction direction) {
    return new ShootCommand(direction, game, messageReceiver);
  }

  public Command makeMoveCommand(Direction direction) {
    return new MoveCommand(direction, game, messageReceiver);
  }
}

