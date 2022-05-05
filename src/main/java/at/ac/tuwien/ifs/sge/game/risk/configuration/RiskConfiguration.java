package at.ac.tuwien.ifs.sge.game.risk.configuration;

import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskMissionConfiguration.continentGroup;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.AFGHANISTAN;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.ALASKA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.ALBERTA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.ARGENTINA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.BRAZIL;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.CENTRAL_AFRICA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.CENTRAL_AMERICA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.CHINA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.EASTERN_AUSTRALIA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.EASTERN_UNITED_STATES;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.EAST_AFRICA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.EGYPT;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.GREAT_BRITAIN;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.GREENLAND;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.ICELAND;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.INDIA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.INDONESIA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.IRKUTSK;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.JAPAN;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.KAMCHATKA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.MADAGASCAR;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.MIDDLE_EAST;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.MONGOLIA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.NEW_GUINEA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.NORTHERN_EUROPE;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.NORTHWEST_TERRITORY;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.NORTH_AFRICA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.ONTARIO;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.PERU;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.QUEBEC;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.SCANDINAVIA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.SIAM;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.SIBERIA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.SOUTHERN_EUROPE;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.SOUTH_AFRICA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.UKRAINE;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.URAL;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.VENEZUELA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.WESTERN_AUSTRALIA;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.WESTERN_EUROPE;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.WESTERN_UNITED_STATES;
import static at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration.YAKUTSK;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

public class RiskConfiguration {

  public static final String defaultMap = "" +
      "zzj--------zzzn\n"
      + "zz6+-----------/8\\zzzm\n"
      + "zz5/l/zs+----+zn\n"
      + "zc+--------+-----------------+-8[" + GREENLAND.getTerritoryId()
      + "]8+--+k+---+w+-----+6\\1/-----------------------------+p\n"
      + "f+-------+m/a\\f/2\\f/n/5\\m+-------+e+v\\o\n"
      + "a+---/8/++-------------------+c\\3+---------+4|a---\\k+--+7+-+------------------+9\\8["
      + SIBERIA.getTerritoryId() + "]5\\c[" + YAKUTSK.getTerritoryId() + "]i+-------------+9\n"
      + "9/c/z1\\1/8+------+4+----/1+----------+----\\5/6[" + SCANDINAVIA.getTerritoryId()
      + "]6|j\\9+----+9+t/f+\n"
      + "3+----+4[" + ALASKA.getTerritoryId() + "]7/f[" + NORTHWEST_TERRITORY.getTerritoryId()
      + "]d+-------++8/7|3/7|4[" + ICELAND.getTerritoryId() + "]5|5+--1+6+7+j+f\\9\\4+j+--+h\\\n"
      + "2/h/s/8/2\\6/8\\1/9+-------+--+4/3/6/1\\5/j/8[" + URAL.getTerritoryId()
      + "]8\\_8+2/1\\h/6+6[" + KAMCHATKA.getTerritoryId() + "]8+\n"
      + "++6+-----------+--------------------+------+8|5\\---+a+j\\5/4+-----+3+---+j+k\\6/1\\/3+--------+2+---+6/1\\3/1\\8|6\n"
      + "\\5++c|j/g|a\\o------+---+7\\a/l\\k\\4/8[" + IRKUTSK.getTerritoryId()
      + "]8\\/a+3+-+3+7+------\n"
      + "1\\---+e|8[" + ALBERTA.getTerritoryId() + "]9/8[" + ONTARIO.getTerritoryId() + "]8|4["
      + QUEBEC.getTerritoryId() + "]6\\n|4[" + GREAT_BRITAIN.getTerritoryId() + "]4|8+--------+b["
      + UKRAINE.getTerritoryId() + "]b+------------+7\\2/j+-----+4|a\\5/\n"
      + "k/h/h/c|n------+---+------1/4[" + NORTHERN_EUROPE.getTerritoryId()
      + "]5\\l/e\\6/1/5/---+a|6\\3+-+9+---+\n"
      + "j/-----------------+---------9|8+---+u\\9+c\\j+g+----+-+-----+5\\2/------+7\\1/3\\l\n"
      + "i/j/8\\8/7/z1+-------+---+----------+--+e/8[" + AFGHANISTAN.getTerritoryId()
      + "]c/3\\b++g+5\\k\n"
      + "h/j/a+------+-------+z1/4[" + WESTERN_EUROPE.getTerritoryId() + "]5/7[" + SOUTHERN_EUROPE
      .getTerritoryId() + "]8\\c+l/5\\s|6+-----+\n"
      + "g/9[" + WESTERN_UNITED_STATES.getTerritoryId() + "]9|i___/z5/a/\\h+---+7|k/7+----+5["
      + MONGOLIA.getTerritoryId() + "]7+----+3+-----/7\\\n"
      + "f/k|h/z8+----+-----+2\\1/+------+-+3/5\\------+e+----+e\\b/6\\1/5+3[" + JAPAN
      .getTerritoryId() + "]5+\n"
      + "f|d_______|6[" + EASTERN_UNITED_STATES.getTerritoryId()
      + "]9/ze|9+b\\1/e\\c/6\\e+---------+8+6|6++/\n"
      + "f|c|n/zc+--+---+4/|c+-----+a+------+---+8+--+t\\6+-+3/\n"
      + "f\\c|h_____/zc/8\\2/1|h/j\\f\\g[" + CHINA.getTerritoryId() + "]c+8\\1/g\n"
      + "g\\------------g/zh/a+/2+----------------+l\\f+--+5+k\\8+h\n"
      + "h\\b|4___________|zg/c\\1/i\\l\\9[" + INDIA.getTerritoryId() + "]8\\3/1\\k+p\n"
      + "i\\a----/zp+-+e+9[" + EGYPT.getTerritoryId() + "]a\\8[" + MIDDLE_EAST.getTerritoryId()
      + "]c+---+e+-+3\\j|p\n"
      + "j\\b|zq/i\\k\\j/5\\e/5+---+3+--+-------+p\n"
      + "k\\5[" + CENTRAL_AMERICA.getTerritoryId()
      + "]4|zp/k+-------+------------+a+--+3/7+-+a+---+7\\1/3|x\n"
      + "l\\a\\--\\zk+u\\c\\a\\2\\1/a|9/5+7+4+x\n"
      + "m+--+b\\zi|d[" + NORTH_AFRICA.getTerritoryId() + "]h+c\\a\\2+b+5+--+7\\c\\w\n"
      + "q+-----+6\\za+-----+v|d\\5+----+e\\5\\a+-+3[" + SIAM.getTerritoryId() + "]6+v\n"
      + "x\\__4\\8+--+v/6|v+e\\5\\j+5+c\\8/w\n"
      + "z1\\4+------+4+-----+n/7+--+r/9[" + EAST_AFRICA.getTerritoryId()
      + "]6+-----+j\\4|d+3+--+x\n"
      + "z2`-+-\\i\\l/c\\7+-----+7+----+l/l+3+e\\1/z2\n"
      + "z7\\7[" + VENEZUELA.getTerritoryId() + "]a+-+h/e\\5/7\\5/7\\i/n\\1/g+z3\n"
      + "z8\\b+-+6\\f/g+---+9\\3/9+-------+8/p+i\\z2\n"
      + "z8/8+-+3+------+d/w\\1/h/8/zb\\z1\n"
      + "z7+----+3/d+-----+6/y+8[" + CENTRAL_AFRICA.getTerritoryId()
      + "]8/8/z9+---+-----+-----------+-----------+\n"
      + "z6/6\\1/k+-----+z1\\f/8+za|4[" + INDONESIA.getTerritoryId() + "]4|b|5[" + NEW_GUINEA
      .getTerritoryId() + "]5|\n"
      + "z6|7+s\\z1+---+9+8/1\\z9+---------+b+-----------+\n"
      + "z7\\6|t+z|4\\9\\6/3\\zj\\9/4|\n"
      + "z8\\5+--+c[" + BRAZIL.getTerritoryId() + "]d|z+5\\9\\4/5\\zj\\7/5|\n"
      + "z9\\8+--+j+--+y/7+---------+--+7+-----+zd\\5/6|\n"
      + "za\\4[" + PERU.getTerritoryId() + "]6\\i|z2|k+------+6|ze\\3/----+--+--+3+-+\n"
      + "zb\\b\\h|z2\\j/5/8|zf+-+4/7\\1/3\\\n"
      + "zc+--+8\\g+z3|h+5|4[" + MADAGASCAR.getTerritoryId() + "]4|ze/6/9+5+\n"
      + "zg\\8\\e/z4\\7[" + SOUTH_AFRICA.getTerritoryId() + "]9|5|8/za+---+6/h\\\n"
      + "zh+--------+9+--+z6\\g+5|7/za/a/c[" + EASTERN_AUSTRALIA.getTerritoryId() + "]6+\n"
      + "zh|9\\7/zb+e/6+------+za+a+---------+9/\n"
      + "zh|a\\5/zd\\9+--+zp|5[" + WESTERN_AUSTRALIA.getTerritoryId() + "]d/9+2\n"
      + "zh|b+3/zf+7/zt|i/a|2\n"
      + "zh|5[" + ARGENTINA.getTerritoryId() + "]6\\1/zg|6/zu+-------------+3/9+-+\n"
      + "zh|d+zh+-----+zzb\\1/9/5\n"
      + "zh|c/zzzz2+6+--+6\n"
      + "zh|9+-+zzzz4\\4/a\n"
      + "zh+8/zzzz8+--+b\n"
      + "zi\\6/zzzzo\n"
      + "zj\\4+zzzzp\n"
      + "zk\\4\\zzzzo\n"
      + "zl\\4\\zzzzn\n"
      + "zm+-+2+zzzzm\n"
      + "zp\\1/zzzzm\n"
      + "zq+zzzzn\n";

  public static final RiskConfiguration RISK_EUROPEAN_DEFAULT_CONFIG
      = new RiskConfiguration(6,
      3, 2,
      new int[]{50, 35, 30, 25, 20},
      true, new int[]{4, 6, 8, 10, 12, 15}, 5, 3, 2,
      true,
      3, 3,
      false, true, false, true,
      RiskMissionConfiguration.defaultMissions(0, 6,
          Arrays.asList(
              continentGroup(
                  RiskContinentConfiguration.ASIA, RiskContinentConfiguration.SOUTH_AMERICA),
              continentGroup(RiskContinentConfiguration.ASIA, RiskContinentConfiguration.AFRICA),
              continentGroup(
                  RiskContinentConfiguration.NORTH_AMERICA, RiskContinentConfiguration.AFRICA),
              continentGroup(
                  RiskContinentConfiguration.NORTH_AMERICA, RiskContinentConfiguration.AUSTRALIA),
              continentGroup(
                  RiskContinentConfiguration.EUROPE, RiskContinentConfiguration.SOUTH_AMERICA,
                  RiskContinentConfiguration.WILDCARD),
              continentGroup(RiskContinentConfiguration.EUROPE,
                  RiskContinentConfiguration.AUSTRALIA, RiskContinentConfiguration.WILDCARD)),
          new int[]{18, 24}, new int[]{2, 1}),
      RiskContinentConfiguration.allContinents,
      RiskTerritoryConfiguration.allTerritories, defaultMap );

  public static final RiskConfiguration RISK_DEFAULT_CONFIG
      = new RiskConfiguration(6,
      3, 2,
      new int[]{50, 35, 30, 25, 20},
      true, new int[]{4, 6, 8, 10, 12, 15}, 5, 3, 2,
      true,
      3, 3,
      false, true, false, false, Collections.emptyList(),
      RiskContinentConfiguration.allContinents,
      RiskTerritoryConfiguration.allTerritories, defaultMap
  );

  private static Yaml riskConfigurationYaml = null;
  private int maxNumberOfPlayers = 2;
  private int maxAttackerDice = 3;
  private int maxDefenderDice = 2;
  private int[] initialTroops = null;
  private boolean withCards = true;
  private int[] tradeInBonus = null;
  private int maxExtraBonus = -1;
  private int cardTypesWithoutJoker = 3;
  private int numberOfJokers = 2;
  private boolean chooseInitialTerritories = true;
  private int reinforcementAtLeast = 3;
  private int reinforcementThreshold = 3;
  private boolean occupyOnlyWithAttackingArmies = false;
  private boolean fortifyOnlyFromSingleTerritory = true;
  private boolean fortifyOnlyWithNonFightingArmies = false;
  private boolean withMissions = true;
  private List<RiskMissionConfiguration> missions = new ArrayList<>();
  private List<RiskContinentConfiguration> continents;
  private List<RiskTerritoryConfiguration> territories;
  private String map;

  public RiskConfiguration() {
  }


  public RiskConfiguration(int maxNumberOfPlayers, int maxAttackerDice, int maxDefenderDice,
      int[] initialTroops, boolean withCards, int[] tradeInBonus, int maxExtraBonus,
      int cardTypesWithoutJoker, int numberOfJokers,
      boolean chooseInitialTerritories, int reinforcementAtLeast, int reinforcementThreshold,
      boolean occupyOnlyWithAttackingArmies, boolean fortifyOnlyFromSingleTerritory,
      boolean fortifyOnlyWithNonFightingArmies, boolean withMissions,
      Collection<RiskMissionConfiguration> missions,
      Collection<RiskContinentConfiguration> continents,
      Collection<RiskTerritoryConfiguration> territories, String map) {
    this.maxNumberOfPlayers = maxNumberOfPlayers;
    this.maxAttackerDice = maxAttackerDice;
    this.maxDefenderDice = maxDefenderDice;
    this.initialTroops = initialTroops;
    this.withCards = withCards;
    this.tradeInBonus = tradeInBonus;
    this.maxExtraBonus = maxExtraBonus;
    this.cardTypesWithoutJoker = cardTypesWithoutJoker;
    this.numberOfJokers = numberOfJokers;
    this.chooseInitialTerritories = chooseInitialTerritories;
    this.reinforcementAtLeast = reinforcementAtLeast;
    this.reinforcementThreshold = reinforcementThreshold;
    this.occupyOnlyWithAttackingArmies = occupyOnlyWithAttackingArmies;
    this.fortifyOnlyFromSingleTerritory = fortifyOnlyFromSingleTerritory;
    this.fortifyOnlyWithNonFightingArmies = fortifyOnlyWithNonFightingArmies;
    this.withMissions = withMissions;
    this.missions.addAll(missions);
    this.continents = new ArrayList<>(new HashSet<>(continents));
    this.territories = new ArrayList<>(new HashSet<>(territories));
    this.map = map;
  }

  public RiskConfiguration(
      Collection<RiskContinentConfiguration> continents,
      Collection<RiskTerritoryConfiguration> territories, String map) {
    this.continents = new ArrayList<>(new HashSet<>(continents));
    this.territories = new ArrayList<>(new HashSet<>(territories));
    this.map = map;
  }

  public static Yaml getYaml() {
    if (riskConfigurationYaml == null) {
      Constructor constructor = new Constructor(RiskConfiguration.class);
      Representer representer = new Representer();
      representer.getPropertyUtils().setSkipMissingProperties(true);
      DumperOptions dumperOptions = new DumperOptions();
      dumperOptions.setDefaultFlowStyle(FlowStyle.AUTO);
      TypeDescription riskConfigurationDescription = new TypeDescription(RiskConfiguration.class);
      riskConfigurationDescription
          .addPropertyParameters("continents", RiskContinentConfiguration.class);
      riskConfigurationDescription
          .addPropertyParameters("territories", RiskTerritoryConfiguration.class);
      riskConfigurationDescription
          .addPropertyParameters("missions", RiskMissionConfiguration.class);
      constructor.addTypeDescription(riskConfigurationDescription);
      riskConfigurationYaml = new Yaml(constructor, representer, dumperOptions);
    }
    return riskConfigurationYaml;
  }


  public int[] getInitialTroops() {
    if (initialTroops == null && territories != null) {
      initialTroops = new int[maxNumberOfPlayers - 1];
      int territoryNumber = Math
          .max(1, BigDecimal.valueOf(territories.size()).setScale(-1, RoundingMode.UP)
              .intValue());
      int steps = Math.max(1, territoryNumber / 10);
      for (int i = 0; i < initialTroops.length; i++) {
        initialTroops[i] = territoryNumber;
        do {
          territoryNumber -= steps;
        } while (initialTroops[i] * (i + 2) < territoryNumber * (i + 3) - (steps * 3));
      }
    }
    return initialTroops;
  }

  public void setInitialTroops(int[] initialTroops) {
    yamlString = null;
    this.initialTroops = initialTroops.clone();
  }

  public int[] getTradeInBonus() {
    if (tradeInBonus == null && territories != null) {
      this.tradeInBonus = new int[getMaxExtraBonus()];
      int territoryNumber = Math
          .max(1,
              BigDecimal.valueOf(territories.size()).setScale(-1, RoundingMode.DOWN).intValue());
      int plus = 2;
      int lastPlus = 1;
      this.tradeInBonus[0] = territoryNumber;
      int i;
      for (i = 1; (i + 1) < getMaxExtraBonus(); i++) {
        this.tradeInBonus[i] = this.tradeInBonus[i - 1] + plus;
      }
      while (plus <= getMaxExtraBonus()) {
        plus += lastPlus;
        this.tradeInBonus[i] = this.tradeInBonus[i - 1] + plus;
        lastPlus = plus;
      }
    }
    return tradeInBonus;
  }


  public void setTradeInBonus(int[] tradeInBonus) {
    yamlString = null;
    this.tradeInBonus = tradeInBonus.clone();
  }

  public int getMaxExtraBonus() {
    if (maxExtraBonus < 0 && territories != null) {
      maxExtraBonus = 1 + Math
          .max(0,
              BigDecimal.valueOf(territories.size()).setScale(-1, RoundingMode.DOWN).intValue());
    }
    return maxExtraBonus;
  }

  public void setMaxExtraBonus(int maxExtraBonus) {
    yamlString = null;
    this.maxExtraBonus = maxExtraBonus;
  }

  public int getMaxNumberOfPlayers() {
    return maxNumberOfPlayers;
  }

  public void setMaxNumberOfPlayers(int maxNumberOfPlayers) {
    yamlString = null;
    this.maxNumberOfPlayers = maxNumberOfPlayers;
  }

  public int getMaxAttackerDice() {
    return maxAttackerDice;
  }

  public void setMaxAttackerDice(int maxAttackerDice) {
    yamlString = null;
    this.maxAttackerDice = maxAttackerDice;
  }

  public int getMaxDefenderDice() {
    return maxDefenderDice;
  }

  public void setMaxDefenderDice(int maxDefenderDice) {
    yamlString = null;
    this.maxDefenderDice = maxDefenderDice;
  }

  public boolean isWithCards() {
    return withCards;
  }

  public void setWithCards(boolean withCards) {
    yamlString = null;
    this.withCards = withCards;
  }

  public int getCardTypesWithoutJoker() {
    return cardTypesWithoutJoker;
  }

  public void setCardTypesWithoutJoker(int cardTypesWithoutJoker) {
    yamlString = null;
    this.cardTypesWithoutJoker = cardTypesWithoutJoker;
  }

  public int getNumberOfJokers() {
    return numberOfJokers;
  }

  public void setNumberOfJokers(int numberOfJokers) {
    yamlString = null;
    this.numberOfJokers = numberOfJokers;
  }

  public boolean isChooseInitialTerritories() {
    return chooseInitialTerritories;
  }

  public void setChooseInitialTerritories(boolean chooseInitialTerritories) {
    yamlString = null;
    this.chooseInitialTerritories = chooseInitialTerritories;
  }

  public int getReinforcementAtLeast() {
    return reinforcementAtLeast;
  }

  public void setReinforcementAtLeast(int reinforcementAtLeast) {
    yamlString = null;
    this.reinforcementAtLeast = reinforcementAtLeast;
  }

  public int getReinforcementThreshold() {
    return reinforcementThreshold;
  }

  public void setReinforcementThreshold(int reinforcementThreshold) {
    yamlString = null;
    this.reinforcementThreshold = reinforcementThreshold;
  }

  public boolean isOccupyOnlyWithAttackingArmies() {
    return occupyOnlyWithAttackingArmies;
  }

  public void setOccupyOnlyWithAttackingArmies(boolean occupyOnlyWithAttackingArmies) {
    yamlString = null;
    this.occupyOnlyWithAttackingArmies = occupyOnlyWithAttackingArmies;
  }

  public boolean isFortifyOnlyFromSingleTerritory() {
    return fortifyOnlyFromSingleTerritory;
  }

  public void setFortifyOnlyFromSingleTerritory(boolean fortifyOnlyFromSingleTerritory) {
    yamlString = null;
    this.fortifyOnlyFromSingleTerritory = fortifyOnlyFromSingleTerritory;
  }

  public boolean isFortifyOnlyWithNonFightingArmies() {
    return fortifyOnlyWithNonFightingArmies;
  }

  public void setFortifyOnlyWithNonFightingArmies(boolean fortifyOnlyWithNonFightingArmies) {
    yamlString = null;
    this.fortifyOnlyWithNonFightingArmies = fortifyOnlyWithNonFightingArmies;
  }

  public boolean isWithMissions() {
    return withMissions;
  }

  public void setWithMissions(boolean withMissions) {
    yamlString = null;
    this.withMissions = withMissions;
  }

  public List<RiskMissionConfiguration> getMissions() {
    return missions;
  }

  public void setMissions(
      List<RiskMissionConfiguration> missions) {
    yamlString = null;
    this.missions = new ArrayList<>(new HashSet<>(missions));
  }

  public List<RiskContinentConfiguration> getContinents() {
    return continents;
  }

  public void setContinents(
      List<RiskContinentConfiguration> continents) {
    yamlString = null;
    this.continents = new ArrayList<>(new HashSet<>(continents));
  }

  public List<RiskTerritoryConfiguration> getTerritories() {
    return territories;
  }

  public void setTerritories(
      List<RiskTerritoryConfiguration> territories) {
    yamlString = null;
    this.territories = new ArrayList<>(new HashSet<>(territories));
  }

  public String getMap() {
    return map;
  }

  public void setMap(String map) {
    yamlString = null;
    this.map = map;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RiskConfiguration that = (RiskConfiguration) o;
    return getMaxNumberOfPlayers() == that.getMaxNumberOfPlayers() &&
        getMaxAttackerDice() == that.getMaxAttackerDice() &&
        getMaxDefenderDice() == that.getMaxDefenderDice() &&
        isWithCards() == that.isWithCards() &&
        getMaxExtraBonus() == that.getMaxExtraBonus() &&
        getCardTypesWithoutJoker() == that.getCardTypesWithoutJoker() &&
        getNumberOfJokers() == that.getNumberOfJokers() &&
        isChooseInitialTerritories() == that.isChooseInitialTerritories() &&
        getReinforcementAtLeast() == that.getReinforcementAtLeast() &&
        getReinforcementThreshold() == that.getReinforcementThreshold() &&
        isOccupyOnlyWithAttackingArmies() == that.isOccupyOnlyWithAttackingArmies() &&
        isFortifyOnlyFromSingleTerritory() == that.isFortifyOnlyFromSingleTerritory() &&
        isFortifyOnlyWithNonFightingArmies() == that.isFortifyOnlyWithNonFightingArmies() &&
        isWithMissions() == that.isWithMissions() &&
        Arrays.equals(getInitialTroops(), that.getInitialTroops()) &&
        Arrays.equals(getTradeInBonus(), that.getTradeInBonus()) &&
        Objects.equals(getMissions(), that.getMissions()) &&
        Objects.equals(getContinents(), that.getContinents()) &&
        Objects.equals(getTerritories(), that.getTerritories()) &&
        Objects.equals(getMap(), that.getMap());
  }

  @Override
  public int hashCode() {
    int result = Objects
        .hash(getMaxNumberOfPlayers(), getMaxAttackerDice(), getMaxDefenderDice(), isWithCards(),
            getMaxExtraBonus(), getCardTypesWithoutJoker(), getNumberOfJokers(),
            isChooseInitialTerritories(), getReinforcementAtLeast(), getReinforcementThreshold(),
            isOccupyOnlyWithAttackingArmies(), isFortifyOnlyFromSingleTerritory(),
            isFortifyOnlyWithNonFightingArmies(), isWithMissions(), getMissions(), getContinents(),
            getTerritories(), getMap());
    result = 31 * result + Arrays.hashCode(getInitialTroops());
    result = 31 * result + Arrays.hashCode(getTradeInBonus());
    return result;
  }

  private String yamlString = null;

  @Override
  public String toString() {
    if (yamlString == null) {
      yamlString = getYaml().dump(this);
    }
    return yamlString;
  }
}
