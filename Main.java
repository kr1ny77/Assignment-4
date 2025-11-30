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

public class Main {
    private static Board gameBoard;
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

                if (x < 1 || x > d || y < 1 || y > d) {
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

                if (x < 1 || x > d || y < 1 || y > d) {
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

        } catch (InvalidBoardSizeException | InvalidNumberOfInsectsException
                 | InvalidNumberOfFoodPointsException
                 | InvalidInsectColorException
                 | InvalidInsectTypeException
                 | InvalidEntityPositionException
                 | DuplicateInsectException
                 | TwoEntitiesOnSamePositionException e) {
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
    private final int x;
    private final int y;

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
