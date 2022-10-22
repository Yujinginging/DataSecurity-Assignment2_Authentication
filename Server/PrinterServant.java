import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Dictionary;

public class PrinterServant extends UnicastRemoteObject implements PrinterService{
    boolean serverStatus = false; //false means server is off, true means on
    String printerOff= "Printer server is off. Start the printer server before selecting another action.";
    ArrayList<Printer> printerList ;
    //
    public PrinterServant(boolean serverStatus) throws RemoteException{
        super();
        printerList = new ArrayList<>();
        this.serverStatus = serverStatus;

        //creating 2 printers in the printList
        Printer p1 = new Printer("1");
        Printer p2 = new Printer("2");
        printerList.add(p1);
        printerList.add(p2);
    }
    @Override
    public String echo(String input) throws RemoteException {
        return null;
    }

    @Override
    public String print(String filename, String printer) throws RemoteException {
        String s = null;
        if (!checkIfPrinterIsOn()){
            return printerOff;
        }

        for (int i = 0; i < printerList.size(); i++) {
            if ((printerList.get(i).getPrinter()).equals(printer)) {
                //add file to the queue
                Printer newPrinter = printerList.get(i);
                int num = newPrinter.getQueue().size()+1;
                newPrinter.queue.add(new File(filename,num));//
                s =  "printing" + " [" + filename + "] on printer " + " " + printer; //printer.fileName; printing ** on the printer **
                printerList.get(i).setQueue(newPrinter.queue);
                break;
            } else {
                s =  "there is no such printer, please try again!"; // if the user enters the wrong printer name.
            }
        }
        return s;
    }

    @Override
    public ArrayList<File> queue(String printer) throws RemoteException {
        for (int i=0;i<printerList.size();i++){
            if ((printerList.get(i).getPrinter()).equals(printer)){
                return printerList.get(i).getQueue();
            }
        }
        return null;
    }

    @Override
    public String topQueue(String printer, int job) throws RemoteException {
        String filename=null;
        for (int i=0;i<printerList.size();i++){
            if ((printerList.get(i).getPrinter()).equals(printer)) {
                Printer p = printerList.get(i);
                ArrayList<File> newQueue = new ArrayList<File>();//create a new printer queue to edit the job number


                //remove the selected job first
                File topfile = p.getFileByJob(job);
                p.queue.remove(topfile);

                //
                filename = topfile.getFileName();
                newQueue.add(new File(filename,1));

                //set the new queue with the selected job on the top
                for (int j=1;j<=p.getQueue().size();j++){
                    newQueue.add(new File(p.getFileNameByJobNumber(j),j+1));
                }
                //replace the printer in the printerlist to the newPrinter
                printerList.get(i).setQueue(newQueue);
                break;
            }
            }

        return "Job number " + job +" named: " + filename + " moves to the top in the queue"; // job number
    }

    @Override
    public String start() throws RemoteException {
        if (!serverStatus){
            serverStatus = true;
            return "The server starts working now";

        }else {
            return "The server has been working already.";
        }
    }

    @Override
    public String stop() throws RemoteException {
        if (serverStatus){
            serverStatus=false;
            return "The server is stopped now";

        }else {
            return "The server is stopped already.";

        }
    }

    @Override
    public String restart() throws RemoteException {
        if (serverStatus){
            //restart

            return "The server has been restarted now!";

        }else {
           return "The server is off, please start the server instead!";
        }
    }

    @Override
    public String status(String printer) throws RemoteException {
        return printer; //printer.status(); //printer status method needed
    }

    @Override
    public String readConfig(String parameter) throws RemoteException {
        return null;
    }

    @Override
    public String setConfig(String parameter, String value) throws RemoteException {
        return null;
    }

    @Override
    public String logOut() throws RemoteException {
        return "Log out";
    }

    public boolean checkIfPrinterIsOn(){
        return serverStatus;
    }

    @Override
    public String toStringQueue(String printer) throws RemoteException {
        ArrayList<File> queue = queue(printer);
        String s = "";
        for (int i=0;i<queue.size();i++){
            s += queue.get(i).getJobNumber() + "   " + queue.get(i).getFileName() + "\n";
        }
        return s;
    }
}
