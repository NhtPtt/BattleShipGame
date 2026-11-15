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

    // Check if a ship can be placed at the specified coordinates
    public boolean canPlaceShip(Ship ship) {
        int x = ship.getX();
        int y = ship.getY();
        int size = ship.getSize();
        Ship.Direction direction = ship.getDirection();

        // Check if the ship would go out of bounds
        for (int i = 0; i < size; i++) {
            int targetX = direction == Ship.Direction.HORIZONTAL ? x + i : x;
            int targetY = direction == Ship.Direction.VERTICAL ? y + i : y;

            if (targetX < 0 || targetX >= SIZE || targetY < 0 || targetY >= SIZE || grid[targetY][targetX] != CellState.EMPTY) {
                return false;
            }
        }
        return true;
    }

    // Place a ship on the board if possible
    public boolean placeShip(Ship ship) {
        if (!canPlaceShip(ship)) {
            return false;
        }

        int x = ship.getX();
        int y = ship.getY();
        int size = ship.getSize();
        Ship.Direction direction = ship.getDirection();

        // Mark the cells occupied by the ship
        for (int i = 0; i < size; i++) {
            int targetX = direction == Ship.Direction.HORIZONTAL ? x + i : x;
            int targetY = direction == Ship.Direction.VERTICAL ? y + i : y;
            grid[targetY][targetX] = CellState.SHIP;
        }
        ships.add(ship);
        return true;
    }

    // Attack a specific coordinate on the board
    public AttackResult attack(int x, int y) {
        // Check if the coordinates are within bounds
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }

        // Check if the cell has already been attacked
        CellState currentState = grid[y][x];
        if (currentState == CellState.HIT || currentState == CellState.MISS) {
            return AttackResult.ALREADY_ATTACKED;
        }

        // Check if the attack hits any ship
        for (Ship ship : ships) {
            if (ship.occupies(x, y)) {
                ship.hit();
                grid[y][x] = CellState.HIT;
                return ship.isSunk() ? AttackResult.SUNK : AttackResult.HIT;
            }
        }

        // If no ship is hit, mark the cell as a miss
        grid[y][x] = CellState.MISS;
        return AttackResult.MISS;
    }
    
    // Get the state of a specific cell on the board
    public CellState getCellState(int x, int y) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        return grid[y][x];
    }

    // Get the list of ships on the board
    public List<Ship> getShips() {
        return Collections.unmodifiableList(ships);
    }

    // Check if all ships on the board have been sunk
    public boolean allShipsSunk() {
        // If there are no ships, return false
        if (ships.isEmpty()) {
            return false;
        }
        // Check if all ships are sunk
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return true;
    }

    // Print the current state of the board, optionally showing ship positions
    public void printBoard(boolean showShips) {
        System.out.print("  ");
        for (int i = 0; i < SIZE; i++) {
            System.out.print(i + " ");
        }
        // Print the grid with row and column indices
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

    // Get the type of ship at a specific coordinate, or 'S' if there is no ship
    private char getShipTypeAt(int x, int y) {
        for (Ship ship : ships) {
            if (ship.occupies(x, y)) {
                return ship.getType();
            }
        }
        return 'S';
    }

    // Reset the board to its initial state, clearing all ships and cell states
    public void resetBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = CellState.EMPTY;
            }
        }
        ships.clear();
    }

    // Remove a ship from the board and update the grid accordingly
    public void removeShip(Ship ship) {
        // Remove the ship from the list and clear its occupied cells
        if (ships.remove(ship)) {
            int x = ship.getX();
            int y = ship.getY();
            int size = ship.getSize();
            Ship.Direction direction = ship.getDirection();

            // Clear the cells occupied by the ship
            for (int i = 0; i < size; i++) {
                int targetX = direction == Ship.Direction.HORIZONTAL ? x + i : x;
                int targetY = direction == Ship.Direction.VERTICAL ? y + i : y;
                grid[targetY][targetX] = CellState.EMPTY;
            }
        }
    }
}