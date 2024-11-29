import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class Main implements TimerListener {
    private JFrame frame;
    private QuizManager quizManager;
    private TimerThread timerThread;
    private int score = 0;
    private String username;
    private Connection connection;

    public Main() {
        initDatabase();
        initGUI();
    }

    private void initDatabase() {
        try {
            // Database connection configuration
            String url = "jdbc:mysql://localhost:3306/quiz_app";
            String user = "root"; // Your DB username
            String password = ""; // Your DB password

            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the database!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to database!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initGUI() {
        frame = new JFrame("Quiz App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null); // Center window
        frame.setLayout(new BorderLayout());

        // Panel to input username
        JPanel startPanel = new JPanel();
        startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.Y_AXIS));
        startPanel.setBackground(new Color(255, 250, 240)); // Light background color

        JLabel nameLabel = new JLabel("Enter your name:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(300, 30)); // Max width for name input

        JButton startButton = new JButton("Start Quiz");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setBackground(new Color(34, 193, 195)); // Attractive color
        startButton.setForeground(Color.WHITE);
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setFocusPainted(false);

        startButton.addActionListener(e -> {
            username = nameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            saveOrUpdateUser();
            loadQuiz();
        });

        startPanel.add(Box.createVerticalStrut(50)); // Add space on top
        startPanel.add(nameLabel);
        startPanel.add(Box.createVerticalStrut(20)); // Add space between name label and text field
        startPanel.add(nameField);
        startPanel.add(Box.createVerticalStrut(20)); // Add space between input and button
        startPanel.add(startButton);
        startPanel.add(Box.createVerticalStrut(50)); // Add space at the bottom

        frame.add(startPanel);
        frame.setVisible(true);
    }

    private void saveOrUpdateUser() {
        try {
            // Insert or update user in the leaderboard
            String query = "INSERT INTO leaderboard (username, score) VALUES (?, ?) ON DUPLICATE KEY UPDATE score = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setInt(2, 0); // Starting score
            stmt.setInt(3, 0); // In case of update, keep score at 0 initially
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadQuiz() {
        try {
            quizManager = new QuizManager(connection); // Pass database connection
            if (!quizManager.hasNextQuestion()) {
                JOptionPane.showMessageDialog(frame, "No questions available in the database.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            showNextQuestion();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error loading the quiz. Please try again.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showNextQuestion() {
        if (!quizManager.hasNextQuestion()) {
            updateScoreInDatabase();
            showResult();
            return;
        }

        Question question = quizManager.getNextQuestion();

        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.setBackground(new Color(255, 250, 240)); // Set background color for questions
        JLabel questionLabel = new JLabel(question.getQuestion());
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        questionPanel.add(questionLabel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 10, 10)); // Adjust grid spacing
        ButtonGroup buttonGroup = new ButtonGroup();

        for (String option : question.getOptions()) {
            JRadioButton optionButton = new JRadioButton(option);
            buttonGroup.add(optionButton);
            optionsPanel.add(optionButton);

            optionButton.addActionListener(e -> {
                if (option.equals(question.getAnswer())) {
                    score += 10;
                }
                timerThread.stopTimer();
                showNextQuestion();
            });
        }

        questionPanel.add(optionsPanel, BorderLayout.CENTER);

        frame.getContentPane().removeAll();
        frame.add(questionPanel, BorderLayout.CENTER);

        // Timer setup
        if (timerThread != null) {
            timerThread.stopTimer();
        }
        timerThread = new TimerThread(15, this); // 15 seconds for each question
        timerThread.start();

        frame.revalidate();
        frame.repaint();
    }

    private void updateScoreInDatabase() {
        try {
            String query = "UPDATE leaderboard SET score = ? WHERE username = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, score);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showResult() {
        frame.getContentPane().removeAll();
        JLabel resultLabel = new JLabel("Quiz Complete! Your score: " + score);
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.add(resultLabel);

        JButton viewLeaderboardButton = new JButton("View Leaderboard");
        viewLeaderboardButton.addActionListener(e -> showLeaderboard());
        resultPanel.add(viewLeaderboardButton);

        frame.add(resultPanel);
        frame.revalidate();
        frame.repaint();
    }

    private void showLeaderboard() {
        try {
            String query = "SELECT username, score FROM leaderboard ORDER BY score DESC LIMIT 10";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            StringBuilder leaderboardText = new StringBuilder("<html><h2>Leaderboard</h2>");
            int rank = 1;
            while (rs.next()) {
                String username = rs.getString("username");
                int score = rs.getInt("score");
                leaderboardText.append(rank++).append(". ").append(username).append(" - ").append(score).append("<br>");
            }
            leaderboardText.append("</html>");

            JOptionPane.showMessageDialog(frame, leaderboardText.toString(), "Leaderboard",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onTimeOut() {
        score -= 5; // Deduct points for time out
        showNextQuestion();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }

    @Override
    public void onTimeUp() {
        // This method can be used for additional logic if required
        throw new UnsupportedOperationException("Unimplemented method 'onTimeUp'");
    }
}
