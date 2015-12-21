package htw.game;

import java.util.*;
import java.util.function.Predicate;

import static htw.HuntTheWumpus.Direction;

public class Game {
  private Set<Cavern> caverns = new HashSet<>();
  private Set<Cavern> pitCaverns = new HashSet<>();
  private Set<Cavern> batCaverns = new HashSet<>();

  private Cavern playerCavern = Cavern.NULL;
  private Cavern wumpusCavern = Cavern.NULL;
  private int quiver = 0;

  private Map<Cavern, Integer> arrowsIn = new HashMap<>();
  private RandomChooser randomChooser = new RandomChooser();

  public Cavern getCavernNamed(String cavernName) {
    return caverns.stream()
        .filter(c -> c.isNamed(cavernName))
        .findAny()
        .orElse(new Cavern(cavernName));
  }

  public void connectCavern(Cavern from, Cavern to, Direction direction) {
    from.addConnection(to, direction);
    caverns.add(from);
    caverns.add(to);
  }

  public Set<Direction> availableDirections() {
    return playerCavern.availableDirections();
  }

  public void addBatCavern(Cavern cavern) {
    batCaverns.add(cavern);
  }

  public void addPitCavern(Cavern cavern) {
    pitCaverns.add(cavern);
  }

  public Cavern getWumpusCavern() {
    return wumpusCavern;
  }

  public void setWumpusCavern(Cavern cavern) {
    wumpusCavern = cavern;
  }

  public Cavern getPlayerCavern() {
    return playerCavern;
  }

  public void setPlayerCavern(Cavern cavern) {
    playerCavern = cavern;
  }

  public boolean playerIsInPitCavern() {
    return pitCaverns.contains(playerCavern);
  }

  public boolean playerIsInBatsCavern() {
    return batCaverns.contains(playerCavern);
  }

  public boolean playerIsInWumpusCavern() {
    return playerCavern.equals(wumpusCavern);
  }

  public boolean wumpusCavernIsNear() {
    return reportNearby(wumpusCavern::equals);
  }

  public boolean pitCavernIsNear() {
    return reportNearby(pitCaverns::contains);
  }

  public boolean batCavernIsNear() {
    return reportNearby(batCaverns::contains);
  }

  private boolean reportNearby(Predicate<Cavern> nearTest) {
    return playerCavern.connectedCaverns().stream()
        .anyMatch(nearTest::test);
  }

  public void randomlyTransportPlayer() {
    List<Cavern> transportChoices = new ArrayList<>(caverns);
    transportChoices.remove(playerCavern);

    playerCavern = randomChooser.chooseFrom(transportChoices);
  }

  public void moveWumpus() {
    List<Cavern> wumpusChoices = wumpusCavern.connectedCaverns();
    wumpusChoices.add(wumpusCavern);

    wumpusCavern = randomChooser.chooseFrom(wumpusChoices);
  }

  public Cavern getPlayerDestinationGoing(Direction direction) {
    return playerCavern.findDestination(direction);
  }

  public void incrementArrowsIn(Cavern arrowCavern, int numberOfArrows) {
    int arrows = getArrowsInCavern(arrowCavern);
    arrowsIn.put(arrowCavern, arrows + numberOfArrows);
  }

  public void clearArrowsInPlayerCavern() {
    arrowsIn.put(playerCavern, 0);
  }

  public int getArrowsInPlayerCavern() {
    return getArrowsInCavern(playerCavern);
  }

  public Integer getArrowsInCavern(Cavern cavern) {
    return zeroIfNull(arrowsIn.get(cavern));
  }

  private int zeroIfNull(Integer integer) {
    if (integer == null)
      return 0;
    else
      return integer;
  }

  public void clearMap() {
    setPlayerCavern(Cavern.NULL);
    setWumpusCavern(Cavern.NULL);

    batCaverns.clear();
    pitCaverns.clear();
    arrowsIn.clear();
    caverns.clear();
  }

  public int getQuiver() {
    return quiver;
  }

  void setQuiver(int arrows) {
    this.quiver = arrows;
  }

  public void decrementQuiverBy(int arrowNumber) {
    quiver -= arrowNumber;
  }

  public void incrementQuiverBy(int arrowNumber) {
    quiver += arrowNumber;
  }

  public boolean quiverEmpty() {
    return quiver == 0;
  }
}
