package at.ac.tuwien.ifs.sge.game.risk.board;

public class RiskTerritory {

  private final int continentId;

  private int occupantPlayerId;
  private int troops;

  public RiskTerritory(int continentId) {
    this(continentId, -1, 0);
  }

  public RiskTerritory(int continentId, int occupantPlayerId, int troops) {
    this.continentId = continentId;
    this.occupantPlayerId = occupantPlayerId;
    this.troops = troops;
  }

  public RiskTerritory(RiskTerritory riskTerritory) {
    this.continentId = riskTerritory.continentId;
    this.occupantPlayerId = riskTerritory.occupantPlayerId;
    this.troops = riskTerritory.troops;
  }

  public int getContinentId() {
    return continentId;
  }

  public int getOccupantPlayerId() {
    return occupantPlayerId;
  }

  public void setOccupantPlayerId(int occupantPlayerId) {
    this.occupantPlayerId = occupantPlayerId;
  }

  public int getTroops() {
    return troops;
  }

  public void setTroops(int troops) {
    this.troops = troops;
  }

  public int addTroops(int troops) {
    this.troops += troops;
    return this.troops;
  }

  public int removeTroops(int troops) {
    this.troops -= troops;
    return this.troops;
  }

}
