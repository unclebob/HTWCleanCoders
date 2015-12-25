package htw.game;

import htw.HtwMessageReceiver;
import htw.HuntTheWumpus;

abstract class GameCommand implements HuntTheWumpus.Command {
  protected Game game;
  protected HtwMessageReceiver messageReceiver;

  public GameCommand(Game game, HtwMessageReceiver messageReceiver) {
    this.game = game;
    this.messageReceiver = messageReceiver;
  }

  public void execute() {
    processCommand();

    game.moveWumpus();

    reportStatus();
  }

  protected abstract void processCommand();

  private void reportStatus() {
    reportAvailableDirections();
    reportSpecialLocations();
  }

  private void reportAvailableDirections() {
    game.availableDirections().forEach(messageReceiver::passage);
  }

  private void reportSpecialLocations() {
    if (game.playerIsInWumpusCavern())
      messageReceiver.wumpusMovesToPlayer();
    if (game.batCavernIsNear())
      messageReceiver.hearBats();
    if (game.pitCavernIsNear())
      messageReceiver.hearPit();
    if (game.wumpusCavernIsNear())
      messageReceiver.smellWumpus();
  }

}
