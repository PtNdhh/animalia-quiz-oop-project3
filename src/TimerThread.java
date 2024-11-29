import javax.swing.SwingUtilities;

public class TimerThread extends Thread {
    private int seconds;
    private TimerListener listener;
    private boolean isRunning = true;

    public TimerThread(int seconds, TimerListener listener) {
        this.seconds = seconds;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            for (int i = seconds; i > 0; i--) {
                if (!isRunning)
                    break;
                final int remainingTime = i;
                SwingUtilities.invokeLater(() -> {
                    int progress = (int) ((double) remainingTime / seconds * 100);
                    listener.updateProgressBar(progress);
                    listener.updateTimerLabel("Time Left: " + remainingTime + "s");
                });
                Thread.sleep(1000);
            }
            listener.onTimeOut();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopTimer() {
        isRunning = false;
    }
}
