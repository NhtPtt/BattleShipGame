import java.util.Scanner;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        String[] uiOptions = {"Swing GUI", "Console"};
        int uiChoice = JOptionPane.showOptionDialog(
                null,
                "Select interface mode:",
                "Battleship Launcher",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                uiOptions,
                uiOptions[0]
        );

        if (uiChoice == JOptionPane.CLOSED_OPTION) return;

        if (uiChoice == 0) {
            SwingUtilities.invokeLater(Main::launchSwingMode);
        } else {
            launchConsoleMode();
        }
    }

    public static void launchSwingMode() {
        String[] modeOptions = {"Player vs AI (Easy)", "Player vs AI (Hard)", "Player vs Player"};
        int modeChoice = JOptionPane.showOptionDialog(
                null,
                "Select game mode:",
                "Battleship Setup",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                modeOptions,
                modeOptions[1]
        );
        if (modeChoice == JOptionPane.CLOSED_OPTION) return;

        String[] placeOptions = {"Manual Placement", "Auto Placement"};
        int placeChoice = JOptionPane.showOptionDialog(
                null,
                "How do you want to place ships?",
                "Battleship Setup",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                placeOptions,
                placeOptions[0]
        );
        if (placeChoice == JOptionPane.CLOSED_OPTION) return;

        GameSettings.GameMode mode = (modeChoice == 2)
                ? GameSettings.GameMode.PLAYER_VS_PLAYER
                : GameSettings.GameMode.PLAYER_VS_AI;
        GameSettings.AIDifficulty difficulty = (modeChoice == 0)
                ? GameSettings.AIDifficulty.EASY
                : GameSettings.AIDifficulty.HARD;
        GameSettings.PlacementMode placement = (placeChoice == 1)
                ? GameSettings.PlacementMode.AUTO
                : GameSettings.PlacementMode.MANUAL;

        GameSettings settings = new GameSettings(mode, difficulty, placement);
        new BattleshipGUI(settings);
    }

    private static void launchConsoleMode() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== BATTLESHIP CONFIGURATION ===");
        System.out.println("1. Player vs AI (Easy)");
        System.out.println("2. Player vs AI (Hard)");
        System.out.println("3. Player vs Player");
        System.out.print("Select game mode (1-3): ");

        int modeChoice = readInt(scanner);
        GameSettings.GameMode mode = (modeChoice == 3)
                ? GameSettings.GameMode.PLAYER_VS_PLAYER
                : GameSettings.GameMode.PLAYER_VS_AI;
        GameSettings.AIDifficulty difficulty = (modeChoice == 2)
                ? GameSettings.AIDifficulty.HARD
                : GameSettings.AIDifficulty.EASY;

        System.out.println("\n1. Auto-place ships");
        System.out.println("2. Manual ship placement");
        System.out.print("Select placement mode (1-2): ");

        int placeChoice = readInt(scanner);
        GameSettings.PlacementMode placement = (placeChoice == 2)
                ? GameSettings.PlacementMode.MANUAL
                : GameSettings.PlacementMode.AUTO;

        GameSettings settings = new GameSettings(mode, difficulty, placement);
        GameManager game = new GameManager(settings, scanner);
        game.start();

        scanner.close();
    }

    private static int readInt(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            scanner.next();
            System.out.print("Invalid number, please re-enter: ");
        }
        return scanner.nextInt();
    }
}