package reducers_listener;

public class ReducersListenerInfo {
    private int step;
    private int expectedFilesCount;

    public ReducersListenerInfo() {
    }

    public ReducersListenerInfo(int step, int expectedFilesCount) {
        this.step = step;
        this.expectedFilesCount = expectedFilesCount;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getExpectedFilesCount() {
        return expectedFilesCount;
    }

    public void setExpectedFilesCount(int expectedFilesCount) {
        this.expectedFilesCount = expectedFilesCount;
    }
}
