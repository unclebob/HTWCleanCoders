package htw.game;

import htw.HtwMessageReceiver;
import htw.HuntTheWumpus;

public abstract class GameCommand implements HuntTheWumpus.Command {
  protected HuntTheWumpusMap map = new HuntTheWumpusMap();
  protected HtwMessageReceiver messageReceiver;
  protected HuntTheWumpusGame game;

  public GameCommand(HuntTheWumpusGame game) {
    this.game = game;
    this.map = game.getMap();
    this.messageReceiver = game.getMessageReceiver();
  }

  public void execute() {
    processCommand();
    game.moveWumpus();
    checkWumpusMovedToPlayer();
    reportStatus();
  }

  private void reportStatus() {
    reportAvailableDirections();
    reportSpecialLocations();
  }

  private void reportAvailableDirections() {
    map.availableDirections().forEach(messageReceiver::passage);
  }

  private void reportSpecialLocations() {
    if (map.batCavernIsNear())
      messageReceiver.hearBats();
    if (map.pitCavernIsNear())
      messageReceiver.hearPit();
    if (map.wumpusCavernIsNear())
      messageReceiver.smellWumpus();
  }

  protected void checkWumpusMovedToPlayer() {
    if (map.playerIsInWumpusCavern())
      messageReceiver.wumpusMovesToPlayer();
  }

  protected abstract void processCommand();

}
