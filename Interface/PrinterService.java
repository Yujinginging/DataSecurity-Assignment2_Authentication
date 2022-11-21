import java.lang.reflect.Array;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Dictionary;

public interface PrinterService extends Remote {
    public String echo(String input) throws RemoteException;

    public String print(String filename, String printer, String token) throws RemoteException; // prints file filename on the specified printer

    public ArrayList<File> queue(String printer, String token) throws RemoteException; // lists the print queue for a given printer on the user's display in lines of the form <job number>   <file name>

    public String topQueue(String printer, int job, String token) throws RemoteException; // moves job to the top of the queue

    public String start(String token) throws RemoteException; // starts the print server

    public String stop(String token) throws RemoteException;   // stops the print server

    public String restart(String token) throws RemoteException; // stops the print server, clears the print queue and starts the print server again

    public String status(String printer, String token) throws RemoteException; // prints status of printer on the user's display

    public String readConfig(String parameter, String token) throws RemoteException; // prints the value of the parameter on the user's display

    public String setConfig(String parameter, String value, String token) throws RemoteException; // sets the parameter to value

    public String logOut(String token) throws RemoteException; //logs out the client

    public String logIn(String login, String password) throws RemoteException;

    public String Register(String login, String password) throws RemoteException;

    public String toStringQueue(String printer, String token) throws RemoteException;

    public String ChangeRole(String user, int newRole, String token) throws RemoteException;

}
