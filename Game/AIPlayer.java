import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class AIPlayer {
    private final GameSettings.AIDifficulty difficulty;
    private final Random random;
    private final Queue<int[]> targetQueue;

    public AIPlayer(GameSettings.AIDifficulty difficulty) {
        this.difficulty = difficulty;
        this.random = new Random();
        this.targetQueue = new LinkedList<>();
    }

    public Board.AttackResult takeTurn(Board targetBoard) {
        while (true) {
            int[] coords = getNextShot(targetBoard);
            int x = coords[0];
            int y = coords[1];

            Board.AttackResult result = targetBoard.attack(x, y);
            if (result != Board.AttackResult.ALREADY_ATTACKED) {
                if (difficulty == GameSettings.AIDifficulty.HARD) {
                    if (result == Board.AttackResult.HIT) {
                        enqueueAdjacentCells(targetBoard, x, y);
                    } else if (result == Board.AttackResult.SUNK) {
                        targetQueue.clear();
                    }
                }
                return result;
            }
        }
    }

    public void reset() {
        targetQueue.clear();
    }

    private int[] getNextShot(Board targetBoard) {
        if (difficulty == GameSettings.AIDifficulty.HARD) {
            while (!targetQueue.isEmpty()) {
                int[] next = targetQueue.poll();
                Board.CellState state = targetBoard.getCellState(next[0], next[1]);
                if (state != Board.CellState.HIT && state != Board.CellState.MISS) {
                    return next;
                }
            }
            return getParityRandomShot(targetBoard);
        }
        return new int[]{random.nextInt(Board.SIZE), random.nextInt(Board.SIZE)};
    }

    private int[] getParityRandomShot(Board targetBoard) {
        for (int attempt = 0; attempt < 50; attempt++) {
            int x = random.nextInt(Board.SIZE);
            int y = random.nextInt(Board.SIZE);
            if ((x + y) % 2 == 0) {
                Board.CellState state = targetBoard.getCellState(x, y);
                if (state != Board.CellState.HIT && state != Board.CellState.MISS) {
                    return new int[]{x, y};
                }
            }
        }
        return new int[]{random.nextInt(Board.SIZE), random.nextInt(Board.SIZE)};
    }

    private void enqueueAdjacentCells(Board targetBoard, int x, int y) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx >= 0 && nx < Board.SIZE && ny >= 0 && ny < Board.SIZE) {
                Board.CellState state = targetBoard.getCellState(nx, ny);
                if (state != Board.CellState.HIT && state != Board.CellState.MISS) {
                    targetQueue.offer(new int[]{nx, ny});
                }
            }
        }
    }
}