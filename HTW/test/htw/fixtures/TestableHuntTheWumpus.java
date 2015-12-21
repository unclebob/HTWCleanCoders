package htw.fixtures;

import htw.HtwMessageReceiver;
import htw.game.HuntTheWumpusGame;
import htw.game.HuntTheWumpusMap;

public class TestableHuntTheWumpus extends HuntTheWumpusGame {
  private boolean wumpusFrozen = false;

  public TestableHuntTheWumpus(HtwMessageReceiver receiver) {
    super(receiver);
    map = new TestableMap();
  }

  public void freezeWumpus() {
    wumpusFrozen = true;
  }

  private class TestableMap extends HuntTheWumpusMap {
    @Override
    public void moveWumpus() {
      if (!wumpusFrozen)
        super.moveWumpus();
    }
  }
}
