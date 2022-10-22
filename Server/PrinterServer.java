import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class PrinterServer {



    public static void main(String[] args) throws RemoteException {
        //
        ArrayList<Printer> printers = new ArrayList<>();
        Printer p1 = new Printer("1");
        printers.add(p1);

        //
        Registry registry = LocateRegistry.createRegistry(5099);
        registry.rebind("printer", new PrinterServiceImpl(false,printers));
    }
}
