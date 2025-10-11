import java.util.*;

public class SudokuSolver {
    private int steps;
    private int backtracks;
    private long startTime;

    // CSP representation
    static class SudokuCSP {
        int[][] grid;
        List<int[]> variables;
        Map<String, List<Integer>> domains;
        List<int[][]> constraints;

        SudokuCSP(int[][] grid) {
            this.grid = grid;
            this.variables = new ArrayList<>();
            this.domains = new HashMap<>();
            this.constraints = new ArrayList<>();
            initializeDomains();
            initializeConstraints();
        }

        private void initializeDomains() {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    variables.add(new int[]{i, j});
                    String key = getKey(i, j);

                    if (grid[i][j] != 0) {
                        domains.put(key, Arrays.asList(grid[i][j]));
                    } else {
                        List<Integer> domain = new ArrayList<>();
                        for (int num = 1; num <= 9; num++) {
                            domain.add(num);
                        }
                        domains.put(key, domain);
                    }
                }
            }
        }

        private void initializeConstraints() {
            // Row constraints
            for (int i = 0; i < 9; i++) {
                for (int j1 = 0; j1 < 9; j1++) {
                    for (int j2 = j1 + 1; j2 < 9; j2++) {
                        constraints.add(new int[][]{{i, j1}, {i, j2}});
                    }
                }
            }

            // Column constraints
            for (int j = 0; j < 9; j++) {
                for (int i1 = 0; i1 < 9; i1++) {
                    for (int i2 = i1 + 1; i2 < 9; i2++) {
                        constraints.add(new int[][]{{i1, j}, {i2, j}});
                    }
                }
            }

            // Box constraints
            for (int boxI = 0; boxI < 3; boxI++) {
                for (int boxJ = 0; boxJ < 3; boxJ++) {
                    List<int[]> boxVars = new ArrayList<>();
                    for (int i = boxI * 3; i < boxI * 3 + 3; i++) {
                        for (int j = boxJ * 3; j < boxJ * 3 + 3; j++) {
                            boxVars.add(new int[]{i, j});
                        }
                    }

                    for (int k1 = 0; k1 < boxVars.size(); k1++) {
                        for (int k2 = k1 + 1; k2 < boxVars.size(); k2++) {
                            constraints.add(new int[][]{boxVars.get(k1), boxVars.get(k2)});
                        }
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

    // Main solving method
    public SolverResult solve(int[][] puzzle) {
        this.steps = 0;
        this.backtracks = 0;
        this.startTime = System.currentTimeMillis();

        SudokuCSP csp = new SudokuCSP(puzzle);
        Map<String, Integer> assignment = new HashMap<>();

        Map<String, Integer> result = backtrackingSearch(assignment, csp);

        double executionTime = (System.currentTimeMillis() - startTime) / 1000.0;

        if (result != null) {
            int[][] solution = assignmentToGrid(result, puzzle);
            return new SolverResult(solution, executionTime, steps, backtracks, true);
        } else {
            return new SolverResult(null, executionTime, steps, backtracks, false);
        }
    }

    // Core backtracking algorithm
    private Map<String, Integer> backtrackingSearch(Map<String, Integer> assignment, SudokuCSP csp) {
        steps++;

        if (isComplete(assignment, csp)) {
            return new HashMap<>(assignment);
        }

        int[] var = selectUnassignedVariable(csp, assignment);
        String varKey = csp.getKey(var[0], var[1]);

        List<Integer> values = orderDomainValues(var, assignment, csp);

        for (int value : values) {
            if (isConsistent(var, value, assignment, csp)) {
                assignment.put(varKey, value);

                Map<String, Integer> result = backtrackingSearch(assignment, csp);
                if (result != null) {
                    return result;
                }

                // Backtrack
                assignment.remove(varKey);
                backtracks++;
            }
        }

        return null;
    }

    private boolean isComplete(Map<String, Integer> assignment, SudokuCSP csp) {
        return assignment.size() == csp.variables.size();
    }

    private int[] selectUnassignedVariable(SudokuCSP csp, Map<String, Integer> assignment) {
        // MRV heuristic: choose variable with smallest domain
        int[] bestVar = null;
        int minDomainSize = Integer.MAX_VALUE;

        for (int[] var : csp.variables) {
            String key = csp.getKey(var[0], var[1]);
            if (!assignment.containsKey(key)) {
                int domainSize = csp.domains.get(key).size();
                if (domainSize < minDomainSize) {
                    minDomainSize = domainSize;
                    bestVar = var;
                }
            }
        }

        return bestVar;
    }

    private List<Integer> orderDomainValues(int[] var, Map<String, Integer> assignment, SudokuCSP csp) {
        String varKey = csp.getKey(var[0], var[1]);
        List<Integer> values = new ArrayList<>(csp.domains.get(varKey));

        // LCV heuristic: sort by number of conflicts
        values.sort((a, b) -> {
            int conflictsA = countConflicts(var, a, assignment, csp);
            int conflictsB = countConflicts(var, b, assignment, csp);
            return Integer.compare(conflictsA, conflictsB);
        });

        return values;
    }

    private int countConflicts(int[] var, int value, Map<String, Integer> assignment, SudokuCSP csp) {
        int conflicts = 0;
        int row = var[0], col = var[1];

        // Check row
        for (int j = 0; j < 9; j++) {
            String key = csp.getKey(row, j);
            if (!assignment.containsKey(key) && csp.domains.get(key).contains(value)) {
                conflicts++;
            }
        }

        // Check column
        for (int i = 0; i < 9; i++) {
            String key = csp.getKey(i, col);
            if (!assignment.containsKey(key) && csp.domains.get(key).contains(value)) {
                conflicts++;
            }
        }

        // Check box
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int i = boxRow; i < boxRow + 3; i++) {
            for (int j = boxCol; j < boxCol + 3; j++) {
                String key = csp.getKey(i, j);
                if (!assignment.containsKey(key) && csp.domains.get(key).contains(value)) {
                    conflicts++;
                }
            }
        }

        return conflicts;
    }

    private boolean isConsistent(int[] var, int value, Map<String, Integer> assignment, SudokuCSP csp) {
        int row = var[0], col = var[1];

        // Check row
        for (int j = 0; j < 9; j++) {
            String key = csp.getKey(row, j);
            if (assignment.containsKey(key) && assignment.get(key) == value) {
                return false;
            }
        }

        // Check column
        for (int i = 0; i < 9; i++) {
            String key = csp.getKey(i, col);
            if (assignment.containsKey(key) && assignment.get(key) == value) {
                return false;
            }
        }

        // Check box
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int i = boxRow; i < boxRow + 3; i++) {
            for (int j = boxCol; j < boxCol + 3; j++) {
                String key = csp.getKey(i, j);
                if (assignment.containsKey(key) && assignment.get(key) == value) {
                    return false;
                }
            }
        }

        return true;
    }

    // Utility methods
    private int[][] assignmentToGrid(Map<String, Integer> assignment, int[][] originalGrid) {
        int[][] grid = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(originalGrid[i], 0, grid[i], 0, 9);
        }

        for (Map.Entry<String, Integer> entry : assignment.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int i = Integer.parseInt(parts[0]);
            int j = Integer.parseInt(parts[1]);
            grid[i][j] = entry.getValue();
        }

        return grid;
    }
}