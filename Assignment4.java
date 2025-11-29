import java.util.HashMap;
import java.util.Map;

public class Assignment4 {
    public static void main(String[] args) {

    }
}

enum Direction {
    N("North"),
    E("East"),
    S("South"),
    W("West"),
    NE("North-East"),
    SE("South-East"),
    SW("South-West"),
    NW("North-West");

    private final String textRepresentation;
    private Direction(String text) {
        this.textRepresentation = text;
    }

    public String getTextRepresentation() {
        return textRepresentation;
    }
}

class Board {
    private final Map<String, BoardEntity> boardData;
    private final int size;

    public Board(int boardSize) {
        this.size = boardSize;
        this.boardData = new HashMap<>();
    }

    private String toKey(EntityPosition position) {
        return position.getX() + "," + position.getY();
    }

    public void addEntity(BoardEntity entity) {
        boardData.put(toKey(entity.getEntityPosition()), entity);
    }

    public BoardEntity getEntity(EntityPosition position) {
        return boardData.get(toKey(position));
    }

    public Direction getDirection(Insect insect) {
        return insect.getBestDirection(boardData, size);
    }

    public int getDirectionSum(Insect insect) {
        Direction dir = getDirection(insect);
        return insect.travelDirection(dir, boardData, size);
    }

}

class EntityPosition {
    private int x;
    private int y;

    public EntityPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

enum InsectColor {
    RED,
    GREEN,
    BLUE,
    YELLOW;

    public static InsectColor toColor(String s) {
        switch (s) {
            case "Red":
                return RED;
            case "Green":
                return GREEN;
            case "Blue":
                return BLUE;
            case "Yellow":
                return YELLOW;
            default:
                throw new InvalidInsectColorException();
        }
    }
}

abstract class BoardEntity {
    protected EntityPosition entityPosition;

    public BoardEntity(EntityPosition entityPosition) {
        this.entityPosition = entityPosition;
    }

    public EntityPosition getEntityPosition() {
        return entityPosition;
    }
}

class FoodPoint extends BoardEntity {
    protected int value;
    public FoodPoint(EntityPosition position, int value) {
        super(position);
        this.value = value;
    }
}

abstract class Insect extends BoardEntity {
    protected InsectColor color;

    public Insect(EntityPosition entityPosition, InsectColor color) {
        super(entityPosition);
        this.color = color;
    }

    public abstract Direction getBestDirection(Map<String, BoardEntity> boardData, int boardSize);

    public abstract int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize);
}

class TwoEntitiesOnSamePositionException extends Exception {
    @Override
    public String getMessage() {
        return "Two entities in the same position";
    }
}

class DuplicatensectException extends Exception {
    @Override
    public String getMessage() {
        return "Duplicate insects";
    }
}

class InvalidEntityPositionException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid entity position";
    }
}

class InvalidInsectTypeException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid insect type";
    }
}

class InvalidNumberOfFoodPointsException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid number of food points";
    }
}

class InvalidNumberOfInsectsException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid number of insects";
    }
}

class InvalidBoardSizeException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid board size";
    }
}