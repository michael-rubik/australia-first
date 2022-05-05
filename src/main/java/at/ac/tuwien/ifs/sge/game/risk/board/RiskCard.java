package at.ac.tuwien.ifs.sge.game.risk.board;

import java.util.Objects;

public class RiskCard {

  public static final int WILDCARD = (-1);
  public static final int JOKER = 0;
  public static final int INFANTRY = 1;
  public static final int ARTILLERY = 2;
  public static final int CAVALRY = 3;
  private static int WILDCARD_TERRITORY = (-1);
  public static final RiskCard wildcard = wildcard();
  private static int JOKER_TERRITORY = (-1);
  public static final RiskCard joker = joker();

  private final int cardType;
  private final int territoryId;

  public RiskCard(int cardType, int territoryId) {
    this.cardType = cardType;
    this.territoryId = territoryId;
  }

  public static RiskCard wildcard() {
    return new RiskCard(WILDCARD, ++WILDCARD_TERRITORY);
  }

  public static RiskCard joker() {
    return new RiskCard(JOKER, ++JOKER_TERRITORY);
  }

  public int getCardType() {
    return cardType;
  }

  public int getTerritoryId() {
    return territoryId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RiskCard riskCard = (RiskCard) o;
    return cardType == riskCard.cardType &&
        territoryId == riskCard.territoryId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardType, territoryId);
  }

  @Override
  public String toString() {
    if (this.equals(wildcard)) {
      return "[*]";
    } else if (this.equals(joker)) {
      return "[J]";
    }
    return "[" + cardType + "@" + territoryId + "]";
  }
}
