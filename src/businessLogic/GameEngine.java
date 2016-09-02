/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package businessLogic;

import static businessLogic.RoundHandler.*;
import data.*;
import java.util.Random;
import static ui.UI.*;

/**
 *
 * @author OnePoker UN
 */
public class GameEngine {

    public static final Random RND = new Random();
    private static GameEngine instance = null;
    private static Tournament tournament;

    private GameEngine() {
    }

    private static GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //startGame();
        test();
    }

    private static void startGame() {
        printWelcome();
        tournament = new Tournament();
        playRound(tournament.getRound());
    }

    private static void test() {
        HandAnalyserTest.test();
    }

}
