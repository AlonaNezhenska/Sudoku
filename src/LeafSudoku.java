import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;

public class LeafSudoku extends JFrame {
    private SudokuBoard board;
    private SudokuSolver solver;
    private JButton[][] cellButtons;
    private JButton solveButton;
    private JButton importButton;
    private JLabel timeLabel;

    // Colors
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
        setTitle("Sudoku Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // the game board
        board = new SudokuBoard();
        cellButtons = new JButton[9][9];

        // main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(LIGHT_GREEN);

        // board panel
        JPanel boardPanel = createBoardPanel();
        mainPanel.add(boardPanel, BorderLayout.CENTER);

        // control panel
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private JPanel createBoardPanel() {
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 6, 6));
        boardPanel.setBorder(BorderFactory.createLineBorder(DARK_GREEN, 3));
        boardPanel.setBackground(DARK_GREEN);

        // 3x3 subgrids
        for (int boxRow = 0; boxRow < 3; boxRow++) {
            for (int boxCol = 0; boxCol < 3; boxCol++) {
                JPanel subGrid = new JPanel(new GridLayout(3, 3, 2, 2));
                subGrid.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                subGrid.setBackground(Color.WHITE);

                // each subgrid 3x3 cells
                for (int cellRow = 0; cellRow < 3; cellRow++) {
                    for (int cellCol = 0; cellCol < 3; cellCol++) {
                        int row = boxRow * 3 + cellRow;
                        int col = boxCol * 3 + cellCol;

                        JButton cellButton = new JButton();
                        cellButton.setPreferredSize(new Dimension(45, 45));
                        cellButton.setFont(new Font("Arial", Font.BOLD, 18));
                        cellButton.setFocusPainted(false);
                        cellButton.setMargin(new Insets(0, 0, 0, 0));

                        // different styling for given numbers vs user input
                        if (board.isGivenNumber(row, col)) {
                            cellButton.setBackground(Color.WHITE);
                            cellButton.setForeground(Color.BLACK);
                            cellButton.setText(String.valueOf(board.getCell(row, col)));
                            cellButton.setEnabled(false);
                        } else {
                            cellButton.setBackground(Color.WHITE);
                            cellButton.setForeground(LEAF_GREEN);
                            cellButton.setEnabled(false); // Disable manual input
                        }

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
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBackground(LIGHT_GREEN);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Import File button
        importButton = new JButton("Import File");
        importButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        importButton.setBackground(new Color(160, 82, 45));
        importButton.setForeground(Color.WHITE);
        importButton.setFocusPainted(false);
        importButton.setPreferredSize(new Dimension(120, 35));
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadPuzzleFromFile();
            }
        });

        // AI Solve button
        solveButton = new JButton("AI Solve");
        solveButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        solveButton.setBackground(SOLVER_BLUE);
        solveButton.setForeground(Color.WHITE);
        solveButton.setFocusPainted(false);
        solveButton.setPreferredSize(new Dimension(120, 35));
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                autoSolvePuzzle();
            }
        });

        // Time label
        timeLabel = new JLabel("Time: --");
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        timeLabel.setForeground(DARK_GREEN);

        controlPanel.add(importButton);
        controlPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        controlPanel.add(solveButton);
        controlPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        controlPanel.add(timeLabel);

        return controlPanel;
    }

    private void autoSolvePuzzle() {
        // Check if there's a puzzle loaded
        boolean hasPuzzle = false;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board.getCell(i, j) != 0) {
                    hasPuzzle = true;
                    break;
                }
            }
            if (hasPuzzle) break;
        }

        if (!hasPuzzle) {
            JOptionPane.showMessageDialog(this,
                    "Please import a puzzle first!",
                    "No Puzzle Loaded",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int response = JOptionPane.showConfirmDialog(this,
                "Solve the puzzle using AI?",
                "AI Solve Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (response != JOptionPane.YES_OPTION) {
            return;
        }

        // current board state -> 2D array for solver
        int[][] currentPuzzle = new int[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                currentPuzzle[i][j] = board.getCell(i, j);
            }
        }

        // using the solver
        SudokuSolver.SolverResult result = solver.solve(currentPuzzle);

        if (result.success) {
            // update the board with solution
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (!board.isGivenNumber(i, j)) {
                        board.setCell(i, j, result.solution[i][j]);
                    }
                }
            }

            // Update the GUI
            refreshBoard();

            // Update time label
            timeLabel.setText(String.format("Time: %.3fs", result.executionTime));

            JOptionPane.showMessageDialog(this,
                    String.format("Puzzle solved in %.3f seconds!", result.executionTime),
                    "Solution Found",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No solution found for this puzzle!",
                    "Solve Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPuzzleFromFile() {
        JFileChooser fileChooser = new JFileChooser();

        // Set file filter for supported formats
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Sudoku Files (*.txt, *.csv)", "txt", "csv");
        fileChooser.setFileFilter(filter);

        // Set current directory to user's home
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try {
                // Parse the puzzle from file
                int[][] puzzle = solver.parsePuzzleFromFile(selectedFile.getAbsolutePath());

                // Load the puzzle into the board
                board.loadPuzzle(puzzle);

                // Reset time label
                timeLabel.setText("Time: --");

                // Refresh the GUI
                refreshBoard();

                JOptionPane.showMessageDialog(this,
                        "Puzzle loaded successfully!",
                        "Load Successful",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading file: " + e.getMessage(),
                        "Load Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid file format. Please ensure it's a valid 9x9 Sudoku grid.",
                        "Invalid File",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshBoard() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board.isGivenNumber(row, col)) {
                    cellButtons[row][col].setText(String.valueOf(board.getCell(row, col)));
                    cellButtons[row][col].setForeground(Color.BLACK);
                    cellButtons[row][col].setBackground(Color.WHITE);
                } else {
                    if (board.getCell(row, col) != 0) {
                        cellButtons[row][col].setText(String.valueOf(board.getCell(row, col)));
                        cellButtons[row][col].setForeground(LEAF_GREEN);
                    } else {
                        cellButtons[row][col].setText("");
                    }
                    cellButtons[row][col].setBackground(Color.WHITE);
                }
            }
        }
    }

    private void setupGame() {
        // Start with empty board
        board.clearBoard();
        refreshBoard();
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

    public SudokuBoard() {
        board = new int[9][9];
        givenNumbers = new boolean[9][9];
        clearBoard();
    }

    public void clearBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = 0;
                givenNumbers[i][j] = false;
            }
        }
    }

    public void loadPuzzle(int[][] puzzle) {
        clearBoard();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = puzzle[i][j];
                // Mark non-zero cells as given numbers
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