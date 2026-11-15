import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class BattleshipGUI extends JFrame {
    private final GameSettings settings;
    private final Board board1;
    private final Board board2;
    private final AIPlayer aiPlayer;
    private final JButton[][] buttons1;
    private final JButton[][] buttons2;
    private final JLabel[] shipLabels1;
    private final JLabel[] shipLabels2;
    private final JLabel statusLabel;
    private final JButton rotateButton;
    private final JButton randomizeButton;
    private final JButton clearButton;
    private final JButton confirmButton;
    private final KeyEventDispatcher keyDispatcher;
    private final Random random;

    private boolean isPlacementPhase;
    private int activePlayer;
    private int currentShipIndex;
    private Ship.Direction currentDirection;
    private boolean gameOver;
    private boolean hideAllShipsDuringHandoff;
    private int hoverX = -1;
    private int hoverY = -1;

    public BattleshipGUI(GameSettings settings) {
        this.settings = settings;
        this.board1 = new Board();
        this.board2 = new Board();
        this.aiPlayer = new AIPlayer(settings.getAiDifficulty());
        this.buttons1 = new JButton[Board.SIZE][Board.SIZE];
        this.buttons2 = new JButton[Board.SIZE][Board.SIZE];
        this.shipLabels1 = new JLabel[GameSettings.DEFAULT_SHIP_TYPES.length];
        this.shipLabels2 = new JLabel[GameSettings.DEFAULT_SHIP_TYPES.length];
        this.random = new Random();

        setTitle("Battleship - Java Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        statusLabel = new JLabel("Welcome to Battleship!");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        rotateButton = new JButton("Direction: HORIZONTAL (Press R)");
        rotateButton.addActionListener(e -> toggleDirection());
        topPanel.add(statusLabel);
        topPanel.add(rotateButton);
        add(topPanel, BorderLayout.NORTH);

        boolean isPvP = settings.getGameMode() == GameSettings.GameMode.PLAYER_VS_PLAYER;
        String title1 = isPvP ? "PLAYER 1 FLEET" : "YOUR FLEET";
        String title2 = isPvP ? "PLAYER 2 FLEET" : "ENEMY WATERS";

        JPanel boardsContainer = new JPanel(new GridLayout(1, 2, 25, 0));
        boardsContainer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        boardsContainer.add(createBoardPanel(title1, buttons1, shipLabels1, 1));
        boardsContainer.add(createBoardPanel(title2, buttons2, shipLabels2, 2));
        add(boardsContainer, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        randomizeButton = new JButton("Randomize Fleet");
        clearButton = new JButton("Clear Board");
        confirmButton = new JButton("Start Battle");
        JButton resetButton = new JButton("New Game");
        JButton menuButton = new JButton("Back to Menu");

        randomizeButton.addActionListener(e -> randomizeActiveFleet());
        clearButton.addActionListener(e -> clearActiveFleet());
        confirmButton.addActionListener(e -> handleConfirmPlacement());
        resetButton.addActionListener(e -> initGame());
        menuButton.addActionListener(e -> returnToMenu());

        bottomPanel.add(randomizeButton);
        bottomPanel.add(clearButton);
        bottomPanel.add(confirmButton);
        bottomPanel.add(resetButton);
        bottomPanel.add(menuButton);
        add(bottomPanel, BorderLayout.SOUTH);

        keyDispatcher = e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_R) {
                if (isPlacementPhase && !gameOver) {
                    toggleDirection();
                    if (hoverX != -1 && hoverY != -1) {
                        previewShipPlacement(activePlayer, hoverX, hoverY);
                    }
                }
            }
            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyDispatcher);

        initGame();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void returnToMenu() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyDispatcher);
        dispose();
        SwingUtilities.invokeLater(Main::launchSwingMode);
    }

    private JPanel createBoardPanel(String title, JButton[][] buttons, JLabel[] shipLabels, int boardOwner) {
        JPanel wrapper = new JPanel(new BorderLayout(5, 8));
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        wrapper.add(titleLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(Board.SIZE + 1, Board.SIZE + 1, 2, 2));
        Font coordFont = new Font("Arial", Font.BOLD, 13);

        for (int r = 0; r <= Board.SIZE; r++) {
            for (int c = 0; c <= Board.SIZE; c++) {
                if (r == 0 && c == 0) {
                    gridPanel.add(new JLabel(""));
                } else if (r == 0) {
                    JLabel colLabel = new JLabel(String.valueOf((char) ('A' + c - 1)), SwingConstants.CENTER);
                    colLabel.setFont(coordFont);
                    gridPanel.add(colLabel);
                } else if (c == 0) {
                    JLabel rowLabel = new JLabel(String.valueOf(r), SwingConstants.CENTER);
                    rowLabel.setFont(coordFont);
                    gridPanel.add(rowLabel);
                } else {
                    final int fx = c - 1;
                    final int fy = r - 1;

                    JButton btn = new JButton();
                    btn.setPreferredSize(new Dimension(38, 38));
                    btn.setFocusPainted(false);
                    btn.setOpaque(true);
                    btn.setBorderPainted(true);
                    btn.setFont(new Font("Arial", Font.BOLD, 14));
                    btn.setForeground(Color.WHITE);

                    btn.addActionListener(e -> handleBoardClick(boardOwner, fx, fy));
                    btn.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            if (isPlacementPhase && activePlayer == boardOwner) {
                                hoverX = fx;
                                hoverY = fy;
                                previewShipPlacement(boardOwner, hoverX, hoverY);
                            }
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            if (isPlacementPhase && activePlayer == boardOwner) {
                                hoverX = -1;
                                hoverY = -1;
                                refreshBoards();
                            }
                        }
                    });

                    buttons[fy][fx] = btn;
                    gridPanel.add(btn);
                }
            }
        }

        wrapper.add(gridPanel, BorderLayout.CENTER);
        wrapper.add(createFleetStatusPanel(shipLabels), BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel createFleetStatusPanel(JLabel[] labels) {
        JPanel statusPanel = new JPanel(new GridLayout(labels.length, 1, 2, 2));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Fleet Status"));
        Font statusFont = new Font("Arial", Font.PLAIN, 12);

        for (int i = 0; i < labels.length; i++) {
            labels[i] = new JLabel("", SwingConstants.LEFT);
            labels[i].setFont(statusFont);
            statusPanel.add(labels[i]);
        }
        return statusPanel;
    }

    private void initGame() {
        board1.resetBoard();
        board2.resetBoard();
        aiPlayer.reset();
        gameOver = false;
        isPlacementPhase = true;
        hideAllShipsDuringHandoff = false;
        activePlayer = 1;
        currentDirection = Ship.Direction.HORIZONTAL;
        rotateButton.setText("Direction: HORIZONTAL (Press R)");

        boolean isPvP = settings.getGameMode() == GameSettings.GameMode.PLAYER_VS_PLAYER;
        confirmButton.setText(isPvP ? "Confirm Player 1 Fleet" : "Start Battle");

        if (!isPvP) {
            autoPlaceShips(board2);
        }

        if (settings.getPlacementMode() == GameSettings.PlacementMode.AUTO) {
            randomizeActiveFleet();
        } else {
            clearActiveFleet();
        }
    }

    private Board getActiveBoard() {
        return activePlayer == 1 ? board1 : board2;
    }

    private JButton[][] getActiveButtons() {
        return activePlayer == 1 ? buttons1 : buttons2;
    }

    private void randomizeActiveFleet() {
        if (!isPlacementPhase || gameOver) return;
        Board target = getActiveBoard();
        target.resetBoard();
        autoPlaceShips(target);
        currentShipIndex = GameSettings.DEFAULT_SHIP_TYPES.length;
        rotateButton.setEnabled(false);
        confirmButton.setEnabled(true);
        randomizeButton.setEnabled(true);
        clearButton.setEnabled(true);
        statusLabel.setText("Player " + activePlayer + " fleet randomized! Confirm or customize again.");
        refreshBoards();
    }

    private void clearActiveFleet() {
        if (!isPlacementPhase || gameOver) return;
        getActiveBoard().resetBoard();
        currentShipIndex = 0;
        rotateButton.setEnabled(true);
        confirmButton.setEnabled(false);
        randomizeButton.setEnabled(true);
        clearButton.setEnabled(true);
        updatePlacementPrompt();
        refreshBoards();
    }

    private void handleConfirmPlacement() {
        if (!isPlacementPhase || currentShipIndex < GameSettings.DEFAULT_SHIP_TYPES.length) return;

        boolean isPvP = settings.getGameMode() == GameSettings.GameMode.PLAYER_VS_PLAYER;
        if (isPvP && activePlayer == 1) {
            hideAllShipsDuringHandoff = true;
            refreshBoards();
            JOptionPane.showMessageDialog(this, "Player 1 fleet confirmed! Pass the device to Player 2.");
            hideAllShipsDuringHandoff = false;

            activePlayer = 2;
            confirmButton.setText("Start Battle");
            if (settings.getPlacementMode() == GameSettings.PlacementMode.AUTO) {
                randomizeActiveFleet();
            } else {
                clearActiveFleet();
            }
            return;
        }

        if (isPvP) {
            hideAllShipsDuringHandoff = true;
            refreshBoards();
            JOptionPane.showMessageDialog(this, "Player 2 fleet confirmed! Pass back to Player 1 to start.");
            hideAllShipsDuringHandoff = false;
        }

        isPlacementPhase = false;
        activePlayer = 1;
        rotateButton.setEnabled(false);
        randomizeButton.setEnabled(false);
        clearButton.setEnabled(false);
        confirmButton.setEnabled(false);
        statusLabel.setText(isPvP ? "Player 1's Turn! Attack Player 2's board." : "Battle Started! Click on ENEMY WATERS to attack.");
        refreshBoards();
    }

    private void toggleDirection() {
        currentDirection = (currentDirection == Ship.Direction.HORIZONTAL)
                ? Ship.Direction.VERTICAL
                : Ship.Direction.HORIZONTAL;
        rotateButton.setText("Direction: " + currentDirection + " (Press R)");
    }

    private void updatePlacementPrompt() {
        char type = GameSettings.DEFAULT_SHIP_TYPES[currentShipIndex];
        int size = GameSettings.DEFAULT_SHIP_SIZES[currentShipIndex];
        statusLabel.setText("Player " + activePlayer + ": Place " + getShipName(type) + " (Size: " + size + ")");
    }

    private void previewShipPlacement(int boardOwner, int x, int y) {
        if (!isPlacementPhase || gameOver || boardOwner != activePlayer) return;
        if (currentShipIndex >= GameSettings.DEFAULT_SHIP_TYPES.length) return;
        refreshBoards();

        Board target = getActiveBoard();
        JButton[][] targetButtons = getActiveButtons();
        char type = GameSettings.DEFAULT_SHIP_TYPES[currentShipIndex];
        int size = GameSettings.DEFAULT_SHIP_SIZES[currentShipIndex];
        Ship tempShip = new Ship(type, size, x, y, currentDirection);
        boolean valid = target.canPlaceShip(tempShip);
        Color previewColor = valid ? new Color(144, 238, 144) : new Color(255, 160, 160);

        for (int i = 0; i < size; i++) {
            int tx = (currentDirection == Ship.Direction.HORIZONTAL) ? x + i : x;
            int ty = (currentDirection == Ship.Direction.VERTICAL) ? y + i : y;
            if (tx >= 0 && tx < Board.SIZE && ty >= 0 && ty < Board.SIZE) {
                targetButtons[ty][tx].setBackground(previewColor);
            }
        }
    }

    private void handleBoardClick(int boardOwner, int x, int y) {
        if (gameOver || hideAllShipsDuringHandoff) return;

        if (isPlacementPhase) {
            if (boardOwner == activePlayer) {
                placeShipOnActiveBoard(x, y);
            }
            return;
        }

        if (settings.getGameMode() == GameSettings.GameMode.PLAYER_VS_AI) {
            if (boardOwner == 2) {
                handlePvEAttack(x, y);
            }
        } else {
            int enemyBoardOwner = (activePlayer == 1) ? 2 : 1;
            if (boardOwner == enemyBoardOwner) {
                handlePvPAttack(x, y);
            }
        }
    }

    private void placeShipOnActiveBoard(int x, int y) {
        if (currentShipIndex >= GameSettings.DEFAULT_SHIP_TYPES.length) return;

        char type = GameSettings.DEFAULT_SHIP_TYPES[currentShipIndex];
        int size = GameSettings.DEFAULT_SHIP_SIZES[currentShipIndex];
        Ship ship = new Ship(type, size, x, y, currentDirection);

        if (getActiveBoard().placeShip(ship)) {
            currentShipIndex++;
            refreshBoards();
            if (currentShipIndex >= GameSettings.DEFAULT_SHIP_TYPES.length) {
                rotateButton.setEnabled(false);
                confirmButton.setEnabled(true);
                statusLabel.setText("All ships placed! Click '" + confirmButton.getText() + "' or 'Clear Board'.");
            } else {
                updatePlacementPrompt();
            }
        }
    }

    private void handlePvEAttack(int x, int y) {
        Board.AttackResult result = board2.attack(x, y);
        if (result == Board.AttackResult.ALREADY_ATTACKED) {
            statusLabel.setText("Already attacked " + formatCoord(x, y) + "! Pick another cell.");
            return;
        }

        refreshBoards();
        if (board2.allShipsSunk()) {
            gameOver = true;
            refreshBoards();
            statusLabel.setText("VICTORY! You sank all enemy ships!");
            JOptionPane.showMessageDialog(this, "Congratulations! You won!");
            return;
        }

        aiPlayer.takeTurn(board1);
        refreshBoards();

        if (board1.allShipsSunk()) {
            gameOver = true;
            refreshBoards();
            statusLabel.setText("DEFEAT! Enemy sank all your ships!");
            JOptionPane.showMessageDialog(this, "Game Over! The AI sank your fleet.");
        } else {
            statusLabel.setText("Shot at " + formatCoord(x, y) + ": " + result + " | Your turn!");
        }
    }

    private void handlePvPAttack(int x, int y) {
        Board targetBoard = (activePlayer == 1) ? board2 : board1;
        Board.AttackResult result = targetBoard.attack(x, y);

        if (result == Board.AttackResult.ALREADY_ATTACKED) {
            statusLabel.setText("Already attacked " + formatCoord(x, y) + "! Pick another cell.");
            return;
        }

        if (targetBoard.allShipsSunk()) {
            gameOver = true;
            refreshBoards();
            statusLabel.setText("PLAYER " + activePlayer + " WINS!");
            JOptionPane.showMessageDialog(this, "Player " + activePlayer + " sank all enemy ships and wins!");
            return;
        }

        int nextPlayer = (activePlayer == 1) ? 2 : 1;
        hideAllShipsDuringHandoff = true;
        refreshBoards();
        JOptionPane.showMessageDialog(
                this,
                "Player " + activePlayer + " shot at " + formatCoord(x, y) + ": " + result +
                "\nPass the device to Player " + nextPlayer + "."
        );

        activePlayer = nextPlayer;
        hideAllShipsDuringHandoff = false;
        statusLabel.setText("Player " + activePlayer + "'s Turn! Attack Player " + (activePlayer == 1 ? 2 : 1) + "'s board.");
        refreshBoards();
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

    private void refreshBoards() {
        boolean showBoard1Ships = shouldShowShips(1);
        boolean showBoard2Ships = shouldShowShips(2);

        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                updateButtonAppearance(buttons1[y][x], board1, x, y, showBoard1Ships);
                updateButtonAppearance(buttons2[y][x], board2, x, y, showBoard2Ships);
            }
        }
        updateFleetLabels(shipLabels1, board1);
        updateFleetLabels(shipLabels2, board2);
    }

    private boolean shouldShowShips(int boardOwner) {
        if (gameOver) return true;
        if (hideAllShipsDuringHandoff) return false;
        if (settings.getGameMode() == GameSettings.GameMode.PLAYER_VS_AI) {
            return boardOwner == 1;
        }
        return activePlayer == boardOwner;
    }

    private void updateButtonAppearance(JButton btn, Board board, int x, int y, boolean showShips) {
        Board.CellState state = board.getCellState(x, y);
        btn.setText("");

        switch (state) {
            case EMPTY -> btn.setBackground(new Color(173, 216, 230));
            case SHIP -> btn.setBackground(showShips ? Color.DARK_GRAY : new Color(173, 216, 230));
            case HIT -> {
                if (isShipSunkAt(board, x, y)) {
                    btn.setBackground(new Color(120, 0, 0));
                    btn.setText("X");
                } else {
                    btn.setBackground(Color.RED);
                }
            }
            case MISS -> btn.setBackground(Color.WHITE);
        }
    }

    private void updateFleetLabels(JLabel[] labels, Board board) {
        List<Ship> ships = board.getShips();
        char[] types = GameSettings.DEFAULT_SHIP_TYPES;
        int[] sizes = GameSettings.DEFAULT_SHIP_SIZES;

        for (int i = 0; i < labels.length; i++) {
            String shipName = getShipName(types[i]) + " (" + sizes[i] + ")";
            if (i < ships.size()) {
                Ship ship = ships.get(i);
                if (ship.isSunk()) {
                    labels[i].setForeground(Color.RED);
                    labels[i].setText("<html><s>" + shipName + " - SUNK</s></html>");
                } else {
                    labels[i].setForeground(new Color(0, 120, 0));
                    labels[i].setText(shipName + " - Afloat");
                }
            } else {
                labels[i].setForeground(Color.GRAY);
                labels[i].setText(shipName + " - Not placed");
            }
        }
    }

    private boolean isShipSunkAt(Board board, int x, int y) {
        for (Ship ship : board.getShips()) {
            if (ship.occupies(x, y)) {
                return ship.isSunk();
            }
        }
        return false;
    }

    private String getShipName(char type) {
        return switch (type) {
            case 'C' -> "Carrier";
            case 'B' -> "Battleship";
            case 'R' -> "Cruiser";
            case 'S' -> "Submarine";
            case 'D' -> "Destroyer";
            default -> "Ship";
        };
    }

    private String formatCoord(int x, int y) {
        return "" + (char) ('A' + x) + (y + 1);
    }
}