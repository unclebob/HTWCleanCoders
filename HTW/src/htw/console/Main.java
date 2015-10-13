package htw.console;

import htw.HtwMessageReceiver;
import htw.HuntTheWumpus;
import htw.game.HuntTheWumpusGame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static htw.HuntTheWumpus.Direction.*;

public class Main implements HtwMessageReceiver {
  private static HuntTheWumpusGame game;

  public static void main(String[] args) throws IOException {
    game = new HuntTheWumpusGame(new Main());
    createMap();
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      HuntTheWumpus.Command c = game.makeRestCommand();
      System.out.println(">");
      String command = br.readLine();
      if (command.equalsIgnoreCase("east"))
        c = game.makeMoveCommand(EAST);
      else if (command.equalsIgnoreCase("west"))
        c = game.makeMoveCommand(WEST);
      else if (command.equalsIgnoreCase("north"))
        c = game.makeMoveCommand(NORTH);
      else if (command.equalsIgnoreCase("south"))
        c = game.makeMoveCommand(SOUTH);
      else if (command.equalsIgnoreCase("rest"))
        c = game.makeRestCommand();
      else if (command.equalsIgnoreCase("shoot west"))
        c = game.makeShootCommand(WEST);
      else if (command.equalsIgnoreCase("shoot east"))
        c = game.makeShootCommand(EAST);
      else if (command.equalsIgnoreCase("shoot north"))
        c = game.makeShootCommand(NORTH);
      else if (command.equalsIgnoreCase("shoot south"))
        c = game.makeShootCommand(SOUTH);

      c.execute();
    }
  }

  private static void createMap() {
    game.connectCavern("1", "7", NORTH);
    game.connectCavern("1", "d", SOUTH);
    game.connectCavern("2", "i", EAST);
    game.connectCavern("2", "k", WEST);
    game.connectCavern("3", "h", NORTH);
    game.connectCavern("3", "j", SOUTH);
    game.connectCavern("4", "b", EAST);
    game.connectCavern("4", "i", WEST);
    game.connectCavern("5", "d", NORTH);
    game.connectCavern("5", "f", SOUTH);
    game.connectCavern("6", "g", EAST);
    game.connectCavern("6", "e", WEST);
    game.connectCavern("7", "1", NORTH);
    game.connectCavern("7", "f", SOUTH);
    game.connectCavern("8", "g", EAST);
    game.connectCavern("8", "k", WEST);
    game.connectCavern("9", "b", NORTH);
    game.connectCavern("9", "j", SOUTH);
    game.connectCavern("a", "c", EAST);
    game.connectCavern("a", "h", WEST);
    game.connectCavern("b", "4", NORTH);
    game.connectCavern("b", "9", SOUTH);
    game.connectCavern("c", "2", EAST);
    game.connectCavern("c", "a", WEST);
    game.connectCavern("d", "1", NORTH);
    game.connectCavern("d", "5", SOUTH);
    game.connectCavern("e", "4", EAST);
    game.connectCavern("e", "6", WEST);
    game.connectCavern("f", "7", NORTH);
    game.connectCavern("f", "c", SOUTH);
    game.connectCavern("g", "6", EAST);
    game.connectCavern("g", "8", WEST);
    game.connectCavern("h", "3", NORTH);
    game.connectCavern("h", "a", SOUTH);
    game.connectCavern("i", "2", EAST);
    game.connectCavern("i", "5", WEST);
    game.connectCavern("j", "3", NORTH);
    game.connectCavern("j", "9", SOUTH);
    game.connectCavern("k", "8", EAST);
    game.connectCavern("k", "e", WEST);
    game.setPlayerCavern("1");
    game.setWumpusCavern("c");
    game.addBatCavern("h");
    game.addPitCavern("8");
    game.setQuiver(5);
  }

  public void noPassage() {
    System.out.println("No Passage.");
  }

  public void hearBats() {
    System.out.println("You hear chirping.");
  }

  public void hearPit() {
    System.out.println("You hear wind.");
  }

  public void smellWumpus() {
    System.out.println("There is a terrible smell.");
  }

  public void passage(HuntTheWumpus.Direction direction) {
    System.out.println("You can go " + direction.name());
  }

  public void noArrows() {
    System.out.println("You have no arrows.");
  }

  public void arrowShot() {
    System.out.println("Thwang!");
  }

  public void playerShootsSelfInBack() {
    System.out.println("You shot yourself in the back.");
  }

  public void playerKillsWumpus() {
    System.out.println("You killed the Wumpus.");
  }

  public void playerShootsWall() {
    System.out.println("You shot the wall.");
  }

  public void arrowsFound(Integer arrowsFound) {
    System.out.println("You found " + arrowsFound + " arrow" + (arrowsFound == 1 ? "" : "s") + ".");
  }

  public void fellInPit() {
    System.out.println("You fell in a pit.");
  }

  public void playerMovesToWumpus() {
    System.out.println("You walked into the waiting arms of the Wumpus.");
  }

  public void wumpusMovesToPlayer() {
    System.out.println("The Wumpus has found you.");
  }

  public void batsTransport() {
    System.out.println("Some bats carried you away.");
  }
}
