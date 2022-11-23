import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PrinterServer {
    private static final int PORT = 5099;
    public static void main(String[] args) throws RemoteException {
        try {
            // Create SSL-based registry
            Registry registry = LocateRegistry.createRegistry(PORT,
                    new RMISSLClientSocketFactory(),
                    new RMISSLServerSocketFactory());

            // Bind this object instance to the name "HelloServer"
            registry.rebind("printer", new PrinterServant(false));

            System.out.println("PrinterServant bound in registry");
        } catch (Exception e) {
            System.out.println("Err: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
