import java.sql.*;
import java.util.List;

public class QuizManager {
    private static List<Question> questions;
    private int currentIndex = 0;

    public QuizManager() {
        loadQuestion();
    }

    private void loadQuestion() {
        questions = List.of(
                new Question(
                        "Hewan apakah yang terkenal suka menyimpan makanan di pipinya, sehingga pipinya jadi kelihatan lucu?",
                        "Gajah", "Tupai", "Hamster", "Panda", "Hamster"),
                new Question(
                        "Hewan apa yang bekerja siang hari sebagai burung dan malam hari jadi pelawak dengan suara lucunya?",
                        "Burung Hantu", "Burung Kakak Tua", "Burung Kolibri", "Ayam", "Burung Hantu"),
                new Question(
                        "Hewan apakah yang bergerak super lambat, tapi kalau bicara soal tidur, dia adalah juaranya?",
                        "Kura-kura", "Siput", "Sloth", "Koala", "Sloth"),
                new Question("Ikan apa yang suka tersenyum bahkan ketika sedang dikejar-kejar?", "Hiu", "Duyung",
                        "Ikan Badut", "Lele", "Ikan Badut"),
                new Question(
                        "Hewan apakah yang disebut sebagai Raja Hutan, tapi kalau diajak berenang, langsung mengeluh?",
                        "Harimau", "Singa", "Serigala", "Kucing", "Singa"),
                new Question("Hewan apakah yang dikenal dengan kantongnya dan sering terlihat melompat?", "Kanguru",
                        "Kucing", "Anjing", "Domba", "Kanguru"),
                new Question("Hewan apakah yang bisa mengubah warna kulitnya untuk bersembunyi?", "Kadal", "Laba-laba",
                        "Kura-kura", "Bunglon", "Bunglon"),
                new Question("Hewan apakah yang dikenal sebagai pengembara lautan dan memiliki tentakel?", "Gurita",
                        "Penguin", "Ikan Salmon", "Sardine", "Gurita"),
                new Question("Hewan apakah yang memiliki cangkang keras dan bisa hidup di darat maupun air?",
                        "Kura-kura", "Tikus", "Ular", "Ikan", "Kura-kura"),
                new Question(
                        "Hewan laut apakah yang dikenal sebagai predator puncak di lautan dan dapat berburu dengan canggih?",
                        "Hiu", "Orca", "Duyung", "Lumba-lumba", "Orca"));

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