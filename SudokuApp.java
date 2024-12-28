import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SudokuApp extends JFrame {
    
    public static void main(String[] args) {
        new MainMenuGUI(); // Start with the main menu window
    }
}

class MainMenuGUI extends JFrame {
    public MainMenuGUI() {
        setTitle("Sudoku Game");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        //JPanel panel = new JPanel();
        //panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Title label
        JLabel titleLabel = new JLabel("Sudoku Game", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        add(titleLabel, BorderLayout.NORTH);
        
        // Create a panel for the buttons with GridLayout
        JPanel buttonPanel = new JPanel(); // 3 rows, 1 column, 10 pixels gap between components
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setFont(new Font("Arial", Font.BOLD, 30));

        // Difficulty selection buttons
        JButton levelButton = new JButton("Select Difficulty");
        JButton highScoreButton = new JButton("High Scores");
        JButton exitButton = new JButton("Exit");

        Dimension buttonSize = new Dimension(250, 70);
        levelButton.setPreferredSize(buttonSize);
        highScoreButton.setPreferredSize(buttonSize);
        exitButton.setPreferredSize(buttonSize);
        
        levelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        highScoreButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Wrap buttons in a JPanel to enforce the size
        JPanel wrapper1 = createFixedSizePanel(levelButton, buttonSize);
        JPanel wrapper2 = createFixedSizePanel(highScoreButton, buttonSize);
        JPanel wrapper3 = createFixedSizePanel(exitButton, buttonSize);

        // Add buttons to the button panel
        buttonPanel.add(wrapper1);
        buttonPanel.add(wrapper2);
        buttonPanel.add(wrapper3);

        // Add the button panel to the center of the frame
        add(buttonPanel);

        // Action listeners for buttons
        levelButton.addActionListener(e -> {
            new LevelSelectionGUI();
            dispose(); // Close the main menu window
        });

        highScoreButton.addActionListener(e -> displayHighScores());
        exitButton.addActionListener(e -> System.exit(0));

        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    private static JPanel createFixedSizePanel(JButton button, Dimension size) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridBagLayout()); // Centers the button
        button.setPreferredSize(size);
        wrapper.add(button);
        return wrapper;
    }

    // Display the high scores
    private void displayHighScores() {
        List<Integer> highScores = readHighScores();
        StringBuilder scores = new StringBuilder("Top 5 High Scores:\n");

        for (int score : highScores) {
            scores.append(score).append(" seconds\n");
        }

        JOptionPane.showMessageDialog(this, scores.toString(), "High Scores", JOptionPane.INFORMATION_MESSAGE);
    }

    // Read high scores from a file
    private List<Integer> readHighScores() {
        List<Integer> scores = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("HighScores.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                scores.add(Integer.parseInt(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(scores);
        return scores.size() > 5 ? scores.subList(0, 5) : scores; // Return top 5 scores
    }
}

class LevelSelectionGUI extends JFrame {
    public LevelSelectionGUI() {
        setTitle("Select Sudoku Level");
        setSize(500, 100);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Difficulty selection buttons
        JButton easyButton = new JButton("Easy");
        JButton mediumButton = new JButton("Medium");
        JButton hardButton = new JButton("Hard");
        // Create the Back button
        JButton backButton = new JButton("Back");

        // Add buttons to the frame
        add(new JLabel("Choose a Difficulty Level:"));
        add(easyButton);
        add(mediumButton);
        add(hardButton);
        add(backButton);
        
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backToMainMenu();
            }
        });

        // Action listeners for buttons
        easyButton.addActionListener(e -> startSudokuGame("Easy"));
        mediumButton.addActionListener(e -> startSudokuGame("Medium"));
        hardButton.addActionListener(e -> startSudokuGame("Hard"));

        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    private void backToMainMenu() {
        new MainMenuGUI(); // Adjust this to the name of your main menu class
        dispose(); // Close the current LevelSelection window
    }

    // Opens the Sudoku game with the selected difficulty level
    private void startSudokuGame(String difficulty) {
        new SudokuGUI(difficulty);
        dispose(); // Close the level selection window
    }
}

class SudokuGUI extends JFrame {
    private JPanel gridPanel;
    private JTextField[][] cells = new JTextField[9][9];
    private int[][] puzzle;       // Current puzzle with blanks
    private int[][] solution;     // Solution for the puzzle
    private JTextField selectedCell; // Tracks the currently selected cell

    private Timer timer;
    private int elapsedSeconds;
    private JLabel timerLabel;

    public SudokuGUI(String difficulty) {
        setTitle("Sudoku Puzzle - " + difficulty + " Level");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setLayout(new BorderLayout());

        // Initialize the timer
        elapsedSeconds = 0; // Reset elapsed time
        timerLabel = new JLabel("Time: 0:00");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Initialize puzzle and solution based on difficulty
        initializePuzzle(difficulty);

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                elapsedSeconds++; // Increment elapsed time by 1 second
                updateTimerLabel(); // Update the timer display
            }
        });
        timer.start(); // Start the timer


        // Buttons and difficulty selector
        JButton newGameButton = new JButton("New Game");
        JButton resetButton = new JButton("Reset");
        JButton showSolutionButton = new JButton("Show Solution");
        
        // Setup button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(newGameButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(showSolutionButton);
        buttonPanel.add(timerLabel);

        // Grid panel
        gridPanel = new JPanel(new GridLayout(9, 9));

        // Initialize cells
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                cells[row][col] = new JTextField();
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                cells[row][col].setFont(new Font("Arial", Font.BOLD, 20));
                cells[row][col].setBorder(new LineBorder(Color.BLACK, 1));

                // Determine border thickness for big and small boxes
                int top = (row % 3 == 0) ? 3 : 1;  // Thicker top border for big boxes
                int left = (col % 3 == 0) ? 3 : 1; // Thicker left border for big boxes
                int bottom = (row == 8) ? 3 : 1;  // Thicker bottom border for the last row
                int right = (col == 8) ? 3 : 1;   // Thicker right border for the last column

                cells[row][col].setBorder(new MatteBorder(top, left, bottom, right, Color.BLACK));
                
                if (puzzle[row][col] != 0) {
                    cells[row][col].setText(String.valueOf(puzzle[row][col]));
                    cells[row][col].setEditable(false);
                    cells[row][col].setBackground(Color.LIGHT_GRAY);
                } else {
                    cells[row][col].setEditable(true);
                    cells[row][col].setForeground(Color.BLUE);
                    cells[row][col].setBackground(Color.WHITE);
                    final int r = row, c = col;
                    // Focus listener to track selected cell
                    cells[row][col].addFocusListener(new java.awt.event.FocusAdapter() {
                        public void focusGained(java.awt.event.FocusEvent e) {
                            selectedCell = cells[r][c];
                        }
                    });

                    // Document listener for validation after typing
                    cells[row][col].getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                        public void insertUpdate(javax.swing.event.DocumentEvent e) {
                            validateInput(r, c);
                        }
                        public void removeUpdate(javax.swing.event.DocumentEvent e) {
                            validateInput(r, c);
                        }
                        public void changedUpdate(javax.swing.event.DocumentEvent e) {
                            validateInput(r, c);
                        }
                    });
                }
                
                gridPanel.add(cells[row][col]);
            }
        }
        
        // Number button panel
        JPanel numberButtonPanel = new JPanel(new GridLayout(1, 9, 5, 10));
        for (int i = 1; i <= 9; i++) {
            JButton numberButton = new JButton(String.valueOf(i));
            numberButton.setFont(new Font("Arial", Font.BOLD, 20));
            numberButton.addActionListener(e -> {
                if (selectedCell != null && selectedCell.isEditable()) {
                    selectedCell.setText(numberButton.getText());
                }
            });
            numberButtonPanel.add(numberButton);
        }

        JPanel gapPanel = new JPanel();
        gapPanel.setPreferredSize(new Dimension(600, 20));
        // Add panels to the frame
        add(buttonPanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(numberButtonPanel, BorderLayout.SOUTH);
        //add(gapPanel, BorderLayout.PAGE_END);

        // Event Listeners
        newGameButton.addActionListener(e -> startNewGame());
        resetButton.addActionListener(e -> resetPuzzle());
        showSolutionButton.addActionListener(e -> showSolution(resetButton));

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Initialize the puzzle based on difficulty level
    private void initializePuzzle(String difficulty) {
        // Generate a new Sudoku solution
        solution = generateSudokuSolution();
        
        // Initialize puzzle with filled values
        puzzle = new int[9][9];
        for (int r = 0; r < 9; r++) {
            System.arraycopy(solution[r], 0, puzzle[r], 0, 9);
        }
    
        int filledCells;
        Random random = new Random();
        switch (difficulty) {
            case "Easy":
                filledCells = random.nextInt(11) + 35; // 35 to 45
                break;
            case "Medium":
                filledCells = random.nextInt(11) + 30; // 30 to 40
                break;
            case "Hard":
                filledCells = random.nextInt(11) + 25; // 25 to 35
                break;
            default:
                filledCells = 36; // Default to 36 if invalid
        }
    
        // Generate a list of all cell positions
        int[][] positions = new int[81][2];
        int index = 0;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                positions[index++] = new int[]{r, c};
            }
        }
    
        // Shuffle the positions array
        for (int i = 0; i < positions.length; i++) {
            int randomIndex = random.nextInt(positions.length);
            // Swap
            int[] temp = positions[i];
            positions[i] = positions[randomIndex];
            positions[randomIndex] = temp;
        }
    
        // Clear cells based on filledCells count
        for (int i = filledCells; i < 81; i++) {
            int row = positions[i][0];
            int col = positions[i][1];
            puzzle[row][col] = 0; // Clear the cell
        }
    }
    
    // Method to generate a random valid Sudoku solution
    private int[][] generateSudokuSolution() {
        int[][] board = new int[9][9];
        fillBoard(board);
        return board;
    }

    // Method to fill the Sudoku board with valid numbers
    private boolean fillBoard(int[][] board) {
        Random random = new Random();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) { // Check for empty cell
                    for (int num = 1; num <= 9; num++) {
                        int n = random.nextInt(9) + 1; // Random number 1-9
                        if (isSafe(board, row, col, n)) {
                            board[row][col] = n; // Place the number
                            if (fillBoard(board)) {
                                return true; // Continue to fill
                            }
                            board[row][col] = 0; // Backtrack
                        }
                    }
                    return false; // No valid number found, need to backtrack
                }
            }
        }
        return true; // Board filled successfully
    }

    // Check if it's safe to place a number in the given position
    private boolean isSafe(int[][] board, int row, int col, int num) {
        for (int x = 0; x < 9; x++) {
            if (board[row][x] == num || board[x][col] == num || board[row - row % 3 + x / 3][col - col % 3 + x % 3] == num) {
                return false;
            }
        }
        return true;
    }

    private void startNewGame() {
        // Stop the timer if it's running
        if (timer.isRunning()) {
            timer.stop();
        }

        new LevelSelectionGUI();
        dispose(); // Close the current Sudoku game window
    }

    private void resetPuzzle() {
        // Stop the timer
        timer.stop();
        elapsedSeconds = 0; // Reset elapsed time
        updateTimerLabel();

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (cells[row][col].isEditable()) {
                    cells[row][col].setText("");
                    cells[row][col].setBackground(Color.WHITE);
                }
            }
        }

        timer.restart(); // Restart the timer
    }

    private void showSolution(JButton resetButton) {
        // Stop the timer
        timer.stop();
    
        // Display the full solution in all cells
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                cells[row][col].setText(String.valueOf(solution[row][col]));
                cells[row][col].setEditable(false); // Disable editing after the solution
                cells[row][col].setBackground(Color.LIGHT_GRAY); // Change background color
            }
        }
    
        // Explicitly update layout and repaint
        gridPanel.revalidate(); // Ensure the layout is updated
        gridPanel.repaint(); // Force repaint of the gridPanel to reflect the changes immediately

        // Disable reset button and display score
        resetButton.setEnabled(false);
        displayScore(); // Show the score based on elapsed time
    }
    
    private void displayScore() {
        // Calculate and display the score
        int score = elapsedSeconds;
        String message = "Your Score: " + score + " seconds"; // Add seconds for clarity
        JOptionPane.showMessageDialog(this, message, "Solution Revealed", JOptionPane.INFORMATION_MESSAGE);
    }
    

    private void validateInput(int row, int col) {
        String text = cells[row][col].getText().trim();
        if (text.isEmpty() || !text.matches("[1-9]")) {
            cells[row][col].setBackground(Color.WHITE); // Reset color if empty
            return;
        }
    
        try {
            int value = Integer.parseInt(text);
            if (value < 1 || value > 9 || !isValid(row, col, value)) {
                cells[row][col].setBackground(Color.RED);
            } else {
                cells[row][col].setBackground(Color.WHITE);
                // Check for game completion here
                if (isGameCompleted()) {
                    timer.stop(); // Stop the timer
                    displayScore(); // Show the score
                }
            }
        } catch (NumberFormatException e) {
            cells[row][col].setBackground(Color.RED);
        }
    }
    
    // Add this method to check if the game is completed
    private boolean isGameCompleted() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (cells[row][col].isEditable() && cells[row][col].getText().isEmpty()) {
                    return false; // Not completed if any editable cell is empty
                }
            }
        }
        return true; // All cells filled correctly
    }
    

    private boolean isValid(int row, int col, int value) {
        for (int i = 0; i < 9; i++) {
            if ((i != col && cells[row][i].getText().equals(String.valueOf(value))) ||
                (i != row && cells[i][col].getText().equals(String.valueOf(value)))) {
                return false;
            }
        }

        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                if ((i != row || j != col) && cells[i][j].getText().equals(String.valueOf(value))) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateTimerLabel() {
        int minutes = elapsedSeconds / 60;
        int seconds = elapsedSeconds % 60;
        timerLabel.setText(String.format("Time: %d:%02d", minutes, seconds));
    }
    
}
