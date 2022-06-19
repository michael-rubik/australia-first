package at.ac.tuwien.ifs.sge.agent.risk;

import at.ac.tuwien.ifs.sge.game.Game;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.configuration.RiskContinentConfiguration;
import at.ac.tuwien.ifs.sge.game.risk.configuration.RiskMissionConfiguration;
import at.ac.tuwien.ifs.sge.util.node.GameNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RiskGameNode<A> implements GameNode<A> {

  private Risk game;
  private int wins;
  private int plays;

  private Map<RiskContinentConfiguration, Integer> continents;

  public RiskGameNode() {
    this(null);
  }

  public RiskGameNode(Game<A, ?> game) {
    this(game, 0, 0);
  }

  public RiskGameNode(Game<A, ?> game, A action) {
    this(game.doAction(action));
  }

  public RiskGameNode(Game<A, ?> game, int wins, int plays) {
    this.game = (Risk) game;
    this.wins = wins;
    this.plays = plays;

    continents = new HashMap<>();
    for (RiskContinentConfiguration continent:
         RiskContinentConfiguration.allContinents) {
      continents.put(continent, 0);
    }
  }


  public Game<A, ?> getGame() {
    return (Game<A, ?>) game;
  }

  public void setGame(Game<A, ?> game) {
    this.game = (Risk) game;
  }

  public int getWins() {
    return wins;
  }

  public void setWins(int wins) {
    this.wins = wins;
  }

  public void incWins() {
    wins++;
  }

  public int getPlays() {
    return plays;
  }

  public void setPlays(int plays) {
    this.plays = plays;
  }

  public void incPlays() {
    plays++;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RiskGameNode<?> riskGameNode = (RiskGameNode<?>) o;
    return wins == riskGameNode.wins &&
            plays == riskGameNode.plays &&
            game.equals(riskGameNode.game);
  }

  @Override
  public int hashCode() {
    return Objects.hash(game, wins, plays);
  }
}
