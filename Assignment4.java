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

    public static InsectColor toColor(String s) throws InvalidInsectColorException {
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

class DuplicateInsectException extends Exception {
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

class InvalidInsectColorException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid insect color";
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

interface OrthogonalMoving {

    int getOrthogonalDirectionVisibleValue(
            Direction dir,
            EntityPosition entityPosition,
            Map<String, BoardEntity> boardData,
            int boardSize
    );

    int travelOrthogonally(
            Direction dir,
            EntityPosition entityPosition,
            InsectColor color,
            Map<String, BoardEntity> boardData,
            int boardSize
    );
}

interface DiagonalMoving {

    int getDiagonalDirectionVisibleValue(
            Direction dir,
            EntityPosition entityPosition,
            Map<String, BoardEntity> boardData,
            int boardSize
    );

    int travelDiagonally(
            Direction dir,
            EntityPosition entityPosition,
            InsectColor color,
            Map<String, BoardEntity> boardData,
            int boardSize
    );
}

class Butterfly extends Insect implements OrthogonalMoving {
    public Butterfly(EntityPosition entityPosition, InsectColor color) {
        super(entityPosition, color);
    }

    @Override
    public int getOrthogonalDirectionVisibleValue(Direction dir, EntityPosition entityPosition,
                                                  Map<String, BoardEntity> boardData, int boardSize) {
        int dx;
        int dy;
        switch (dir) {
            case N:
                dx = -1;
                dy = 0;
                break;
            case S:
                dx = 1;
                dy = 0;
                break;
            case E:
                dx = 0;
                dy = 1;
                break;
            case W:
                dx = 0;
                dy = -1;
                break;
            default:
                return 0;
        }
        int x = entityPosition.getX() + dx;
        int y = entityPosition.getY() + dy;
        int sum = 0;
        while (x >= 1 && x <= boardSize && y >= 1 && y <= boardSize) {
            String key = x + "," + y;
            BoardEntity item = boardData.get(key);
            if (item instanceof FoodPoint food) {
                sum += food.value;
            }
            x += dx;
            y += dy;
        }
        return sum;
    }

    @Override
    public int travelOrthogonally(Direction dir, EntityPosition entityPosition, InsectColor color,
                                  Map<String, BoardEntity> boardData, int boardSize) {
        int dx;
        int dy;
        switch (dir) {
            case N:
                dx = -1;
                dy = 0;
                break;
            case S:
                dx = 1;
                dy = 0;
                break;
            case E:
                dx = 0;
                dy = 1;
                break;
            case W:
                dx = 0;
                dy = -1;
                break;
            default:
                return 0;
        }
        int x = entityPosition.getX() + dx;
        int y = entityPosition.getY() + dy;
        String oldKey = entityPosition.getX() + "," + entityPosition.getY();
        boardData.remove(oldKey);
        int sum = 0;
        while (x >= 1 && x <= boardSize && y >= 1 && y <= boardSize) {
            String key = x + "," + y;
            BoardEntity item = boardData.get(key);
            if (item instanceof FoodPoint food) {
                sum += food.value;
                boardData.remove(key);
            }
            if (item instanceof Insect) {
                break;
            }
            x += dx;
            y += dy;
        }
        return sum;
    }

    @Override
    public Direction getBestDirection(Map<String, BoardEntity> boardData, int boardSize) {
        Direction dir = Direction.N;
        Direction bestDir = Direction.N;
        int bestValue = getOrthogonalDirectionVisibleValue(dir, this.entityPosition, boardData, boardSize);
        dir = Direction.E;
        int value = getOrthogonalDirectionVisibleValue(dir, this.entityPosition, boardData, boardSize);
        if  (value > bestValue) {
            bestValue = value;
            bestDir = dir;
        }
        dir = Direction.S;
        value = getOrthogonalDirectionVisibleValue(dir, this.entityPosition, boardData, boardSize);
        if  (value > bestValue) {
            bestValue = value;
            bestDir = dir;
        }
        dir = Direction.W;
        value = getOrthogonalDirectionVisibleValue(dir, this.entityPosition, boardData, boardSize);
        if  (value > bestValue) {
            bestValue = value;
            bestDir = dir;
        }
        return bestDir;
    }

    @Override
    public int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize) {
        return travelOrthogonally(dir, entityPosition, color, boardData, boardSize);
    }

}
