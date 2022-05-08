package at.ac.tuwien.ifs.sge.agent.risk;

import at.ac.tuwien.ifs.sge.agent.mctsagent.McGameNode;
import at.ac.tuwien.ifs.sge.agent.mctsagent.MctsAgent;
import at.ac.tuwien.ifs.sge.game.Game;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import at.ac.tuwien.ifs.sge.util.tree.Tree;

import java.util.concurrent.TimeUnit;

public class AustraliaFirstAgent extends MctsAgent<Risk, RiskAction> {


	@Override
	public RiskAction computeNextAction(Risk game, long computationTime, TimeUnit timeUnit) {
		return super.computeNextAction(game, computationTime, timeUnit);
	}



	@Override
	protected Tree<McGameNode<RiskAction>> mcSelection(Tree<McGameNode<RiskAction>> tree) {
		return super.mcSelection(tree);
	}

	@Override
	protected void mcExpansion(Tree<McGameNode<RiskAction>> tree) {
		super.mcExpansion(tree);
	}

	@Override
	protected boolean mcSimulation(Tree<McGameNode<RiskAction>> tree, int simulationsAtLeast, int proportion) {
		return super.mcSimulation(tree, simulationsAtLeast, proportion);
	}

	@Override
	protected boolean mcSimulation(Tree<McGameNode<RiskAction>> tree) {
		return super.mcSimulation(tree);
	}

	@Override
	protected boolean mcSimulation(Tree<McGameNode<RiskAction>> tree, long timeout) {
		return super.mcSimulation(tree, timeout);
	}

	@Override
	protected boolean mcHasWon(Game<RiskAction, ?> game) {
		return super.mcHasWon(game);
	}

	@Override
	protected void mcBackPropagation(Tree<McGameNode<RiskAction>> tree, boolean win) {
		super.mcBackPropagation(tree, win);
	}

	@Override
	protected double upperConfidenceBound(Tree<McGameNode<RiskAction>> tree, double c) {
		return super.upperConfidenceBound(tree, c);
	}

	@Override
	public String toString() {
		return "AustraliaFirstAgent";
	}
}
