package htw.game;

import java.util.List;

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

  protected List<Cavern> accumulateCavernsGoing(Direction direction, List<Cavern> caverns, Cavern initialCavern) {
    return caverns;
  }
}
