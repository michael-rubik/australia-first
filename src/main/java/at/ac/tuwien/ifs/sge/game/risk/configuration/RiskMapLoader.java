package at.ac.tuwien.ifs.sge.game.risk.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RiskMapLoader implements Callable<String> {

  private static final String DEFAULT_TERRITORY_REGEX = "X";
  private static final Pattern trailingWhiteSpacePattern = Pattern.compile(" *$}");
  private final Pattern territoryPattern;
  private final String origMap;
  private final List<String> territories;

  public RiskMapLoader(Path mapFile, Path territoriesFile) throws IOException {
    this(Files.readString(mapFile), Files.readAllLines(territoriesFile));
  }

  public RiskMapLoader(Path mapFile, Path territoriesFile, String territoryRegex)
      throws IOException {
    this(Files.readString(mapFile), Files.readAllLines(territoriesFile), territoryRegex);
  }


  public RiskMapLoader(String origMap, List<String> territories) {
    this(origMap, territories, DEFAULT_TERRITORY_REGEX);
  }

  public RiskMapLoader(String origMap, List<String> territories, String territoryRegex) {
    this.origMap = origMap;
    this.territories = territories;
    this.territoryPattern = Pattern.compile(territoryRegex);
  }

  @Override
  public String call() {
    Matcher matcher;
    String map = origMap;
    for (String territory : territories) {
      matcher = territoryPattern.matcher(map);
      map = matcher.replaceFirst("[\"+" + territory + ".getTerritoryId()+\"]");
    }
    matcher = trailingWhiteSpacePattern.matcher(map);
    map = matcher.replaceAll("");

    for (int i = Character.MAX_RADIX - 1; i >= 1; i--) {
      matcher = Pattern.compile(" {" + i + "}").matcher(map);
      map = matcher.replaceAll(Integer.toString(i, Character.MAX_RADIX));
    }

    return map;
  }
}
