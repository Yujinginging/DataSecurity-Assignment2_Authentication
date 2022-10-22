import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

public class PrinterServiceImpl implements PrinterService{
    boolean serverStatus = false; //false means server is off, true means on
    ArrayList<Printer> printerList ;

    @Override
    public String echo(String input) throws RemoteException {
        return null;
    }

    @Override
    public String print(String filename, String printer) throws RemoteException {
        for (int i=0; i<printerList.size();i++){
            if (printerList.get(i).printer == printer){
                printerList.get(i).addFileIntoQueue(filename);
            }
        }
        return "printing" + filename + "on the printer " + printer; //printer.fileName; printing ** on the printer **
    }

    @Override
    public Dictionary queue(String printer) throws RemoteException {
        return null; // printer.getQueue()
    }

    @Override
    public String topQueue(String printer, int job) throws RemoteException {
        return "moves to the top in the queue"; // job number
    }

    @Override
    public String start() throws RemoteException {
        if (serverStatus==false){
            serverStatus = true;
            return "The server starts working now";

        }else {
            return "The server has been working already.";
        }
    }

    @Override
    public String stop() throws RemoteException {
        if (serverStatus == true){
            serverStatus=false;
            return "The server is stopped now";

        }else {
            return "The server is stopped already.";

        }
    }

    @Override
    public String restart() throws RemoteException {
        if (serverStatus == true){
            //restart

            return "The server has been restarted now!";

        }else {
           return "The server is off, please start the server instead!";
        }
    }

    @Override
    public String status(String printer) throws RemoteException {
        return null; //printer.status(); //printer status method needed
    }

    @Override
    public String readConfig(String parameter) throws RemoteException {
        return null;
    }

    @Override
    public String setConfig(String parameter, String value) throws RemoteException {
        return null;
    }
}
