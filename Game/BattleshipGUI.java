import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class BattleshipGUI extends JFrame {
    private final GameSettings settings;
    private final Board playerBoard;
    private final Board aiBoard;
    private final AIPlayer aiPlayer;
    private final JButton[][] playerButtons;
    private final JButton[][] aiButtons;
    private final JLabel[] playerShipLabels;
    private final JLabel[] aiShipLabels;
    private final JLabel statusLabel;
    private final JButton rotateButton;
    private final Random random;

    private boolean isPlacementPhase;
    private int currentShipIndex;
    private Ship.Direction currentDirection;
    private boolean gameOver;
    private int hoverX = -1;
    private int hoverY = -1;

    public BattleshipGUI(GameSettings settings) {
        this.settings = settings;
        this.playerBoard = new Board();
        this.aiBoard = new Board();
        this.aiPlayer = new AIPlayer(settings.getAiDifficulty());
        this.playerButtons = new JButton[Board.SIZE][Board.SIZE];
        this.aiButtons = new JButton[Board.SIZE][Board.SIZE];
        this.playerShipLabels = new JLabel[GameSettings.DEFAULT_SHIP_TYPES.length];
        this.aiShipLabels = new JLabel[GameSettings.DEFAULT_SHIP_TYPES.length];
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

        JPanel boardsContainer = new JPanel(new GridLayout(1, 2, 25, 0));
        boardsContainer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        boardsContainer.add(createBoardPanel("YOUR FLEET", playerButtons, playerShipLabels, true));
        boardsContainer.add(createBoardPanel("ENEMY WATERS", aiButtons, aiShipLabels, false));
        add(boardsContainer, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton resetButton = new JButton("New Game");
        resetButton.addActionListener(e -> initGame());
        bottomPanel.add(resetButton);
        add(bottomPanel, BorderLayout.SOUTH);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_R) {
                if (isPlacementPhase && !gameOver) {
                    toggleDirection();
                    if (hoverX != -1 && hoverY != -1) {
                        previewShipPlacement(hoverX, hoverY);
                    }
                }
            }
            return false;
        });

        initGame();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createBoardPanel(String title, JButton[][] buttons, JLabel[] shipLabels, boolean isPlayerBoard) {
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

                    if (isPlayerBoard) {
                        btn.addActionListener(e -> handlePlayerBoardClick(fx, fy));
                        btn.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent e) {
                                hoverX = fx;
                                hoverY = fy;
                                previewShipPlacement(hoverX, hoverY);
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                hoverX = -1;
                                hoverY = -1;
                                refreshBoards();
                            }
                        });
                    } else {
                        btn.addActionListener(e -> handleEnemyBoardClick(fx, fy));
                    }

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
        playerBoard.resetBoard();
        aiBoard.resetBoard();
        aiPlayer.reset();
        gameOver = false;
        currentShipIndex = 0;
        currentDirection = Ship.Direction.HORIZONTAL;
        rotateButton.setText("Direction: HORIZONTAL (Press R)");

        autoPlaceShips(aiBoard);

        if (settings.getPlacementMode() == GameSettings.PlacementMode.AUTO) {
            autoPlaceShips(playerBoard);
            isPlacementPhase = false;
            rotateButton.setEnabled(false);
            statusLabel.setText("Battle Started! Click on ENEMY WATERS to fire.");
        } else {
            isPlacementPhase = true;
            rotateButton.setEnabled(true);
            updatePlacementPrompt();
        }
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
        statusLabel.setText("Place " + getShipName(type) + " (Size: " + size + ") on YOUR FLEET");
    }

    private void previewShipPlacement(int x, int y) {
        if (!isPlacementPhase || gameOver) return;
        refreshBoards();

        char type = GameSettings.DEFAULT_SHIP_TYPES[currentShipIndex];
        int size = GameSettings.DEFAULT_SHIP_SIZES[currentShipIndex];
        Ship tempShip = new Ship(type, size, x, y, currentDirection);
        boolean valid = playerBoard.canPlaceShip(tempShip);
        Color previewColor = valid ? new Color(144, 238, 144) : new Color(255, 160, 160);

        for (int i = 0; i < size; i++) {
            int tx = (currentDirection == Ship.Direction.HORIZONTAL) ? x + i : x;
            int ty = (currentDirection == Ship.Direction.VERTICAL) ? y + i : y;
            if (tx >= 0 && tx < Board.SIZE && ty >= 0 && ty < Board.SIZE) {
                playerButtons[ty][tx].setBackground(previewColor);
            }
        }
    }

    private void handlePlayerBoardClick(int x, int y) {
        if (!isPlacementPhase || gameOver) return;

        char type = GameSettings.DEFAULT_SHIP_TYPES[currentShipIndex];
        int size = GameSettings.DEFAULT_SHIP_SIZES[currentShipIndex];
        Ship ship = new Ship(type, size, x, y, currentDirection);

        if (playerBoard.placeShip(ship)) {
            currentShipIndex++;
            refreshBoards();
            if (currentShipIndex >= GameSettings.DEFAULT_SHIP_TYPES.length) {
                isPlacementPhase = false;
                rotateButton.setEnabled(false);
                statusLabel.setText("All ships placed! Click on ENEMY WATERS to attack.");
            } else {
                updatePlacementPrompt();
            }
        }
    }

    private void handleEnemyBoardClick(int x, int y) {
        if (isPlacementPhase || gameOver) return;

        Board.AttackResult result = aiBoard.attack(x, y);
        if (result == Board.AttackResult.ALREADY_ATTACKED) {
            statusLabel.setText("Already attacked " + formatCoord(x, y) + "! Pick another cell.");
            return;
        }

        refreshBoards();
        if (aiBoard.allShipsSunk()) {
            gameOver = true;
            statusLabel.setText("VICTORY! You sank all enemy ships!");
            JOptionPane.showMessageDialog(this, "Congratulations! You won!");
            return;
        }

        aiPlayer.takeTurn(playerBoard);
        refreshBoards();

        if (playerBoard.allShipsSunk()) {
            gameOver = true;
            statusLabel.setText("DEFEAT! Enemy sank all your ships!");
            JOptionPane.showMessageDialog(this, "Game Over! The AI sank your fleet.");
        } else {
            statusLabel.setText("Shot at " + formatCoord(x, y) + ": " + result + " | Your turn!");
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

    private void refreshBoards() {
        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                updateButtonAppearance(playerButtons[y][x], playerBoard, x, y, true);
                updateButtonAppearance(aiButtons[y][x], aiBoard, x, y, false);
            }
        }
        updateFleetLabels(playerShipLabels, playerBoard);
        updateFleetLabels(aiShipLabels, aiBoard);
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