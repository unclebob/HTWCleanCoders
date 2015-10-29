package htw.factory;

import htw.HtwMessageReceiver;
import htw.HuntTheWumpus;

import java.lang.reflect.Constructor;

public class HtwFactory {
  public static HuntTheWumpus makeGame(String htwClassName, HtwMessageReceiver receiver) {
    HuntTheWumpus huntTheWumpus = null;
    try {
      Class<?> htwClass = Class.forName(htwClassName);
      Constructor<?> htwClassConstructor = htwClass.getConstructor(HtwMessageReceiver.class);
      huntTheWumpus = (HuntTheWumpus) htwClassConstructor.newInstance(receiver);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
    return huntTheWumpus;
  }
}
