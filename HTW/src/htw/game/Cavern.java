package htw.game;

import htw.HuntTheWumpus;

import java.util.*;

import static htw.HuntTheWumpus.*;

class Cavern {
  public static final Cavern NULL = new NullCavern();

  private String name;
  private Map<HuntTheWumpus.Direction, Cavern> connections = new HashMap<>();

  public Cavern(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public boolean isNamed(String name) {
    return this.name.equals(name);
  }

  public Cavern findDestination(Direction direction) {
    Cavern destination = connections.get(direction);
    return (destination != null) ? destination : new NullCavern();
  }

  public Set<HuntTheWumpus.Direction> availableDirections() {
    return connections.keySet();
  }

  public List<Cavern> connectedCaverns() {
    return new ArrayList<>(connections.values());
  }

  public void addConnection(Cavern to, Direction direction) {
    connections.put(direction, to);
  }

  public List<Cavern> getCavernsGoing(Direction direction) {
    List<Cavern> caverns = new ArrayList<>();
    return findDestination(direction).accumulateCavernsGoing(direction, caverns, this);
  }

  protected List<Cavern> accumulateCavernsGoing(Direction direction, List<Cavern> caverns, Cavern initialCavern) {
    caverns.add(this);
    if (initialCavern.equals(this))
      return caverns;

    return findDestination(direction).accumulateCavernsGoing(direction, caverns, initialCavern);
  }

  public boolean isNull() {
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Cavern) {
      Cavern c = (Cavern) obj;
      return getName().equals(c.getName());
    }

    return false;
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }
}

