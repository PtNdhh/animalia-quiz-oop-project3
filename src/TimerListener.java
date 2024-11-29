public interface TimerListener {
    void onTimeOut();

    void updateProgressBar(int progress);

    void updateTimerLabel(String text);
}
