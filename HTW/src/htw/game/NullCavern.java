package htw.game;

import static htw.HuntTheWumpus.Direction;

class NullCavern extends Cavern {
  public NullCavern() {
    super("NONE");
  }

  public boolean isNull() {
    return true;
  }

  public Cavern findDestination(Direction direction) {
    return this;
  }
}
