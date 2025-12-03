import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.io.IOException;


/**
 * Entry point of the insect board simulation.
 * Reads input from input.txt, validates data, runs the simulation on the board
 * and writes results or error message to output.txt.
 */
public class Main {
    private static Board gameBoard;
    /**
     * Runs the simulation: reads board size, insects, food points and produces the output file.
     */
    public static void main(String[] args) {
        String error = null;
        final int minSize = 4;
        final int maxSize = 1000;
        final int minNumber = 1;
        final int maxNumberInsects = 16;
        final int maxNumberFood = 200;
        List<Insect> insectsInOrder = new ArrayList<>();
        List<String> results = new ArrayList<>();
        try (Scanner sc = new Scanner(new File("input.txt"))) {

            int d = sc.nextInt();
            if (d < minSize || d > maxSize) {
                throw new InvalidBoardSizeException();
            }

            int n = sc.nextInt();
            if (n < minNumber || n > maxNumberInsects) {
                throw new InvalidNumberOfInsectsException();
            }

            int m = sc.nextInt();
            if (m < minNumber || m > maxNumberFood) {
                throw new InvalidNumberOfFoodPointsException();
            }

            gameBoard = new Board(d);
            Set<String> typeColorUsed = new HashSet<>();
            Set<String> occupiedCells = new HashSet<>();

            for (int i = 0; i < n; i++) {
                String colorStr = sc.next();
                String typeStr  = sc.next();
                int x = sc.nextInt();
                int y = sc.nextInt();

                InsectColor color = InsectColor.toColor(colorStr);

                Insect insect;
                EntityPosition pos = new EntityPosition(x, y);
                switch (typeStr) {
                    case "Ant":
                        insect = new Ant(pos, color);
                        break;
                    case "Butterfly":
                        insect = new Butterfly(pos, color);
                        break;
                    case "Spider":
                        insect = new Spider(pos, color);
                        break;
                    case "Grasshopper":
                        insect = new Grasshopper(pos, color);
                        break;
                    default:
                        throw new InvalidInsectTypeException();
                }

                if (x < minNumber || x > d || y < minNumber || y > d) {
                    throw new InvalidEntityPositionException();
                }

                String colorKey = typeStr + "-" + colorStr;
                if (typeColorUsed.contains(colorKey)) {
                    throw new DuplicateInsectException();
                }
                typeColorUsed.add(colorKey);

                String cellKey = x + "," + y;
                if (occupiedCells.contains(cellKey)) {
                    throw new TwoEntitiesOnSamePositionException();
                }
                occupiedCells.add(cellKey);

                gameBoard.addEntity(insect);
                insectsInOrder.add(insect);
            }

            for (int i = 0; i < m; i++) {
                int amount = sc.nextInt();
                int x = sc.nextInt();
                int y = sc.nextInt();

                if (x < minNumber || x > d || y < minNumber || y > d) {
                    throw new InvalidEntityPositionException();
                }

                String cellKey = x + "," + y;
                if (occupiedCells.contains(cellKey)) {
                    throw new TwoEntitiesOnSamePositionException();
                }
                occupiedCells.add(cellKey);

                gameBoard.addEntity(new FoodPoint(new EntityPosition(x, y), amount));
            }

            for (Insect insect : insectsInOrder) {
                Direction dir = gameBoard.getDirection(insect);
                int eaten = gameBoard.getDirectionSum(insect);

                String colorStr = insect.color.name().charAt(0) + insect.color.name().substring(1).toLowerCase();
                String name = insect.getClass().getSimpleName();

                String line = colorStr + " " + name + " " + dir.getTextRepresentation() + " " + eaten;
                results.add(line);
            }

        } catch (Exception e) {
            error = e.getMessage();
        } catch (IOException e) {
        }

        try (FileWriter fw = new FileWriter("output.txt")) {
            if (error != null) {
                fw.write(error + System.lineSeparator());
            } else {
                for (String s : results) {
                    fw.write(s);
                    fw.write(System.lineSeparator());
                }
            }
        } catch (IOException ignored) {
        }
    }
}

/**
 * Represents one of the eight possible movement directions on the board.
 */
enum Direction {
    N("North"),
    E("East"),
    S("South"),
    W("West"),
    NE("North-East"),
    SE("South-East"),
    SW("South-West"),
    NW("North-West");

    private String textRepresentation;
    private Direction(String text) {
        this.textRepresentation = text;
    }
    /**
     * Returns the text representation of this direction.
     * @return text representation of the direction
     */
    public String getTextRepresentation() {
        return textRepresentation;
    }
}
/**
 * Represents the square game board holding insects and food points.
 */
class Board {
    private Map<String, BoardEntity> boardData;
    private int size;
    /**
     * Creates a new board of the given size.
     * @param boardSize size of the board (both width and height)
     */
    public Board(int boardSize) {
        this.size = boardSize;
        this.boardData = new HashMap<>();
    }
    /**
     * Converts a board position to an internal key in the map.
     * @param position board position
     * @return string key in the form "x,y"
     */
    private String toKey(EntityPosition position) {
        return position.getX() + "," + position.getY();
    }
    /**
     * Adds or replaces an entity at its position on the board.
     * @param entity entity to add
     */
    public void addEntity(BoardEntity entity) {
        boardData.put(toKey(entity.getEntityPosition()), entity);
    }
    /**
     * Returns the entity located at the given position.
     * @param position board position to query
     * @return entity at the position, or null if empty
     */
    public BoardEntity getEntity(EntityPosition position) {
        return boardData.get(toKey(position));
    }
    /**
     * Computes the best movement direction for the given insect.
     * @param insect insect for which to compute direction
     * @return direction that provides the highest visible nutritional value
     */
    public Direction getDirection(Insect insect) {
        return insect.getBestDirection(boardData, size);
    }
    /**
     * Calculates the total amount of food collected by moving the insect
     * along its best direction and updates the board accordingly.
     * @param insect insect that travels
     * @return total value of food collected by the insect
     */
    public int getDirectionSum(Insect insect) {
        Direction dir = getDirection(insect);
        return insect.travelDirection(dir, boardData, size);
    }

}
/**
 * Immutable position of an entity on the board.
 */
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
/**
 * Represents possible colors of insects.
 */
enum InsectColor {
    RED,
    GREEN,
    BLUE,
    YELLOW;
    /**
     * Converts a string from input into the corresponding insect color.
     * @param s color string from input
     * @return matching InsectColor value
     * @throws InvalidInsectColorException if the color string is not supported
     */
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
/**
 * Base class for any entity that can be placed on the board.
 */
abstract class BoardEntity {
    protected EntityPosition entityPosition;

    public BoardEntity(EntityPosition entityPosition) {
        this.entityPosition = entityPosition;
    }

    public EntityPosition getEntityPosition() {
        return entityPosition;
    }
}
/**
 * Food point on the board with a certain value.
 */
class FoodPoint extends BoardEntity {
    protected int value;
    public FoodPoint(EntityPosition position, int value) {
        super(position);
        this.value = value;
    }
}
/**
 * Abstract base class for all insects that can move and collect food.
 */
abstract class Insect extends BoardEntity {
    protected InsectColor color;

    public Insect(EntityPosition entityPosition, InsectColor color) {
        super(entityPosition);
        this.color = color;
    }
    /**
     * Computes the best movement direction for this insect on the board.
     * @param boardData map of board positions to entities
     * @param boardSize size of the board
     * @return best direction for this insect
     */
    public abstract Direction getBestDirection(Map<String, BoardEntity> boardData, int boardSize);
    /**
     * Moves the insect in the given direction, collects food and updates the board.
     * @param dir       direction in which to travel
     * @param boardData map of board positions to entities
     * @param boardSize size of the board
     * @return total value of the food collected along the path
     */
    public abstract int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize);
}
/**
 * Base class for all custom game exceptions.
 */
class Exception extends Throwable {
    public String getMessage() {
        return "Something went wrong";
    }
}
/**
 * Thrown when two entities attempt to occupy the same position.
 */
class TwoEntitiesOnSamePositionException extends Exception {
    @Override
    public String getMessage() {
        return "Two entities in the same position";
    }
}
/**
 * Thrown when an insect with duplicate type and color appears in the input.
 */
class DuplicateInsectException extends Exception {
    @Override
    public String getMessage() {
        return "Duplicate insects";
    }
}
/**
 * Thrown when an entity position lies outside the board.
 */
class InvalidEntityPositionException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid entity position";
    }
}
/**
 * Thrown when an unknown insect type is encountered in the input.
 */
class InvalidInsectTypeException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid insect type";
    }
}
/**
 * Thrown when an unknown insect color is encountered in the input.
 */
class InvalidInsectColorException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid insect color";
    }
}
/**
 * Thrown when the number of food points in the input is invalid.
 */
class InvalidNumberOfFoodPointsException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid number of food points";
    }
}
/**
 * Thrown when the number of insects in the input is invalid.
 */
class InvalidNumberOfInsectsException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid number of insects";
    }
}
/**
 * Thrown when the board size in the input is invalid.
 */
class InvalidBoardSizeException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid board size";
    }
}
/**
 * Defines behavior for insects that can move in orthogonal directions.
 */
interface OrthogonalMoving {
    /**
     * Computes the visible total food value in the given orthogonal direction.
     * @param dir           orthogonal direction to check
     * @param entityPosition current position of the insect
     * @param boardData     map of board positions to entities
     * @param boardSize     size of the board
     * @return sum of visible food values in that direction
     */
    int getOrthogonalDirectionVisibleValue(
            Direction dir,
            EntityPosition entityPosition,
            Map<String, BoardEntity> boardData,
            int boardSize
    );
    /**
     * Moves orthogonally in the given direction, collecting food until a stopping condition.
     * @param dir           direction of travel
     * @param entityPosition starting position of the insect
     * @param color         color of the insect (used when encountering other insects)
     * @param boardData     map of board positions to entities
     * @param boardSize     size of the board
     * @return total value of collected food
     */
    int travelOrthogonally(
            Direction dir,
            EntityPosition entityPosition,
            InsectColor color,
            Map<String, BoardEntity> boardData,
            int boardSize
    );
}
/**
 * Defines behavior for insects that can move diagonally.
 */
interface DiagonalMoving {
    /**
     * Computes the visible total food value in the given diagonal direction.
     * @param dir           diagonal direction to check
     * @param entityPosition current position of the insect
     * @param boardData     map of board positions to entities
     * @param boardSize     size of the board
     * @return sum of visible food values in that direction
     */
    int getDiagonalDirectionVisibleValue(
            Direction dir,
            EntityPosition entityPosition,
            Map<String, BoardEntity> boardData,
            int boardSize
    );
    /**
     * Moves diagonally in the given direction, collecting food until a stopping condition.
     * @param dir           direction of travel
     * @param entityPosition starting position of the insect
     * @param color         color of the insect (used when encountering other insects)
     * @param boardData     map of board positions to entities
     * @param boardSize     size of the board
     * @return total value of collected food
     */
    int travelDiagonally(
            Direction dir,
            EntityPosition entityPosition,
            InsectColor color,
            Map<String, BoardEntity> boardData,
            int boardSize
    );
}
/**
 * Butterfly insect that moves and looks only in orthogonal directions.
 */
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
            if (item instanceof Insect insect) {
                if (insect.color != color) {
                    break;
                }
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
/**
 * Spider insect that moves and looks only in diagonal directions.
 */
class Spider extends Insect implements DiagonalMoving {
    public Spider(EntityPosition entityPosition, InsectColor color) {
        super(entityPosition, color);
    }

    @Override
    public int getDiagonalDirectionVisibleValue(Direction dir, EntityPosition entityPosition,
                                                  Map<String, BoardEntity> boardData, int boardSize) {
        int dx;
        int dy;
        switch (dir) {
            case NE:
                dx = -1;
                dy = 1;
                break;
            case SE:
                dx = 1;
                dy = 1;
                break;
            case SW:
                dx = 1;
                dy = -1;
                break;
            case NW:
                dx = -1;
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
    public int travelDiagonally(Direction dir, EntityPosition entityPosition, InsectColor color,
                                  Map<String, BoardEntity> boardData, int boardSize) {
        int dx;
        int dy;
        switch (dir) {
            case NE:
                dx = -1;
                dy = 1;
                break;
            case SE:
                dx = 1;
                dy = 1;
                break;
            case SW:
                dx = 1;
                dy = -1;
                break;
            case NW:
                dx = -1;
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
            if (item instanceof Insect insect) {
                if (insect.color != color) {
                    break;
                }
            }
            x += dx;
            y += dy;
        }
        return sum;
    }

    @Override
    public Direction getBestDirection(Map<String, BoardEntity> boardData, int boardSize) {
        Direction dir = Direction.NE;
        Direction bestDir = Direction.NE;
        int bestValue = getDiagonalDirectionVisibleValue(dir, this.entityPosition, boardData, boardSize);
        dir = Direction.SE;
        int value = getDiagonalDirectionVisibleValue(dir, this.entityPosition, boardData, boardSize);
        if  (value > bestValue) {
            bestValue = value;
            bestDir = dir;
        }
        dir = Direction.SW;
        value = getDiagonalDirectionVisibleValue(dir, this.entityPosition, boardData, boardSize);
        if  (value > bestValue) {
            bestValue = value;
            bestDir = dir;
        }
        dir = Direction.NW;
        value = getDiagonalDirectionVisibleValue(dir, this.entityPosition, boardData, boardSize);
        if  (value > bestValue) {
            bestValue = value;
            bestDir = dir;
        }
        return bestDir;
    }

    @Override
    public int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize) {
        return travelDiagonally(dir, entityPosition, color, boardData, boardSize);
    }

}
/**
 * Ant insect that can move both orthogonally and diagonally.
 */
class Ant extends Insect implements DiagonalMoving, OrthogonalMoving {
    public Ant(EntityPosition entityPosition, InsectColor color) {
        super(entityPosition, color);
    }

    @Override
    public int getDiagonalDirectionVisibleValue(Direction dir, EntityPosition entityPosition,
                                                Map<String, BoardEntity> boardData, int boardSize) {
        int dx;
        int dy;
        switch (dir) {
            case NE:
                dx = -1;
                dy = 1;
                break;
            case SE:
                dx = 1;
                dy = 1;
                break;
            case SW:
                dx = 1;
                dy = -1;
                break;
            case NW:
                dx = -1;
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
    public int travelDiagonally(Direction dir, EntityPosition entityPosition, InsectColor color,
                                Map<String, BoardEntity> boardData, int boardSize) {
        int dx;
        int dy;
        switch (dir) {
            case NE:
                dx = -1;
                dy = 1;
                break;
            case SE:
                dx = 1;
                dy = 1;
                break;
            case SW:
                dx = 1;
                dy = -1;
                break;
            case NW:
                dx = -1;
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
            if (item instanceof Insect insect) {
                if (insect.color != color) {
                    break;
                }
            }
            x += dx;
            y += dy;
        }
        return sum;
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
            if (item instanceof Insect insect) {
                if (insect.color != color) {
                    break;
                }
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
        if (value > bestValue) {
            bestValue = value;
            bestDir = dir;
        }
        dir = Direction.S;
        value = getOrthogonalDirectionVisibleValue(dir, this.entityPosition, boardData, boardSize);
        if (value > bestValue) {
            bestValue = value;
            bestDir = dir;
        }
        dir = Direction.W;
        value = getOrthogonalDirectionVisibleValue(dir, this.entityPosition, boardData, boardSize);
        if (value > bestValue) {
            bestValue = value;
            bestDir = dir;
        }
        dir = Direction.NE;
        value = getDiagonalDirectionVisibleValue(dir, this.entityPosition, boardData, boardSize);
        if (value > bestValue) {
            bestValue = value;
            bestDir = dir;
        }
        dir = Direction.SE;
        value = getDiagonalDirectionVisibleValue(dir, this.entityPosition, boardData, boardSize);
        if (value > bestValue) {
            bestValue = value;
            bestDir = dir;
        }
        dir = Direction.SW;
        value = getDiagonalDirectionVisibleValue(dir, this.entityPosition, boardData, boardSize);
        if (value > bestValue) {
            bestValue = value;
            bestDir = dir;
        }
        dir = Direction.NW;
        value = getDiagonalDirectionVisibleValue(dir, this.entityPosition, boardData, boardSize);
        if (value > bestValue) {
            bestValue = value;
            bestDir = dir;
        }
        return bestDir;
    }
    @Override
    public int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize) {
        switch (dir) {
            case N:
            case E:
            case S:
            case W:
                return travelOrthogonally(dir, entityPosition, color, boardData, boardSize);
            case NE:
            case SE:
            case SW:
            case NW:
                return travelDiagonally(dir, entityPosition, color, boardData, boardSize);
            default:
                return 0;
        }
    }
}
/**
 * Grasshopper insect that moves orthogonally in jumps of length two.
 */
class Grasshopper extends Insect {
    public Grasshopper(EntityPosition entityPosition, InsectColor color) {
        super(entityPosition, color);
    }

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
        int x = entityPosition.getX() + (2 * dx);
        int y = entityPosition.getY() + (2 * dy);
        int sum = 0;
        while (x >= 1 && x <= boardSize && y >= 1 && y <= boardSize) {
            String key = x + "," + y;
            BoardEntity item = boardData.get(key);
            if (item instanceof FoodPoint food) {
                sum += food.value;
            }
            x += 2 * dx;
            y += 2 * dy;
        }
        return sum;
    }

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
        int x = entityPosition.getX() + (2 * dx);
        int y = entityPosition.getY() + (2 * dy);
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
            if (item instanceof Insect insect) {
                if (insect.color != color) {
                    break;
                }
            }
            x += 2 * dx;
            y += 2 * dy;
        }
        return sum;
    }

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

    public int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize) {
        return travelOrthogonally(dir, entityPosition, color, boardData, boardSize);
    }

}
