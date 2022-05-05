package at.ac.tuwien.ifs.sge.engine.risk;

import at.ac.tuwien.ifs.sge.game.risk.configuration.RiskMapLoader;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "rmg", description = "Convert a raw map to a compressed string for Risk.")
public class RiskMapGenerator implements Callable<Void> {

  @Parameters(index = "0", paramLabel = "MAP", description = "File of map")
  private Path map;

  @Parameters(index = "1", paramLabel = "TERRITORIES", description = "List of territories, separated via newlines")
  private Path territories;

  public static void main(String[] args) {
    CommandLine.call(new RiskMapGenerator(), args);
  }


  @Override
  public Void call() throws Exception {
    RiskMapLoader riskMapLoader = new RiskMapLoader(map, territories);
    System.out.println(riskMapLoader.call());
    return null;
  }


}
