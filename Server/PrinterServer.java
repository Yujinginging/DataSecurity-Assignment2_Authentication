import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class PrinterServer {

    public static void main(String[] args) throws RemoteException {


        //
        Registry registry = LocateRegistry.createRegistry(5099);
        registry.rebind("printer", new PrinterServant(false));
    }
}
