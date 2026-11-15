import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== BATTLESHIP CONFIGURATION ===");
        System.out.println("1. Player vs AI (Easy)");
        System.out.println("2. Player vs AI (Hard)");
        System.out.println("3. Player vs Player");
        System.out.print("Select game mode (1-3): ");

        int modeChoice = scanner.nextInt();
        GameSettings.GameMode mode = (modeChoice == 3)
                ? GameSettings.GameMode.PLAYER_VS_PLAYER
                : GameSettings.GameMode.PLAYER_VS_AI;
        GameSettings.AIDifficulty difficulty = (modeChoice == 2)
                ? GameSettings.AIDifficulty.HARD
                : GameSettings.AIDifficulty.EASY;

        System.out.println("\n1. Auto-place ships");
        System.out.println("2. Manual ship placement");
        System.out.print("Select placement mode (1-2): ");

        int placeChoice = scanner.nextInt();
        GameSettings.PlacementMode placement = (placeChoice == 2)
                ? GameSettings.PlacementMode.MANUAL
                : GameSettings.PlacementMode.AUTO;

        GameSettings settings = new GameSettings(mode, difficulty, placement);

        GameManager game = new GameManager(settings, scanner);
        game.start();

        scanner.close();
    }
}