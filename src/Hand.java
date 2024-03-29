import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.IntStream;

import tiles.Bamboo;
import tiles.Circle;
import tiles.Character;
import tiles.Dragon;
import tiles.DragonType;
import tiles.Suit;
import tiles.Tile;
import tiles.Wind;
import tiles.WindType;

public class Hand {
    public static final int MAX_TILES = 13;
    public static final int FIRST_INDEX = 0;

    private ArrayList<Bamboo> bamboos;
    private ArrayList<Circle> circles;
    private ArrayList<Character> characters;
    private ArrayList<Dragon> dragons;
    private ArrayList<Wind> winds;

    public Hand() {
        bamboos = new ArrayList<>();
        circles = new ArrayList<>();
        characters = new ArrayList<>();
        dragons = new ArrayList<>();
        winds = new ArrayList<>();
    }

    private Hand(ArrayList<Bamboo> bamboos, ArrayList<Circle> circles,
                 ArrayList<Character> characters, ArrayList<Dragon> dragons, ArrayList<Wind> winds) {
        this.bamboos = bamboos;
        this.circles = circles;
        this.characters = characters;
        this.dragons = dragons;
        this.winds = winds;
    }

    private int count() {
        return bamboos.size() + circles.size() + characters.size() + dragons.size() + winds.size();
    }

    private boolean isEmpty() {
        return count() == 0;
    }

    private boolean contains(Tile t) {
        return bamboos.contains(t)
                || circles.contains(t)
                || characters.contains(t)
                || winds.contains(t)
                || dragons.contains(t);
    }

    public Hand add(Tile tile) {
        return editHand(tile, true);
    }

    public Hand remove(Tile tile) {
        return editHand(tile, false);
    }

    private Hand editHand(Tile tile, boolean isAdd) {
        ArrayList<Bamboo> newBamboos = new ArrayList<>(this.bamboos);
        ArrayList<Circle> newCircles = new ArrayList<>(this.circles);
        ArrayList<Character> newCharacters = new ArrayList<>(this.characters);
        ArrayList<Dragon> newDragons = new ArrayList<>(this.dragons);
        ArrayList<Wind> newWinds = new ArrayList<>(this.winds);
        if (tile instanceof Bamboo) {
            if (isAdd) {
                newBamboos.add((Bamboo) tile);
            } else {
                newBamboos.remove(tile);
            }
        } else if (tile instanceof Circle) {
            if (isAdd) {
                newCircles.add((Circle) tile);
            } else {
                newCircles.remove(tile);
            }
        } else if (tile instanceof Character) {
            if (isAdd) {
                newCharacters.add((Character) tile);
            } else {
                newCharacters.remove(tile);
            }
        } else if (tile instanceof Dragon) {
            if (isAdd) {
                newDragons.add((Dragon) tile);
            } else {
                newDragons.remove(tile);
            }
        } else if (tile instanceof Wind) {
            if (isAdd) {
                newWinds.add((Wind) tile);
            } else {
                newWinds.remove(tile);
            }
        }
        return new Hand(newBamboos, newCircles, newCharacters, newDragons, newWinds).reOrder();
    }

    public Hand reOrder() {
        ArrayList<Bamboo> newBamboos = new ArrayList<>(this.bamboos);
        Collections.sort(newBamboos);
        ArrayList<Circle> newCircles = new ArrayList<>(this.circles);
        Collections.sort(newCircles);
        ArrayList<Character> newCharacters = new ArrayList<>(this.characters);
        Collections.sort(newCharacters);
        ArrayList<Dragon> newDragons = new ArrayList<>(this.dragons);
        Collections.sort(newDragons);
        ArrayList<Wind> newWinds = new ArrayList<>(this.winds);
        Collections.sort(newWinds);

        return new Hand(newBamboos, newCircles, newCharacters, newDragons, newWinds);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        this.bamboos.forEach(bamboo -> result.append(bamboo + " "));
        this.circles.forEach(circle -> result.append(circle + " "));
        this.characters.forEach(character -> result.append(character + " "));
        this.winds.forEach(winds -> result.append(winds + " "));
        this.dragons.forEach(dragon -> result.append(dragon + " "));
        return result.toString();
    }

    public static Hand createSampleHand() {
        Hand hand = new Hand();
        hand = hand.add(new Character(1));
        hand = hand.add(new Character(1));
        hand = hand.add(new Character(1));
        hand = hand.add(new Character(2));
        hand = hand.add(new Character(3));
        hand = hand.add(new Character(4));
        hand = hand.add(new Character(5));
        hand = hand.add(new Character(6));
        hand = hand.add(new Character(7));
        hand = hand.add(new Character(8));
        hand = hand.add(new Character(9));
        hand = hand.add(new Character(9));
        hand = hand.add(new Character(9));
        hand = hand.add(new Character(9));
        return hand.reOrder();
    }

    public ArrayList<Tile> solve() {
        assert count() >= 13;
        ArrayList<Tile> remainingTilesOnTable = getRemainingTiles(this);
        ArrayList<Tile> winningTiles = new ArrayList<>();
        Hand originalHand = this;
        if (originalHand.isWinningHand()) {
            winningTiles.add(null);
        }
        for (Tile t : remainingTilesOnTable) {
            Hand newHand = originalHand.add(t);
            if (newHand.isWinningHand()) {
                winningTiles.add(t);
            }
        }
        return winningTiles;
    }

    private boolean isWinningHand() {
        assert count() >= 14;
        Hand currentHand = this;
        // 0 tai also considered win
        ArrayList<Tile> eyes = this.extractEyes();
        boolean isWinning = false;
        for (Tile t : eyes) {
            Hand newHand = currentHand.remove(t);
            newHand = newHand.remove(t);
            isWinning = assertInSets(newHand);
            if (isWinning) {
                break;
            }
        }
        return isWinning;
    }

    private static boolean isSameHand(Hand hand1, Hand hand2) {
        return hand1.equals(hand2);
    }

    private static boolean assertInSets(Hand hand) {
        if (hand.isEmpty()) {
            return true;
        }
        Hand noKong = extractKong(hand);
        Hand noChi = extractChi(hand);
        Hand noPong = extractPong(hand);
        boolean kongWin = false;
        boolean chiWin = false;
        boolean pongWin = false;
        if (!isSameHand(noKong, hand)) {
            kongWin = assertInSets(noKong);
        }
        if (!isSameHand(noChi, hand)) {
            chiWin = assertInSets(noChi);
        }
        if (!isSameHand(noPong, hand)) {
            pongWin = assertInSets(noPong);
        }
        return kongWin || chiWin || pongWin;
    }

    private static Hand extractChi(Hand hand) {
        hand = hand.reOrder();
        Tile firstTile = hand.getFirstTile();
        if (firstTile instanceof Wind || firstTile instanceof Dragon) {
            return hand;
        }
        return removeConsecutiveTiles(hand, firstTile);
    }

    private static Hand removeConsecutiveTiles(Hand hand, Tile t) {
        try {
            Hand newHand = hand;

            Suit firstTile = (Suit) t;
            Suit secondTile = firstTile.getNextTile();
            Suit thirdTile = secondTile.getNextTile();

            if (!hand.contains(firstTile)) {
                return hand;
            }
            if (!hand.contains(secondTile)) {
                return hand;
            }
            if (!hand.contains(thirdTile)) {
                return hand;
            }

            newHand = newHand.remove(firstTile);
            newHand = newHand.remove(secondTile);
            newHand = newHand.remove(thirdTile);

            return newHand;
        } catch (IllegalArgumentException e) {
            return hand;
        }
    }

    private static Hand extractPong(Hand hand) {
        hand = hand.reOrder();
        Tile firstTile = hand.getFirstTile();
        if (hand.containsCount(firstTile, 3)) {
            return removeThree(hand, firstTile);
        }
        return hand;
    }

    private static Hand removeThree(Hand hand, Tile t) {
        return remove(hand, t, 3);
    }

    private static Hand removeFour(Hand hand, Tile t) {
        return remove(hand, t, 4);
    }

    private static Hand remove(Hand hand, Tile t, int count) {
        Hand newHand = hand;
        if (hand.containsCount(t, count)) {
            for (int i = 0; i < count; i++) {
                newHand = newHand.remove(t);
            }
        }
        return newHand;
    }

    private Tile getFirstTile() throws IllegalArgumentException {
        assert this.count() > 0;
        if (bamboos.size() > 0) {
            return bamboos.get(FIRST_INDEX);
        }
        if (circles.size() > 0) {
            return circles.get(FIRST_INDEX);
        }
        if (characters.size() > 0) {
            return characters.get(FIRST_INDEX);
        }
        if (winds.size() > 0) {
            return winds.get(FIRST_INDEX);
        }
        if (dragons.size() > 0) {
            return dragons.get(FIRST_INDEX);
        }
        throw new IllegalArgumentException();
    }

    private static Hand extractKong(Hand hand) {
        Hand newHand = hand;
        for (Tile t : hand.bamboos) {
           newHand = removeFour(newHand, t);
           if (!isSameHand(newHand, hand)) {
               return newHand;
           }
        }
        for (Tile t : hand.circles) {
            newHand = removeFour(newHand, t);
            if (!isSameHand(newHand, hand)) {
                return newHand;
            }
        }
        for (Tile t : hand.characters) {
            newHand = removeFour(newHand, t);
            if (!isSameHand(newHand, hand)) {
                return newHand;
            }
        }
        for (Tile t : hand.winds) {
            newHand = removeFour(newHand, t);
            if (!isSameHand(newHand, hand)) {
                return newHand;
            }
        }
        for (Tile t : hand.dragons) {
            newHand = removeFour(newHand, t);
            if (!isSameHand(newHand, hand)) {
                return newHand;
            }
        }
        return newHand;
    }


    private boolean containsCount(Tile t, int count) {
        if (t instanceof Bamboo) {
            return assertHasCountInCollection(bamboos, t, count);
        }
        if (t instanceof Circle) {
            return assertHasCountInCollection(circles, t, count);
        }
        if (t instanceof Character) {
            return assertHasCountInCollection(characters, t, count);
        }
        if (t instanceof Wind) {
            return assertHasCountInCollection(winds, t, count);
        }
        if (t instanceof Dragon) {
            return assertHasCountInCollection(dragons, t, count);
        }
        return false;
    }

    private static boolean assertHasCountInCollection(ArrayList<? extends Tile> tiles, Tile t, int count) {
        return tiles
                .stream()
                .filter(tile -> tile.equals(t))
                .count() == count;
    }

    private ArrayList<Tile> extractEyes() {
        HashSet<Tile> noDuplicates = new HashSet<>();
        addAllTilesIntoCollection(noDuplicates);

        ArrayList<Tile> hand = new ArrayList<>();
        addAllTilesIntoCollection(hand);

        noDuplicates.forEach(tile -> hand.remove(tile));

        HashSet<Tile> handNoDuplicates = new HashSet<>(hand);
        return new ArrayList<>(handNoDuplicates);
    }

    private void addAllTilesIntoCollection(Collection<Tile> collection) {
        collection.addAll(this.characters);
        collection.addAll(this.dragons);
        collection.addAll(this.circles);
        collection.addAll(this.bamboos);
        collection.addAll(this.winds);

    }

    private static ArrayList<Tile> getRemainingTiles(Hand hand) {
        ArrayList<Tile> tiles = getDefaultTiles();
        hand.bamboos.forEach(tiles::remove);
        hand.circles.forEach(tiles::remove);
        hand.characters.forEach(tiles::remove);
        hand.winds.forEach(tiles::remove);
        hand.dragons.forEach(tiles::remove);
        HashSet<Tile> set = new HashSet<>(tiles); //remove duplicates
        return new ArrayList<>(set);
    }

    private static ArrayList<Tile> getDefaultTiles() {
        ArrayList<Tile> tiles = new ArrayList<>();

        addAllSuits(tiles);
        addFourOfEachDragon(tiles);

        return tiles;
    }

    private static void addAllSuits(ArrayList<Tile> tiles) {
        IntStream.rangeClosed(1, 9).forEach(value -> {
            addFourOfEachSuit(tiles, value);
        });
    }

    private static void addFourOfEachSuit(ArrayList<Tile> tiles, int value) {
        IntStream.rangeClosed(1, 4).forEach(i -> {
            tiles.add(new Bamboo(value));
            tiles.add(new Circle(value));
            tiles.add(new Character((value)));
        });
    }

    private static void addFourOfEachDragon(ArrayList<Tile> tiles) {
        IntStream.rangeClosed(1, 4).forEach(i -> {
            tiles.add(new Wind(WindType.NORTH));
            tiles.add(new Wind(WindType.SOUTH));
            tiles.add(new Wind(WindType.EAST));
            tiles.add(new Wind(WindType.WEST));

            tiles.add(new Dragon(DragonType.RED));
            tiles.add(new Dragon(DragonType.GREEN));
            tiles.add(new Dragon(DragonType.WHITE));
        });
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Hand)) {
            return false;
        }
        Hand otherHand = (Hand) other;
        otherHand.reOrder();
        this.reOrder();

        return this.bamboos.equals(otherHand.bamboos)
                && this.circles.equals(otherHand.circles)
                && this.characters.equals(otherHand.characters)
                && this.winds.equals(otherHand.winds)
                && this.dragons.equals(otherHand.dragons);

    }

}
