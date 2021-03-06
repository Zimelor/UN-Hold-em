package business;

import static business.DeckFactory.cloneHand;
import static business.DeckFactory.createHand;
import data.Card;
import data.Hand;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author OnePoker UN Estudiante
 */
public class HandHelper {

    /**
     * Reference Author @subskybox See
     * <a href = "http://www.codeproject.com/Articles/569271/A-Poker-hand-analyzer-in-JavaScript-using-bit-math"></a>
     *
     * Sets rankName and rank of the input hand, given it is a 5 cards Hand
     *
     * //0x403c Ace low Straight //(s / (s & -s) == 31) Straight //0x7c00
     * RoyalFlush
     *
     * @param hand
     * @return
     */
    public static Hand.HandRank rankHand(Hand hand) {
        int[] ranks = hand.getCardRanks();
        int[] suits = hand.getCardSuits();
        long s = 0, v = 0, o;
        for (int i = 0; i < 5; i++) {
            s += 1 << ranks[i];
            o = (long) Math.pow(2, (ranks[i] - 2) * 4);
            v += o * (((v / o) & 15) + 1);
        }
        v = v % 15 - ((s / (s & -s) == 31) || (s == 0x403c) ? 3 : 1);
        v -= (allEqual(suits) ? 1 : 0) * ((s == 0x7c00) ? -5 : 1);
        switch ((int) v) {
            case 4: {
                return Hand.HandRank.HIGHCARD;
            }
            case 5: {
                return Hand.HandRank.PAIR;
            }
            case 6: {
                return Hand.HandRank.TWOPAIR;
            }
            case 7: {
                return Hand.HandRank.ROYAL;
            }
            case 2: {
                return Hand.HandRank.STRAIGHT;
            }
            case 3: {
                return Hand.HandRank.FLUSH;
            }
            case 9: {
                return Hand.HandRank.FULLHOUSE;
            }
            case 0: {
                return Hand.HandRank.FOUR;
            }
            case 1: {
                return Hand.HandRank.STRAIGHTFLUSH;
            }
            case 8: {
                return Hand.HandRank.THREE;
            }
            default: {
                throw new Error();
            }
        }
    }

    public static boolean allEqual(int[] x) {
        int first = x[0];
        for (int i = 0; i < x.length; i++) {
            if (x[i] != first) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>
     * See :
     * <a href = "http://stackoverflow.com/questions/33859993/get-all-possible-5-card-poker-hands-given-7-cards"></a>
     * for the combinatorics Uses {@link data.Hand#compareTo(data.Hand) }
     * Uses {@link  businessLogic.HandComparator#compare(data.Hand, data.Hand)}
     * </p>
     *
     * @param playerHand the player Hand
     * @param comunitary the tableHand
     * @return bestHand the best hand that can be obtained from
     */
    public static Hand bestHand(Hand playerHand, Hand comunitary) {
        Hand merge = createHand("array");
        Hand bestHand = comunitary;
        bestHand.setRank(rankHand(bestHand));
        merge.addAll(playerHand);
        merge.addAll(comunitary);

        Hand temp = createHand("linked");
        temp.addAll(comunitary);
        int cardSelected = 0;
        for (int firstCard = 0; firstCard < 7; firstCard++) {
            for (int secondCard = firstCard + 1; secondCard < 7; secondCard++) {
                // every card that is not the first or second will added to the hand
                for (int i = 0; i < 7; i++) {
                    if (i != firstCard && i != secondCard) {
                        temp.set(cardSelected++, merge.getCard(i));
                    }
                }
                Collections.sort(temp.getCards());
                temp.setRank(rankHand(temp));
                bestHand = compare(bestHand, temp) > 0 ? bestHand : cloneHand(temp);
                cardSelected = 0;
            }
        }
        return bestHand;
    }

    /**
     * Only works for a ordered hand
     *
     * @param hand
     * @return
     */
    public static boolean isSuitedConnector(Hand hand) {
        if (hand.size() == 2) {
            int[] suits = hand.getCardSuits();
            int[] ranks = hand.getCardRanks();
            return (suits[0] == suits[1]) && ((ranks[0] == (ranks[1] - 1)));
        } else {
            throw new IllegalArgumentException("Suited connectors are only for two cards");
        }
    }

    /**
     *
     * @param hand the first hand to compare
     * @param anotherHand the second hand to compare
     * @return 1 if hand ranks higher than anotherHand, 0 if both rank equal, -1
     * if anotherHand ranks higher
     */
    public static int compare(Hand hand, Hand anotherHand) {
        int out;
        out = Integer.compare(hand.getRank().getValue(), anotherHand.getRank().getValue());
        if (out == 0) {
            switch (hand.getRank()) {
                // 1 pair
                case HIGHCARD:
                //2 pair
                case PAIR:
                case TWOPAIR:
                    return comparePair(hand, anotherHand);
                case THREE:
                    return compareThree(hand, anotherHand);
                case FULLHOUSE:
                    return compareThree(hand, anotherHand);
                default:
                    int thisHighCard = highCard(hand);
                    int anotherHighCard = highCard(anotherHand);
                    out = Integer.compare(thisHighCard, anotherHighCard);
                    if (out == 0) {
                        return compareKickers(hand, anotherHand, thisHighCard);
                    }
            }
        }
        return out;
    }

    /**
     *
     * @param hand the hand
     * @return highest ranking card in hand
     */
    public static int highCard(Hand hand) {
        int maxCard = hand.getCard(0).getValue();
        for (int i = 1; i < hand.size(); i++) {
            maxCard = hand.getCard(i).getValue() > maxCard ? hand.getCard(i).getValue() : maxCard;
        }
        return maxCard;
    }

    /**
     * Only works for a sorted hand
     *
     * @param hand the hand
     * @param sizeFilter the position that have been checked
     * @return highestPair in hand
     */
    public static int highestPair(Hand hand, int sizeFilter) {
        if (sizeFilter == 2) {
            return hand.getCard(0).getValue();
        } else if (hand.getCard(sizeFilter - 1).getValue() == hand.getCard(sizeFilter - 2).getValue()) {
            return hand.getCard(sizeFilter - 1).getValue();
        } else {
            return highestPair(hand, sizeFilter - 1);
        }
    }

    /**
     * NOTES/ TO DO: Create unit tests for highestPair and highestThree there
     * may be an error
     */
    /**
     * Only works for a sorted hand
     *
     * @param hand the hand
     * @param sizeFilter the position that have been checked
     * @return highest three of a kind in hand
     */
    public static int highestThree(Hand hand, int sizeFilter) {
        if (sizeFilter == 3) {
            return hand.getCard(0).getValue();
        } else if (hand.getCard(sizeFilter - 1).getValue() == hand.getCard(sizeFilter - 2).getValue() && hand.getCard(sizeFilter - 1).getValue() == hand.getCard(sizeFilter - 3).getValue()) {
            return hand.getCard(sizeFilter - 1).getValue();
        } else {
            return highestThree(hand, sizeFilter - 1);
        }
    }

    /**
     * hand and anotherHand rankName is "1 Pair" or "2 Pair"
     *
     * @param hand
     * @param anotherHand
     * @return 1 if hand ranks higher than anotherHand, 0 if both rank equal, -1
     * if anotherHand ranks higher
     */
    public static int comparePair(Hand hand, Hand anotherHand) {
        int out;
        int thisPair = highestPair(hand, hand.size());
        int anotherPair = highestPair(anotherHand, hand.size());
        out = Integer.compare(thisPair, anotherPair);
        if (out == 0) {
            if (hand.getRank() == Hand.HandRank.TWOPAIR) {
                thisPair = highestPair(hand, 3);
                anotherPair = highestPair(anotherHand, 3);
                out = Integer.compare(thisPair, anotherPair);
                if (out == 0) {
                    return compareKickers(hand, anotherHand, thisPair);
                }
            } else {
                out = compareKickers(hand, anotherHand, thisPair);
            }
        }

        return out;
    }

    /**
     * hand and anotherHand rankName is "Three of a kind"
     *
     * @param hand
     * @param anotherHand
     * @return 1 if hand ranks higher than anotherHand, 0 if both rank equal, -1
     * if anotherHand ranks higher
     */
    public static int compareThree(Hand hand, Hand anotherHand) {
        int out;
        int thisThree = highestThree(hand, hand.size());
        int anotherThree = highestThree(anotherHand, hand.size());
        out = Integer.compare(thisThree, anotherThree);
        if (out == 0) {
            return compareKickers(hand, anotherHand, thisThree);
        }
        return out;
    }

    /**
     *
     * @param hand the hand
     * @param anotherHand
     * @param startingKicker
     * @return 1 if hand kickers rank higher than anotherHand, 0 if both hands
     * kickers rank equal, -1 if anotherHand kickers rank higher
     */
    public static int compareKickers(Hand hand, Hand anotherHand, int startingKicker) {
        ArrayList<Integer> foo = new ArrayList<>();
        foo.add(startingKicker);
        ArrayList<Integer> handKickers = (ArrayList) foo.clone();
        ArrayList<Integer> anotherKickers = (ArrayList) foo.clone();
        int out = 0;
        while (out == 0) {
            out = Integer.compare(kicker(hand, handKickers), kicker(anotherHand, anotherKickers));
            if (handKickers.get(handKickers.size() - 1) == -1 || anotherKickers.get(anotherKickers.size() - 1) == -1) {
                break;
            }
        }
        return out;
    }

    /**
     *
     * @param hand
     * @param filter
     * @return highest next kicker that is not in filter
     */
    public static int kicker(Hand hand, List<Integer> filter) {
        //if kicker returns -1 then no comparation was made, then compareKickers should terminate
        int kicker = -1;
        for (int i = 0; i < hand.size(); i++) {
            Card temp = hand.getCard(i);
            if (temp.getValue() > kicker && !filter.contains(temp.getValue())) {
                kicker = temp.getValue();
            }
        }
        filter.add(kicker);
        return kicker;
    }
}
