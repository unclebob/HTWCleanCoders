package htw.game;

import htw.HtwMessageReceiver;

import static htw.HuntTheWumpus.Direction;

class MoveCommand extends GameCommand {
  private Direction direction;

  public MoveCommand(Direction direction, Game game, HtwMessageReceiver messageReceiver) {
    super(game, messageReceiver);
    this.direction = direction;
  }

  public void processCommand() {
    try {
      movePlayer(direction);
      checkNewCavern();
    } catch (NoPassage exception) {
      messageReceiver.noPassage();
    }
  }

  private void checkNewCavern() {
    checkForWumpus();
    checkForPit();
    checkForBats();
    checkForArrows();
  }

  private void checkForWumpus() {
    if (game.playerIsInWumpusCavern())
      messageReceiver.playerMovesToWumpus();
  }

  private void checkForBats() {
    if (game.playerIsInBatsCavern()) {
      messageReceiver.batsTransport();
      game.randomlyTransportPlayer();
    }
  }

  public void movePlayer(Direction direction) {
    Cavern destination = game.getPlayerDestinationGoing(direction);
    if (destination.isNull()) {
      throw new NoPassage();
    }

    game.setPlayerCavern(destination);
  }

  private void checkForPit() {
    if (game.playerIsInPitCavern())
      messageReceiver.fellInPit();
  }

  private void checkForArrows() {
    int arrowsFound = game.getArrowsInPlayerCavern();
    if (arrowsFound > 0)
      messageReceiver.arrowsFound(arrowsFound);
    game.incrementQuiverBy(arrowsFound);
    game.clearArrowsInPlayerCavern();
  }

  private class NoPassage extends RuntimeException {}
}
