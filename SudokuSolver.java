import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SudokuSolver extends JFrame {

    private SudokuCell[][] cells = new SudokuCell[9][9];
    private JComboBox<String> presetCombo;
    private JCheckBox visualModeCheck;
    private JSlider speedSlider;
    private JButton solveButton;
    private JButton clearButton;
    private JButton resetButton;
    private JButton stopButton;
    private JLabel statusLabel;

    private boolean isSolving = false;
    private VisualSolverWorker activeWorker = null;

    private final int[][][] PRESETS = {
        new int[9][9],

        {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
        },

        {
            {0, 0, 0, 2, 6, 0, 7, 0, 1},
            {6, 8, 0, 0, 7, 0, 0, 9, 0},
            {1, 9, 0, 0, 0, 4, 5, 0, 0},
            {8, 2, 0, 1, 0, 0, 0, 4, 0},
            {0, 0, 4, 6, 0, 2, 9, 0, 0},
            {0, 5, 0, 0, 0, 3, 0, 2, 8},
            {0, 0, 9, 3, 0, 0, 0, 7, 4},
            {0, 4, 0, 0, 5, 0, 0, 3, 6},
            {7, 0, 3, 0, 1, 8, 0, 0, 0}
        },

        {
            {0, 0, 0, 6, 0, 0, 4, 0, 0},
            {7, 0, 0, 0, 0, 3, 6, 0, 0},
            {0, 0, 0, 0, 9, 1, 0, 8, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 5, 0, 1, 8, 0, 0, 0, 3},
            {0, 0, 0, 3, 0, 6, 0, 4, 5},
            {0, 4, 0, 2, 0, 0, 0, 6, 0},
            {9, 0, 3, 0, 0, 0, 0, 0, 0},
            {0, 2, 0, 0, 0, 0, 1, 0, 0}
        },

        {
            {0, 2, 0, 6, 0, 8, 0, 0, 0},
            {5, 8, 0, 0, 0, 9, 7, 0, 0},
            {0, 0, 0, 0, 4, 0, 0, 0, 0},
            {3, 7, 0, 0, 0, 5, 0, 0, 0},
            {6, 0, 0, 0, 0, 0, 0, 0, 4},
            {0, 0, 8, 0, 0, 0, 0, 1, 3},
            {0, 0, 0, 0, 2, 0, 0, 0, 0},
            {0, 0, 9, 8, 0, 0, 0, 3, 6},
            {0, 0, 0, 3, 0, 6, 0, 9, 0}
        },

        {
            {5, 1, 6, 8, 4, 9, 7, 3, 2},
            {3, 0, 7, 6, 0, 5, 0, 0, 0},
            {8, 0, 9, 1, 0, 4, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0}
        }
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SudokuSolver().setVisible(true);
        });
    }

    public SudokuSolver() {
        super("Sudoku Solver & Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 750);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 15));
        mainPanel.setBackground(new Color(30, 30, 46));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(30, 30, 46));
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Sudoku Solver");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Recursive Backtracking Visualizer");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(168, 170, 195));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(subtitleLabel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel boardOuterPanel = new JPanel(new GridLayout(3, 3, 4, 4));
        boardOuterPanel.setBackground(new Color(108, 92, 231));
        boardOuterPanel.setBorder(BorderFactory.createLineBorder(new Color(108, 92, 231), 4));

        JPanel[][] subGridPanels = new JPanel[3][3];
        for (int br = 0; br < 3; br++) {
            for (int bc = 0; bc < 3; bc++) {
                subGridPanels[br][bc] = new JPanel(new GridLayout(3, 3, 1, 1));
                subGridPanels[br][bc].setBackground(new Color(30, 30, 46));
                boardOuterPanel.add(subGridPanels[br][bc]);
            }
        }

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                SudokuCell cell = new SudokuCell(r, c);
                cells[r][c] = cell;

                int subGridRow = r / 3;
                int subGridCol = c / 3;
                subGridPanels[subGridRow][subGridCol].add(cell);

                AbstractDocument doc = (AbstractDocument) cell.getDocument();
                doc.setDocumentFilter(new DigitDocumentFilter(cell));
            }
        }

        mainPanel.add(boardOuterPanel, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBackground(new Color(30, 30, 46));
        controlsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(42, 42, 64), 2),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel configRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        configRow.setBackground(new Color(30, 30, 46));

        JLabel presetLabel = new JLabel("Presets:");
        presetLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        presetLabel.setForeground(Color.WHITE);

        String[] presetsNames = {"Custom (Blank)", "Easy Grid", "Medium Grid", "Hard Grid", "Expert Grid", "Unsolvable Test"};
        presetCombo = new JComboBox<>(presetsNames);
        presetCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        presetCombo.setPreferredSize(new Dimension(150, 30));
        presetCombo.setBackground(Color.WHITE);

        visualModeCheck = new JCheckBox("Visual Mode");
        visualModeCheck.setFont(new Font("Segoe UI", Font.BOLD, 14));
        visualModeCheck.setForeground(Color.WHITE);
        visualModeCheck.setBackground(new Color(30, 30, 46));
        visualModeCheck.setSelected(true);

        configRow.add(presetLabel);
        configRow.add(presetCombo);
        configRow.add(Box.createRigidArea(new Dimension(10, 0)));
        configRow.add(visualModeCheck);

        controlsPanel.add(configRow);
        controlsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel sliderRow = new JPanel(new BorderLayout(10, 0));
        sliderRow.setBackground(new Color(30, 30, 46));
        sliderRow.setMaximumSize(new Dimension(500, 40));

        JLabel speedLabel = new JLabel("Speed: ");
        speedLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        speedLabel.setForeground(new Color(168, 170, 195));

        speedSlider = new JSlider(1, 500, 25);
        speedSlider.setBackground(new Color(30, 30, 46));
        speedSlider.setInverted(true); 

        JLabel speedText = new JLabel("Fast (25ms)  ");
        speedText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        speedText.setForeground(new Color(168, 170, 195));
        speedText.setPreferredSize(new Dimension(90, 20));

        speedSlider.addChangeListener(e -> {
            int val = speedSlider.getValue();
            if (val <= 10) {
                speedText.setText("Instant (" + val + "ms)");
            } else if (val >= 450) {
                speedText.setText("Slow (" + val + "ms)");
            } else {
                speedText.setText("Medium (" + val + "ms)");
            }
        });

        sliderRow.add(speedLabel, BorderLayout.WEST);
        sliderRow.add(speedSlider, BorderLayout.CENTER);
        sliderRow.add(speedText, BorderLayout.EAST);

        controlsPanel.add(sliderRow);
        controlsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel buttonsRow = new JPanel(new GridLayout(1, 4, 10, 0));
        buttonsRow.setBackground(new Color(30, 30, 46));
        buttonsRow.setMaximumSize(new Dimension(550, 40));

        solveButton = new JButton("Solve");
        solveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        solveButton.setBackground(new Color(108, 92, 231)); 
        solveButton.setForeground(Color.WHITE);
        solveButton.setFocusPainted(false);
        solveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resetButton.setBackground(new Color(99, 110, 114));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clearButton.setBackground(new Color(72, 84, 96));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        stopButton = new JButton("Stop");
        stopButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        stopButton.setBackground(new Color(235, 77, 75)); 
        stopButton.setForeground(Color.WHITE);
        stopButton.setFocusPainted(false);
        stopButton.setEnabled(false);
        stopButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonsRow.add(solveButton);
        buttonsRow.add(resetButton);
        buttonsRow.add(clearButton);
        buttonsRow.add(stopButton);

        controlsPanel.add(buttonsRow);
        controlsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusRow.setBackground(new Color(30, 30, 46));
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setForeground(Color.WHITE);
        statusRow.add(statusLabel);

        controlsPanel.add(statusRow);

        mainPanel.add(controlsPanel, BorderLayout.SOUTH);
        add(mainPanel);

        presetCombo.addActionListener(e -> {
            resetBoardToCurrentPreset();
        });

        resetButton.addActionListener(e -> {
            resetBoardToCurrentPreset();
        });

        clearButton.addActionListener(e -> {
            presetCombo.setSelectedIndex(0);
            resetBoardToCurrentPreset();
        });

        solveButton.addActionListener(e -> {
            startSolvingProcess();
        });

        stopButton.addActionListener(e -> {
            stopSolvingProcess();
        });

        presetCombo.setSelectedIndex(2);
    }

    private int[][] getBoardMatrix() {
        int[][] matrix = new int[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                matrix[r][c] = getCellValue(r, c);
            }
        }
        return matrix;
    }

    private int getCellValue(int r, int c) {
        String val = cells[r][c].getText().trim();
        if (val.isEmpty()) return 0;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int getVisualDelay() {
        return speedSlider.getValue();
    }

    private void resetBoardToCurrentPreset() {
        if (isSolving) {
            stopSolvingProcess();
        }
        int index = presetCombo.getSelectedIndex();
        if (index >= 0 && index < PRESETS.length) {
            loadPreset(PRESETS[index]);
        }
    }

    private void loadPreset(int[][] board) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                SudokuCell cell = cells[r][c];
                AbstractDocument doc = (AbstractDocument) cell.getDocument();
                DocumentFilter filter = doc.getDocumentFilter();
                doc.setDocumentFilter(null);

                cell.setConflict(false);
                if (board[r][c] == 0) {
                    cell.setText("");
                    cell.setInitial(false);
                } else {
                    cell.setText(String.valueOf(board[r][c]));
                    cell.setInitial(true);
                }

                doc.setDocumentFilter(filter); 
                cell.updateAppearance();
            }
        }
        statusLabel.setText("Ready");
        statusLabel.setForeground(Color.WHITE);
    }

    private void validateBoard() {
        if (isSolving) return; 

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                cells[r][c].setConflict(false);
            }
        }

        boolean hasConflict = false;
        int[][] currentMatrix = getBoardMatrix();

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int val = currentMatrix[r][c];
                if (val != 0) {
                    if (!isValidMove(currentMatrix, r, c, val)) {
                        cells[r][c].setConflict(true);
                        hasConflict = true;
                    }
                }
            }
        }

        if (hasConflict) {
            statusLabel.setText("Warning: Conflicting numbers detected in grid!");
            statusLabel.setForeground(new Color(235, 77, 75));
        } else {
            statusLabel.setText("Ready");
            statusLabel.setForeground(Color.WHITE);
        }
    }

    private boolean validateEntireBoardBeforeSolve(int[][] matrix) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int val = matrix[r][c];
                if (val != 0) {
                    if (!isValidMove(matrix, r, c, val)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean isValidMove(int[][] board, int row, int col, int num) {
        for (int i = 0; i < 9; i++) {
            if (i != col && board[row][i] == num) return false;
            if (i != row && board[i][col] == num) return false;
        }

        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int r = boxRow + i;
                int c = boxCol + j;
                if ((r != row || c != col) && board[r][c] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    private void startSolvingProcess() {
        int[][] grid = getBoardMatrix();

        if (!validateEntireBoardBeforeSolve(grid)) {
            JOptionPane.showMessageDialog(this, 
                "The board contains active Sudoku rule violations. Please resolve conflicts highlighted in red before solving.",
                "Input Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        isSolving = true;
        solveButton.setEnabled(false);
        clearButton.setEnabled(false);
        resetButton.setEnabled(false);
        presetCombo.setEnabled(false);
        visualModeCheck.setEnabled(false);
        stopButton.setEnabled(true);

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                SudokuCell cell = cells[r][c];
                int val = grid[r][c];
                cell.setEditable(false);
                if (val != 0) {
                    cell.setInitial(true);
                } else {
                    cell.setInitial(false);
                }
                cell.updateAppearance();
            }
        }

        if (visualModeCheck.isSelected()) {
            statusLabel.setText("Executing Step-by-Step Backtracking Visualizer...");
            statusLabel.setForeground(new Color(241, 196, 15));
            activeWorker = new VisualSolverWorker(grid);
            activeWorker.execute();
        } else {
            statusLabel.setText("Solving board instantly...");
            statusLabel.setForeground(Color.WHITE);
            
            long startTime = System.currentTimeMillis();
            boolean solved = solveInstantBacktracking(grid);
            long endTime = System.currentTimeMillis();

            isSolving = false;
            solveButton.setEnabled(true);
            clearButton.setEnabled(true);
            resetButton.setEnabled(true);
            presetCombo.setEnabled(true);
            visualModeCheck.setEnabled(true);
            stopButton.setEnabled(false);

            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    cells[r][c].setEditable(true);
                }
            }

            if (solved) {
                for (int r = 0; r < 9; r++) {
                    for (int c = 0; c < 9; c++) {
                        SudokuCell cell = cells[r][c];
                        if (!cell.isInitial()) {
                            cell.setText(String.valueOf(grid[r][c]));
                            cell.setForeground(new Color(46, 204, 113)); 
                        }
                    }
                }
                statusLabel.setText("Solved instantly in " + (endTime - startTime) + " ms!");
                statusLabel.setForeground(new Color(46, 204, 113));
                JOptionPane.showMessageDialog(this, "Sudoku solved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                statusLabel.setText("No solution exists for this puzzle!");
                statusLabel.setForeground(new Color(235, 77, 75));
                JOptionPane.showMessageDialog(this, "No solution exists for this puzzle.", "Unsolvable", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void stopSolvingProcess() {
        if (activeWorker != null) {
            activeWorker.stopSolving();
            activeWorker.cancel(true);
            activeWorker = null;
        }
        isSolving = false;
        
        solveButton.setEnabled(true);
        clearButton.setEnabled(true);
        resetButton.setEnabled(true);
        presetCombo.setEnabled(true);
        visualModeCheck.setEnabled(true);
        stopButton.setEnabled(false);

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                cells[r][c].setEditable(true);
            }
        }

        statusLabel.setText("Solving stopped by user.");
        statusLabel.setForeground(Color.WHITE);
    }

    private boolean solveInstantBacktracking(int[][] grid) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (grid[r][c] == 0) {
                    for (int val = 1; val <= 9; val++) {
                        if (isValidMove(grid, r, c, val)) {
                            grid[r][c] = val;
                            if (solveInstantBacktracking(grid)) {
                                return true;
                            }
                            grid[r][c] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }
    enum CellState {
        TRYING,
        SUCCESS,
        BACKTRACK
    }

    static class CellUpdate {
        final int row;
        final int col;
        final int value;
        final CellState state;

        CellUpdate(int row, int col, int value, CellState state) {
            this.row = row;
            this.col = col;
            this.value = value;
            this.state = state;
        }
    }

    private class VisualSolverWorker extends SwingWorker<Boolean, CellUpdate> {
        private final int[][] grid;
        private volatile boolean stopped = false;

        public VisualSolverWorker(int[][] initialGrid) {
            this.grid = new int[9][9];
            for (int r = 0; r < 9; r++) {
                System.arraycopy(initialGrid[r], 0, this.grid[r], 0, 9);
            }
        }

        public void stopSolving() {
            this.stopped = true;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            return solveVisual(0, 0);
        }

        private boolean solveVisual(int row, int col) throws InterruptedException {
            if (stopped || isCancelled()) {
                return false;
            }

            if (row == 9) {
                return true; 
            }

            int nextRow = (col == 8) ? row + 1 : row;
            int nextCol = (col == 8) ? 0 : col + 1;

            if (grid[row][col] != 0) {
                return solveVisual(nextRow, nextCol);
            }

            for (int num = 1; num <= 9; num++) {
                if (stopped || isCancelled()) {
                    return false;
                }

                publish(new CellUpdate(row, col, num, CellState.TRYING));
                Thread.sleep(getVisualDelay());

                if (isValidMove(grid, row, col, num)) {
                    grid[row][col] = num;
                    publish(new CellUpdate(row, col, num, CellState.SUCCESS));
                    Thread.sleep(getVisualDelay());

                    if (solveVisual(nextRow, nextCol)) {
                        return true;
                    }

                    grid[row][col] = 0;
                }
            }

            publish(new CellUpdate(row, col, 0, CellState.BACKTRACK));
            Thread.sleep(getVisualDelay());
            return false;
        }

        @Override
        protected void process(List<CellUpdate> chunks) {
            if (stopped || isCancelled()) return;

            for (CellUpdate update : chunks) {
                SudokuCell cell = cells[update.row][update.col];
                if (cell.isInitial()) continue; 

                if (update.value == 0) {
                    cell.setText("");
                } else {
                    cell.setText(String.valueOf(update.value));
                }

                switch (update.state) {
                    case TRYING:
                        cell.setBackground(new Color(241, 196, 15)); 
                        cell.setForeground(Color.BLACK);
                        break;
                    case SUCCESS:
                        cell.setBackground(new Color(46, 204, 113));
                        cell.setForeground(Color.WHITE);
                        break;
                    case BACKTRACK:
                        cell.setBackground(new Color(235, 77, 75)); 
                        cell.setForeground(Color.WHITE);
                        break;
                }
            }
        }

        @Override
        protected void done() {
            isSolving = false;
            solveButton.setEnabled(true);
            clearButton.setEnabled(true);
            resetButton.setEnabled(true);
            presetCombo.setEnabled(true);
            visualModeCheck.setEnabled(true);
            stopButton.setEnabled(false);

            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    cells[r][c].setEditable(true);
                }
            }

            try {
                if (isCancelled() || stopped) {
                    statusLabel.setText("Solving aborted by user.");
                    statusLabel.setForeground(Color.WHITE);
                    resetBoardToCurrentPreset();
                    return;
                }

                boolean solved = get();
                if (solved) {
                    for (int r = 0; r < 9; r++) {
                        for (int c = 0; c < 9; c++) {
                            SudokuCell cell = cells[r][c];
                            cell.updateAppearance();
                            if (!cell.isInitial()) {
                                cell.setForeground(new Color(46, 204, 113)); 
                            }
                        }
                    }
                    statusLabel.setText("Solved successfully step-by-step!");
                    statusLabel.setForeground(new Color(46, 204, 113));
                    JOptionPane.showMessageDialog(SudokuSolver.this, "Sudoku solved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText("No solution exists for this puzzle!");
                    statusLabel.setForeground(new Color(235, 77, 75));
                    JOptionPane.showMessageDialog(SudokuSolver.this, "No solution exists for this puzzle.", "Unsolvable", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                statusLabel.setText("Solve aborted or error occurred.");
                statusLabel.setForeground(new Color(235, 77, 75));
                e.printStackTrace();
            }
        }
    }

    static class SudokuCell extends JTextField {
        private final int row;
        private final int col;
        private boolean isInitial = false;
        private boolean isConflict = false;

        public SudokuCell(int row, int col) {
            this.row = row;
            this.col = col;
            setHorizontalAlignment(JTextField.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 22));
            setBackground(new Color(42, 42, 64)); 
            setForeground(new Color(129, 236, 236)); 
            setCaretColor(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }

        public int getRow() { return row; }
        public int getCol() { return col; }

        public void setInitial(boolean initial) {
            this.isInitial = initial;
            updateAppearance();
        }

        public boolean isInitial() { return isInitial; }

        public void setConflict(boolean conflict) {
            this.isConflict = conflict;
            updateAppearance();
        }

        public void updateAppearance() {
            if (isConflict) {
                setBackground(new Color(120, 20, 20)); 
                setForeground(new Color(255, 200, 200));
            } else {
                setBackground(new Color(42, 42, 64));
                if (isInitial) {
                    setFont(new Font("Segoe UI", Font.BOLD, 22));
                    setForeground(Color.WHITE); 
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 22));
                    setForeground(new Color(129, 236, 236)); 
                }
            }
        }
    }

    static class DigitDocumentFilter extends DocumentFilter {
        private final SudokuCell cell;

        public DigitDocumentFilter(SudokuCell cell) {
            this.cell = cell;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (isValidDigitInput(fb.getDocument().getText(0, fb.getDocument().getLength()), string, offset)) {
                super.insertString(fb, offset, string, attr);
                triggerValidation();
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException {
            if (isValidDigitInput(fb.getDocument().getText(0, fb.getDocument().getLength()), text, offset)) {
                super.replace(fb, offset, length, text, attr);
                triggerValidation();
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            super.remove(fb, offset, length);
            triggerValidation();
        }

        private boolean isValidDigitInput(String currentText, String incomingText, int offset) {
            if (incomingText == null || incomingText.isEmpty()) {
                return true;
            }
            if (incomingText.length() > 1 || !Character.isDigit(incomingText.charAt(0)) || incomingText.charAt(0) == '0') {
                return false;
            }
            return currentText.length() < 1 || (currentText.length() == 1 && offset == 0);
        }

        private void triggerValidation() {
            SwingUtilities.invokeLater(() -> {
                SudokuSolver solver = (SudokuSolver) SwingUtilities.getWindowAncestor(cell);
                if (solver != null) {
                    String val = cell.getText().trim();
                    if (val.isEmpty()) {
                        cell.setInitial(false);
                    }
                    cell.updateAppearance();
                    solver.validateBoard();
                }
            });
        }
    }
}
