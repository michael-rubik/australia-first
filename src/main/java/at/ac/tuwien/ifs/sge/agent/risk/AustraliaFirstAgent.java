package at.ac.tuwien.ifs.sge.agent.risk;

import at.ac.tuwien.ifs.sge.agent.AbstractGameAgent;
import at.ac.tuwien.ifs.sge.agent.GameAgent;
import at.ac.tuwien.ifs.sge.agent.mctsagent.McGameNode;
import at.ac.tuwien.ifs.sge.engine.Logger;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskBoard;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskTerritory;
import at.ac.tuwien.ifs.sge.game.risk.configuration.RiskTerritoryConfiguration;
import at.ac.tuwien.ifs.sge.util.Util;
import at.ac.tuwien.ifs.sge.util.tree.DoubleLinkedTree;
import at.ac.tuwien.ifs.sge.util.tree.Tree;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AustraliaFirstAgent extends AbstractGameAgent<Risk, RiskAction> implements
		GameAgent<Risk, RiskAction> {


	private static final int MAX_PRINT_THRESHOLD = 97;
	private static int INSTANCE_NR_COUNTER = 1;

	private final int instanceNr;


	private static Set<RiskTerritoryConfiguration> AUSTRALIA_TERRITORIES_CONFIG = Set.of(
			RiskTerritoryConfiguration.EASTERN_AUSTRALIA,
			RiskTerritoryConfiguration.WESTERN_AUSTRALIA,
			RiskTerritoryConfiguration.INDONESIA,
			RiskTerritoryConfiguration.NEW_GUINEA);

	private static Set<Integer> AUSTRALIA_TERRITORIES_IDS = AUSTRALIA_TERRITORIES_CONFIG.stream().
			map(RiskTerritoryConfiguration::getTerritoryId).collect(Collectors.toSet());

	private static Set<RiskTerritory> AUSTRALIA_TERRITORIES = AUSTRALIA_TERRITORIES_CONFIG.stream().
			map(RiskTerritoryConfiguration::getTerritory).collect(Collectors.toSet());


	private final double exploitationConstant;
	private Comparator<Tree<McGameNode<RiskAction>>> gameMcTreeUCTComparator;
	private Comparator<Tree<McGameNode<RiskAction>>> gameMcTreeSelectionComparator;
	private Comparator<Tree<McGameNode<RiskAction>>> gameMcTreePlayComparator;
	private Comparator<McGameNode<RiskAction>> gameMcNodePlayComparator;
	private Comparator<Tree<McGameNode<RiskAction>>> gameMcTreeWinComparator;
	private Comparator<McGameNode<RiskAction>> gameMcNodeWinComparator;
	private Comparator<Tree<McGameNode<RiskAction>>> gameMcTreeMoveComparator;
	private Comparator<McGameNode<RiskAction>> gameMcNodeMoveComparator;
	private Comparator<McGameNode<RiskAction>> gameMcNodeGameComparator;
	private Comparator<Tree<McGameNode<RiskAction>>> gameMcTreeGameComparator;

	private Tree<McGameNode<RiskAction>> mcTree;

	public AustraliaFirstAgent() {
		this(null);
	}

	public AustraliaFirstAgent(Logger log) {
		this(Math.sqrt(2), log);
	}

	public AustraliaFirstAgent(double exploitationConstant, Logger log) {
		super(log);
		this.exploitationConstant = (1 + Math.sqrt(5)) / 2;
		mcTree = new DoubleLinkedTree<>();
		instanceNr = INSTANCE_NR_COUNTER++;
	}

	@Override
	public void setUp(int numberOfPlayers, int playerId) {
		super.setUp(numberOfPlayers, playerId);
		mcTree.clear();
		mcTree.setNode(new McGameNode<>());

		gameMcTreeUCTComparator = Comparator
				.comparingDouble(t -> upperConfidenceBound(t, exploitationConstant));

		gameMcNodePlayComparator = Comparator.comparingInt(McGameNode::getPlays);
		gameMcTreePlayComparator = (o1, o2) -> gameMcNodePlayComparator
				.compare(o1.getNode(), o2.getNode());

		gameMcNodeWinComparator = Comparator.comparingInt(McGameNode::getWins);
		gameMcTreeWinComparator = (o1, o2) -> gameMcNodeWinComparator
				.compare(o1.getNode(), o2.getNode());

		gameMcNodeGameComparator = (o1, o2) -> gameComparator.compare(o1.getGame(), o2.getGame());
		gameMcTreeGameComparator = (o1, o2) -> gameMcNodeGameComparator
				.compare(o1.getNode(), o2.getNode());

		gameMcTreeSelectionComparator = gameMcTreeUCTComparator.thenComparing(gameMcTreeGameComparator);

		gameMcNodeMoveComparator = gameMcNodePlayComparator.thenComparing(gameMcNodeWinComparator)
				.thenComparing(gameMcNodeGameComparator);
		gameMcTreeMoveComparator = (o1, o2) -> gameMcNodeMoveComparator
				.compare(o1.getNode(), o2.getNode());
	}

	@Override
	public RiskAction computeNextAction(Risk game, long computationTime, TimeUnit timeUnit) {
		super.setTimers(computationTime, timeUnit);

		log.tra_("Searching for root of tree");
		boolean foundRoot = Util.findRoot(mcTree, game);
		if (foundRoot) {
			log._trace(", done.");
		} else {
			log._trace(", failed.");
		}

		log.tra_("Check if best move will eventually end game: ");
		if (sortPromisingCandidates(mcTree, gameMcNodeMoveComparator.reversed())) {
			log._trace("Yes");
			return Collections.max(mcTree.getChildren(), gameMcTreeMoveComparator).getNode().getGame()
					.getPreviousAction();
		}
		log._trace("No");

		int looped = 0;

		log.debf_("MCTS with %d simulations at confidence %.1f%%", mcTree.getNode().getPlays(),
				Util.percentage(mcTree.getNode().getWins(), mcTree.getNode().getPlays()));

		int printThreshold = 1;

		while (!shouldStopComputation()) {

			if (looped++ % printThreshold == 0) {
				log._deb_("\r");
				log.debf_("MCTS with %d simulations at confidence %.1f%%", mcTree.getNode().getPlays(),
						Util.percentage(mcTree.getNode().getWins(), mcTree.getNode().getPlays()));
			}
			Tree<McGameNode<RiskAction>> tree = mcTree;

			tree = mcSelection(tree);
			mcExpansion(tree);
			boolean won = mcSimulation(tree, 128, 2);
			mcBackPropagation(tree, won);

			if (printThreshold < MAX_PRINT_THRESHOLD) {
				printThreshold = Math.max(1, Math.min(MAX_PRINT_THRESHOLD,
						Math.round(mcTree.getNode().getPlays() * 11.1111111111F)));
			}

		}

		long elapsedTime = Math.max(1, System.nanoTime() - START_TIME);
		log._deb_("\r");
		log.debf_("MCTS with %d simulations at confidence %.1f%%", mcTree.getNode().getPlays(),
				Util.percentage(mcTree.getNode().getWins(), mcTree.getNode().getPlays()));
		log._debugf(
				", done in %s with %s/simulation.",
				Util.convertUnitToReadableString(elapsedTime,
						TimeUnit.NANOSECONDS, timeUnit),
				Util.convertUnitToReadableString(elapsedTime / Math.max(1, mcTree.getNode().getPlays()),
						TimeUnit.NANOSECONDS,
						TimeUnit.NANOSECONDS));

		if (mcTree.isLeaf()) {
			log._debug(". Could not find a move, choosing the next best greedy option.");
			return Collections.max(game.getPossibleActions(),
					(o1, o2) -> gameComparator.compare(game.doAction(o1), game.doAction(o2)));
		}

		return Collections.max(mcTree.getChildren(), gameMcTreeMoveComparator).getNode().getGame()
				.getPreviousAction();
	}

	protected boolean sortPromisingCandidates(Tree<McGameNode<RiskAction>> tree,
	                                          Comparator<McGameNode<RiskAction>> comparator) {
		boolean isDetermined = true;
		while (!tree.isLeaf() && isDetermined) {
			isDetermined = tree.getChildren().stream()
					.allMatch(c -> c.getNode().getGame().getCurrentPlayer() >= 0);
			if (tree.getNode().getGame().getCurrentPlayer() == playerId) {
				tree.sort(comparator);
			} else {
				tree.sort(comparator.reversed());
			}
			tree = tree.getChild(0);
		}
		return isDetermined && tree.getNode().getGame().isGameOver();
	}


	protected Tree<McGameNode<RiskAction>> mcSelection(Tree<McGameNode<RiskAction>> tree) {
		int depth = 0;
		while (!tree.isLeaf() && (depth++ % 31 != 0 || !shouldStopComputation())) {
			List<Tree<McGameNode<RiskAction>>> children = new ArrayList<>(tree.getChildren());
			if (tree.getNode().getGame().getCurrentPlayer() < 0) {
				RiskAction action = tree.getNode().getGame().determineNextAction();
				for (Tree<McGameNode<RiskAction>> child : children) {
					if (child.getNode().getGame().getPreviousAction().equals(action)) {
						tree = child;
						break;
					}
				}
			} else {
				tree = Collections.max(children, gameMcTreeSelectionComparator);
			}
		}
		return tree;
	}

	protected void mcExpansion(Tree<McGameNode<RiskAction>> tree) {
		if (tree.isLeaf()) {
			Risk game = (Risk) tree.getNode().getGame();

			RiskBoard board = game.getBoard();
			Set<Integer> occupiedTerritories = board.getTerritoriesOccupiedByPlayer(playerId);

			Set<RiskAction> possibleActions = game.getPossibleActions();

			if(!hasOccupiedAustralia(occupiedTerritories)) {
				possibleActions = reconquerAustralia(possibleActions, occupiedTerritories);
			} else {
				possibleActions = useMaximumTroops(possibleActions);
			}

			int i = 0;

			for (RiskAction possibleAction : possibleActions) {
				tree.add(new McGameNode<>(game, possibleAction));
			}
		}
	}

	private Set<RiskAction> useMaximumTroops(Set<RiskAction> possibleActions) {
		int max = 0;

		for(RiskAction action : possibleActions) {
			if (action.troops() >= 0){
				max = action.troops();
			}
		}
		final int maximum = max;

		possibleActions = possibleActions.stream().filter(a ->
				a.troops() == maximum).collect(Collectors.toSet());

		return possibleActions;
	}

	private Set<RiskAction> reconquerAustralia(Set<RiskAction> possibleActions, Set<Integer> occupiedTerritories) {

		possibleActions = possibleActions.stream().filter(a ->
				AUSTRALIA_TERRITORIES_IDS.contains(a.selected()) ||
						hasOccupiedAustralia(occupiedTerritories)).collect(Collectors.toSet());

		return possibleActions;
	}

	protected boolean mcSimulation(Tree<McGameNode<RiskAction>> tree, int simulationsAtLeast, int proportion) {
		int simulationsDone = tree.getNode().getPlays();
		if (simulationsDone < simulationsAtLeast && shouldStopComputation(proportion)) {
			int simulationsLeft = simulationsAtLeast - simulationsDone;
			return mcSimulation(tree, nanosLeft() / simulationsLeft);
		} else if (simulationsDone == 0) {
			return mcSimulation(tree, TIMEOUT / 2L - nanosElapsed());
		}

		return mcSimulation(tree);
	}

	protected boolean mcSimulation(Tree<McGameNode<RiskAction>> tree) {
		Risk game = (Risk) tree.getNode().getGame();

		int depth = 0;
		while (!game.isGameOver() && (depth++ % 31 != 0 || !shouldStopComputation())) {

			if (game.getCurrentPlayer() < 0) {
				game = (Risk) game.doAction();
			} else {
				game = (Risk) game.doAction(Util.selectRandom(game.getPossibleActions(), random));
			}

		}

		return mcHasWon(game);
	}

	protected boolean mcSimulation(Tree<McGameNode<RiskAction>> tree, long timeout) {
		long startTime = System.nanoTime();
		Risk game = (Risk) tree.getNode().getGame();

		int depth = 0;
		while (!game.isGameOver() && (System.nanoTime() - startTime <= timeout) && (depth++ % 31 != 0
				|| !shouldStopComputation())) {

			if (game.getCurrentPlayer() < 0) {
				game = (Risk) game.doAction();
			} else {
				game = (Risk) game.doAction(Util.selectRandom(game.getPossibleActions(), random));
			}

		}

		return mcHasWon(game);
	}

	protected boolean mcHasWon(Risk game) {
		double[] evaluation = game.getGameUtilityValue();
		double score = Util.scoreOutOfUtility(evaluation, playerId);
		if (!game.isGameOver() && score > 0) {
			evaluation = game.getGameHeuristicValue();
			score = Util.scoreOutOfUtility(evaluation, playerId);
		}

		boolean win = score == 1D;
		boolean tie = score > 0;

		win = win || (tie && random.nextBoolean());

		return win;
	}


	protected void mcBackPropagation(Tree<McGameNode<RiskAction>> tree, boolean win) {
		int depth = 0;
		while (!tree.isRoot() && (depth++ % 31 != 0 || !shouldStopComputation())) {
			tree = tree.getParent();
			tree.getNode().incPlays();
			if (win) {
				tree.getNode().incWins();
			}
		}
	}

	protected double upperConfidenceBound(Tree<McGameNode<RiskAction>> tree, double c) {
		double w = tree.getNode().getWins();
		double n = Math.max(tree.getNode().getPlays(), 1);
		double N = n;
		if (!tree.isRoot()) {
			N = tree.getParent().getNode().getPlays();
		}

		return (w / n) + c * Math.sqrt(Math.log(N) / n);
	}

	private  boolean hasOccupiedTerritories(Set<Integer> occupiedTerritories, Integer... territoryIds){
		return hasOccupiedTerritories(occupiedTerritories, Arrays.asList(territoryIds));
	}

	private boolean hasOccupiedTerritories(Set<Integer> occupiedTerritories, Collection<Integer> territoryIds){
		return occupiedTerritories.containsAll(territoryIds);
	}

	private boolean hasOccupiedAustralia(Set<Integer> occupiedTerritories){
		return hasOccupiedTerritories(occupiedTerritories, AUSTRALIA_TERRITORIES_IDS);
	}


	@Override
	public String toString() {
		if (instanceNr > 1 || AustraliaFirstAgent.INSTANCE_NR_COUNTER > 2) {
			return String.format("%s%d", "AustraliaFirstAgent#", instanceNr);
		}
		return "AustraliaFirstAgent";
	}
}
