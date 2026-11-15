import java.util.Random;
import java.util.Scanner;

public class GameManager {
    private final GameSettings settings;
    private final Scanner scanner;
    private final Board board1;
    private final Board board2;
    private final AIPlayer aiPlayer;
    private final Random random;

    public GameManager(GameSettings settings, Scanner scanner) {
        this.settings = settings;
        this.scanner = scanner;
        this.board1 = new Board();
        this.board2 = new Board();
        this.aiPlayer = new AIPlayer(settings.getAiDifficulty());
        this.random = new Random();
    }

    public void start() {
        setupPhase();
        battlePhase();
    }

    private void setupPhase() {
        System.out.println("\n=== PLAYER 1 SHIP PLACEMENT ===");
        if (settings.getPlacementMode() == GameSettings.PlacementMode.MANUAL) {
            manualPlaceShips(board1);
        } else {
            autoPlaceShips(board1);
            board1.printBoard(true);
        }

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
                    Board.AttackResult aiResult = aiPlayer.takeTurn(board1);
                    System.out.println("AI attack result: " + aiResult);
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

    private void autoPlaceShips(Board board) {
        char[] types = GameSettings.DEFAULT_SHIP_TYPES;
        int[] sizes = GameSettings.DEFAULT_SHIP_SIZES;

        for (int i = 0; i < types.length; i++) {
            boolean placed = false;
            while (!placed) {
                int x = random.nextInt(Board.SIZE);
                int y = random.nextInt(Board.SIZE);
                Ship.Direction dir = random.nextBoolean() ? Ship.Direction.HORIZONTAL : Ship.Direction.VERTICAL;
                Ship ship = new Ship(types[i], sizes[i], x, y, dir);
                placed = board.placeShip(ship);
            }
        }
    }

    private int readInt() {
        while (!scanner.hasNextInt()) {
            scanner.next();
            System.out.print("Invalid number, please re-enter: ");
        }
        return scanner.nextInt();
    }

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