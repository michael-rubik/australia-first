package at.ac.tuwien.ifs.sge.game.risk.board;

import at.ac.tuwien.ifs.sge.game.ActionRecord;
import at.ac.tuwien.ifs.sge.game.Dice;
import at.ac.tuwien.ifs.sge.game.Game;
import at.ac.tuwien.ifs.sge.game.risk.configuration.RiskConfiguration;
import at.ac.tuwien.ifs.sge.game.risk.util.PriestLogic;
import at.ac.tuwien.ifs.sge.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Risk implements Game<RiskAction, RiskBoard> {

  private final static int CASUALTIES_PLAYER = -6;
  private final static int DRAW_CARD_PLAYER = -2;
  private final static int MISSION_FULFILLED_PLAYER = -3;
  private final static int BONUS_PLAYER = -5;

  private final boolean canonical;
  private final Dice attackerDice;
  private final Dice defenderDice;
  private int currentPlayerId;
  private List<ActionRecord<RiskAction>> actionRecords;
  private RiskBoard board;

  public Risk() {
    this(RiskConfiguration.RISK_DEFAULT_CONFIG, 2);
  }

  public Risk(int numberOfPlayers) {
    this(RiskConfiguration.RISK_DEFAULT_CONFIG, numberOfPlayers);
  }

  public Risk(String yaml, int numberOfPlayers) {
    this(yaml == null || yaml.isEmpty() ? RiskConfiguration.RISK_DEFAULT_CONFIG
        : (RiskConfiguration) RiskConfiguration.getYaml().load(yaml), numberOfPlayers);
  }

  public Risk(RiskConfiguration configuration, int numberOfPlayers) {
    this(0, true, Collections.emptyList(), new RiskBoard(configuration, numberOfPlayers));
    if (numberOfPlayers > configuration.getMaxNumberOfPlayers()) {
      throw new IllegalArgumentException("Wrong number of players");
    }
  }

  public Risk(Risk risk) {
    this(risk.currentPlayerId, risk.canonical, risk.actionRecords, risk.board);
  }

  public Risk(int currentPlayerId, boolean canonical,
      List<ActionRecord<RiskAction>> actionRecords, RiskBoard board) {
    this.currentPlayerId = currentPlayerId;
    this.canonical = canonical;
    this.actionRecords = new ArrayList<>(actionRecords);
    this.board = new RiskBoard(board);
    if (!(getMinimumNumberOfPlayers() <= getNumberOfPlayers()
        && getNumberOfPlayers() <= getMaximumNumberOfPlayers())) {
      throw new IllegalArgumentException("Wrong number of players");
    }

    this.attackerDice = new Dice(board.getMaxAttackerDice());
    this.defenderDice = new Dice(board.getMaxDefenderDice());
  }

  private static Set<RiskAction> possibleCasualties(final int attackerDice,
      final int defenderDice) {
    final int dice = Math.min(attackerDice, defenderDice);
    return IntStream.rangeClosed(0, dice)
        .mapToObj(die -> RiskAction.casualties(die, dice - die)).collect(Collectors.toSet());
  }

  private static char decreaseLexicographical(char c) {
    if (c == '0') {
      return c;
    }
    if (c == 'a') {
      return '9';
    }

    return --c;
  }

  private static char increaseLexicographical(char c) {
    if (c == 'z') {
      return 'z';
    }
    if (c == '9') {
      return 'a';
    }

    return ++c;
  }

  private static String whitespace(char c) {
    if ('a' <= c) {
      return whitespace((c - 'a') + 10);
    }
    return whitespace(c - '0');
  }

  private static String whitespace(int n) {
    String ret = "";
    for (int i = 0; i < n; i++) {
      ret = ret.concat(" ");
    }

    return ret;
  }

  private static Risk stripOutUnknownInformation(Risk game) {
    game.board.stripOutUnknownInformation();
    return game;
  }

  private static Risk stripOutUnknownInformation(Risk game, int player) {
    Risk next = stripOutUnknownInformation(game);
    next.board.stripOutUnknownInformation(player);

    return next;
  }

  @Override
  public boolean isGameOver() {
    // all territories belong to one player
    return !isInitialSelect() && board.getTerritories().values().stream().mapToInt(
        RiskTerritory::getOccupantPlayerId).distinct().count()
        == 1L;
  }

  @Override
  public int getMinimumNumberOfPlayers() {
    return 2;
  }

  @Override
  public int getMaximumNumberOfPlayers() {
    return Integer.MAX_VALUE;
  }

  @Override
  public int getNumberOfPlayers() {
    return board.getNumberOfPlayers();
  }

  @Override
  public int getCurrentPlayer() {
    return currentPlayerId;
  }

  @Override
  public double getUtilityValue(int i) {
    Set<Integer> playersInGame = board.getTerritories().values().stream()
        .map(RiskTerritory::getOccupantPlayerId).collect(Collectors.toSet());
    if (playersInGame.size() <= 1 && playersInGame.contains(i)) {
      return 1D;
    }
    return 0D;
  }

  @Override
  public double getHeuristicValue(int player) {
    return board.getNrOfTerritoriesOccupiedByPlayer(player);
  }

  @Override
  public Set<RiskAction> getPossibleActions() {
    if (isGameOver()) {
      return Collections.emptySet();
    }
    if (currentPlayerId < 0) {
      if (board.isAttack()) {
        return casualtiesGPA();
      } else if (currentPlayerId == BONUS_PLAYER) {
        return bonusGPA();
      }
    } else if (isInitialSelect()) {
      return initialSelectGPA();
    } else if (isInitialReinforce()) {
      return initialReinforceGPA();
    } else if (board.hasToTradeInCards(currentPlayerId)) {
      return tradeInGPA();
    } else if (board.isReinforcementPhase()) {
      return reinforceGPA();
    } else if (board.isAttackPhase()) {
      return attackGPA();
    } else if (board.isOccupyPhase()) {
      return occupyGPA();
    } else if (board.isFortifyPhase()) {
      return fortifyGPA();
    }

    return Collections.emptySet();
  }

  private boolean isInitialSelect() {
    if (board.isInitialSelectMaybe() && (board.getTerritories().values().stream().anyMatch(
        t -> !(0 <= t.getOccupantPlayerId() && t.getOccupantPlayerId() < getNumberOfPlayers())))) {
      return true;
    }
    board.disableInitialSelectMaybe();
    return false;
  }

  private boolean isInitialReinforce() {
    if (board.isInitialReinforceMaybe() && board.areReinforcementsLeft()) {
      return true;
    }
    board.disableInitialReinforceMaybe();
    return false;
  }

  private boolean couldMissionBeDone() {
    return couldMissionBeDone(currentPlayerId);
  }

  private boolean couldMissionBeDone(int player) {
    return PriestLogic.possible(board.missionFulfilled(player));
  }

  private boolean isMissionDone() {
    return isMissionDone(currentPlayerId);
  }

  private boolean isMissionDone(int player) {
    return PriestLogic.valid(board.missionFulfilled(player));
  }

  private Set<RiskAction> initialSelectGPA() {
    return board.getTerritories().entrySet().stream().filter(
        t -> !(0 <= t.getValue().getOccupantPlayerId()
            && t.getValue().getOccupantPlayerId() < getNumberOfPlayers())).mapToInt(Entry::getKey)
        .mapToObj(RiskAction::select).collect(Collectors.toSet());
  }

  private Set<RiskAction> initialReinforceGPA() {
    return board.getTerritoriesOccupiedByPlayer(this.currentPlayerId).stream()
        .map(id -> RiskAction.reinforce(id, 1)).collect(Collectors.toSet());
  }

  private Set<RiskAction> tradeInGPA() {
    return board.getTradeInSlots(currentPlayerId).stream().map(RiskAction::playCards)
        .collect(Collectors.toUnmodifiableSet());
  }

  private Set<RiskAction> bonusGPA() {
    return IntStream
        .rangeClosed(board.getMinMatchingTerritories(), board.getMaxMatchingTerritories())
        .mapToObj(RiskAction::bonusTroopsFromCards).collect(Collectors.toUnmodifiableSet());
  }

  private Set<RiskAction> reinforceGPA() {
    Set<RiskAction> actions = new HashSet<>();
    int reinforcementsLeft = board.reinforcementsLeft(currentPlayerId);

    if (board.couldTradeInCards(currentPlayerId)) {
      actions.addAll(tradeInGPA());
    }

    Map<Integer, RiskTerritory> territories = board.getTerritories().entrySet().stream().filter(
        t -> t.getValue().getOccupantPlayerId() == currentPlayerId &&
            !board.isReinforcedAlready(t.getKey()))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    if (territories.size() == 1) {
      for (Entry<Integer, RiskTerritory> territory : territories.entrySet()) {
        actions.add(RiskAction.reinforce(territory.getKey(), reinforcementsLeft));
      }
    } else {

      final int tradeInTerritoryBonus = board.getTradeInTerritoryBonus();
      final int promisedReinforcements = (int) (territories.keySet().stream()
          .filter(t -> board.inBonusTerritories(t)).count() * tradeInTerritoryBonus);

      for (Entry<Integer, RiskTerritory> territory : territories.entrySet()) {
        final int territoryId = territory.getKey();
        final boolean inBonusTerritories = board.inBonusTerritories(territoryId);
        for (int r = (inBonusTerritories ? tradeInTerritoryBonus : 1);
            r <= (reinforcementsLeft - (promisedReinforcements - (inBonusTerritories
                ? tradeInTerritoryBonus : 0)));
            r++) {
          actions.add(RiskAction.reinforce(territoryId, r));
        }
      }
    }
    return actions;
  }

  private Set<RiskAction> attackGPA() {
    Set<RiskAction> actions = new HashSet<>();
    actions.add(RiskAction.endPhase());

    Set<Integer> territories = board
        .getTerritoriesOccupiedByPlayerWithMoreThanOneTroops(this.currentPlayerId);
    for (Integer territory : territories) {
      Set<Integer> neighbors = board.neighboringEnemyTerritories(territory);
      int maxAttack = board.getMaxAttackingTroops(territory);
      for (int t = 1; t <= maxAttack; t++) {
        final int finalT = t;
        actions.addAll(
            neighbors.stream().map(n -> RiskAction.attack(territory, n, finalT))
                .collect(Collectors.toSet()));
      }
    }

    return actions;
  }

  private Set<RiskAction> casualtiesGPA() {
    return possibleCasualties(board.getNrOfAttackerDice(), board.getNrOfDefenderDice());
  }

  private Set<RiskAction> occupyGPA() {
    return IntStream.rangeClosed(1, board.getMaxOccupy()).mapToObj(RiskAction::occupy).collect(
        Collectors.toSet());
  }

  private Set<RiskAction> fortifyGPA() {
    Set<RiskAction> actions = new HashSet<>();
    actions.add(RiskAction.endPhase());

    for (Integer src : board.getTerritoriesOccupiedByPlayerWithMoreThanOneTroops(currentPlayerId)) {
      for (Integer dest : board.getFortifyableTerritories(src)) {
        for (int t = 1; t <= board.getFortifyableTroops(src); t++) {
          actions.add(RiskAction.fortify(src, dest, t));
        }
      }
    }

    return actions;
  }

  @Override
  public RiskBoard getBoard() {
    return new RiskBoard(board);
  }

  @Override
  public boolean isValidAction(RiskAction riskAction) {
    if (riskAction == null || isGameOver()) {
      return false;
    }
    if (currentPlayerId < 0) {
      if (board.isAttack()) {
        int armiesFought = Math.min(board.getNrOfAttackerDice(), board.getNrOfDefenderDice());
        int attackerCasualties = riskAction.attackerCasualties();
        int defenderCasualties = riskAction.defenderCasualties();
        return attackerCasualties + defenderCasualties == armiesFought;
      } else if (currentPlayerId == BONUS_PLAYER) {
        return riskAction.isBonus() && board.getMinMatchingTerritories() <= riskAction.getBonus()
            && riskAction.getBonus() <= board.getMaxMatchingTerritories();
      }
    } else if (isInitialSelect()) {
      int selected = riskAction.selected();
      return board.isTerritory(selected) && !(
          0 <= board.getTerritoryOccupantId(selected)
              && board.getTerritoryOccupantId(selected) < getNumberOfPlayers());
    } else if (isInitialReinforce()) {
      return board.isTerritory(riskAction.reinforcedId()) && riskAction.troops() == 1
          && board.getTerritoryOccupantId(riskAction.reinforcedId()) == currentPlayerId;
    } else if (board.hasToTradeInCards(currentPlayerId)) {
      return riskAction.isCardIds();
    } else if (riskAction.isCardIds()) {
      return board.allowedToTradeIn(currentPlayerId) && board
          .canTradeInAsSet(riskAction.playedCards(), currentPlayerId);
    } else if (board.isReinforcementPhase()) {
      int reinforcementsLeft = board.reinforcementsLeft(currentPlayerId);
      int reinforced = riskAction.reinforcedId();
      int reinforceOptionsLeft = (int) board.getTerritoriesOccupiedByPlayer(currentPlayerId)
          .stream().filter(t -> !board.isReinforcedAlready(t)).count();
      return 1 <= riskAction.troops() && riskAction.troops() <= reinforcementsLeft && (
          !board.inBonusTerritories(reinforced) || board.getTradeInTerritoryBonus() <= riskAction
              .troops()) && board.isTerritory(reinforced) && !board.isReinforcedAlready(reinforced)
          && board.getTerritoryOccupantId(reinforced) == currentPlayerId && (
          reinforceOptionsLeft != 1 || riskAction.troops() == reinforcementsLeft);
    } else if (board.isAttackPhase()) {

      int attackingId = riskAction.attackingId();
      int defendingId = riskAction.defendingId();
      int troops = riskAction.troops();

      return riskAction.isEndPhase() || (
          board.getTerritoryOccupantId(attackingId) == this.currentPlayerId
              && 0 < troops
              && troops <= board.getMaxAttackingTroops(attackingId)
              && troops < board.getTerritoryTroops(attackingId)
              && board.areNeighbors(attackingId, defendingId));

    } else if (board.isOccupyPhase()) {
      return 1 <= riskAction.troops() && riskAction.troops() <= board.getMaxOccupy();
    } else if (board.isFortifyPhase()) {
      int fortifyingId = riskAction.fortifyingId();
      int fortifiedId = riskAction.fortifiedId();
      int troops = riskAction.troops();

      return riskAction.isEndPhase() || (board.isTerritory(fortifyingId) && board
          .isTerritory(fortifiedId)
          && board.getTerritoryOccupantId(fortifyingId) == currentPlayerId
          && board.getTerritoryOccupantId(fortifiedId) == currentPlayerId
          && 0 < troops
          && troops <= board.getFortifyableTroops(fortifyingId)
          && board.canFortify(fortifyingId, fortifiedId));

    }
    return false;
  }

  @Override
  public Game<RiskAction, RiskBoard> doAction(RiskAction riskAction) {
    if (riskAction == null) {
      throw new IllegalArgumentException("Found null");
    }
    if (isGameOver()) {
      throw new IllegalArgumentException("Game is over");
    }
    Risk next = null;
    if (currentPlayerId < 0) {
      if (board.isAttack()) {
        next = casualtiesDA(riskAction);
      } else if (currentPlayerId == BONUS_PLAYER) {
        next = bonusDA(riskAction);
      }
    } else if (isInitialSelect()) {
      next = initialSelectDA(riskAction);
    } else if (isInitialReinforce()) {
      next = initialReinforceDA(riskAction);
    } else if (board.allowedToTradeIn(currentPlayerId) && riskAction.isCardIds()) {
      next = tradeInDA(riskAction);
    } else if (board.isReinforcementPhase()) {
      next = reinforceDA(riskAction);
    } else if (board.isAttackPhase()) {
      next = attackDA(riskAction);
    } else if (board.isOccupyPhase()) {
      next = occupyDA(riskAction);
    } else if (board.isFortifyPhase()) {
      next = fortifyDA(riskAction);
    }

    if (next != null) {
      next.actionRecords.add(new ActionRecord<>(this.currentPlayerId, riskAction));
    }
    return next;
  }

  private Risk initialSelectDA(RiskAction riskAction) {
    int selected = riskAction.selected();

    if (!board.isTerritory(selected)) {
      throw new IllegalArgumentException(
          "Specified territoryId is not assigned a territory, could therefore not select");
    }

    if (0 <= board.getTerritoryOccupantId(selected)
        && board.getTerritoryOccupantId(selected) < getNumberOfPlayers()) {
      throw new IllegalArgumentException(
          "Specified territoryId has already an occupant, could therefore not select");
    }

    Risk next = new Risk(this);
    next.board.initialSelect(selected, next.currentPlayerId);
    next.currentPlayerId =
        (next.currentPlayerId + (getNumberOfPlayers() - 1)) % getNumberOfPlayers();

    if (!next.isInitialSelect()) {
      if (next.isInitialReinforce()) {
        next.currentPlayerId = 0;
      } else {
        next.currentPlayerId = 1;
        next.board.endMove(1);
      }
    }

    return next;
  }

  private Risk initialReinforceDA(RiskAction riskAction) {
    int reinforcedId = riskAction.reinforcedId();
    int troops = riskAction.troops();
    {
      String errorMsg = "";

      if (!board.isTerritory(reinforcedId)) {
        errorMsg = "Reinforced territory is not an assigned territoryId";
      } else if (board.getTerritoryOccupantId(reinforcedId) != currentPlayerId) {
        errorMsg = "Reinforced territory is not occupied by currentPlayer";
      }

      if (troops != 1) {
        if (!errorMsg.isEmpty()) {
          errorMsg = errorMsg.concat(", ");
        }
        errorMsg = errorMsg.concat(troops + " is an illegal number of troops");
      }

      if (!errorMsg.isEmpty()) {
        throw new IllegalArgumentException(errorMsg.concat(", could therefore not reinforce"));
      }
    }
    Risk next = new Risk(this);
    next.board.reinforce(currentPlayerId, reinforcedId, troops);

    if (next.isInitialReinforce()) {
      do {
        next.currentPlayerId =
            (next.currentPlayerId + (getNumberOfPlayers() - 1)) % getNumberOfPlayers();
      } while (next.board.reinforcementsLeft(next.currentPlayerId) <= 0);
    } else {
      next.currentPlayerId = 1;
      next.board.endMove(1);
    }

    return next;
  }

  private int nextPlayerId(int player) {
    player = (player + 1) % getNumberOfPlayers();
    for (int n = 1; n < getNumberOfPlayers(); n++, player = (player + 1) % getNumberOfPlayers()) {
      if (board.isPlayerStillAlive(player)) {
        return player;
      }
    }
    return player;
  }

  private int nextPlayerId() {
    return nextPlayerId(currentPlayerId);
  }

  private Risk tradeInDA(RiskAction riskAction) {

    Set<Integer> cardIds = riskAction.playedCards();
    if (!board.allowedToTradeIn(currentPlayerId)) {
      throw new IllegalArgumentException("Cards cannot be traded in at this time");
    }
    if (!board.canTradeInAsSet(cardIds, currentPlayerId)) {
      final List<RiskCard> playerCards = board.getPlayerCards(currentPlayerId);
      throw new IllegalArgumentException("The cards [" + cardIds.stream()
          .map(i -> "(" + i + "," + (i < playerCards.size() ? playerCards.get(i) : "oob") + ")")
          .collect(
              Collectors.joining(", ")) + "] cannot be traded in as a set");
    }

    Risk next = new Risk(this);

    next.board.tradeIn(cardIds, next.currentPlayerId);

    next.currentPlayerId = Risk.BONUS_PLAYER;

    return next;
  }

  private Risk bonusDA(RiskAction riskAction) {
    if (!riskAction.isBonus()) {
      throw new IllegalArgumentException("Action does not determine bonus.");
    }
    if (!(board.getMinMatchingTerritories() <= riskAction.getBonus()
        && riskAction.getBonus() <= board.getMaxMatchingTerritories())) {
      throw new IllegalArgumentException("Not correct amount of bonus for trade in.");
    }

    Risk next = new Risk(this);

    next.currentPlayerId = next.board.getTradedInId();
    next.board.awardBonus(riskAction.getBonus(), next.currentPlayerId);

    return next;
  }

  private Risk reinforceDA(RiskAction riskAction) {
    int reinforcedId = riskAction.reinforcedId();
    int troops = riskAction.troops();
    {
      String errorMsg = "";
      if (!board.isTerritory(reinforcedId)) {
        errorMsg = "Reinforced territory is not an assigned territoryId";
      } else if (board.getTerritoryOccupantId(reinforcedId) != currentPlayerId) {
        errorMsg = "Reinforced territory is not occupied by currentPlayer";
      } else if (board.isReinforcedAlready(reinforcedId)) {
        errorMsg = "Reinforced territory was already reinforced in an previous action";
      }
      if (!(1 <= troops && troops <= board.reinforcementsLeft(currentPlayerId))) {
        if (!errorMsg.isEmpty()) {
          errorMsg = errorMsg.concat(", ");
        }
        errorMsg = errorMsg.concat(troops + " is an illegal number of troops");
      } else if (board.inBonusTerritories(reinforcedId) && troops < board
          .getTradeInTerritoryBonus()) {
        if (!errorMsg.isEmpty()) {
          errorMsg = errorMsg.concat(", ");
        }
        errorMsg = errorMsg.concat(
            "Reinforced territory is required to be reinforced with at least " + board
                .getTradeInTerritoryBonus() + " troops");
      } else {
        int reinforceOptionsLeft = (int) board.getTerritoriesOccupiedByPlayer(currentPlayerId)
            .stream().filter(t -> !board.isReinforcedAlready(t)).count();
        if (reinforceOptionsLeft == 1 && troops != board.reinforcementsLeft(currentPlayerId)) {
          if (!errorMsg.isEmpty()) {
            errorMsg = errorMsg.concat(", ");
          }
          errorMsg = errorMsg.concat("Need to deploy all reinforcements");
        }
      }

      if (!errorMsg.isEmpty()) {
        throw new IllegalArgumentException(errorMsg.concat(", could therefore not reinforce"));
      }
    }

    Risk next = new Risk(this);

    next.board.reinforce(next.currentPlayerId, reinforcedId, troops);
    if (next.board.reinforcementsLeft(currentPlayerId) == 0) {
      next.board.endReinforcementPhase();
    }

    return next;

  }

  private Risk attackDA(RiskAction riskAction) {
    int attackingId = riskAction.attackingId();
    int defendingId = riskAction.defendingId();
    int troops = riskAction.troops();

    Risk next = new Risk(this);
    if (!riskAction.isEndPhase()) {
      String errorMsg = "";
      if (!(board.isTerritory(attackingId) && board.isTerritory(defendingId))) {
        if (!board.isTerritory(attackingId)) {
          errorMsg = "Attacking territoryId is not assigned any territory";
        }
        if (!board.isTerritory(defendingId)) {
          if (!errorMsg.isEmpty()) {
            errorMsg = errorMsg.concat(", ");
          }
          errorMsg = errorMsg.concat("Defending territoryId is not assigned any territory");
        }
      } else if (board.getTerritoryOccupantId(attackingId) != this.currentPlayerId) {
        errorMsg = "Attacking territory does not belong to currentPlayer";
      } else if (!(0 < troops
          && troops <= board.getMaxAttackingTroops(attackingId)
          && troops < board.getTerritoryTroops(attackingId))) {
        errorMsg = "Illegal number of troops";
      }
      if (!board.areNeighbors(attackingId, defendingId)) {
        if (!errorMsg.isEmpty()) {
          errorMsg = errorMsg.concat(", ");
        }
        errorMsg = errorMsg
            .concat("Attacking and defending territory are not neighboring territories");
      }

      if (!errorMsg.isEmpty()) {
        throw new IllegalArgumentException(errorMsg.concat(", could therefore not attack"));
      }

      next.currentPlayerId = CASUALTIES_PLAYER;

      next.board.startAttack(attackingId, defendingId, troops);
    } else {
      next.board.endAttackPhase();
    }

    return next;
  }

  private Risk casualtiesDA(RiskAction riskAction) {
    int attackerCasualties = riskAction.attackerCasualties();
    int defenderCasualties = riskAction.defenderCasualties();
    {
      int armiesFought = Math.min(board.getNrOfAttackerDice(), board.getNrOfDefenderDice());

      if (attackerCasualties + defenderCasualties != armiesFought) {
        throw new IllegalArgumentException(
            attackerCasualties + " attacking casualties and " + defenderCasualties
                + " do not match " + armiesFought + ", could therefore not subtract casualties");
      }

    }

    Risk next = new Risk(this);
    next.currentPlayerId = next.board.endAttack(attackerCasualties, defenderCasualties);
    return next;
  }

  private Risk occupyDA(RiskAction riskAction) {
    if (!(1 <= riskAction.troops() && riskAction.troops() <= board.getMaxOccupy())) {
      throw new IllegalArgumentException(
          riskAction.troops() + " is an illegal number of troops, could therefore not occupy");
    }

    Risk next = new Risk(this);
    next.board.occupy(riskAction.troops());

    return next;
  }

  private Risk fortifyDA(RiskAction riskAction) {
    int fortifyingId = riskAction.fortifyingId();
    int fortifiedId = riskAction.fortifiedId();
    int troops = riskAction.troops();

    Risk next = new Risk(this);
    if (riskAction.isEndPhase()) {
      next.endMove();
    } else {
      {
        StringBuilder errorMsg = new StringBuilder();

        if (!board.isTerritory(fortifyingId) || !board.isTerritory(fortifiedId)) {
          if (!board.isTerritory(fortifyingId)) {
            errorMsg.append("FortifyingId is not assigned any territory");
          }
          if (!board.isTerritory(fortifiedId)) {
            if (errorMsg.length() > 0) {
              errorMsg.append(", ");
            }
            errorMsg.append("FortifiedId is not assigned any territory");
          }
        } else {
          if (board.getTerritoryOccupantId(fortifyingId) != currentPlayerId) {
            errorMsg.append("FortifyingId is not occupied by current player");
          }

          if (board.getTerritoryOccupantId(fortifiedId) != currentPlayerId) {
            if (errorMsg.length() > 0) {
              errorMsg.append(", ");
            }
            errorMsg.append("FortifiedId is not occupied by current player");
          }

          if (!(0 < troops && troops <= board.getFortifyableTroops(fortifyingId))) {
            if (errorMsg.length() > 0) {
              errorMsg.append(", ");
            }
            errorMsg.append(troops).append(" is an illegal number of troops");
          }

          if (!board.canFortify(fortifyingId, fortifiedId)) {
            if (errorMsg.length() > 0) {
              errorMsg.append(", ");
            }
            errorMsg.append("FortifyingId cannot reach FortifiedId");
          }

        }

        if (errorMsg.length() > 0) {
          throw new IllegalArgumentException(
              errorMsg.append(", could therefore not fortify").toString());
        }
      }

      next.board.fortify(fortifyingId, fortifiedId, troops);

      if (next.board.isFortifyOnlyFromSingleTerritory()) {
        next.endMove();
      }
    }

    return next;
  }

  private void endMove() {
    this.board.drawCardIfPossible(this.currentPlayerId);
    this.currentPlayerId = nextPlayerId();
    this.board.endMove(this.currentPlayerId);
  }

  @Override
  public RiskAction determineNextAction() {
    if (currentPlayerId >= 0) {
      return null;
    }

    if (board.isAttack()) {
      return calculateCasualties();
    } else if (currentPlayerId == BONUS_PLAYER) {
      return calculateBonus();
    }

    return null;
  }

  private RiskAction calculateCasualties() {
    int attacker = board.getNrOfAttackerDice();
    int defender = board.getNrOfDefenderDice();

    int compareDice = Math.min(attacker, defender);
    attackerDice.rollN(attacker);
    defenderDice.rollN(defender);

    attackerDice.sortReverse();
    defenderDice.sortReverse();

    attacker = 0;
    defender = 0;

    for (int die = 0; die < compareDice; die++) {
      if (attackerDice.getFaceOf(die) > defenderDice.getFaceOf(die)) {
        defender++;
      } else {
        attacker++;
      }
    }

    return RiskAction.casualties(attacker, defender);
  }

  private RiskAction calculateBonus() {
    int min = board.getMinMatchingTerritories();
    int max = board.getMaxMatchingTerritories();

    if (min == max) {
      return RiskAction.bonusTroopsFromCards(max);
    }

    int drawn = max - min;
    int nrOfCardsInPool =
        (board.getNumberOfCards() + drawn) - (board.getDiscardedPile().size() + board
            .getPlayerCards(board.getTradedInId()).size());

    int bonus = min;

    List<Integer> pool = IntStream.range(0, nrOfCardsInPool).boxed()
        .collect(Collectors.toCollection(LinkedList::new));
    Collections.shuffle(pool);

    for (int i = 0; i < drawn; i++) {
      if (pool.get(i) < drawn) {
        bonus++;
      }
    }

    return RiskAction.bonusTroopsFromCards(bonus);
  }

  @Override
  public List<ActionRecord<RiskAction>> getActionRecords() {
    return this.actionRecords;
  }

  @Override
  public boolean isCanonical() {
    return this.canonical;
  }

  @Override
  public Game<RiskAction, RiskBoard> getGame(int p) {
    if (!canonical) {
      return new Risk(this);
    }
    return stripOutUnknownInformation(new Risk(this), p);
  }

  @Override
  public String toString() {
    return "Risk: " + currentPlayerId + ", " + Arrays.toString(getGameUtilityValue());
  }

  @Override
  public String toTextRepresentation() {
    //TODO: untangle this mess
    StringBuilder map = new StringBuilder(board.getMap());
    final Map<Integer, RiskTerritory> territories = board.getTerritories();

    for (Integer i : territories.keySet()) {
      String target = "[" + i + "]";
      int occupantPlayerId = territories.get(i).getOccupantPlayerId();
      int troops = territories.get(i).getTroops();
      String troopsString = String.valueOf(troops);
      if (troops >= 1000) {
        troops /= 1000;
        troopsString = troops + "k";
      }
      String replacement = String.format("%d:%s", occupantPlayerId, troopsString);
      int pre = map.indexOf(target);
      int post = pre + (target.length() - 1);

      int toCutWhiteSpace = replacement.length() - 1;

      int preStop = pre;
      int preWhiteSpace = 0;

      char c = '0';
      while (0 < preStop && (('0' <= c && c <= '9') || ('a' <= c && c <= 'z'))) {
        preWhiteSpace += Integer.parseInt(String.valueOf(c), Character.MAX_RADIX);
        preStop--;
        c = map.charAt(preStop);
      }

      if (('0' <= c && c <= '9') || ('a' <= c && c <= 'z')) {
        preWhiteSpace += Integer.parseInt(String.valueOf(c), Character.MAX_RADIX);
      }

      c = '0';

      int postStop = post;
      int postWhiteSpace = 0;

      while (postStop < (map.length() - 1) && (('0' <= c && c <= '9') || ('a' <= c && c <= 'z'))) {
        postWhiteSpace += Integer.parseInt(String.valueOf(c), Character.MAX_RADIX);
        postStop++;
        c = map.charAt(postStop);
      }

      if (('0' <= c && c <= '9') || ('a' <= c && c <= 'z')) {
        postWhiteSpace += Integer.parseInt(String.valueOf(c), Character.MAX_RADIX);
      }

      int leftOverWhiteSpace = Math.max(0, preWhiteSpace + postWhiteSpace - toCutWhiteSpace);

      preWhiteSpace = leftOverWhiteSpace % 2;
      leftOverWhiteSpace -= preWhiteSpace;

      preWhiteSpace += leftOverWhiteSpace / 2;
      postWhiteSpace = leftOverWhiteSpace / 2;

      int preStart = Math.max(0, preStop + 1);
      int preEnd = Math.max(0, pre);
      map.replace(preStart, preEnd,
          convertIntToAlphaNumericWhitespaceWithWidth(preWhiteSpace, preEnd - preStart));
      int postStart = Math.min(post + 1, map.length());
      int postEnd = Math.min(postStop, map.length());
      map.replace(postStart, postEnd,
          convertIntToAlphaNumericWhitespaceWithWidth(postWhiteSpace, postEnd - postStart));

      map.replace(pre, post + 1, "[" + replacement + "]");
    }

    {
      int i = 0;
      boolean consume = true;
      while (i < map.length()) {
        char c = map.charAt(i);
        if (consume) {
          if (c == '[') {
            consume = false;
            map.deleteCharAt(i);
          } else if (('0' <= c && c <= '9') || ('a' <= c && c <= 'z')) {
            String whitespace = whitespace(c);
            map.replace(i, i + 1, whitespace);
            i += whitespace.length() - 1;
          }
        } else if (c == ']') {
          consume = true;
          map.deleteCharAt(i);
          i--;
        }
        i++;
      }
    }
    map.append('\n');
    for (int p = 0; p < getNumberOfPlayers(); p++) {
      Set<Integer> playerTerritoryIds = board.getTerritoriesOccupiedByPlayer(p);
      int playerTroops = playerTerritoryIds.stream().mapToInt(board::getTerritoryTroops).sum();
      map.append("Player ").append(p).append(':').append('\n')
          .append('\t').append("Territories: ").append(playerTerritoryIds.size()).append('\n')
          .append('\t').append("Troops: ").append(playerTroops).append('\n');
    }
    map.deleteCharAt(map.length() - 1);
    return map.toString();
  }

  private static String convertIntToAlphaNumericWhitespaceWithWidth(int i, int width) {
    if (width <= 0) {
      return "";
    }
    int div = i / Character.MAX_RADIX;
    int rem = i % Character.MAX_RADIX;

    StringBuilder stringBuilder = Util.appendNTimes(new StringBuilder(), 'z', div)
        .append(Integer.toString(rem, Character.MAX_RADIX));

    Util.appendNTimes(stringBuilder, '0', width - Math.max(1, div + rem));

    return stringBuilder.toString();
  }

}
