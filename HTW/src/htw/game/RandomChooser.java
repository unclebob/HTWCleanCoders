package htw.game;

import java.util.List;

class RandomChooser {
  public <T> T chooseFrom(List<T> choices) {
    int nChoices = choices.size();
    int choice = randomChoice(nChoices);
    return choices.get(choice);
  }

  private int randomChoice(int numberOfPossibleChoices) {
    return (int) (Math.random() * numberOfPossibleChoices);
  }
}
