import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board {

    public enum CellState {
        EMPTY, SHIP, HIT, MISS
    }

    public enum AttackResult {
        HIT, MISS, SUNK, ALREADY_ATTACKED
    }

    public static final int SIZE = 10;
    private CellState[][] grid;
    private List<Ship> ships;

    public Board() {
        grid = new CellState[SIZE][SIZE];
        ships = new ArrayList<>();
        resetBoard();
    }

    public boolean canPlaceShip(Ship ship) {
        int x = ship.getX();
        int y = ship.getY();
        int size = ship.getSize();
        Ship.Direction direction = ship.getDirection();

        for (int i = 0; i < size; i++) {
            int targetX = direction == Ship.Direction.HORIZONTAL ? x + i : x;
            int targetY = direction == Ship.Direction.VERTICAL ? y + i : y;

            if (targetX < 0 || targetX >= SIZE || targetY < 0 || targetY >= SIZE || grid[targetY][targetX] != CellState.EMPTY) {
                return false;
            }
        }
        return true;
    }

    public boolean placeShip(Ship ship) {
        if (!canPlaceShip(ship)) {
            return false;
        }

        int x = ship.getX();
        int y = ship.getY();
        int size = ship.getSize();
        Ship.Direction direction = ship.getDirection();

        for (int i = 0; i < size; i++) {
            int targetX = direction == Ship.Direction.HORIZONTAL ? x + i : x;
            int targetY = direction == Ship.Direction.VERTICAL ? y + i : y;
            grid[targetY][targetX] = CellState.SHIP;
        }
        ships.add(ship);
        return true;
    }

    public AttackResult attack(int x, int y) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }

        CellState currentState = grid[y][x];
        if (currentState == CellState.HIT || currentState == CellState.MISS) {
            return AttackResult.ALREADY_ATTACKED;
        }

        for (Ship ship : ships) {
            if (ship.occupies(x, y)) {
                ship.hit();
                grid[y][x] = CellState.HIT;
                return ship.isSunk() ? AttackResult.SUNK : AttackResult.HIT;
            }
        }

        grid[y][x] = CellState.MISS;
        return AttackResult.MISS;
    }

    public CellState getCellState(int x, int y) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        return grid[y][x];
    }

    public List<Ship> getShips() {
        return Collections.unmodifiableList(ships);
    }

    public boolean allShipsSunk() {
        if (ships.isEmpty()) {
            return false;
        }
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return true;
    }

    public void printBoard(boolean showShips) {
        System.out.print("  ");
        for (int i = 0; i < SIZE; i++) {
            System.out.print(i + " ");
        }
        System.out.println();
        for (int y = 0; y < SIZE; y++) {
            System.out.print(y + " ");
            for (int x = 0; x < SIZE; x++) {
                CellState state = grid[y][x];
                char displayChar = switch (state) {
                    case EMPTY -> '.';
                    case SHIP -> showShips ? getShipTypeAt(x, y) : '.';
                    case HIT -> 'X';
                    case MISS -> 'O';
                };
                System.out.print(displayChar + " ");
            }
            System.out.println();
        }
    }

    private char getShipTypeAt(int x, int y) {
        for (Ship ship : ships) {
            if (ship.occupies(x, y)) {
                return ship.getType();
            }
        }
        return 'S';
    }

    public void resetBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = CellState.EMPTY;
            }
        }
        ships.clear();
    }

    public void removeShip(Ship ship) {
        if (ships.remove(ship)) {
            int x = ship.getX();
            int y = ship.getY();
            int size = ship.getSize();
            Ship.Direction direction = ship.getDirection();

            for (int i = 0; i < size; i++) {
                int targetX = direction == Ship.Direction.HORIZONTAL ? x + i : x;
                int targetY = direction == Ship.Direction.VERTICAL ? y + i : y;
                grid[targetY][targetX] = CellState.EMPTY;
            }
        }
    }
}