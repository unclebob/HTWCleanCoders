package htw.fixtures;

public class WumpusWasIn {
  private String cavern = "";
  public void setCavern(String cavern) {
    this.cavern = cavern;
  }
  public int times() {
    return zeroIfNull(TestContext.wumpusCaverns.get(cavern));
  }

  private int zeroIfNull(Integer integer) {
    return integer == null ? 0 : integer;
  }
}
