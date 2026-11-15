import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

public class GameManager {
    private final GameSettings settings;
    private final Scanner scanner;
    private final Board board1;
    private final Board board2;
    private final Random random;
    private final Queue<int[]> aiTargetQueue;

    public GameManager(GameSettings settings, Scanner scanner) {
        this.settings = settings;
        this.scanner = scanner;
        this.board1 = new Board();
        this.board2 = new Board();
        this.random = new Random();
        this.aiTargetQueue = new LinkedList<>();
    }

    public void start() {
        setupPhase();
        battlePhase();
    }
    
    // Setup phase where players place their ships on the board
    private void setupPhase() {
        // Player 1 ship placement
        System.out.println("\n=== PLAYER 1 SHIP PLACEMENT ===");
        // Depending on the placement mode, either manually place ships or automatically place them
        if (settings.getPlacementMode() == GameSettings.PlacementMode.MANUAL) {
            manualPlaceShips(board1);
        } else {
            autoPlaceShips(board1);
            board1.printBoard(true);
        }

        // Player 2 ship placement
        if (settings.getGameMode() == GameSettings.GameMode.PLAYER_VS_PLAYER) {
            System.out.println("\n=== PLAYER 2 SHIP PLACEMENT ===");
            if (settings.getPlacementMode() == GameSettings.PlacementMode.MANUAL) {
                manualPlaceShips(board2);
            } else {
                autoPlaceShips(board2);
                board2.printBoard(true);
            }
        } else {
            autoPlaceShips(board2);
        }
    }

    private void battlePhase() {
        boolean isPlayer1Turn = true;

        while (!board1.allShipsSunk() && !board2.allShipsSunk()) {
            if (isPlayer1Turn) {
                System.out.println("\n=== PLAYER 1 TURN ===");
                displayBoards(board1, board2, settings.getGameMode() == GameSettings.GameMode.PLAYER_VS_AI);
                handleHumanTurn(board2, "Player 1");
            } else {
                if (settings.getGameMode() == GameSettings.GameMode.PLAYER_VS_PLAYER) {
                    System.out.println("\n=== PLAYER 2 TURN ===");
                    displayBoards(board2, board1, false);
                    handleHumanTurn(board1, "Player 2");
                } else {
                    System.out.println("\n=== AI TURN ===");
                    handleAiTurn();
                }
            }
            isPlayer1Turn = !isPlayer1Turn;
        }

        announceWinner();
    }

    private void displayBoards(Board ownBoard, Board opponentBoard, boolean showOwnShips) {
        System.out.println("--- YOUR BOARD ---");
        ownBoard.printBoard(showOwnShips);
        System.out.println("--- OPPONENT BOARD ---");
        opponentBoard.printBoard(false);
    }

    // Handle a human player's turn by prompting for attack coordinates and processing the attack
    private void handleHumanTurn(Board targetBoard, String playerName) {
        while (true) {
            System.out.print(playerName + ", enter attack coordinates (x y): ");
            int x = readInt();
            int y = readInt();

            try {
                Board.AttackResult result = targetBoard.attack(x, y);
                if (result == Board.AttackResult.ALREADY_ATTACKED) {
                    System.out.println("Cell (" + x + ", " + y + ") was already attacked. Try again.");
                } else {
                    System.out.println(playerName + " shot at (" + x + ", " + y + "): " + result);
                    break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Coordinates out of bounds (0-9). Try again.");
            }
        }
    }

    // Handle the AI's turn by selecting attack coordinates based on difficulty and processing the attack
    private void handleAiTurn() {
        while (true) {
            int[] coords = getAiNextShot();
            int x = coords[0];
            int y = coords[1];

            Board.AttackResult result = board1.attack(x, y);
            if (result != Board.AttackResult.ALREADY_ATTACKED) {
                System.out.println("AI shot at (" + x + ", " + y + "): " + result);
                if (settings.getAiDifficulty() == GameSettings.AIDifficulty.HARD) {
                    if (result == Board.AttackResult.HIT) {
                        enqueueAdjacentCells(x, y);
                    } else if (result == Board.AttackResult.SUNK) {
                        aiTargetQueue.clear();
                    }
                }
                break;
            }
        }
    }

    // Get the next shot coordinates for the AI based on its difficulty level
    private int[] getAiNextShot() {
        if (settings.getAiDifficulty() == GameSettings.AIDifficulty.HARD) {

            // If there are queued target cells from previous hits, use them first
            while (!aiTargetQueue.isEmpty()) {

                // Get the next target from the queue and check if it's valid
                int[] next = aiTargetQueue.poll();
                Board.CellState state = board1.getCellState(next[0], next[1]);

                // Only return the next target if it hasn't been attacked yet
                if (state != Board.CellState.HIT && state != Board.CellState.MISS) {
                    return next;
                }
            }
        }
        return new int[]{random.nextInt(Board.SIZE), random.nextInt(Board.SIZE)};
    }

    // Enqueue adjacent cells of a hit ship to the AI's target queue for future attacks
    private void enqueueAdjacentCells(int x, int y) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        // Check each adjacent cell and add it to the queue if it's within bounds and hasn't been attacked yet
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx >= 0 && nx < Board.SIZE && ny >= 0 && ny < Board.SIZE) {
                Board.CellState state = board1.getCellState(nx, ny);
                if (state != Board.CellState.HIT && state != Board.CellState.MISS) {
                    aiTargetQueue.offer(new int[]{nx, ny});
                }
            }
        }
    }

    // Manual ship placement for a player, prompting for coordinates and direction for each ship
    private void manualPlaceShips(Board board) {
        char[] types = GameSettings.DEFAULT_SHIP_TYPES;
        int[] sizes = GameSettings.DEFAULT_SHIP_SIZES;

        for (int i = 0; i < types.length; i++) {
            board.printBoard(true);
            while (true) {
                System.out.println("Placing ship '" + types[i] + "' (size " + sizes[i] + ")");
                System.out.print("Enter start x, start y, and direction (0 = Horizontal, 1 = Vertical): ");
                int x = readInt();
                int y = readInt();
                int dirInput = readInt();

                Ship.Direction dir = (dirInput == 1) ? Ship.Direction.VERTICAL : Ship.Direction.HORIZONTAL;
                Ship ship = new Ship(types[i], sizes[i], x, y, dir);

                if (board.placeShip(ship)) {
                    break;
                }
                System.out.println("Invalid placement (out of bounds or overlapping). Try again.");
            }
        }
    }

    // Automatic ship placement for a player, randomly placing ships on the board without overlaps
    private void autoPlaceShips(Board board) {
        char[] types = GameSettings.DEFAULT_SHIP_TYPES;
        int[] sizes = GameSettings.DEFAULT_SHIP_SIZES;

        // Randomly place each ship on the board until a valid position is found
        for (int i = 0; i < types.length; i++) {
            boolean placed = false;
            // Keep trying random positions until the ship is successfully placed
            while (!placed) {
                int x = random.nextInt(Board.SIZE);
                int y = random.nextInt(Board.SIZE);
                Ship.Direction dir = random.nextBoolean() ? Ship.Direction.HORIZONTAL : Ship.Direction.VERTICAL;
                Ship ship = new Ship(types[i], sizes[i], x, y, dir);
                placed = board.placeShip(ship);
            }
        }
    }

    // Read an integer from the scanner, prompting the user until a valid integer is entered
    private int readInt() {
        while (!scanner.hasNextInt()) {
            scanner.next();
            System.out.print("Invalid number, please re-enter: ");
        }
        return scanner.nextInt();
    }

    // Announce the winner of the game based on which player's ships have all been sunk
    private void announceWinner() {
        System.out.println("\n=== GAME OVER ===");
        if (board2.allShipsSunk()) {
            System.out.println("PLAYER 1 WINS!");
        } else {
            String winner = (settings.getGameMode() == GameSettings.GameMode.PLAYER_VS_PLAYER)
                    ? "PLAYER 2"
                    : "AI";
            System.out.println(winner + " WINS!");
        }
    }
}