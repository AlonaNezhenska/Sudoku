import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class LeafSudoku extends JFrame {
    private SudokuBoard board;
    private SudokuSolver solver;
    private JButton[][] cellButtons;
    private JButton[] numberButtons;
    private JButton clearCellButton;
    private JButton newGameButton;
    private JButton solveButton;
    private JButton loadPuzzleButton;
    private JLabel titleLabel;
    private JLabel instructionLabel;
    private JLabel selectNumberLabel;
    private JLabel performanceLabel;
    private int selectedNumber = 0;

    // Color scheme matching the leaf theme
    private final Color LEAF_GREEN = new Color(76, 153, 0);
    private final Color LIGHT_GREEN = new Color(220, 240, 200);
    private final Color DARK_GREEN = new Color(0, 100, 0);
    private final Color SOLVER_BLUE = new Color(70, 130, 180);

    public LeafSudoku() {
        solver = new SudokuSolver();
        initializeGUI();
        setupGame();
    }

    private void initializeGUI() {
        setTitle("Leaf Sudoku - AI Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the game board
        board = new SudokuBoard();
        cellButtons = new JButton[9][9];
        numberButtons = new JButton[9];

        // Create main panel with leaf-themed background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(LIGHT_GREEN);

        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Create center panel for board and controls
        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setBackground(LIGHT_GREEN);

        // Create board panel
        JPanel boardPanel = createBoardPanel();
        centerPanel.add(boardPanel, BorderLayout.CENTER);

        // Create control panel
        JPanel controlPanel = createControlPanel();
        centerPanel.add(controlPanel, BorderLayout.EAST);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(LIGHT_GREEN);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Title
        titleLabel = new JLabel("Leaf Sudoku - AI Solver");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 32));
        titleLabel.setForeground(DARK_GREEN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Instruction
        instructionLabel = new JLabel("Play manually or use AI solver. Black numbers are fixed.");
        instructionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        instructionLabel.setForeground(Color.DARK_GRAY);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        headerPanel.add(instructionLabel);

        return headerPanel;
    }

    private JPanel createBoardPanel() {
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 6, 6));
        boardPanel.setBorder(BorderFactory.createLineBorder(DARK_GREEN, 3));
        boardPanel.setBackground(DARK_GREEN);

        // Create 3x3 subgrids
        for (int boxRow = 0; boxRow < 3; boxRow++) {
            for (int boxCol = 0; boxCol < 3; boxCol++) {
                JPanel subGrid = new JPanel(new GridLayout(3, 3, 2, 2));
                subGrid.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                subGrid.setBackground(Color.WHITE);

                // Fill each subgrid with 3x3 cells
                for (int cellRow = 0; cellRow < 3; cellRow++) {
                    for (int cellCol = 0; cellCol < 3; cellCol++) {
                        int row = boxRow * 3 + cellRow;
                        int col = boxCol * 3 + cellCol;

                        JButton cellButton = new JButton();
                        cellButton.setPreferredSize(new Dimension(45, 45));
                        cellButton.setFont(new Font("Arial", Font.BOLD, 18));
                        cellButton.setFocusPainted(false);
                        cellButton.setMargin(new Insets(0, 0, 0, 0));

                        // Set different styling for given numbers vs user input
                        if (board.isGivenNumber(row, col)) {
                            cellButton.setBackground(Color.WHITE);
                            cellButton.setForeground(Color.BLACK);
                            cellButton.setText(String.valueOf(board.getCell(row, col)));
                            cellButton.setEnabled(false);
                        } else {
                            cellButton.setBackground(Color.WHITE);
                            cellButton.setForeground(LEAF_GREEN);
                            cellButton.setEnabled(true);
                        }

                        // Add action listener for cell input
                        final int finalRow = row;
                        final int finalCol = col;
                        cellButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                handleCellClick(finalRow, finalCol);
                            }
                        });

                        cellButtons[row][col] = cellButton;
                        subGrid.add(cellButton);
                    }
                }
                boardPanel.add(subGrid);
            }
        }

        return boardPanel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(LIGHT_GREEN);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        // Select a Number section
        selectNumberLabel = new JLabel("Select a Number");
        selectNumberLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        selectNumberLabel.setForeground(DARK_GREEN);
        selectNumberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel numberPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        numberPanel.setBackground(LIGHT_GREEN);
        numberPanel.setMaximumSize(new Dimension(150, 150));

        for (int i = 0; i < 9; i++) {
            numberButtons[i] = new JButton(String.valueOf(i + 1));
            numberButtons[i].setFont(new Font("Arial", Font.BOLD, 18));
            numberButtons[i].setPreferredSize(new Dimension(45, 45));
            numberButtons[i].setBackground(Color.WHITE);
            numberButtons[i].setForeground(LEAF_GREEN);
            numberButtons[i].setFocusPainted(false);

            final int number = i + 1;
            numberButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectedNumber = number;
                    updateNumberButtons();
                }
            });

            numberPanel.add(numberButtons[i]);
        }

        // AI Solver button
        solveButton = new JButton("AI Solve");
        solveButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        solveButton.setBackground(SOLVER_BLUE);
        solveButton.setForeground(Color.WHITE);
        solveButton.setFocusPainted(false);
        solveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        solveButton.setMaximumSize(new Dimension(150, 40));
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                autoSolvePuzzle();
            }
        });

        // Load Puzzle button
        loadPuzzleButton = new JButton("Load Puzzle");
        loadPuzzleButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loadPuzzleButton.setBackground(new Color(160, 82, 45)); // Sienna brown
        loadPuzzleButton.setForeground(Color.WHITE);
        loadPuzzleButton.setFocusPainted(false);
        loadPuzzleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadPuzzleButton.setMaximumSize(new Dimension(150, 40));
        loadPuzzleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadPuzzleFromFile();
            }
        });

        // Control buttons
        clearCellButton = new JButton("Clear Cell");
        clearCellButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        clearCellButton.setBackground(Color.WHITE);
        clearCellButton.setForeground(Color.RED);
        clearCellButton.setFocusPainted(false);
        clearCellButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearCellButton.setMaximumSize(new Dimension(150, 40));
        clearCellButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedNumber = 0;
                updateNumberButtons();
            }
        });

        newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        newGameButton.setBackground(LEAF_GREEN);
        newGameButton.setForeground(Color.WHITE);
        newGameButton.setFocusPainted(false);
        newGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameButton.setMaximumSize(new Dimension(150, 40));
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startNewGame();
            }
        });

        // Performance label
        performanceLabel = new JLabel("Performance: --");
        performanceLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        performanceLabel.setForeground(Color.DARK_GRAY);
        performanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to control panel
        controlPanel.add(selectNumberLabel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(numberPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPanel.add(solveButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(loadPuzzleButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(clearCellButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(newGameButton);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        controlPanel.add(performanceLabel);

        return controlPanel;
    }

    private void handleCellClick(int row, int col) {
        if (selectedNumber != 0 && !board.isGivenNumber(row, col)) {
            if (selectedNumber == 0) { // Clear cell
                board.setCell(row, col, 0);
                cellButtons[row][col].setText("");
                cellButtons[row][col].setForeground(LEAF_GREEN);
            } else {
                board.setCell(row, col, selectedNumber);
                cellButtons[row][col].setText(String.valueOf(selectedNumber));
                cellButtons[row][col].setForeground(LEAF_GREEN);

                // Check if the game is complete
                if (board.isComplete()) {
                    JOptionPane.showMessageDialog(this,
                            "Congratulations! You've solved the Sudoku!",
                            "Game Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    private void updateNumberButtons() {
        for (int i = 0; i < 9; i++) {
            if (selectedNumber == i + 1) {
                numberButtons[i].setBackground(LEAF_GREEN);
                numberButtons[i].setForeground(Color.WHITE);
            } else {
                numberButtons[i].setBackground(Color.WHITE);
                numberButtons[i].setForeground(LEAF_GREEN);
            }
        }

        // Update clear button
        if (selectedNumber == 0) {
            clearCellButton.setBackground(LEAF_GREEN);
            clearCellButton.setForeground(Color.WHITE);
        } else {
            clearCellButton.setBackground(Color.WHITE);
            clearCellButton.setForeground(Color.RED);
        }
    }

    private void autoSolvePuzzle() {
        int response = JOptionPane.showConfirmDialog(this,
                "This will automatically solve the puzzle using AI.\nContinue?",
                "AI Solve Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (response != JOptionPane.YES_OPTION) {
            return;
        }

        // Convert current board state to 2D array for solver
        int[][] currentPuzzle = new int[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                currentPuzzle[i][j] = board.getCell(i, j);
            }
        }

        // Use the solver
        SudokuSolver.SolverResult result = solver.solve(currentPuzzle);

        if (result.success) {
            // Update the board with solution
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (!board.isGivenNumber(i, j)) {
                        board.setCell(i, j, result.solution[i][j]);
                    }
                }
            }

            // Update the GUI
            refreshBoard();

            // Show performance metrics
            performanceLabel.setText(String.format("Steps: %d, Backtracks: %d, Time: %.3fs",
                    result.steps, result.backtracks, result.executionTime));

            JOptionPane.showMessageDialog(this,
                    String.format("Puzzle solved!\n\nAlgorithm Performance:\n• Steps: %d\n• Backtracks: %d\n• Time: %.3f seconds",
                            result.steps, result.backtracks, result.executionTime),
                    "AI Solve Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No solution found for this puzzle configuration!",
                    "Solve Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPuzzleFromFile() {
        JOptionPane.showMessageDialog(this,
                "File loading feature would be implemented here.\nFor now, use 'New Game' for sample puzzles.",
                "Load Puzzle",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void startNewGame() {
        board.generateNewPuzzle();
        selectedNumber = 0;
        performanceLabel.setText("Performance: --");
        updateNumberButtons();
        refreshBoard();
    }

    private void refreshBoard() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board.isGivenNumber(row, col)) {
                    cellButtons[row][col].setText(String.valueOf(board.getCell(row, col)));
                    cellButtons[row][col].setForeground(Color.BLACK);
                    cellButtons[row][col].setEnabled(false);
                } else {
                    if (board.getCell(row, col) != 0) {
                        cellButtons[row][col].setText(String.valueOf(board.getCell(row, col)));
                        cellButtons[row][col].setForeground(LEAF_GREEN);
                    } else {
                        cellButtons[row][col].setText("");
                    }
                    cellButtons[row][col].setEnabled(true);
                }
            }
        }
    }

    private void setupGame() {
        board.generateNewPuzzle();
        selectedNumber = 0;
        updateNumberButtons();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LeafSudoku().setVisible(true);
            }
        });
    }
}

class SudokuBoard {
    private int[][] board;
    private boolean[][] givenNumbers;
    private Random random;

    public SudokuBoard() {
        board = new int[9][9];
        givenNumbers = new boolean[9][9];
        random = new Random();
    }

    public void generateNewPuzzle() {
        // Clear the board
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = 0;
                givenNumbers[i][j] = false;
            }
        }

        // Generate a simple puzzle
        generateSimplePuzzle();
    }

    private void generateSimplePuzzle() {
        // A simple predefined puzzle (easy level)
        int[][] puzzle = {
                {5, 3, 0, 0, 7, 0, 0, 0, 0},
                {6, 0, 0, 1, 9, 5, 0, 0, 0},
                {0, 9, 8, 0, 0, 0, 0, 6, 0},
                {8, 0, 0, 0, 6, 0, 0, 0, 3},
                {4, 0, 0, 8, 0, 3, 0, 0, 1},
                {7, 0, 0, 0, 2, 0, 0, 0, 6},
                {0, 6, 0, 0, 0, 0, 2, 8, 0},
                {0, 0, 0, 4, 1, 9, 0, 0, 5},
                {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };

        // Copy the puzzle and mark given numbers
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = puzzle[i][j];
                givenNumbers[i][j] = (puzzle[i][j] != 0);
            }
        }
    }

    public int getCell(int row, int col) {
        return board[row][col];
    }

    public void setCell(int row, int col, int value) {
        if (!givenNumbers[row][col]) {
            board[row][col] = value;
        }
    }

    public boolean isGivenNumber(int row, int col) {
        return givenNumbers[row][col];
    }

    public boolean isComplete() {
        // Check if all cells are filled and valid
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }
        return isValidSolution();
    }

    private boolean isValidSolution() {
        // Check rows, columns, and 3x3 boxes
        for (int i = 0; i < 9; i++) {
            if (!isValidRow(i) || !isValidColumn(i) || !isValidBox(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidRow(int row) {
        boolean[] seen = new boolean[10];
        for (int col = 0; col < 9; col++) {
            int num = board[row][col];
            if (num != 0 && seen[num]) {
                return false;
            }
            seen[num] = true;
        }
        return true;
    }

    private boolean isValidColumn(int col) {
        boolean[] seen = new boolean[10];
        for (int row = 0; row < 9; row++) {
            int num = board[row][col];
            if (num != 0 && seen[num]) {
                return false;
            }
            seen[num] = true;
        }
        return true;
    }

    private boolean isValidBox(int box) {
        boolean[] seen = new boolean[10];
        int startRow = (box / 3) * 3;
        int startCol = (box % 3) * 3;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int num = board[startRow + i][startCol + j];
                if (num != 0 && seen[num]) {
                    return false;
                }
                seen[num] = true;
            }
        }
        return true;
    }
}