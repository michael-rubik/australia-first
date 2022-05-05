package at.ac.tuwien.ifs.sge.agent.risk;

import at.ac.tuwien.ifs.sge.agent.risk.RiskAgent;
import at.ac.tuwien.ifs.sge.engine.Logger;
import at.ac.tuwien.ifs.sge.game.Game;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskBoard;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.ac.tuwien.ifs.sge.game.risk.board.Risk;

public class Tests {

    Logger log = new Logger(-2, "", "", "", System.out, "",
            "", System.out, "", "", System.out, "", "",
            System.err, "", "", System.err, ""
    );

    @Test
    public void testAdd() {
        assertEquals(42, Integer.sum(19, 23));
    }

    @Test
    public void testDivide() {
        assertThrows(ArithmeticException.class, () -> {
            Integer.divideUnsigned(42, 0);
        });
    }


    @Test
    public void text_example() {
        Risk exampleGame = new Risk();
        RiskAgent agent = new RiskAgent(log);
        // Bring game and agent to the required state
        RiskAction action = agent.computeNextAction(exampleGame, 30, TimeUnit.SECONDS);
        Risk next = (Risk) exampleGame.doAction(action);
        //Test if agent behaves as expected

    }
}
