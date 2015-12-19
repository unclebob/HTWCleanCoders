package htw;

import htw.HuntTheWumpus.Direction;

public interface HtwMessageReceiver {
  void noPassage();
  void hearBats();
  void hearPit();
  void smellWumpus();
  void passage(Direction direction);
  void noArrows();
  void arrowShot();
  void playerShootsSelfInBack();
  void playerKillsWumpus();
  void playerShootsWall();
  void arrowsFound(Integer arrowsFound);
  void fellInPit();
  void playerMovesToWumpus();
  void wumpusMovesToPlayer();
  void batsTransport();
  void addPitCoverToAdjacentCavern(Direction direction);
  void cavernNotAdjacentForPitCover();
  void noPitCover();
}
