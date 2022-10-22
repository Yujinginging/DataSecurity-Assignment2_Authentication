import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class PrinterServer {

    public static void main(String[] args) throws RemoteException {
        //creating a list of 2 printers and sending the list to the Printer Servant
        ArrayList<Printer> printers = new ArrayList<>();
        Printer p1 = new Printer("1");
        Printer p2 = new Printer("2");
        printers.add(p1);
        printers.add(p2);

        //
        Registry registry = LocateRegistry.createRegistry(5099);
        registry.rebind("printer", new PrinterServant(false,printers));
    }
}
