import java.util.Dictionary;

public class Printer {
    String printer;
    String fileName;

    Dictionary queue;

    public Printer(String printer){
        this.printer = printer;
    }
    public Dictionary getQueue(){
        return queue;
    }

    public void setQueue(Dictionary queue){
        queue = queue;
    }

    public void addFileIntoQueue(String fileName){
        int jobNumber = queue.size()+1;
        queue.put(jobNumber,fileName);
    }

}
