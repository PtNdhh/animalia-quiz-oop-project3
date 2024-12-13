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
        new Question("Hewan apakah yang terkenal suka menyimpan makanan di pipinya, sehingga pipinya jadi kelihatan lucu?", "Gajah", "Tupai", "Hamster", "Panda", "Hamster"),
        new Question("Hewan apa yang bekerja siang hari sebagai burung dan malam hari jadi pelawak dengan suara lucunya?", "Burung Hantu", "Burung Kakak Tua", "Burung Kolibri", "Ayam", "Burung Hantu"),
        new Question("Hewan apakah yang bergerak super lambat, tapi kalau bicara soal tidur, dia adalah juaranya?", "Kura-kura", "Siput", "Sloth", "Koala", "Sloth"),
        new Question("Ikan apa yang suka tersenyum bahkan ketika sedang dikejar-kejar?", "Hiu", "Duyung", "Ikan Badut", "Lele", "Ikan Badut"),
        new Question("Hewan apakah yang disebut sebagai Raja Hutan, tapi kalau diajak berenang, langsung mengeluh?", "Harimau", "Singa", "Serigala", "Kucing", "Singa")
        );

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