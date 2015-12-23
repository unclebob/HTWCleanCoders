package htw;

public interface HtwMessageReceiver {
  void noPassage();
  void hearBats();
  void hearPit();
  void smellWumpus();
  void passage(HuntTheWumpus.Direction direction);
  void noArrows();
  void arrowShot();
  void playerShootsSelfInBack();
  void playerKillsWumpus();
  void playerShootsWall();
  void arrowsFound(Integer arrowsFound);
  void flashlightFound();
  void fellInPit();
  void playerMovesToWumpus();
  void wumpusMovesToPlayer();
  void batsTransport();
}
