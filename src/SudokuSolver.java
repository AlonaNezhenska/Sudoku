import java.util.*;
import java.io.*;
import java.nio.file.*;

public class SudokuSolver {
    private int steps;
    private int backtracks;
    private long startTime;

    // CSP representation
    static class SudokuCSP {
        int[][] grid;
        List<int[]> variables;
        Map<String, List<Integer>> domains;

        SudokuCSP(int[][] grid) {
            this.grid = grid;
            this.variables = new ArrayList<>();
            this.domains = new HashMap<>();
            initializeVariablesAndDomains();
        }

        private void initializeVariablesAndDomains() {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    variables.add(new int[]{i, j});
                    String key = getKey(i, j);

                    if (grid[i][j] != 0) {
                        // Pre-assigned value - domain is fixed
                        domains.put(key, Arrays.asList(grid[i][j]));
                    } else {
                        // Empty cell - domain is all possible values
                        List<Integer> domain = new ArrayList<>();
                        for (int num = 1; num <= 9; num++) {
                            domain.add(num);
                        }
                        domains.put(key, domain);
                    }
                }
            }
        }

        private String getKey(int i, int j) {
            return i + "," + j;
        }
    }

    // Result class
    public static class SolverResult {
        public int[][] solution;
        public double executionTime;
        public int steps;
        public int backtracks;
        public boolean success;

        public SolverResult(int[][] solution, double executionTime, int steps, int backtracks, boolean success) {
            this.solution = solution;
            this.executionTime = executionTime;
            this.steps = steps;
            this.backtracks = backtracks;
            this.success = success;
        }
    }

    // File parsing methods
    public int[][] parsePuzzleFromFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        int[][] puzzle = new int[9][9];

        int row = 0;
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                continue; // Skip empty lines and comments
            }

            if (filePath.toLowerCase().endsWith(".csv")) {
                parseCSVLine(puzzle, row, line);
            } else {
                parseTXTLine(puzzle, row, line);
            }
            row++;

            if (row >= 9) break; // Only read first 9 valid rows
        }

        return puzzle;
    }

    private void parseCSVLine(int[][] puzzle, int row, String line) {
        String[] values = line.split(",");
        for (int col = 0; col < Math.min(9, values.length); col++) {
            puzzle[row][col] = parseCellValue(values[col].trim());
        }
    }

    private void parseTXTLine(int[][] puzzle, int row, String line) {
        // Handle various TXT formats: space-separated, tab-separated, or continuous
        String[] values;
        if (line.contains(",")) {
            values = line.split(",");
        } else if (line.contains(" ")) {
            values = line.split("\\s+");
        } else if (line.contains("\t")) {
            values = line.split("\t");
        } else {
            // Continuous format (e.g., "530070000")
            values = new String[9];
            for (int i = 0; i < Math.min(9, line.length()); i++) {
                values[i] = String.valueOf(line.charAt(i));
            }
        }

        for (int col = 0; col < Math.min(9, values.length); col++) {
            puzzle[row][col] = parseCellValue(values[col].trim());
        }
    }

    private int parseCellValue(String cell) {
        if (cell.isEmpty() || cell.equals(".") || cell.equals("_") || cell.equals("-")) {
            return 0; // Empty cell
        }
        try {
            return Integer.parseInt(cell);
        } catch (NumberFormatException e) {
            return 0; // Invalid character treated as empty
        }
    }

    // Main solving method
    public SolverResult solve(int[][] puzzle) {
        this.steps = 0;
        this.backtracks = 0;
        this.startTime = System.currentTimeMillis();

        SudokuCSP csp = new SudokuCSP(puzzle);
        Map<String, Integer> assignment = new HashMap<>(); // Initial state: empty assignment

        // Start recursive backtracking search
        Map<String, Integer> result = backtrackingSearch(assignment, csp);

        double executionTime = (System.currentTimeMillis() - startTime) / 1000.0;

        if (result != null) {
            int[][] solution = convertAssignmentToGrid(result);
            return new SolverResult(solution, executionTime, steps, backtracks, true);
        } else {
            return new SolverResult(null, executionTime, steps, backtracks, false);
        }
    }

    // Core backtracking algorithm following CSP approach
    private Map<String, Integer> backtrackingSearch(Map<String, Integer> assignment, SudokuCSP csp) {
        steps++;

        // Goal test: is the current assignment complete?
        if (isComplete(assignment, csp)) {
            return new HashMap<>(assignment); // Return complete assignment
        }

        // Select an unassigned variable
        int[] var = selectUnassignedVariable(assignment, csp);
        String varKey = csp.getKey(var[0], var[1]);

        // Try each value in the variable's domain
        for (int value : csp.domains.get(varKey)) {
            // Successor function: assign value if it doesn't conflict with current assignment
            if (isConsistent(assignment, var, value)) {
                // Assign the value
                assignment.put(varKey, value);

                // Recursively search
                Map<String, Integer> result = backtrackingSearch(assignment, csp);
                if (result != null) {
                    return result; // Solution found
                }

                // If no legal assignments, backtrack (remove assignment)
                assignment.remove(varKey);
                backtracks++;
            }
        }

        // If there are no legal assignments, return failure
        return null;
    }

    // Goal test: check if assignment is complete (all variables assigned)
    private boolean isComplete(Map<String, Integer> assignment, SudokuCSP csp) {
        return assignment.size() == csp.variables.size();
    }

    // Select next unassigned variable
    private int[] selectUnassignedVariable(Map<String, Integer> assignment, SudokuCSP csp) {
        for (int[] var : csp.variables) {
            String key = csp.getKey(var[0], var[1]);
            if (!assignment.containsKey(key)) {
                return var;
            }
        }
        return null; // All variables assigned
    }

    // Check if assignment is consistent (no conflicts)
    private boolean isConsistent(Map<String, Integer> assignment, int[] var, int value) {
        int row = var[0], col = var[1];

        // Check row constraint
        for (int j = 0; j < 9; j++) {
            String key = getKey(row, j);
            if (assignment.containsKey(key) && assignment.get(key) == value) {
                return false; // Conflict in row
            }
        }

        // Check column constraint
        for (int i = 0; i < 9; i++) {
            String key = getKey(i, col);
            if (assignment.containsKey(key) && assignment.get(key) == value) {
                return false; // Conflict in column
            }
        }

        // Check 3x3 box constraint
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int i = boxRow; i < boxRow + 3; i++) {
            for (int j = boxCol; j < boxCol + 3; j++) {
                String key = getKey(i, j);
                if (assignment.containsKey(key) && assignment.get(key) == value) {
                    return false; // Conflict in box
                }
            }
        }

        return true; // No conflicts
    }

    // Helper method to create key from coordinates
    private String getKey(int i, int j) {
        return i + "," + j;
    }

    // Convert assignment map back to 2D grid
    private int[][] convertAssignmentToGrid(Map<String, Integer> assignment) {
        int[][] grid = new int[9][9];

        for (Map.Entry<String, Integer> entry : assignment.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int i = Integer.parseInt(parts[0]);
            int j = Integer.parseInt(parts[1]);
            grid[i][j] = entry.getValue();
        }

        return grid;
    }
}