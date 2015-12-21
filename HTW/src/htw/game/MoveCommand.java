package htw.game;

import static htw.HuntTheWumpus.Direction;

class MoveCommand extends GameCommand {
  private Direction direction;

  public MoveCommand(Direction direction, HuntTheWumpusGame game) {
    super(game);
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
    if (map.getWumpusCavern().equals(map.getPlayerCavern()))
      messageReceiver.playerMovesToWumpus();
  }

  private void checkForBats() {
    if (map.playerIsInBatsCavern()) {
      messageReceiver.batsTransport();
      map.randomlyTransportPlayer();
    }
  }

  public void movePlayer(Direction direction) {
    Cavern destination = map.getPlayerCavern().findDestination(direction);
    if (destination.isNull()) {
      throw new NoPassage();
    }

    map.setPlayerCavern(destination);
  }

  private void checkForPit() {
    if (map.playerIsInPitCavern())
      messageReceiver.fellInPit();
  }

  private void checkForArrows() {
    int arrowsFound = game.getArrowsInCavern(map.getPlayerCavern().getName());
    if (arrowsFound > 0)
      messageReceiver.arrowsFound(arrowsFound);
    game.incrementQuiverBy(arrowsFound);
    map.clearArrowsInPlayerCavern();
  }

  private class NoPassage extends RuntimeException {}
}
