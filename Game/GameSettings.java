public class GameSettings {
    public enum GameMode {
        PLAYER_VS_AI,
        PLAYER_VS_PLAYER
    }

    public enum AIDifficulty {
        EASY,
        HARD
    }

    public enum PlacementMode {
        AUTO,
        MANUAL
    }
    
    // Default ship types and sizes for the game
    public static final char[] DEFAULT_SHIP_TYPES = {'C', 'B', 'R', 'S', 'D'};
    public static final int[] DEFAULT_SHIP_SIZES = {5, 4, 3, 3, 2};
    
    // Default board size for the game
    private GameMode gameMode;
    private AIDifficulty aiDifficulty;
    private PlacementMode placementMode;

    // Constructor to initialize game settings with specified modes
    public GameSettings(GameMode gameMode, AIDifficulty aiDifficulty, PlacementMode placementMode) {
        this.gameMode = gameMode;
        this.aiDifficulty = aiDifficulty;
        this.placementMode = placementMode;
    }

    // Getters and setters for game settings
    public GameMode getGameMode() { return gameMode; }
    public AIDifficulty getAiDifficulty() { return aiDifficulty; }
    public PlacementMode getPlacementMode() { return placementMode; }

    public void setGameMode(GameMode gameMode) { this.gameMode = gameMode; }
    public void setAiDifficulty(AIDifficulty aiDifficulty) { this.aiDifficulty = aiDifficulty; }
    public void setPlacementMode(PlacementMode placementMode) { this.placementMode = placementMode; }
}