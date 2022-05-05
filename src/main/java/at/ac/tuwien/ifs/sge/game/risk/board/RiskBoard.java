package at.ac.tuwien.ifs.sge.game.risk.board;

import at.ac.tuwien.ifs.sge.game.risk.configuration.RiskConfiguration;
import at.ac.tuwien.ifs.sge.game.risk.configuration.RiskContinentConfiguration;
import at.ac.tuwien.ifs.sge.game.risk.configuration.RiskMissionConfiguration;
import at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration;
import at.ac.tuwien.ifs.sge.game.risk.mission.RiskMission;
import at.ac.tuwien.ifs.sge.game.risk.mission.RiskMissionType;
import at.ac.tuwien.ifs.sge.game.risk.util.PriestLogic;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class RiskBoard {

  private static final Set<Set<Integer>> TRADE_IN_3_OUT_OF_5 = Set
      .of(Set.of(0, 1, 2), Set.of(0, 1, 3), Set.of(0, 2, 3), Set.of(1, 2, 3), Set.of(0, 1, 4),
          Set.of(0, 2, 4), Set.of(1, 2, 4), Set.of(0, 3, 4), Set.of(1, 3, 4), Set.of(2, 3, 4));

  private static final Set<Set<Integer>> TRADE_IN_3_OUT_OF_4 = Set
      .of(Set.of(0, 1, 2), Set.of(0, 1, 3), Set.of(0, 2, 3), Set.of(1, 2, 3));

  private static final Set<Set<Integer>> TRADE_IN_3_OUT_OF_3 = Set.of(Set.of(0, 1, 2));

  private static final Set<Multiset<Integer>> SETS_3_OUT_OF_5 = Set.of(
      ImmutableMultiset.of(-1, -1, -1),
      ImmutableMultiset.of(1, 1, 1), ImmutableMultiset.of(2, 2, 2), ImmutableMultiset.of(3, 3, 3),
      ImmutableMultiset.of(0, 1, 1), ImmutableMultiset.of(0, 2, 2), ImmutableMultiset.of(0, 3, 3),
      ImmutableMultiset.of(1, 2, 3),
      ImmutableMultiset.of(0, 2, 3), ImmutableMultiset.of(0, 1, 3), ImmutableMultiset.of(0, 1, 2),
      ImmutableMultiset.of(-1, 1, 1), ImmutableMultiset.of(-1, 2, 2),
      ImmutableMultiset.of(-1, 3, 3),
      ImmutableMultiset.of(-1, 0, 1), ImmutableMultiset.of(-1, 0, 2),
      ImmutableMultiset.of(-1, 0, 3),
      ImmutableMultiset.of(-1, 2, 3), ImmutableMultiset.of(-1, 1, 3),
      ImmutableMultiset.of(-1, 1, 2),
      ImmutableMultiset.of(-1, -1, 1), ImmutableMultiset.of(-1, -1, 2),
      ImmutableMultiset.of(-1, -1, 3),
      ImmutableMultiset.of(-1, -1, 0));

  //settings
  private final int numberOfPlayers;
  private final int maxAttackerDice;
  private final int maxDefenderDice;

  private final boolean withCards;
  private final int[] tradeInBonus;
  private final int tradeInTerritoryBonus = 2;
  private final int maxExtraBonus;
  private final int cardTypesWithoutJoker;
  private final int reinforcementAtLeast;
  private final int reinforcementThreshold;
  private final boolean occupyOnlyWithAttackingArmies;
  private final boolean fortifyOnlyFromSingleTerritory;
  private final boolean fortifyOnlyWithNonFightingArmies;
  private final boolean withMissions;
  //board
  private final Graph<Integer, DefaultEdge> gameBoard;
  private final Map<Integer, RiskTerritory> territories;
  private final Map<Integer, Graph<Integer, DefaultEdge>> fortifyConnectivityGraph;
  private final Map<Integer, ConnectivityInspector<Integer, DefaultEdge>> fortifyConnectivityInspector;
  private final Deque<RiskCard> deckOfCards;
  private final List<RiskCard> discardPile;
  private final Set<RiskMission> allMissions;
  private final RiskMission[] playerMissions;
  private final Map<Integer, List<RiskCard>> playerCards;
  private final Map<Integer, RiskContinent> continents;
  private final int[] nonDeployedReinforcements;
  private final Set<Integer> reinforcedTerritories;
  private final Map<Integer, Integer> involvedTroopsInAttacks;
  private final String map;
  private Set<Integer> tradeInTerritories;
  private int minMatchingTerritories;
  private int maxMatchingTerritories;
  private int tradeIns;
  private int attackingId;
  private int defendingId;
  private int troops;

  private int tradedInId;
  private boolean hasOccupiedCountry;
  private RiskPhase phase;
  private boolean initialSelectMaybe;
  private boolean initialReinforceMaybe;

  RiskBoard(RiskConfiguration configuration, int numberOfPlayers) {
    this.numberOfPlayers = numberOfPlayers;
    maxAttackerDice = configuration.getMaxAttackerDice();
    maxDefenderDice = configuration.getMaxDefenderDice();
    withCards = configuration.isWithCards();
    cardTypesWithoutJoker = configuration.getCardTypesWithoutJoker();
    if (!(0 <= configuration.getNumberOfJokers()
        && configuration.getNumberOfJokers() < cardTypesWithoutJoker)) {
      throw new IllegalArgumentException(
          configuration.getNumberOfJokers() + " is an illegal number of jokers");
    }
    reinforcementAtLeast = configuration.getReinforcementAtLeast();
    reinforcementThreshold = configuration.getReinforcementThreshold();
    occupyOnlyWithAttackingArmies = configuration.isOccupyOnlyWithAttackingArmies();
    fortifyOnlyFromSingleTerritory = configuration.isFortifyOnlyFromSingleTerritory();
    fortifyOnlyWithNonFightingArmies = configuration.isFortifyOnlyWithNonFightingArmies();
    withMissions = configuration.isWithMissions();
    if (withMissions && !configuration.getMissions().isEmpty()) {
      allMissions = configuration.getMissions().stream()
          .map(RiskMissionConfiguration::getMission)
          .filter(m -> m.getRiskMissionType() != RiskMissionType.LIBERATE_PLAYER || m.getTargetIds()
              .stream().allMatch(i -> i < numberOfPlayers)).collect(Collectors.toUnmodifiableSet());
      playerMissions = new RiskMission[numberOfPlayers];
      selectRandomMissions(new ArrayList<>(allMissions), playerMissions);
    } else {
      allMissions = null;
      playerMissions = null;
    }
    Set<RiskTerritoryConfiguration> territoriesConfiguration = new HashSet<>(
        configuration.getTerritories());

    Map<Integer, RiskTerritory> territoryMap = new HashMap<>();

    for (RiskTerritoryConfiguration riskTerritoryConfiguration : territoriesConfiguration) {
      territoryMap.put(riskTerritoryConfiguration.getTerritoryId(),
          riskTerritoryConfiguration.getTerritory());
    }

    territories = Map.copyOf(territoryMap);

    gameBoard = new SimpleGraph<>(DefaultEdge.class);
    if (fortifyOnlyFromSingleTerritory) {
      fortifyConnectivityGraph = null;
    } else {
      fortifyConnectivityGraph = new HashMap<>();
      for (int p = 0; p < numberOfPlayers; p++) {
        fortifyConnectivityGraph.put(p, new SimpleGraph<>(DefaultEdge.class));
      }
    }
    for (RiskTerritoryConfiguration territoryConfiguration : territoriesConfiguration) {
      gameBoard.addVertex(territoryConfiguration.getTerritoryId());
      if (!fortifyOnlyFromSingleTerritory) {
        for (int p = 0; p < numberOfPlayers; p++) {
          fortifyConnectivityGraph.get(p).addVertex(territoryConfiguration.getTerritoryId());
        }
      }
    }
    for (RiskTerritoryConfiguration territoryConfiguration : territoriesConfiguration) {
      for (Integer connect : territoryConfiguration.getConnects()) {
        gameBoard.addEdge(territoryConfiguration.getTerritoryId(), connect);
      }
    }

    tradeIns = 0;
    if (withCards) {
      tradeInBonus = configuration.getTradeInBonus();
      maxExtraBonus = configuration.getMaxExtraBonus();

      List<RiskCard> cardList = territoriesConfiguration.stream().map(
          territoryConfiguration -> new RiskCard(territoryConfiguration.getCardType(),
              territoryConfiguration.getTerritoryId()))
          .collect(Collectors.toCollection(() -> new ArrayList<>(
              territoriesConfiguration.size() + configuration.getNumberOfJokers())));

      for (RiskCard riskCard : cardList) {
        if (!(1 <= riskCard.getCardType()
            && riskCard.getCardType() <= configuration.getCardTypesWithoutJoker())) {
          throw new IllegalArgumentException("Illegal card type found: " + riskCard.toString());
        }
      }

      for (int i = 0; i < configuration.getNumberOfJokers(); i++) {
        cardList.add(new RiskCard(RiskCard.JOKER, -1));
      }
      Collections.shuffle(cardList);
      deckOfCards = new ArrayDeque<>(cardList);
      discardPile = Collections.emptyList();
      playerCards = IntStream.range(0, numberOfPlayers).boxed().collect(Collectors
          .toUnmodifiableMap(p -> p, p -> new ArrayList<>(cardSlots())));

      tradeInTerritories = Collections.emptySet();
    } else {
      tradeInBonus = null;
      maxExtraBonus = 0;
      deckOfCards = null;
      playerCards = null;
      discardPile = null;
      tradeInTerritories = null;
    }

    Set<RiskContinentConfiguration> continentsConfiguration = new HashSet<>(
        configuration.getContinents());

    this.continents = continentsConfiguration.stream().collect(Collectors
        .toUnmodifiableMap(RiskContinentConfiguration::getContinentId,
            RiskContinentConfiguration::getContinent, (a, b) -> b));

    nonDeployedReinforcements = new int[numberOfPlayers];
    int[] initialTroops = configuration.getInitialTroops();
    Arrays.fill(nonDeployedReinforcements,
        initialTroops[Math.max(0, Math.min(numberOfPlayers - 2, initialTroops.length - 1))]);

    reinforcedTerritories = Collections.emptySet();

    if (!configuration.isChooseInitialTerritories()) {
      List<Integer> ids = new ArrayList<>(territories.keySet());
      Collections.shuffle(ids);
      int p;
      {
        int i;
        for (p = numberOfPlayers - 1, i = 0;
            i < ids.size();
            i++, p = (p + (numberOfPlayers - 1)) % numberOfPlayers) {
          RiskTerritory territory = territories.get(ids.get(i));
          territory.setOccupantPlayerId(p);
          territory.setTroops(1);
          nonDeployedReinforcements[p]--;
        }
      }

      if (!fortifyOnlyFromSingleTerritory) {
        for (DefaultEdge edge : gameBoard.edgeSet()) {
          int src = gameBoard.getEdgeSource(edge);
          int dst = gameBoard.getEdgeTarget(edge);
          int occupant;
          if ((occupant = getTerritoryOccupantId(src)) == getTerritoryOccupantId(dst)) {
            fortifyConnectivityGraph.get(occupant).addEdge(src, dst);
          }
        }
      }
    }

    if (fortifyOnlyFromSingleTerritory) {
      fortifyConnectivityInspector = null;
    } else {
      fortifyConnectivityInspector = new HashMap<>();

      for (int p = 0; p < numberOfPlayers; p++) {
        fortifyConnectivityInspector
            .put(p, new ConnectivityInspector<>(fortifyConnectivityGraph.get(p)));
      }
    }

    involvedTroopsInAttacks = new HashMap<>();

    attackingId = -1;
    defendingId = -1;
    troops = 0;
    hasOccupiedCountry = false;

    phase = RiskPhase.REINFORCEMENT;

    initialSelectMaybe = true;
    initialReinforceMaybe = true;

    tradedInId = -5;
    minMatchingTerritories = 0;
    maxMatchingTerritories = 0;

    map = configuration.getMap();
  }

  RiskBoard(RiskBoard riskBoard) {
    this(riskBoard.numberOfPlayers, riskBoard.maxAttackerDice, riskBoard.maxDefenderDice,
        riskBoard.withCards, riskBoard.tradeInBonus, riskBoard.maxExtraBonus, riskBoard.tradeIns,
        riskBoard.cardTypesWithoutJoker, riskBoard.reinforcementAtLeast,
        riskBoard.reinforcementThreshold, riskBoard.occupyOnlyWithAttackingArmies,
        riskBoard.fortifyOnlyFromSingleTerritory, riskBoard.fortifyOnlyWithNonFightingArmies,
        riskBoard.withMissions, riskBoard.gameBoard, riskBoard.territories,
        riskBoard.fortifyConnectivityGraph, riskBoard.fortifyConnectivityInspector,
        riskBoard.deckOfCards, riskBoard.discardPile, riskBoard.allMissions,
        riskBoard.playerMissions, riskBoard.playerCards, riskBoard.continents,
        riskBoard.nonDeployedReinforcements, riskBoard.reinforcedTerritories,
        riskBoard.involvedTroopsInAttacks, riskBoard.attackingId, riskBoard.defendingId,
        riskBoard.troops, riskBoard.hasOccupiedCountry, riskBoard.phase,
        riskBoard.initialSelectMaybe, riskBoard.initialReinforceMaybe, riskBoard.tradedInId,
        riskBoard.tradeInTerritories, riskBoard.minMatchingTerritories,
        riskBoard.maxMatchingTerritories, riskBoard.map);
  }

  private RiskBoard(int numberOfPlayers, int maxAttackerDice, int maxDefenderDice,
      boolean withCards, int[] tradeInBonus, int maxExtraBonus, int tradeIns,
      int cardTypesWithoutJoker, int reinforcementAtLeast,
      int reinforcementThreshold, boolean occupyOnlyWithAttackingArmies,
      boolean fortifyOnlyFromSingleTerritory, boolean fortifyOnlyWithNonFightingArmies,
      boolean withMissions,
      Graph<Integer, DefaultEdge> gameBoard, Map<Integer, RiskTerritory> territories,
      Map<Integer, Graph<Integer, DefaultEdge>> fortifyConnectivityGraph,
      Map<Integer, ConnectivityInspector<Integer, DefaultEdge>> fortifyConnectivityInspector,
      Collection<RiskCard> deckOfCards, Collection<RiskCard> discardPile,
      Set<RiskMission> allMissions,
      RiskMission[] playerMissions,
      Map<Integer, List<RiskCard>> playerCards,
      Map<Integer, RiskContinent> continents, int[] nonDeployedReinforcements,
      Collection<Integer> reinforcedTerritories,
      Map<Integer, Integer> involvedTroopsInAttacks, int attackingId,
      int defendingId, int troops, boolean hasOccupiedCountry, RiskPhase phase,
      boolean initialSelectMaybe, boolean initialReinforceMaybe, int tradedInId,
      Set<Integer> tradeInTerritories, int minMatchingTerritories, int maxMatchingTerritories,
      String map) {
    this.numberOfPlayers = numberOfPlayers;
    this.maxAttackerDice = maxAttackerDice;
    this.maxDefenderDice = maxDefenderDice;
    this.withCards = withCards;
    this.tradeInBonus = tradeInBonus;
    this.maxExtraBonus = maxExtraBonus;
    this.tradeIns = tradeIns;
    this.cardTypesWithoutJoker = cardTypesWithoutJoker;
    this.reinforcementAtLeast = reinforcementAtLeast;
    this.reinforcementThreshold = reinforcementThreshold;
    this.occupyOnlyWithAttackingArmies = occupyOnlyWithAttackingArmies;
    this.fortifyOnlyFromSingleTerritory = fortifyOnlyFromSingleTerritory;
    this.fortifyOnlyWithNonFightingArmies = fortifyOnlyWithNonFightingArmies;
    this.withMissions = withMissions;
    this.gameBoard = gameBoard;
    this.territories = Collections.unmodifiableMap(copyTerritories(territories));
    if (fortifyOnlyFromSingleTerritory) {
      this.fortifyConnectivityGraph = fortifyConnectivityGraph;
      this.fortifyConnectivityInspector = fortifyConnectivityInspector;
    } else {
      this.fortifyConnectivityGraph = new HashMap<>(fortifyConnectivityGraph.size() + 1, 1.00f);
      for (int p = 0; p < numberOfPlayers; p++) {
        this.fortifyConnectivityGraph.put(p, new SimpleGraph<>(DefaultEdge.class));
      }
      for (Integer vertex : gameBoard.vertexSet()) {
        for (int p = 0; p < numberOfPlayers; p++) {
          this.fortifyConnectivityGraph.get(p).addVertex(vertex);
        }
      }
      for (DefaultEdge edge : this.gameBoard.edgeSet()) {
        int src = this.gameBoard.getEdgeSource(edge);
        int dst = this.gameBoard.getEdgeTarget(edge);
        int occupant;
        if ((occupant = getTerritoryOccupantId(src)) == getTerritoryOccupantId(dst)) {
          if (occupant < 0) {
            break;
          }
          this.fortifyConnectivityGraph.get(occupant).addEdge(src, dst);
        }
      }

      this.fortifyConnectivityInspector = new HashMap<>(this.fortifyConnectivityGraph.size() + 1,
          1.00f);
      for (Entry<Integer, Graph<Integer, DefaultEdge>> entry : fortifyConnectivityGraph
          .entrySet()) {
        this.fortifyConnectivityInspector
            .put(entry.getKey(), new ConnectivityInspector<>(entry.getValue()));
      }

    }

    this.deckOfCards = deckOfCards != null ? new ArrayDeque<>(deckOfCards) : null;
    this.discardPile = discardPile != null ? new ArrayList<>(discardPile) : null;
    this.allMissions = allMissions;
    this.playerMissions = playerMissions != null ? playerMissions.clone() : null;
    if (playerCards == null) {
      this.playerCards = null;
    } else {
      this.playerCards = new HashMap<>(1 + (int) (0.75f * playerCards.size()), 0.75f);
      for (Entry<Integer, List<RiskCard>> entry : playerCards.entrySet()) {
        this.playerCards.put(entry.getKey(), new ArrayList<>(entry.getValue()));
      }
    }
    this.continents = continents;
    this.nonDeployedReinforcements = nonDeployedReinforcements.clone();
    this.reinforcedTerritories = new HashSet<>(reinforcedTerritories);
    this.involvedTroopsInAttacks = new HashMap<>(involvedTroopsInAttacks);
    this.attackingId = attackingId;
    this.defendingId = defendingId;
    this.troops = troops;
    this.hasOccupiedCountry = hasOccupiedCountry;
    this.phase = phase;
    this.initialSelectMaybe = initialSelectMaybe;
    this.initialReinforceMaybe = initialReinforceMaybe;
    this.tradedInId = tradedInId;
    this.tradeInTerritories = tradeInTerritories != null ? new HashSet<>(tradeInTerritories) : null;
    this.minMatchingTerritories = Math
        .max(0, Math.min(minMatchingTerritories, cardTypesWithoutJoker));
    this.maxMatchingTerritories = Math
        .max(this.minMatchingTerritories, Math.min(maxMatchingTerritories, cardTypesWithoutJoker));
    this.map = map;
  }

  private static Map<Integer, RiskTerritory> copyTerritories(
      Map<Integer, RiskTerritory> territories) {
    return territories.keySet().stream().collect(Collectors
        .toMap(i -> i, i -> new RiskTerritory(territories.get(i)), (a, b) -> b));
  }

  private static void selectRandomMissions(List<RiskMission> missionList,
      RiskMission[] playerMissions) {
    Optional<RiskMission> fallbackOptional = missionList.stream()
        .filter(m -> m.getRiskMissionType() == RiskMissionType.OCCUPY_TERRITORY).findFirst();

    if (fallbackOptional.isEmpty()) {
      System.err
          .println("Warning: No fallback (any OCCUPY_TERRITORY) mission could be determined."
              + " Mission-dealing could take a while");

      if (missionList.size() < playerMissions.length) {
        throw new IllegalArgumentException("More players then missions");
      }
    } else {
      for (int i = missionList.size() - 1; i < playerMissions.length; i++) {
        missionList.add(fallbackOptional.get());
      }
    }

    boolean playerLiberateThemselves;
    do {
      playerLiberateThemselves = false;
      Collections.shuffle(missionList);
      for (int i = 0; i < playerMissions.length; i++) {
        RiskMission riskMission = missionList.get(i);
        playerMissions[i] = riskMission;
        playerLiberateThemselves = playerLiberateThemselves
            || (riskMission.getRiskMissionType() == RiskMissionType.LIBERATE_PLAYER
            && riskMission.getTargetIds().contains(i));
      }
    } while (fallbackOptional.isEmpty() && playerLiberateThemselves);

    if (fallbackOptional.isPresent() && playerLiberateThemselves) {
      for (int i = 0; i < playerMissions.length; i++) {
        if (playerMissions[i].getRiskMissionType() == RiskMissionType.LIBERATE_PLAYER
            && playerMissions[i].getTargetIds().contains(i)) {
          playerMissions[i] = fallbackOptional.get();
        }
      }
    }

  }

  /**
   * Return the number of players participating in this game.
   *
   * @return the number of players participating in this game.
   */
  public int getNumberOfPlayers() {
    return numberOfPlayers;
  }

  /**
   * Returns all territories mapped from their territoryId.
   *
   * @return a map of territories mapped by their territoryId.
   */
  public Map<Integer, RiskTerritory> getTerritories() {
    return territories;
  }

  /**
   * Returns a set of all territoryIds present on this board. This is equivalent to
   * getTerritories().keySet().
   *
   * @return a set of all territoryIds.
   */
  public Set<Integer> getTerritoryIds() {
    return territories.keySet();
  }

  /**
   * Checks if a given territoryId is present on the board. This is equivalent to
   * getTerritories().containsKey(territoryId).
   *
   * @param territoryId the id of the territory
   * @return true iff present on the board, otherwise false.
   */
  public boolean isTerritory(int territoryId) {
    return territories.containsKey(territoryId);
  }

  /**
   * Returns all continents mapped from their continentId.
   *
   * @return a map of territories mapped by their territoryId.
   */
  public Map<Integer, RiskContinent> getContinents() {
    return continents;
  }

  /**
   * Returns a set of all continentIds present on this board. This is equivalent to
   * getContinents().keySet().
   *
   * @return a set of all territoryIds.
   */
  public Set<Integer> getContinentIds() {
    return continents.keySet();
  }

  /**
   * Checks if a given continentId is present on the board. This is equivalent to
   * getContinents().containsKey(continentId).
   *
   * @param continentId the id of the continent
   * @return true iff present on the board, otherwise false.
   */
  public boolean isContinent(int continentId) {
    return continents.containsKey(continentId);
  }


  /**
   * Returns the number of bonus troops if the continent is taken. If continentId is not present on
   * the board 0 is returned instead.
   *
   * @param continentId the id of the continent
   * @return the number of bonus troops if the continent is taken
   */
  public int getContinentBonus(int continentId) {
    if (isContinent(continentId)) {
      return continents.get(continentId).getTroopBonus();
    }
    return 0;
  }

  /**
   * Returns which player currently occupies the given territory. A negative number indicates either
   * that noone has yet occupied this territory or that the territoryId does not exist on the board.
   * Check with isTerritory(territoryId) to determine the difference.
   *
   * @param territoryId the id of the territory
   * @return the id of the occupying player.
   */
  public int getTerritoryOccupantId(int territoryId) {
    return territories.containsKey(territoryId) ? territories.get(territoryId)
        .getOccupantPlayerId()
        : -1;
  }

  private void setTerritoryOccupantId(int territoryOccupantId, int playerId) {
    if (isTerritory(territoryOccupantId)) {
      territories.get(territoryOccupantId).setOccupantPlayerId(playerId);
    }
  }

  /**
   * Returns how many troops currently are stationed in the given territory. Zero indicates either
   * that there are no troops stationed or that the territoryId does not exist on the board. Check
   * with isTerritory(territoryId) to determine the difference.
   *
   * @param territoryId the id of the territory
   * @return the id of the occupying player.
   */
  public int getTerritoryTroops(int territoryId) {
    return territories.containsKey(territoryId) ? territories.get(territoryId).getTroops() : 0;
  }

  String getMap() {
    return map;
  }

  boolean isInitialSelectMaybe() {
    return initialSelectMaybe;
  }

  boolean isInitialReinforceMaybe() {
    return initialReinforceMaybe;
  }

  void disableInitialSelectMaybe() {
    initialSelectMaybe = false;
  }

  void disableInitialReinforceMaybe() {
    initialReinforceMaybe = false;
  }

  void initialSelect(int selected, int playerId) {
    RiskTerritory territory = territories.get(selected);
    territory.setOccupantPlayerId(playerId);
    territory.setTroops(1);
    nonDeployedReinforcements[playerId]--;
  }

  void endMove(int nextPlayer) {
    phase = RiskPhase.REINFORCEMENT;
    involvedTroopsInAttacks.clear();
    hasOccupiedCountry = false;
    awardReinforcements(nextPlayer);
    reinforcedTerritories.clear();
  }

  private void awardReinforcements(int player) {
    int occupiedTerritories = Math.toIntExact(territories.values().stream()
        .filter(t -> t.getOccupantPlayerId() == player).count());
    int reinforcements = Math
        .max(reinforcementAtLeast, occupiedTerritories / reinforcementThreshold);

    for (Entry<Integer, RiskContinent> continent : continents.entrySet()) {
      if (continentConquered(player, continent.getKey())) {
        reinforcements += continent.getValue().getTroopBonus();
      }
    }

    nonDeployedReinforcements[player] += reinforcements;
  }

  boolean areReinforcementsLeft() {
    for (int nonDeployedReinforcement : nonDeployedReinforcements) {
      if (nonDeployedReinforcement > 0) {
        return true;
      }
    }
    return false;
  }

  int reinforcementsLeft(int player) {
    return nonDeployedReinforcements[player];
  }

  /**
   * Returns true iff this board is in the Reinforcement Phase.
   *
   * @return true iff this board is in the Reinforcement Phase.
   */
  public boolean isReinforcementPhase() {
    return phase == RiskPhase.REINFORCEMENT;
  }

  /**
   * Returns true iff this board is in the Attack Phase.
   *
   * @return true iff this board is in the Attack Phase.
   */
  public boolean isAttackPhase() {
    return phase == RiskPhase.ATTACK;
  }

  /**
   * Returns true iff this board is in the Occupy Phase.
   *
   * @return true iff this board is in the Occupy Phase.
   */
  public boolean isOccupyPhase() {
    return phase == RiskPhase.OCCUPY;
  }

  /**
   * Returns true iff this board is in the Fortify Phase.
   *
   * @return true iff this board is in the Fortify Phase.
   */
  public boolean isFortifyPhase() {
    return phase == RiskPhase.FORTIFY;
  }

  int getMaxAttackerDice() {
    return maxAttackerDice;
  }

  int getMaxDefenderDice() {
    return maxDefenderDice;
  }

  int getNrOfDefenderDice() {

    if (!territories.containsKey(defendingId)) {
      return 0;
    }

    return Math.min(maxDefenderDice, territories.get(defendingId).getTroops());
  }

  int getNrOfAttackerDice() {

    if (!territories.containsKey(attackingId)) {
      return 0;
    }

    return Math.min(maxAttackerDice, troops);
  }

  void reinforce(int player, int reinforcedId, int troops) {
    if (territories.containsKey(reinforcedId)) {
      RiskTerritory territory = territories.get(reinforcedId);
      territory.setTroops(territory.getTroops() + troops);
      nonDeployedReinforcements[player] -= troops;
      reinforcedTerritories.add(reinforcedId);
    }
  }

  boolean isReinforcedAlready(int reinforcedId) {
    return reinforcedTerritories.contains(reinforcedId);
  }

  void endReinforcementPhase() {
    phase = RiskPhase.ATTACK;
    reinforcedTerritories.clear();
    tradeInTerritories.clear();
    tradedInId = -5;
  }

  /**
   * Return a set of ids of the neighboring territories. An empty set indicates that the territoryId
   * is not present on the board.
   *
   * @param territoryId the id of the territory
   * @return a set of ids of the neighboring territories.
   */
  public Set<Integer> neighboringTerritories(int territoryId) {
    return Graphs.neighborSetOf(gameBoard, territoryId);
  }

  /**
   * Return a set of ids of the neighboring territories which are not occupied with the same
   * occupantId. An empty set indicates that the territoryId is not present on the board or that
   * this territory is surrounded by friendly neighbors. Check with isTerritory(territoryId) to
   * determine the difference.
   *
   * @param territoryId the id of the territory
   * @return a set of ids of the neighboring enemy territories.
   */
  public Set<Integer> neighboringEnemyTerritories(int territoryId) {
    final int self = getTerritoryOccupantId(territoryId);
    return Graphs.neighborSetOf(gameBoard, territoryId).stream()
        .filter(id -> getTerritoryOccupantId(id) != self).collect(Collectors.toSet());
  }

  /**
   * Return a set of ids of the neighboring territories which are not occupied with the same
   * occupantId. An empty set indicates that the territoryId is not present on the board or that the
   * territory is surrounded by enemy neighbors. Check with isTerritory(territoryId) to determine
   * the difference.
   *
   * @param territoryId the id of the territory
   * @return a set of ids of the neighboring friendly territories.
   */
  public Set<Integer> neighboringFriendlyTerritories(int territoryId) {
    final int self = getTerritoryOccupantId(territoryId);
    return Graphs.neighborSetOf(gameBoard, territoryId).stream()
        .filter(id -> getTerritoryOccupantId(id) == self).collect(Collectors.toSet());
  }

  /**
   * Return the number of troops that can be moved from this territory. This is equal to
   * getTerritoryTroops(territoryId) - 1. Note that if the territoryId does not exist this method
   * will return -1.
   *
   * @param territoryId the id of the territory
   * @return the number of mobile troops in a given territory.
   */
  public int getMobileTroops(int territoryId) {
    return getTerritoryTroops(territoryId) - 1;
  }

  /**
   * Return the maximum number of troops that a territory can attack with. In games where occupying
   * only with attacking troops is allowed this is equal to the number of mobile troops. In other
   * games this is capped at the number of attacker dice.
   *
   * @param attackingId the id of the attacking territory
   * @return the number of troops allowed to attack at once in a given territory.
   */
  public int getMaxAttackingTroops(int attackingId) {
    int troops = getMobileTroops(attackingId);
    if (occupyOnlyWithAttackingArmies) {
      return troops;
    }

    return Math.min(troops, getMaxAttackerDice());
  }

  /**
   * Check if two territories are neighbors. Also returns false if either id does not exist. This
   * method is symmetrical however not reflexive or transitive.
   *
   * @param territoryId1 the id of the first territory
   * @param territoryId2 the id of the second territory
   * @return true iff the two territories differ and are neighbors.
   */
  public boolean areNeighbors(int territoryId1, int territoryId2) {
    return gameBoard.containsEdge(territoryId1, territoryId2);
  }

  /**
   * Return a set of ids of territories which are occupied by the given player. An empty set
   * indicates that the player has no longer occupied any territories or that the player does not
   * exist. Check if playerId is less than getNumberOfPlayers() to determine the difference.
   *
   * @param playerId the id of the player
   * @return a set of territories occupied by a given player.
   */
  public Set<Integer> getTerritoriesOccupiedByPlayer(final int playerId) {
    return territories.entrySet().stream()
        .filter(entry -> entry.getValue().getOccupantPlayerId() == playerId).map(Entry::getKey)
        .collect(Collectors.toSet());
  }

  /**
   * Return the number of territories currently occupied by the given player. Zero indicates that
   * the player has no longer occupied any territories or that the player does not exist. Check if
   * playerId is less than getNumberOfPlayers() to determine the difference.
   *
   * @param playerId the id of the player
   * @return the number of territories occupied by a given player.
   */
  public int getNrOfTerritoriesOccupiedByPlayer(final int playerId) {
    return Math.toIntExact(territories.entrySet().stream()
        .filter(entry -> entry.getValue().getOccupantPlayerId() == playerId).count());
  }

  /**
   * Check if there is any territory occupied by the given player. False indicates that the player
   * has no longer occupied any territories or that the player does not exits. Check if playerId is
   * less getNumberOfPlayers() to determine the difference.
   *
   * @param playerId the id of the player
   * @return true iff there is any territory with the playerId as occupantId
   */
  public boolean isPlayerStillAlive(final int playerId) {
    return territories.values().stream().anyMatch(t -> t.getOccupantPlayerId() == playerId);
  }

  /**
   * Return a set of ids of territories occupied by the given player with more than one troop
   * stationed in it. An empty set indicates that there are no territories with more than one troop
   * stationed left or that the player does not exist. Check if playerId is less than
   * getNumberOfPlayers() to determine the difference.
   *
   * @param playerId the id of the player
   * @return a set of ids of all the territories occupied by the given player with more than one
   * troop stationed.
   */
  public Set<Integer> getTerritoriesOccupiedByPlayerWithMoreThanOneTroops(final int playerId) {
    return territories.entrySet().stream()
        .filter(entry -> entry.getValue().getOccupantPlayerId() == playerId
            && entry.getValue().getTroops() > 1).map(Entry::getKey)
        .collect(Collectors.toSet());
  }

  void startAttack(int attackingId, int defendingId, int troops) {
    this.attackingId = attackingId;
    this.defendingId = defendingId;
    this.troops = troops;
  }

  boolean isAttack() {
    return phase == RiskPhase.ATTACK && attackingId >= 0 && defendingId >= 0 && troops > 0;
  }

  int endAttack(int attackerCasualties, int defendingCasualties) {
    int attackerId = getTerritoryOccupantId(attackingId);
    if (isAttack()) {
      territories.get(attackingId).removeTroops(attackerCasualties);
      territories.get(defendingId).removeTroops(defendingCasualties);
      troops -= attackerCasualties;
      involvedTroopsInAttacks.compute(attackingId,
          (k, v) -> (v == null) ? (troops)
              : Math.max(v, troops));

      if (getTerritoryTroops(defendingId) == 0) {
        setTerritoryOccupantId(defendingId, getTerritoryOccupantId(attackingId));
        phase = RiskPhase.OCCUPY;
        hasOccupiedCountry = true;
        if (occupyOnlyWithAttackingArmies) {
          occupy(troops);
        }
      } else {
        attackingId = -1;
        defendingId = -1;
        troops = 0;
      }
    }
    return attackerId;
  }

  void endAttackPhase() {
    phase = RiskPhase.FORTIFY;
    attackingId = -1;
    defendingId = -1;
    troops = 0;
  }

  int getMaxOccupy() {
    if (occupyOnlyWithAttackingArmies) {
      return troops;
    }

    return getMobileTroops(attackingId);
  }

  void occupy(int troops) {
    territories.get(attackingId).removeTroops(troops);
    territories.get(defendingId).addTroops(troops);
    involvedTroopsInAttacks
        .compute(attackingId, (k, v) -> v == null ? 0 : Math.max(0, v - troops));
    involvedTroopsInAttacks.compute(defendingId, (k, v) -> v == null ? troops : v + troops);
    if (!fortifyOnlyFromSingleTerritory) {
      int attackerId = getTerritoryOccupantId(attackingId);
      for (Integer neighbor : neighboringFriendlyTerritories(defendingId)) {
        DefaultEdge edge = fortifyConnectivityGraph.get(attackerId).addEdge(neighbor, defendingId);
        fortifyConnectivityInspector.get(attackerId).edgeAdded(
            new GraphEdgeChangeEvent<>(this, GraphEdgeChangeEvent.EDGE_ADDED, edge, neighbor,
                defendingId));
      }
    }
    attackingId = -1;
    defendingId = -1;
    this.troops = 0;
    phase = RiskPhase.ATTACK;
  }

  PriestLogic missionFulfilled(int player) {
    if (playerMissions == null) {
      return PriestLogic.FALSE;
    }
    RiskMission mission = playerMissions[player];

    if (mission.getRiskMissionType() == RiskMissionType.WILDCARD ||
        mission.getRiskMissionType() == RiskMissionType.LIBERATE_PLAYER) {
      return missionFulfilled(mission);
    } else if (mission.getRiskMissionType() == RiskMissionType.OCCUPY_TERRITORY) {
      return PriestLogic.fromBoolean(
          territoriesOccupied(player, mission.getTargetIds(), mission.getOccupyingWith()));
    } else if (mission.getRiskMissionType() == RiskMissionType.CONQUER_CONTINENT) {
      Set<Integer> conqueredContinents = playerConqueredContinents()
          .getOrDefault(player, Collections.emptySet());
      return PriestLogic.fromBoolean(conqueredContinents.size() >= mission.getTargetIds().size()
          && mission.getTargetIds().stream().filter(i -> i >= 0)
          .allMatch(conqueredContinents::contains));
    }

    return PriestLogic.FALSE;
  }

  PriestLogic missionFulfilled(RiskMission mission) {
    if (mission.getRiskMissionType() == RiskMissionType.LIBERATE_PLAYER) {
      return PriestLogic
          .fromBoolean(mission.getTargetIds().stream().noneMatch(this::isPlayerStillAlive));
    } else if (mission.getRiskMissionType() == RiskMissionType.CONQUER_CONTINENT) {
      return PriestLogic.fromBoolean(playerConqueredContinents().entrySet().stream()
          .anyMatch(
              e -> e.getValue().size() >= mission.getTargetIds().size()
                  //at least the required amount of continents are conquered
                  && mission.getTargetIds().stream().filter(id -> id >= 0)
                  .allMatch(
                      id -> e.getValue()
                          .contains(id)))); //all the required continents are conquered
    } else if (mission.getRiskMissionType() == RiskMissionType.OCCUPY_TERRITORY) {
      return PriestLogic.fromBoolean(IntStream.range(0, numberOfPlayers).anyMatch(
          p -> territoriesOccupied(p, mission.getTargetIds(), mission.getOccupyingWith())));
    } else if (mission.getRiskMissionType() == RiskMissionType.WILDCARD) {
      PriestLogic v = PriestLogic.FALSE;
      Iterator<RiskMission> iterator = allMissions.stream()
          .filter(m -> m.getRiskMissionType() != RiskMissionType.WILDCARD)
          .iterator(); //TODO: TEST, potential source of bugs
      if (iterator.hasNext()) {
        RiskMission aMission = iterator.next();
        v = missionFulfilled(aMission);
        while (iterator.hasNext() && PriestLogic.certain(v)) {
          aMission = iterator.next();
          v = PriestLogic.maybe(v, missionFulfilled(aMission));
        }
      }
      return v;
    }

    return PriestLogic.FALSE;
  }

  private boolean continentsConquered(int player, Collection<Integer> targetIds) {
    return targetIds.stream().allMatch(c -> continentConquered(player, c));
  }

  private boolean continentConquered(int player, int continent) {
    return continents.containsKey(continent) && territories.values().stream()
        .filter(t -> t.getContinentId() == continent)
        .allMatch(t -> t.getOccupantPlayerId() == player);
  }

  private boolean territoriesOccupied(int player, Collection<Integer> targetIds, int atLeast) {
    Set<Integer> occupiedTerritories = getTerritoriesOccupiedByPlayer(player);

    return occupiedTerritories.size() <= targetIds.size() // enough territories occupied
        && occupiedTerritories.stream().allMatch(
        t -> getTerritoryTroops(t) >= atLeast)
        // all territories occupied with at least required amount
        && targetIds.stream().filter(i -> i >= 0)
        .allMatch(occupiedTerritories::contains); // all required territories occupied
  }

  private Map<Integer, Set<Integer>> playerConqueredContinents() {
    Map<Integer, Map<Integer, RiskTerritory>> continents = new HashMap<>();
    for (Entry<Integer, RiskTerritory> territory : territories.entrySet()) {
      int continent = territory.getValue().getContinentId();
      continents.putIfAbsent(continent, new HashMap<>());
      continents.get(continent).put(territory.getKey(), territory.getValue());
    }

    Set<Integer> toRemove = new TreeSet<>();

    for (Entry<Integer, Map<Integer, RiskTerritory>> continent : continents.entrySet()) {
      if (continent.getValue().values().stream().mapToInt(RiskTerritory::getOccupantPlayerId)
          .distinct().count() != 1) {
        toRemove.add(continent.getKey());
      }
    }

    for (Integer remove : toRemove) {
      continents.remove(remove);
    }

    Map<Integer, Set<Integer>> playerConqueredContinents = new HashMap<>();

    for (Entry<Integer, Map<Integer, RiskTerritory>> continent : continents.entrySet()) {
      int player = continent.getValue().values().stream().findFirst()
          .map(RiskTerritory::getOccupantPlayerId).orElse(-1);

      if (player >= 0) {
        playerConqueredContinents.putIfAbsent(player, new TreeSet<>());
        playerConqueredContinents.get(player).add(continent.getKey());
      }

    }

    return playerConqueredContinents;
  }

  /**
   * Return a set of ids which are fortifyable given that there are enough troops in the specified
   * territory. An empty set either indicates that the territory does not exist or that it is
   * surrounded by enemy neighbors.
   *
   * @param territoryId the id of the territory
   * @return a set of ids which are fortifyable from the given territoryId.
   */
  public Set<Integer> getFortifyableTerritories(int territoryId) {
    if (fortifyOnlyFromSingleTerritory) {
      return neighboringFriendlyTerritories(territoryId);
    }

    int player = getTerritoryOccupantId(territoryId);
    Set<Integer> fortifyableTerritories = fortifyConnectivityInspector.get(player)
        .connectedSetOf(territoryId);
    fortifyableTerritories.remove(territoryId);
    return fortifyableTerritories;
  }

  /**
   * Check if the territory from fortifyingId can succesfully fortify fortifiedId. This requires
   * depending on the rule set a path of friendly territories or them being neighbors. Note that
   * this function also returns false if either territoryId does not exist.
   *
   * @param fortifyingId the territory from which is to be fortified from
   * @param fortifiedId the territory which is to be fortified
   * @return true iff the territory associated with fortifyingId can fortify the territory
   * associated with fortifiedId
   */
  public boolean canFortify(int fortifyingId, int fortifiedId) {
    int occupant = getTerritoryOccupantId(fortifyingId);
    return occupant >= 0 && occupant == getTerritoryOccupantId(fortifiedId)
        && (fortifyOnlyFromSingleTerritory || fortifyConnectivityInspector.get(occupant)
        .pathExists(fortifyingId, fortifiedId))
        && (!fortifyOnlyFromSingleTerritory || areNeighbors(fortifyingId, fortifiedId));
  }

  void fortify(int fortifyingId, int fortifiedId, int troops) {
    territories.get(fortifyingId).removeTroops(troops);
    territories.get(fortifiedId).addTroops(troops);
  }

  boolean isFortifyOnlyFromSingleTerritory() {
    return fortifyOnlyFromSingleTerritory;
  }

  /**
   * Return the number of troops which are allowed to fortify from this territoryId. Under the
   * default ruleset this is equal to the number of mobile troops. With
   * fortifyOnlyWithNonFightingArmies this is the number of troops not involved in any attacks.
   *
   * @param territoryId the id of the territory
   * @return the number of troops which are legal to fortify from
   */
  public int getFortifyableTroops(int territoryId) {
    int troops = getTerritoryTroops(territoryId);
    if (fortifyOnlyWithNonFightingArmies) {
      troops -= involvedTroopsInAttacks.get(territoryId);
    }

    return Math.min(troops, getMobileTroops(territoryId));
  }

  private boolean containsTradeableSet(Multiset<Integer> cardTypes) {
    if (cardTypes.size() < cardTypesWithoutJoker) {
      return false;
    }
    int jokers = cardTypes.contains(RiskCard.JOKER) ? 1 : 0;

    int wildcards = cardTypes.count(RiskCard.WILDCARD);

    if (jokers + wildcards >= cardTypesWithoutJoker) {
      return true;
    }

    int[] count = IntStream.rangeClosed(1, cardTypesWithoutJoker).map(cardTypes::count).toArray();

    int mostCards = Arrays.stream(count).max().orElse(0);

    if (mostCards + jokers + wildcards >= cardTypesWithoutJoker) {
      return true;
    }

    int distinctCards = Arrays.stream(count).map(i -> i <= 0 ? 0 : 1).sum();
    return distinctCards + jokers + wildcards >= cardTypesWithoutJoker;
  }

  private boolean containsTradeableSet(List<RiskCard> playerCards) {
    if (!withCards || playerCards.size() < cardTypesWithoutJoker) {
      return false;
    }
    return containsTradeableSet(playerCards.stream().map(RiskCard::getCardType)
        .collect(ImmutableMultiset.toImmutableMultiset()));
  }

  private boolean canTradeInAsSet(Multiset<Integer> cardTypes) {

    int jokers = cardTypes.count(RiskCard.JOKER);

    if (jokers > 1) {
      return false;
    }

    int wildcards = cardTypes.count(RiskCard.WILDCARD);

    if (jokers + wildcards == cardTypesWithoutJoker) {
      return true;
    }

    int[] count = IntStream.rangeClosed(1, cardTypesWithoutJoker).map(cardTypes::count).toArray();

    int mostCards = Arrays.stream(count).max().orElse(0);

    if (mostCards + jokers + wildcards == cardTypesWithoutJoker) {
      return true;
    }

    int distinctCards = Arrays.stream(count).map(i -> i <= 0 ? 0 : 1).sum();
    return distinctCards + jokers + wildcards == cardTypesWithoutJoker;
  }

  private boolean canTradeInAsSet(List<RiskCard> playerCards) {
    if (playerCards.size() != cardTypesWithoutJoker) {
      return false;
    }

    return canTradeInAsSet(playerCards.stream().map(RiskCard::getCardType)
        .collect(ImmutableMultiset.toImmutableMultiset()));

  }

  boolean canTradeInAsSet(Set<Integer> slotIds, int player) {
    if (!withCards || playerCards == null || !playerCards.containsKey(player)) {
      return false;
    }
    return canTradeInAsSet(slotIds.stream().map(i -> playerCards.get(player).get(i).getCardType())
        .collect(ImmutableMultiset.toImmutableMultiset()));
  }

  /**
   * Check if a given player could theoretically be able to trade in cards. Note that if withCards
   * is false this function will always return false.
   *
   * @param player the id of the player
   * @return true iff the given player has enough cards and they could contain a set.
   */
  public boolean couldTradeInCards(int player) {
    if (!withCards || playerCards == null || !playerCards.containsKey(player)) {
      return false;
    }

    List<RiskCard> riskCards = getPlayerCards(player);
    return containsTradeableSet(riskCards);
  }

  /**
   * Check if a given player has too many cards and has to trade in their cards at the next possible
   * action. Note that if withCards is false this function will always return false.
   *
   * @param player the id of the player
   * @return true iff the given player's card slots are full.
   */
  public boolean hasToTradeInCards(int player) {
    return withCards && playerCards.containsKey(player)
        && playerCards.get(player).size() >= cardSlots();
  }

  private Set<Set<Integer>> getTradeInSlotCandidates(int player) {
    if (!withCards || playerCards == null || !playerCards.containsKey(player)) {
      return Collections.emptySet();
    }
    final int playerCardsSize = playerCards.get(player).size();

    if (cardTypesWithoutJoker == 3) {
      if (playerCardsSize < 3) {
        return Collections.emptySet();
      }
      if (playerCardsSize == 3) {
        return TRADE_IN_3_OUT_OF_3;
      }
      if (playerCardsSize == 4) {
        return TRADE_IN_3_OUT_OF_4;
      }
      return TRADE_IN_3_OUT_OF_5;
    }

    return Sets.combinations(IntStream.range(0, playerCardsSize).boxed().collect(
        Collectors.toSet()), cardTypesWithoutJoker);
  }

  /**
   * Checks if the slot is a tradeable set.
   *
   * @param slotIds - the ids of the set
   * @param player - the player
   * @return true if it is a valid set
   */
  private boolean checkSlotCandidateDefault(Set<Integer> slotIds, int player) {
    if (!withCards || playerCards == null || !playerCards.containsKey(player)
        || slotIds.size() != cardTypesWithoutJoker) {
      return false;
    }

    ImmutableMultiset<Integer> cardtypes = slotIds.stream()
        .map(i -> playerCards.get(player).get(i).getCardType())
        .collect(ImmutableMultiset.toImmutableMultiset());

    return SETS_3_OUT_OF_5.contains(cardtypes);
  }

  private boolean checkSlotCandidate(Set<Integer> slotIds, int player) {
    if (!withCards || playerCards == null || !playerCards.containsKey(player)
        || slotIds.size() != cardTypesWithoutJoker) {
      return false;
    }

    if (cardTypesWithoutJoker == 3) {
      return checkSlotCandidateDefault(slotIds, player);
    }

    return canTradeInAsSet(slotIds, player);
  }

  Set<Set<Integer>> getTradeInSlots(int player) {
    return getTradeInSlotCandidates(player).stream().filter(c -> checkSlotCandidate(c, player))
        .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Return a immutable list view of the cards of a given player. Note that in uncanonical games
   * this might contain wildcards.
   *
   * @param player the id of the player
   * @return a list of cards of a given player.
   */
  public List<RiskCard> getPlayerCards(int player) {
    return Collections
        .unmodifiableList(playerCards.getOrDefault(player, Collections.emptyList()));
  }

  private int cardSlots() {
    return cardSlots(cardTypesWithoutJoker);
  }

  private int cardSlots(int cardTypesWithoutJoker) {
    cardTypesWithoutJoker--;
    return cardTypesWithoutJoker * cardTypesWithoutJoker + 1;
  }

  boolean allowedToTradeIn(int player) {
    return withCards && (isReinforcementPhase()
        || playerCards.getOrDefault(player, Collections.emptyList()).size() >= cardSlots());
  }

  /**
   * Return the number of troops the nth trade in is worth. Note that the enumeration starts with 0,
   * i.e. the first trade in is the 0th by this function. Returns 0 if n is negative.
   *
   * @param n the nth trade in, starting from 0
   * @return the number of troops awarded by the nth trade in.
   */
  public int getTradeInBonus(int n) {
    if (n < 0) {
      return 0;
    }
    if (n >= tradeInBonus.length) {
      return tradeInBonus[tradeInBonus.length - 1] + (n - tradeInBonus.length + 1)
          * maxExtraBonus;
    }
    return tradeInBonus[n];
  }

  /**
   * Return the number of troops the next trade in is worth.
   *
   * @return the number of troops the next trade in is worth.
   */
  public int getTradeInBonus() {
    return getTradeInBonus(tradeIns);
  }

  void tradeIn(Set<Integer> cardIds, int player) {
    List<RiskCard> cards = cardIds.stream()
        .map(i -> playerCards.get(player).get(i)).collect(Collectors.toCollection(LinkedList::new));
    this.discardPile.addAll(cards);
    tradeInTerritories = cards.stream().filter(
        c -> c.getCardType() != RiskCard.WILDCARD && c.getCardType() != RiskCard.JOKER
            && getTerritoryOccupantId(c.getTerritoryId()) == player).map(RiskCard::getTerritoryId)
        .collect(Collectors.toUnmodifiableSet());

    Set<Integer> discarded = this.discardPile.stream().map(RiskCard::getTerritoryId).collect(
        Collectors.toUnmodifiableSet());

    long maxPossible = territories.entrySet().stream().filter(
        t -> t.getValue().getOccupantPlayerId() == player && !discarded.contains(t.getKey()))
        .count();

    long numberOfWildcards = cards.stream().filter(c -> c.getCardType() == RiskCard.WILDCARD)
        .count();

    minMatchingTerritories = tradeInTerritories.size();
    maxMatchingTerritories =
        minMatchingTerritories + (int) Math.min(numberOfWildcards, maxPossible);
    this.playerCards.get(player).removeAll(cards);
    reinforcedTerritories.clear();
    phase = RiskPhase.REINFORCEMENT;
    tradedInId = player;
  }

  void awardBonus(int nrOfMatchingTerritories, int player) {
    this.nonDeployedReinforcements[player] +=
        getTradeInBonus() + nrOfMatchingTerritories * tradeInTerritoryBonus;
    tradeIns++;
  }

  int getMinMatchingTerritories() {
    return minMatchingTerritories;
  }

  int getMaxMatchingTerritories() {
    return maxMatchingTerritories;
  }

  int getNrOfBonusTerritories() {
    return tradeInTerritories.size();
  }

  boolean inBonusTerritories(int id) {
    return tradeInTerritories.contains(id);
  }

  /**
   * Return the number of troops it is worth that a set containing a card with a territory owned by
   * the player is worth.
   *
   * @return the number of troops bonus per territory owned in a traded set.
   */
  public int getTradeInTerritoryBonus() {
    return tradeInTerritoryBonus;
  }


  Set<Integer> getBonusTerritories() {
    return Collections.unmodifiableSet(tradeInTerritories);
  }

  /**
   * Return the number of cards left in the deck of cards.
   *
   * @return the number of cards left in the deck of cards.
   */
  public int getCardsLeft() {
    return deckOfCards.size();
  }

  /**
   * Return a immutable view of the discarded pile.
   *
   * @return the discarded pile.
   */
  public Collection<RiskCard> getDiscardedPile() {
    return Collections.unmodifiableList(discardPile);
  }

  /**
   * Return the number of cards in the game. This is equal to number of territories plus the number
   * of jokers. Note that if withCards is false that this function will always return 0.
   *
   * @return the number of cards in the game.
   */
  public int getNumberOfCards() {
    if (!withCards || playerCards == null || discardPile == null || deckOfCards == null) {
      return 0;
    }
    return playerCards.values().stream().mapToInt(List::size).sum() + discardPile.size()
        + deckOfCards.size();
  }

  int getTradedInId() {
    return tradedInId;
  }

  void drawCardIfPossible(int player) {
    if (withCards && hasOccupiedCountry && playerCards.containsKey(player)) {
      if (deckOfCards.isEmpty()) {
        reshuffle();
      }
      if (!deckOfCards.isEmpty()) {
        playerCards.get(player).add(deckOfCards.pop());
      }
    }
  }

  private void reshuffle() {
    if (withCards && discardPile != null && deckOfCards != null) {
      Collections.shuffle(discardPile);
      deckOfCards.addAll(discardPile);
      discardPile.clear();
    }
  }

  void stripOutUnknownInformation() {
    stripOutCardInformation();
  }

  void stripOutUnknownInformation(int player) {
    stripOutCardInformation(player);
  }

  private void stripOutCardInformation() {
    List<RiskCard> deckOfCards = this.deckOfCards.stream().map(c -> RiskCard.wildcard())
        .collect(Collectors.toCollection(ArrayList::new));

    Collections.shuffle(deckOfCards);

    this.deckOfCards.clear();
    this.deckOfCards.addAll(deckOfCards);

  }

  private void stripOutCardInformation(int player) {
    for (Entry<Integer, List<RiskCard>> playerCard : playerCards.entrySet()) {
      int playerSlot = playerCard.getKey();
      if (playerSlot != player) {
        playerCards.get(playerSlot).replaceAll(c -> RiskCard.wildcard());
      }
    }
  }

  private enum RiskPhase {
    REINFORCEMENT,
    ATTACK,
    OCCUPY,
    FORTIFY,
  }

}
