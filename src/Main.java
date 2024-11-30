import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import javax.sound.sampled.*;
import javax.swing.*;

public class Main implements TimerListener {
    private JFrame frame;
    private QuizManager quizManager;
    private TimerThread timerThread;
    private int score = 0;
    private String username;
    private Connection connection;
    private JProgressBar progressBar; // Progress bar for countdown
    private JLabel timerLabel; // To display remaining time
    private Clip clip;

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
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null); // Center window
        frame.setLayout(new BorderLayout());

        // Panel for the background
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw the background image
                ImageIcon startBg = new ImageIcon(
                        new ImageIcon("assets\\startBG.png")
                                .getImage().getScaledInstance(1000, 600, Image.SCALE_SMOOTH));
                g.drawImage(startBg.getImage(), 0, 0, getWidth(), getHeight(), null);
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setOpaque(false); // Make panel transparent for the background image

        // Start button panel
        JPanel startButtonPanel = new JPanel();
        startButtonPanel.setOpaque(false); // Make this panel transparent as well
        startButtonPanel.setLayout(new GridBagLayout()); // Use GridBagLayout for centering

        // Start button
        // Start button
        ImageIcon startBt = new ImageIcon(
                new ImageIcon("assets\\startBT.png")
                        .getImage().getScaledInstance(200, 120, Image.SCALE_SMOOTH));
        JButton startButton_1 = new JButton(startBt);
        startButton_1.setFocusable(false);
        startButton_1.setOpaque(false);
        startButton_1.setContentAreaFilled(false);
        startButton_1.setBorderPainted(false); // Remove border around the button

        // Add action listener to the start button
        startButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Switch to start panel when start button is clicked
                initStartPanel();
            }
        });

        // Create GridBagConstraints to center the button
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER; // Center the button
        startButtonPanel.add(startButton_1, gbc); // Add the start button to the panel

        // Add the start button panel to the background panel
        backgroundPanel.add(startButtonPanel, BorderLayout.CENTER);
        frame.add(backgroundPanel);

        frame.setVisible(true);
    }

    private void initStartPanel() {
        // Create start panel for user input
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

        // Add action listener for the start quiz button
        startButton.addActionListener(e -> {
            username = nameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            saveOrUpdateUser();
            loadQuiz();
        });

        // Add components to start panel
        startPanel.add(Box.createVerticalStrut(50)); // Add space on top
        startPanel.add(nameLabel);
        startPanel.add(Box.createVerticalStrut(20)); // Space between label and text field
        startPanel.add(nameField);
        startPanel.add(Box.createVerticalStrut(20)); // Space between input and button
        startPanel.add(startButton);
        startPanel.add(Box.createVerticalStrut(50)); // Space at the bottom

        // Switch to start panel
        frame.getContentPane().removeAll(); // Remove previous components
        frame.add(startPanel); // Add the start panel
        frame.revalidate(); // Refresh layout
        frame.repaint(); // Refresh frame
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
        quizManager = new QuizManager(); // Pass database connection
        try {
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
            updateScoreInDatabase(); // Memperbarui skor ke database setelah kuis selesai
            showResult(); // Tampilkan hasil akhir
            return;
        }

        Question question = quizManager.getNextQuestion();

        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.setBackground(new Color(255, 250, 240)); // Set background color for questions
        JLabel questionLabel = new JLabel(question.getQuestion());
        questionLabel.setFont(new Font("Arial", Font.BOLD, 20));
        questionLabel.setForeground(Color.decode("#3C928E"));
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        questionPanel.add(questionLabel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 10, 10)); // Adjust grid spacing
        ButtonGroup buttonGroup = new ButtonGroup();

        String[] colors = { "#FF5733", "#FFC300", "#33B5FF", "#28A745" }; // Red, Yellow, Blue, Green
        int colorIndex = 0;

        // Add option buttons and handle answers
        for (String option : question.getOptions()) {
            JRadioButton optionButton = new JRadioButton(option);
            buttonGroup.add(optionButton);
            optionsPanel.add(optionButton);

            optionButton.setBackground(Color.decode(colors[colorIndex]));
            optionButton.setOpaque(true);

            optionsPanel.add(optionButton);

            // Update the color index for the next option
            colorIndex = (colorIndex + 1) % colors.length;

            optionButton.addActionListener(e -> {
                boolean isCorrect = option.equals(question.getAnswer());
                if (isCorrect) {
                    score += 15;
                    System.out.println("Correct! Current score: " + score); // Debugging line to check score
                }
                // score += 5;
                timerThread.stopTimer(); // Stop the timer when an answer is selected
            });
        }

        questionPanel.add(optionsPanel, BorderLayout.CENTER);

        // Panel for timer and progress bar
        JPanel timerPanel = new JPanel(new BorderLayout());
        timerPanel.setBackground(new Color(200, 200, 200));

        // Timer label
        timerLabel = new JLabel("Time Left: 15s");
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        timerPanel.add(timerLabel, BorderLayout.CENTER);

        // Move progress bar to the bottom
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(100);
        progressBar.setStringPainted(false); // Remove percentage display
        progressBar.setForeground(Color.GREEN);

        frame.getContentPane().removeAll();
        frame.add(timerPanel, BorderLayout.NORTH);
        frame.add(questionPanel, BorderLayout.CENTER);
        frame.add(progressBar, BorderLayout.SOUTH); // Add progress bar to the bottom

        // Timer setup
        if (timerThread != null) {
            timerThread.stopTimer();
        }
        timerThread = new TimerThread(10, this); // 15 seconds for each question
        timerThread.start();
        playMusic("assets\\song.wav");

        frame.revalidate();
        frame.repaint();
    }

    private void playMusic(String musicPath) {
        try {
            if (clip != null && clip.isRunning()) {
                clip.stop(); // Stop the previous music
            }
            File audioFile = new File(musicPath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateScoreInDatabase() {
        try {
            // Update the score in the database after the quiz
            String query = "UPDATE leaderboard SET score = ? WHERE username = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, score); // Update with the final score
            stmt.setString(2, username); // Match by username
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showResult() {

        stopMusic();

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

    private void stopMusic() {
        // Stop the clip if it is playing
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
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
        // score -= 5; // Deduct points for time out
        showNextQuestion();
    }

    @Override
    public void updateProgressBar(int progress) {
        progressBar.setValue(progress); // Update progress bar value
    }

    @Override
    public void updateTimerLabel(String text) {
        timerLabel.setText(text); // Update timer label
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }
}