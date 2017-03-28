package reducer_wrapper;

public class ReducerStepInfo {
    private int step;
    private int filesToProcess;
    private int filesProcessed;
    private int batchesCount;

    public ReducerStepInfo() {
    }

    public ReducerStepInfo(int step, int filesToProcess, int filesProcessed, int batchesCount) {
        this.step = step;
        this.filesToProcess = filesToProcess;
        this.filesProcessed = filesProcessed;
        this.batchesCount = batchesCount;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getFilesToProcess() {
        return filesToProcess;
    }

    public void setFilesToProcess(int filesToProcess) {
        this.filesToProcess = filesToProcess;
    }

    public int getFilesProcessed() {
        return filesProcessed;
    }

    public void setFilesProcessed(int filesProcessed) {
        this.filesProcessed = filesProcessed;
    }

    public int getBatchesCount() {
        return batchesCount;
    }

    public void setBatchesCount(int batchesCount) {
        this.batchesCount = batchesCount;
    }

    @Override
    public String toString() {
        return "ReducerStepInfo{" +
                "step=" + step +
                ", filesToProcess=" + filesToProcess +
                ", filesProcessed=" + filesProcessed +
                ", batchesCount=" + batchesCount +
                '}';
    }
}
