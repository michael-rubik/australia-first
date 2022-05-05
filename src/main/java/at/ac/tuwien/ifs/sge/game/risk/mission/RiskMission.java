package at.ac.tuwien.ifs.sge.game.risk.mission;

import at.ac.tuwien.ifs.sge.game.risk.configuration.RiskMissionConfiguration;
import java.util.List;
import java.util.Objects;

public class RiskMission {

  public static final int WILDCARD_ID = (-1);
  public static final RiskMission FALLBACK = RiskMissionConfiguration.occupyTerritories(24, 2)
      .get(0).getMission();

  private final RiskMissionType riskMissionType;
  private final List<Integer> targetIds;
  private final int occupyingWith;

  public RiskMission(RiskMissionType riskMissionType, List<Integer> targetIds, int occupyingWith) {
    this.riskMissionType = riskMissionType;
    this.targetIds = targetIds;
    this.occupyingWith = occupyingWith;
  }

  public RiskMission(RiskMissionType riskMissionType, List<Integer> targetIds) {
    this(riskMissionType, targetIds, 0);
  }

  public RiskMissionType getRiskMissionType() {
    return riskMissionType;
  }

  public List<Integer> getTargetIds() {
    return targetIds;
  }

  public int getOccupyingWith() {
    return occupyingWith;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RiskMission that = (RiskMission) o;
    return occupyingWith == that.occupyingWith &&
        riskMissionType == that.riskMissionType &&
        targetIds.equals(that.targetIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(riskMissionType, targetIds, occupyingWith);
  }
}
