package at.ac.tuwien.ifs.sge.agent.risk;

import at.ac.tuwien.ifs.sge.game.risk.board.RiskTerritory;
import at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration;

import java.util.Set;
import java.util.stream.Collectors;

public final class TerritoryConstants {



	public static Set<RiskTerritoryConfiguration> AUSTRALIA_TERRITORIES_CONFIG = Set.of(
			RiskTerritoryConfiguration.EASTERN_AUSTRALIA,
			RiskTerritoryConfiguration.WESTERN_AUSTRALIA,
			RiskTerritoryConfiguration.INDONESIA,
			RiskTerritoryConfiguration.NEW_GUINEA);

	public static Set<Integer> AUSTRALIA_TERRITORIES_IDS = AUSTRALIA_TERRITORIES_CONFIG.stream().
			map(RiskTerritoryConfiguration::getTerritoryId).collect(Collectors.toSet());

	public static Set<RiskTerritory> AUSTRALIA_TERRITORIES = AUSTRALIA_TERRITORIES_CONFIG.stream().
			map(RiskTerritoryConfiguration::getTerritory).collect(Collectors.toSet());

	public static Set<RiskTerritoryConfiguration> SOUTH_AMERICA_TERRITORIES_CONFIG = Set.of(
			RiskTerritoryConfiguration.ARGENTINA,
			RiskTerritoryConfiguration.BRAZIL,
			RiskTerritoryConfiguration.PERU,
			RiskTerritoryConfiguration.VENEZUELA);

	public static Set<Integer> SOUTH_AMERICA_TERRITORIES_IDS = SOUTH_AMERICA_TERRITORIES_CONFIG.stream().
			map(RiskTerritoryConfiguration::getTerritoryId).collect(Collectors.toSet());

	public static Set<RiskTerritory> SOUTH_AMERICA_TERRITORIES = SOUTH_AMERICA_TERRITORIES_CONFIG.stream().
			map(RiskTerritoryConfiguration::getTerritory).collect(Collectors.toSet());

	public static Set<RiskTerritoryConfiguration> AFRICA_TERRITORIES_CONFIG = Set.of(
			RiskTerritoryConfiguration.CENTRAL_AFRICA,
			RiskTerritoryConfiguration.EAST_AFRICA,
			RiskTerritoryConfiguration.EGYPT,
			RiskTerritoryConfiguration.NORTH_AFRICA,
			RiskTerritoryConfiguration.SOUTH_AFRICA,
			RiskTerritoryConfiguration.MADAGASCAR);

	public static Set<Integer> AFRICA_TERRITORIES_IDS = AFRICA_TERRITORIES_CONFIG.stream().
			map(RiskTerritoryConfiguration::getTerritoryId).collect(Collectors.toSet());

	public static Set<RiskTerritory> AFRICA_TERRITORIES = AFRICA_TERRITORIES_CONFIG.stream().
			map(RiskTerritoryConfiguration::getTerritory).collect(Collectors.toSet());

	public static Set<RiskTerritoryConfiguration> SIAM_TERRITORIES_CONFIG = Set.of(RiskTerritoryConfiguration.SIAM);
	public static Set<Integer> SIAM_TERRITORIES_IDS = SIAM_TERRITORIES_CONFIG.stream().
			map(RiskTerritoryConfiguration::getTerritoryId).collect(Collectors.toSet());

}
