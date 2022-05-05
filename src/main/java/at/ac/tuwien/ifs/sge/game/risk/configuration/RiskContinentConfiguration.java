package at.ac.tuwien.ifs.sge.game.risk.configuration;

import at.ac.tuwien.ifs.sge.game.risk.board.RiskContinent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RiskContinentConfiguration {


  public static final RiskContinentConfiguration WILDCARD = new RiskContinentConfiguration(-1, 0);
  private static int cid = 0;
  public static final RiskContinentConfiguration NORTH_AMERICA = new RiskContinentConfiguration(
      cid++,
      5);
  public static final RiskContinentConfiguration SOUTH_AMERICA = new RiskContinentConfiguration(
      cid++,
      2);
  public static final RiskContinentConfiguration EUROPE = new RiskContinentConfiguration(cid++, 5);
  public static final RiskContinentConfiguration AFRICA = new RiskContinentConfiguration(cid++, 3);
  public static final RiskContinentConfiguration ASIA = new RiskContinentConfiguration(cid++, 7);
  public static final RiskContinentConfiguration AUSTRALIA = new RiskContinentConfiguration(cid++,
      2);
  public static final List<RiskContinentConfiguration> allContinents = Arrays
      .asList(NORTH_AMERICA, SOUTH_AMERICA, EUROPE, AFRICA, ASIA, AUSTRALIA);
  private int continentId;
  private int troopBonus;

  public RiskContinentConfiguration() {
  }

  public RiskContinentConfiguration(int continentId, int troopBonus) {
    this.continentId = continentId;
    this.troopBonus = troopBonus;
  }

  public RiskContinent getContinent() {
    return new RiskContinent(troopBonus);
  }

  public int getContinentId() {
    return continentId;
  }

  public void setContinentId(int continentId) {
    this.continentId = continentId;
  }

  public int getTroopBonus() {
    return troopBonus;
  }

  public void setTroopBonus(int troopBonus) {
    this.troopBonus = troopBonus;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RiskContinentConfiguration that = (RiskContinentConfiguration) o;
    return continentId == that.continentId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(continentId);
  }
}
