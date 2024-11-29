import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizManager {
    private Connection connection;
    private static List<Question> questions;
    private int currentIndex = 0;

    public QuizManager(Connection connection) {
        this.connection = connection;
    }

    public QuizManager() {
        loadQuestion();
    }

    private void loadQuestion() {
        questions = List.of(
                new Question("What is the capital of France?", "Paris", "Berlin", "Madrid", "Rome", "Paris"),
                new Question("Which planet is known as the Red Planet?", "Earth", "Mars", "Jupiter", "Venus", "Mars"),
                new Question("What is the largest ocean on Earth?", "Atlantic", "Indian", "Pacific", "Arctic",
                        "Pacific"),
                new Question("Who wrote the play 'Romeo and Juliet'?", "William Shakespeare", "Charles Dickens",
                        "Jane Austen", "Mark Twain", "William Shakespeare"),
                new Question("What is the smallest country in the world?", "Vatican City", "Monaco", "Nauru",
                        "San Marino", "Vatican City"));

    }

    public List<Question> loadQuestions() {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT id, question, option_a, option_b, option_c, option_d, correct_answer FROM questions";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String questionText = rs.getString("question");
                List<String> options = new ArrayList<>();
                options.add(rs.getString("option_a"));
                options.add(rs.getString("option_b"));
                options.add(rs.getString("option_c"));
                options.add(rs.getString("option_d"));
                String correctAnswer = rs.getString("correct_answer");

                questions.add(new Question(id, questionText, options, correctAnswer));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return questions;
    }

    public Question getNextQuestion() {
        if (currentIndex < questions.size()) {
            return questions.get(currentIndex++);
        }
        return null;
    }

    public boolean hasNextQuestion() {
        return currentIndex < questions.size();
    }

}