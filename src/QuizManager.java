import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizManager {
    private Connection connection;

    public QuizManager(Connection connection) {
        this.connection = connection;
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

    public boolean hasNextQuestion() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasNextQuestion'");
    }

    public Question getNextQuestion() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNextQuestion'");
    }

}
