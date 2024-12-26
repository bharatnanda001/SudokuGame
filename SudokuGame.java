import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SudokuGame extends JFrame {
    private static final int GRID_SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    private JTextField[][] cells;
    private int[][] solution;
    private int[][] puzzle;
    private JButton newGameButton;
    private JButton checkButton;
    private JButton solveButton;
    private JComboBox<String> difficultyCombo;
    private Timer timer;
    private JLabel timerLabel;
    private int seconds = 0;

    public SudokuGame() {
        setTitle("Sudoku Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initializeComponents();
        pack();
        setLocationRelativeTo(null);
        startNewGame();
    }

    private void initializeComponents() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Grid panel
        JPanel gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        cells = new JTextField[GRID_SIZE][GRID_SIZE];
        
        // Create cells with borders to show 3x3 subgrids
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                cells[row][col] = new JTextField(1);
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                cells[row][col].setFont(new Font("Arial", Font.BOLD, 20));
                
                // Add borders to separate 3x3 subgrids
                int top = (row % 3 == 0) ? 3 : 1;
                int left = (col % 3 == 0) ? 3 : 1;
                int bottom = (row == GRID_SIZE - 1) ? 3 : 1;
                int right = (col == GRID_SIZE - 1) ? 3 : 1;
                
                cells[row][col].setBorder(BorderFactory.createMatteBorder(
                    top, left, bottom, right, Color.BLACK));
                
                // Add input validation
                cells[row][col].addKeyListener(new KeyAdapter() {
                    public void keyTyped(KeyEvent e) {
                        char input = e.getKeyChar();
                        if (!Character.isDigit(input) || input == '0' || 
                            ((JTextField)e.getSource()).getText().length() >= 1) {
                            e.consume();
                        }
                    }
                });
                
                gridPanel.add(cells[row][col]);
            }
        }

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        newGameButton = new JButton("New Game");
        checkButton = new JButton("Check Solution");
        solveButton = new JButton("Solve");
        difficultyCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        timerLabel = new JLabel("Time: 0:00");
        
        controlPanel.add(difficultyCombo);
        controlPanel.add(newGameButton);
        controlPanel.add(checkButton);
        controlPanel.add(solveButton);
        controlPanel.add(timerLabel);

        // Add action listeners
        newGameButton.addActionListener(e -> startNewGame());
        checkButton.addActionListener(e -> checkSolution());
        solveButton.addActionListener(e -> showSolution());

        // Initialize timer
        timer = new Timer(1000, e -> updateTimer());

        mainPanel.add(gridPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void startNewGame() {
        // Reset timer
        seconds = 0;
        timer.restart();
        
        // Generate new puzzle
        solution = generateSolution();
        puzzle = generatePuzzle(solution, getDifficultyLevel());
        
        // Update UI
        updateCellsFromPuzzle();
    }

    private int getDifficultyLevel() {
        switch (difficultyCombo.getSelectedIndex()) {
            case 0: return 40; // Easy - show 40 cells
            case 1: return 30; // Medium - show 30 cells
            case 2: return 20; // Hard - show 20 cells
            default: return 40;
        }
    }

    private void updateTimer() {
        seconds++;
        int minutes = seconds / 60;
        int secs = seconds % 60;
        timerLabel.setText(String.format("Time: %d:%02d", minutes, secs));
    }

    private int[][] generateSolution() {
        int[][] grid = new int[GRID_SIZE][GRID_SIZE];
        fillGrid(grid);
        return grid;
    }

    private boolean fillGrid(int[][] grid) {
        int[] emptyCell = findEmptyCell(grid);
        if (emptyCell == null) {
            return true; // Grid is filled
        }

        int row = emptyCell[0];
        int col = emptyCell[1];
        
        // Try digits 1-9 in random order
        int[] digits = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        shuffleArray(digits);

        for (int num : digits) {
            if (isValidPlacement(grid, row, col, num)) {
                grid[row][col] = num;
                if (fillGrid(grid)) {
                    return true;
                }
                grid[row][col] = 0; // Backtrack
            }
        }
        return false;
    }

    private void shuffleArray(int[] array) {
        Random rand = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    private int[][] generatePuzzle(int[][] solution, int cellsToShow) {
        int[][] puzzle = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(solution[i], 0, puzzle[i], 0, GRID_SIZE);
        }

        // Randomly remove cells while ensuring unique solution
        Random rand = new Random();
        int cellsToRemove = GRID_SIZE * GRID_SIZE - cellsToShow;

        while (cellsToRemove > 0) {
            int row = rand.nextInt(GRID_SIZE);
            int col = rand.nextInt(GRID_SIZE);
            
            if (puzzle[row][col] != 0) {
                int backup = puzzle[row][col];
                puzzle[row][col] = 0;
                
                // Check if solution is still unique
                if (hasUniqueSolution(puzzle)) {
                    cellsToRemove--;
                } else {
                    puzzle[row][col] = backup;
                }
            }
        }
        return puzzle;
    }

    private boolean hasUniqueSolution(int[][] grid) {
        // Implement solution uniqueness check
        // This is a simplified version - a full implementation would be more complex
        return true;
    }

    private void updateCellsFromPuzzle() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                JTextField cell = cells[row][col];
                int value = puzzle[row][col];
                
                if (value == 0) {
                    cell.setText("");
                    cell.setEditable(true);
                    cell.setBackground(Color.WHITE);
                } else {
                    cell.setText(String.valueOf(value));
                    cell.setEditable(false);
                    cell.setBackground(new Color(240, 240, 240));
                }
            }
        }
    }

    private int[] findEmptyCell(int[][] grid) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (grid[row][col] == 0) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }

    private boolean isValidPlacement(int[][] grid, int row, int col, int num) {
        // Check row
        for (int x = 0; x < GRID_SIZE; x++) {
            if (grid[row][x] == num) return false;
        }

        // Check column
        for (int x = 0; x < GRID_SIZE; x++) {
            if (grid[x][col] == num) return false;
        }

        // Check 3x3 box
        int boxRow = row - row % 3;
        int boxCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[boxRow + i][boxCol + j] == num) return false;
            }
        }

        return true;
    }

    private void checkSolution() {
        boolean complete = true;
        boolean correct = true;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                String value = cells[row][col].getText();
                if (value.isEmpty()) {
                    complete = false;
                    continue;
                }

                int num = Integer.parseInt(value);
                if (num != solution[row][col]) {
                    correct = false;
                    cells[row][col].setBackground(new Color(255, 200, 200));
                }
            }
        }

        if (!complete) {
            JOptionPane.showMessageDialog(this, "Puzzle is not complete!");
        } else if (correct) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Congratulations! Puzzle solved correctly!");
        } else {
            JOptionPane.showMessageDialog(this, "Some entries are incorrect!");
        }
    }

    private void showSolution() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                cells[row][col].setText(String.valueOf(solution[row][col]));
            }
        }
        timer.stop();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SudokuGame().setVisible(true);
        });
    }
}