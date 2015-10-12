package htw.fixtures;

import htw.HtwMessageReceiver;
import htw.game.HuntTheWumpusGame;

public class TestableHuntTheWumpus extends HuntTheWumpusGame {
  private boolean wumpusFrozen = false;

  protected void moveWumpus() {
    if (!wumpusFrozen)
      super.moveWumpus();
  }

  public TestableHuntTheWumpus(HtwMessageReceiver receiver) {
    super(receiver);
  }

  public void freezeWumpus() {
    wumpusFrozen = true;
  }
}
