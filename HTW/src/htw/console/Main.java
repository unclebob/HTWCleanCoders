package htw.console;

import htw.HtwMessageReceiver;
import htw.HuntTheWumpus;
import htw.HuntTheWumpus.Direction;
import htw.factory.HtwFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static htw.HuntTheWumpus.Direction.*;

public class Main implements HtwMessageReceiver {
  private static boolean playing = true;
  private static HuntTheWumpus game;
  private static int hitPoints = 10;
  private static final List<String> caverns = new ArrayList<>();
  private static final String[] environments = new String[]{
    " bright",
    " clean",
    " warm",
    "n airy",
    " crisp",
    "",
    " cool",
    " stuffy",
    " drafty",
    " dreadful"
  };

  private static final String[] shapes = new String[] {
    "small",
    "tiny",
    "rectangular",
    "round",
    "large",
    "enormous",
    "tilted",
    "elegant",
    "enchanting"
  };

  private static final String[] cavernTypes = new String[] {
    "kitchen",
    "bedroom",
    "closet",
    "living room",
    "dining room",
    "foyer",
    "hallway",
    "wardrobe",
    "suite",
    "bath"
  };

  private static final String[] adornments = new String[] {
   "smelling of gingerbread.",
    "with a beautifully decorated tree.",
    "with an elf doll watching from a shelf.",
    "",
    "filled with presents.",
    "with a small table holding cookies and milk.",
    "with a toy train running in circles.",
    "with a partridge in a pear tree.",
    "with four maids-a-milking. Why are they milking? Two words: egg. nog.",
    "that is filled with Christmas music.",
    "that is decked with boughs of holly.",
    "that makes you feel jolly."
  };

  public static void main(String[] args) throws IOException {
    game = HtwFactory.makeGame("htw.game.HuntTheWumpusGame", new Main());
    createMap();
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    displayInstructions();
    game.makeRestCommand().execute();
    while (playing) {
      System.out.println();
      System.out.println("You are in " + game.getPlayerCavern());
      System.out.println();
      System.out.println("Health: " + hitPoints + " Candy canes: " + game.getQuiver() + " Flashlight?: " + (game.getPlayerHasFlashlight() ? "Yes" : "No"));
      HuntTheWumpus.Command c = game.makeRestCommand();
      System.out.println(">");
      String command = br.readLine();
      if (command.equalsIgnoreCase("e"))
        c = game.makeMoveCommand(EAST);
      else if (command.equalsIgnoreCase("w"))
        c = game.makeMoveCommand(WEST);
      else if (command.equalsIgnoreCase("n"))
        c = game.makeMoveCommand(NORTH);
      else if (command.equalsIgnoreCase("s"))
        c = game.makeMoveCommand(SOUTH);
      else if (command.equalsIgnoreCase("r"))
        c = game.makeRestCommand();
      else if (command.equalsIgnoreCase("sw"))
        c = game.makeShootCommand(WEST);
      else if (command.equalsIgnoreCase("se"))
        c = game.makeShootCommand(EAST);
      else if (command.equalsIgnoreCase("sn"))
        c = game.makeShootCommand(NORTH);
      else if (command.equalsIgnoreCase("ss"))
        c = game.makeShootCommand(SOUTH);
      else if (command.equalsIgnoreCase("q"))
        return;

      c.execute();
    }
    displayResults();
  }

  private static void displayInstructions() throws IOException {
    System.out.println("You must save Christmas!");
    System.out.println("");
    System.out.println("Maybe you weren't on your best behavior this year, but this is much worse than you expected.");
    System.out.println("Krampus, the hairy, horned beast who punishes bad children at Christmas, has gotten into your house on Christmas Eve!");
    System.out.println("For some reason, he manages to show up the same day your house gets infested by flying monkeys.");
    System.out.println("You've got 10 candy canes you can shoot at Krampus to try to drive him away.");
    System.out.println("There's a flashlight somewhere in the house - find it and maybe it will be helpful.");
    System.out.println("Good luck! Only you can save Christmas! Of course, if you'd behaved better throughout the year, this wouldn't be happening, but never mind, you can be a flawed hero!");
    System.out.println("");
    System.out.println("Press <ENTER> when you're ready to start...");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    br.readLine();
  }

  private static void displayResults() {
    if (game.getKrampusDefeated()) {
      System.out.println("Now the bad children will get the same gifts as the good children! Wait a minute, that can't be right...");
      System.out.println("Nevermind - presents for everyone! Yay!");
    } else {
      System.out.println("Hopefully you've learned a lesson, and you'll behave well enough next year that Krampus won't visit you again.");
    }
  }

  private static void createMap() {
    int ncaverns = (int) (Math.random() * 30.0 + 10.0);
    while (ncaverns-- > 0)
      caverns.add(makeName());

    for (String cavern : caverns) {
      maybeConnectCavern(cavern, NORTH);
      maybeConnectCavern(cavern, SOUTH);
      maybeConnectCavern(cavern, EAST);
      maybeConnectCavern(cavern, WEST);
    }

    String playerCavern = anyCavern();
    String flashlightCavern = anyOther(playerCavern);
    game.setPlayerCavern(playerCavern);
    game.setFlashlightCavern(flashlightCavern);
    game.setWumpusCavern(anyOther(playerCavern));
    game.addBatCavern(anyOther(playerCavern));
    game.addBatCavern(anyOther(playerCavern));
    game.addBatCavern(anyOther(playerCavern));

    game.addPitCavern(anyOther(playerCavern));
    game.addPitCavern(anyOther(playerCavern));
    game.addPitCavern(anyOther(playerCavern));

    game.setQuiver(5);
  }

  private static String makeName() {

    return "a" + chooseName(environments) + " " + chooseName(shapes) + " " +
      chooseName(cavernTypes) + " " + chooseName(adornments);
  }

  private static String chooseName(String[] names) {
    int n = names.length;
    int choice = (int)(Math.random() * (double) n);
    return names[choice];
  }

  private static void maybeConnectCavern(String cavern, Direction direction) {
    if (Math.random() > .2) {
      String other = anyOther(cavern);
      connectIfAvailable(cavern, direction, other);
      connectIfAvailable(other, direction.opposite(), cavern);
    }
  }

  private static void connectIfAvailable(String from, Direction direction, String to) {
    if (game.findDestination(from, direction) == null) {
      game.connectCavern(from, to, direction);
    }
  }

  private static String anyOther(String cavern) {
    String otherCavern = cavern;
    while (cavern.equals(otherCavern)) {
      otherCavern = anyCavern();
    }
    return otherCavern;
  }

  private static String anyCavern() {
    return caverns.get((int) (Math.random() * caverns.size()));
  }

  public void noPassage() {
    System.out.println("No Passage.");
  }

  public void hearBats() {
    System.out.println("You hear screeching.");
  }

  public void hearPit() {
    System.out.println("You hear creaking.");
  }

  public void smellWumpus() {
    System.out.println("There is a terrible smell. It smells like sulphur and disappointment.");
  }

  public void passage(Direction direction) {
    System.out.println("You can go " + direction.name());
  }

  public void noArrows() {
    System.out.println("You have no candy canes to fire.");
  }

  public void arrowShot() {
    System.out.println("Thwang! Take that, cavity prevention!");
  }

  public void playerShootsSelfInBack() {
    System.out.println("Ow!  You shot yourself in the back.");
    hit(3);
  }

  public void playerKillsWumpus() {
    System.out.println("Your candy cane hits Krampus and drives him away!");
    playing=false;
  }

  public void playerShootsWall() {
    System.out.println("You shot the wall and the ricochet hurt you.");
    hit(3);
  }

  public void arrowsFound(Integer arrowsFound) {
    System.out.println("You found " + arrowsFound + " candy cane" + (arrowsFound == 1 ? "" : "s") + ".");
  }

  public void flashlightFound() {
    System.out.println("You found a flashlight! Flying monkeys are afraid of the light, maybe this will be useful.");
  }

  public void fellInPit() {
    System.out.println("You fell down some stairs and hurt yourself.");
    hit(4);
  }

  public void playerMovesToWumpus() {
    System.out.println("You walked into the waiting arms of Krampus. He knows of your bad behavior, and has coal for you instead of presents.");
    playing = false;
  }

  public void wumpusMovesToPlayer() {
    System.out.println("Krampus has found you. You have been given coal and birch branches as punishment for your bad behavior.");
    playing = false;
  }

  public void batsTransport() {
    System.out.println("Some flying monkeys carried you away. They kept asking about Dorothy.");
  }

  private void hit(int points) {
    hitPoints -= points;
    if (hitPoints <= 0) {
      System.out.println("You are rendered unconscious. You sleep through Christmas and don't get any presents.");
      playing = false;
    }
  }
}
