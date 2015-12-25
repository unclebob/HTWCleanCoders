package htw.fixtures;

import htw.HtwMessageReceiver;
import htw.game.HuntTheWumpusFacade;
import htw.game.Game;

public class TestableHuntTheWumpus extends HuntTheWumpusFacade {
  private boolean wumpusFrozen = false;

  public TestableHuntTheWumpus(HtwMessageReceiver receiver) {
    super(receiver);
    game = new TestableMap();
  }

  public void freezeWumpus() {
    wumpusFrozen = true;
  }

  private class TestableMap extends Game {
    @Override
    public void moveWumpus() {
      if (!wumpusFrozen)
        super.moveWumpus();
    }
  }
}
