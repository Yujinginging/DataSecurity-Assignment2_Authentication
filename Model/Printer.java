import java.util.ArrayList;

public class Printer {
    String printer;

    File file;

    boolean status;
    ArrayList<File> queue = new ArrayList<File>();

    public Printer(String printer){
        this.printer = printer;
    }
    public String getPrinter(){
        return printer;
    }
    public ArrayList<File> getQueue(){
        return queue;
    }

    public void setQueue(ArrayList<File> queue){
        queue = queue;
    }

    public void addFileIntoQueue(String fileName){

        if (queue.size() >0) {
            int jobNumber = queue.size() + 1;
            queue.add(new File(fileName, jobNumber));
        }else {
            int jobNumber = 1;
            queue.add(new File(fileName, jobNumber));

        }
    }

    public boolean getStatus(){
        return status;
    }

    public void setStatus(boolean status){
        this.status = status;
    }

    public String getFileNameByJobNumber(int jobNumber){
        for (int i =0;i<queue.size();i++){
            if (queue.get(i).getJobNumber() == jobNumber ){
                return queue.get(i).getFileName();
            }
        }
        return null;
    }

    public File getFileByJob(int job){
        for (int i =0;i<queue.size();i++){
            if (queue.get(i).getJobNumber() == job ){
                return queue.get(i);
            }
        }return  null;
    }
}
