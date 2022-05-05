package at.ac.tuwien.ifs.sge.game.risk.board;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RiskAction {

  private static final RiskAction END_PHASE = new RiskAction(-2, -4, -8);
  private static final int CASUALTIES_ID = -1;
  private static final int OCCUPY_ID = -2;
  private static final int CARD_ID = -3;
  private static final int BONUS_ID = -4;

  private final int srcId;
  private final int targetId;
  private final int value;

  private RiskAction(int srcId, int targetId, int value) {
    this.srcId = srcId;
    this.targetId = targetId;
    this.value = value;
  }

  private RiskAction(int targetId, int value) {
    this.srcId = -1;
    this.targetId = targetId;
    this.value = value;
  }

  private RiskAction(int value) {
    this.srcId = -1;
    this.targetId = -1;
    this.value = value;
  }

  /**
   * Used in the territory selection phase. Claims the specified id.
   *
   * @param id - the id of the territory
   * @return an action modelling the selection of a territory
   */
  public static RiskAction select(int id) {
    return new RiskAction(id, 1);
  }

  /**
   * Used in the reinforcement phase. Uses the specified number of troops from the unused
   * reinforcements to bolster the given id.
   *
   * @param id - the id of the territory
   * @param troops - the number of troops to reinforce
   * @return an action modelling the reinforcement of a territory with a number of troops
   */
  public static RiskAction reinforce(int id, int troops) {
    return new RiskAction(id, troops);
  }

  /**
   * Used in the attack phase. Uses the specified number of troops from the mobile troops of a
   * territory to attack another territory.
   *
   * @param attackingId - the id of the territory to attack from
   * @param defendingId - the id of the territory to attack to
   * @param troops - the number of troops used in the attack
   * @return an action modelling the attack of a territory to another territory with a number of
   * troops
   */
  public static RiskAction attack(int attackingId, int defendingId, int troops) {
    return new RiskAction(attackingId, defendingId, troops);
  }

  /**
   * Used in the occupy phase. After a successful attack the attacker must specify how many troops
   * they want to use to occupy the captured territory.
   *
   * @param troops - the number of troops
   * @return an action modelling using a number of troops to occupy a just captured territory
   */
  public static RiskAction occupy(int troops) {
    return new RiskAction(OCCUPY_ID, OCCUPY_ID, troops);
  }

  /**
   * Used in the fortify phase. The player is allowed to fortify in the end of their turn. Uses the
   * specified number of troops from the mobile troops of a territory to move to another territory.
   *
   * @param fortifyingId - the id of the territory to fortify from
   * @param fortifiedId - the id of the territory to fortify to
   * @param troops - the number of troops used to fortify
   * @return an action modelling the fortification of a territory with a number of troops from
   * another territory
   */
  public static RiskAction fortify(int fortifyingId, int fortifiedId, int troops) {
    return new RiskAction(fortifyingId, fortifiedId, troops);
  }

  /**
   * In any non-selfending phases used to end a phase.
   *
   * @return an action modelling the end of a phase
   */
  public static RiskAction endPhase() {
    return END_PHASE;
  }

  /**
   * Used in an attack. Sums how many casualties each player has.
   *
   * @param attacker - the number of troops lost for the attacker
   * @param defender - the number of troops lost for the defender
   * @return an action modelling the casualties of each player
   */
  public static RiskAction casualties(int attacker, int defender) {
    return new RiskAction(CASUALTIES_ID, CASUALTIES_ID,
        attacker | (defender << (Integer.SIZE / 2)));
  }

  /**
   * Used in the begin of the reinforcement phase. Uses the specified card slots to trade in values
   * of cards. This method is rather complicated to use so rather use playCards(). Each card slot is
   * enumerated by a value. The specified id acts as an binary array where the enumerated value is
   * the inclusion of a card slot. For example: 0b00111 would mean the card slots 0, 1, 2 but not 3
   * and 4.
   *
   * @param id - the binary values of the card slots
   * @return an action modelling the trade in of the cards in the specified card slots
   */
  public static RiskAction cardSlots(int id) {
    return new RiskAction(CARD_ID, CARD_ID, id);
  }

  /**
   * Used in the begin of the reinforcement phase. Uses the specified card slots to trade in values
   * of cards.
   *
   * @param ids - slots as an distinct values
   * @return an action modelling the trade in of the cards in the specified card slots
   */
  public static RiskAction playCards(int... ids) {
    return new RiskAction(CARD_ID, CARD_ID, idsToSlotIds(ids));
  }

  /**
   * Used in the beginning of the reinforcement phase. Uses the specified card slots to trade in
   * values of cards.
   *
   * @param ids - slots as an iterable
   * @return an action modelling the trade in of the cards in the specified card slots
   */
  public static RiskAction playCards(Iterable<Integer> ids) {
    return new RiskAction(CARD_ID, CARD_ID, idsToSlotIds(ids));
  }

  /**
   * Used after trading in cards. Used to model how many troops a player is awarded after trading in
   * a set of cards. This might be indeterminant in an uncanonical game
   *
   * @param nr - number of bonus troops
   * @return an action modelling the award of a number of troops after trade in
   */
  public static RiskAction bonusTroopsFromCards(int nr) {
    return new RiskAction(BONUS_ID, BONUS_ID, nr);
  }

  /**
   * Returns which territory was selected. Note that this method only has defined behaviour if it
   * was created using RiskAction.select().
   *
   * @return which territory was selected
   */
  public int selected() {
    return targetId;
  }

  /**
   * Returns which territory was reinforced. Note that this method only has defined behaviour if it
   * was created using RiskAction.reinforce().
   *
   * @return which territory was reinforced
   */
  public int reinforcedId() {
    return targetId;
  }

  /**
   * Returns which territory was attacking. Note that this method only has defined behaviour if it
   * was created using RiskAction.attack().
   *
   * @return which territory was attacking
   */
  public int attackingId() {
    return srcId;
  }

  /**
   * Returns which territory was defending. Note that this method only has defined behaviour if it
   * was created using RiskAction.attack().
   *
   * @return which territory was defending
   */
  public int defendingId() {
    return targetId;
  }

  /**
   * Returns which number of troops was involved. Note that this method only has defined behaviour
   * if it was created using RiskAction.attack(), RiskAction.reinforce(), RiskAction.fortify()
   *
   * @return which number of troops was involved
   */
  public int troops() {
    return value;
  }

  /**
   * Returns which card slots where traded in. Note that this method only has defined behaviour if
   * it was created using RiskAction.cardSlots() RiskAction.playCards().
   *
   * @return which card slots where traded in
   */
  public Set<Integer> playedCards() {
    return RiskAction.slotIdsToIds(value);
  }

  /**
   * Returns which territory was fortifying. Note that this method only has defined behaviour if it
   * was created using RiskAction.fortify().
   *
   * @return which territory was fortifying
   */
  public int fortifyingId() {
    return srcId;
  }

  /**
   * Returns which territory was fortified. Note that this method only has defined behaviour if it
   * was created using RiskAction.fortify().
   *
   * @return which territory was fortifying
   */
  public int fortifiedId() {
    return targetId;
  }

  /**
   * Returns how many casualties the attacker had. Note that this method only has defined behaviour
   * if it was created using RiskAction.casualties().
   *
   * @return how many casualties the attacker had
   */
  public int attackerCasualties() {
    return value & (~0 >>> (Integer.SIZE / 2));
  }

  /**
   * Returns how many casualties the defender had. Note that this method only has defined behaviour
   * if it was created using RiskAction.casualties().
   *
   * @return how many casualties the defender had
   */
  public int defenderCasualties() {
    return (value >>> (Integer.SIZE / 2)) & (~0 >>> (Integer.SIZE / 2));
  }

  /**
   * Returns how many bonus troops the last trade in brought. Note that this method only has defined
   * behaviour if it was created using RiskAction.bonusTroopsFromCards().
   *
   * @return how many bonus troops the last trade in brought
   */
  public int getBonus() {
    return value;
  }

  /**
   * Returns true iff the action models the end phase action.
   *
   * @return true iff the action models the end phase action
   */
  public boolean isEndPhase() {
    return srcId == END_PHASE.srcId && targetId == END_PHASE.targetId && value == END_PHASE.value;
  }

  /**
   * Returns true iff the action models the play of cardslots.
   *
   * @return true iff the action models the play of cardslots.
   */
  public boolean isCardIds() {
    return srcId == CARD_ID && targetId == CARD_ID;
  }

  /**
   * Returns true iff the action models the bonus of a trade in.
   *
   * @return true iff the action models the bonus of a trade in
   */
  public boolean isBonus() {
    return srcId == BONUS_ID && targetId == BONUS_ID;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RiskAction that = (RiskAction) o;
    return srcId == that.srcId &&
        targetId == that.targetId &&
        value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(srcId, targetId, value);
  }

  @Override
  public String toString() {
    if (isEndPhase()) {
      return "end phase";
    }

    if (srcId == targetId && srcId == CASUALTIES_ID) {
      return String.format("%dX%d", this.attackerCasualties(), this.defenderCasualties());
    }

    if (srcId == targetId && srcId == OCCUPY_ID) {
      return "O" + value;
    }

    if (srcId == targetId && srcId == CARD_ID) {
      return "C".concat(playedCards().toString());
    }

    if (srcId == targetId && srcId == BONUS_ID) {
      return "B" + value;
    }

    if (srcId == -1) {
      return "-(" + value + ")->" + targetId;
    }

    return srcId + "-(" + value + ")->" + targetId;

  }

  public static RiskAction fromString(String string) {
    if (string.equals("end phase")) {
      return endPhase();
    }

    if (string.startsWith("O")) {
      int troops = Integer.parseInt(string.substring(1));
      return occupy(troops);
    }
    if (string.contains("X")) {
      String[] casualties = string.split("X");
      int attacker = Integer.parseInt(casualties[0]);
      int defender = Integer.parseInt(casualties[1]);
      return casualties(attacker, defender);
    }

    if (string.startsWith("B")) {
      return bonusTroopsFromCards(Integer.parseInt(string.substring(1)));
    }

    if (string.startsWith("C[")) {
      string = string.substring(2, string.length() - 1);
      return playCards(Arrays.stream(string.split(", ")).map(Integer::parseInt)
          .collect(Collectors.toUnmodifiableSet()));
    }

    if (string.startsWith("-(")) {
      int troopsStringEnd = string.indexOf(')');
      String troopsString = string.substring(2, troopsStringEnd);

      int troops = Integer.parseInt(troopsString);
      int destination = Integer.parseInt(string.substring(troopsStringEnd + 3));

      return reinforce(destination, troops);
    }

    if (string.contains("(")) {
      int srcStart = 0;
      int srcEnd = string.indexOf('(') - 1;

      int troopStart = srcEnd + 2;
      int troopEnd = string.indexOf(')');

      int destStart = troopEnd + 3;

      return attack(Integer.parseInt(string.substring(srcStart, srcEnd)),
          Integer.parseInt(string.substring(destStart)),
          Integer.parseInt(string.substring(troopStart, troopEnd)));

    }

    return null;
  }


  private static int idsToSlotIds(int... ids) {
    int value = 0;
    for (int id : ids) {
      value |= (1 << id);
    }
    return value;
  }


  private static int idsToSlotIds(Iterable<Integer> ids) {
    int value = 0;
    for (int id : ids) {
      value |= (1 << id);
    }
    return value;
  }

  private static Set<Integer> slotIdsToIds(final int ids) {
    return IntStream.range(0, Integer.SIZE - Integer.numberOfLeadingZeros(ids))
        .filter(i -> ((ids & (1 << i)) >>> i) != 0)
        .boxed().collect(Collectors.toSet());
  }


}
