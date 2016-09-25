package tests;

import static businessLogic.DeckHelper.deal;
import static businessLogic.DeckHelper.dealToPlayers;
import static businessLogic.TableHandler.compareHands;
import data.DealingDeck;
import data.Hand;
import data.Table;
import static ui.UI.printStandings;
import static ui.UI.printTest;

/**
 *
 * @author OnePoker UN
 */
public class HandAnalyserTest {

    public static void test() {

        Table table = new Table();
        dealToPlayers(table);
        DealingDeck dealer = table.getDealingDeck();
        Hand comunitario = table.getTableHand();
        //printDeck(dealer);
        deal(dealer, comunitario, 5);
        System.out.println("Comunitario: \n" + comunitario);
        //bestHand(table.getPlayerHand(0),comunitario);
        printStandings(table);
        compareHands(table);
        printStandings(table);
        printTest();
        System.out.println("\u2620");
        //printHands();
    }

}
