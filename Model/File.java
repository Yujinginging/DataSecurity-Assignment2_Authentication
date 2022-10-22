public class File {
    String fileName;
    int jobNumber;

    public File(String fileName, int jobNumber) {
        this.fileName = fileName;
        this.jobNumber = jobNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(int jobNumber) {
        this.jobNumber = jobNumber;
    }
}
