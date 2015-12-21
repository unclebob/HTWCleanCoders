package htw.game;

import htw.HtwMessageReceiver;
import htw.HuntTheWumpus;

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
    return new RestCommand(this);
  }

  public Command makeShootCommand(Direction direction) {
    return new ShootCommand(direction, this);
  }

  public Command makeMoveCommand(Direction direction) {
    return new MoveCommand(direction, this);
  }

  public HuntTheWumpusMap getMap() {
    return map;
  }

  public HtwMessageReceiver getMessageReceiver() {
    return messageReceiver;
  }

  void decrementQuiverBy(int arrowNumber) {
    quiver -= arrowNumber;
  }

  boolean quiverEmpty() {
    return quiver == 0;
  }

  public void incrementQuiverBy(int arrowNumber) {
    quiver += arrowNumber;
  }
}

