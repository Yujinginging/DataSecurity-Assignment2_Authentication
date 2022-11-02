import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.Dictionary;
import java.util.concurrent.TimeUnit;

public class Client {
    public static void main(String[] args) throws IOException, NotBoundException, InterruptedException {
        PrinterService service = (PrinterService) Naming.lookup("rmi://localhost:5099/printer");

        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        //int selectedService = selectService(consoleReader);

        boolean logOutFlag = false;
        boolean logInFlag  = false;
        String activeToken = "";

        System.out.println("Welcome to the print server. If its you first time connecting, please register. Otherwise proceed with login.");
        System.out.println("Press [R] for registering.");
        System.out.println("Press [L] for login.");
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        String line = consoleReader.readLine();
//      System.out.println(line);

        while (!logInFlag) {

            if (line.equals("R")) {
                System.out.println("--------------------------");
                System.out.println("Client Register:");
                String registerAction;
                registerAction = registerClient(consoleReader, service);
                System.out.println(registerAction);

                line = "L";
            }
            if (line.equals("L") ) {
                System.out.println("--------------------------");
                System.out.println("Client Login: ");
                String loginAction;
                loginAction = loginClient(consoleReader, service);
                activeToken = loginAction;
                System.out.println("Client logged in!");
                logInFlag = true;
            }
            else {
                System.out.println("Press [R] or [L] in order to continue!");
                line = consoleReader.readLine();
            }
        }

        // Loop of printer actions from the client, until the client asks to Log out !
        while (!logOutFlag){
            String printerAction = printerCall(consoleReader, selectService(consoleReader), service, activeToken);
            System.out.println(printerAction);
            if (printerAction.equals("Log out")){
                System.out.println("Client logged out from the server.");
                logOutFlag = true;
            }
            TimeUnit.SECONDS.sleep(3);
        }
    }

    private static String printerCall(BufferedReader consoleReader, int selectedService, PrinterService service, String activeToken) throws IOException {
        String message = " ";
        if (selectedService == 1) {
            message = service.start(activeToken);
        } else if (selectedService == 2) {
            message = service.stop(activeToken);
        } else if (selectedService == 3) {
            message = service.restart(activeToken);
        } else if (selectedService == 4) {
            System.out.println("Which file you want to be printed: ");
            String file = consoleReader.readLine();
            System.out.println("Which printer you want to use: ");
            String printer = consoleReader.readLine();

            message = service.print(file, printer, activeToken);
        } else if (selectedService == 5) {
            System.out.println("Which printer you want to select the queue from: ");
            String printer = consoleReader.readLine();

            message = service.toStringQueue(printer, activeToken);
        } else if (selectedService == 6){
            System.out.println("Which printer you want to select the status from: ");
            String printer = consoleReader.readLine();

            System.out.println("Which job number you want to be printed: ");
            int jobNumber = Integer.parseInt(consoleReader.readLine());

            message = service.topQueue(printer,jobNumber, activeToken);
        }else if (selectedService == 7) {
            System.out.println("Which printer you want to select the status from: ");
            String printer = consoleReader.readLine();

            message = service.status(printer, activeToken);
        } else if (selectedService == 8) {
            System.out.println("Which parameter you want to get the value from: ");
            String parameter = consoleReader.readLine();

            message = service.readConfig(parameter, activeToken);
        } else if (selectedService == 9) {
            System.out.println("Which parameter you want to set its value : ");
            String parameter = consoleReader.readLine();
            System.out.println("Which is the new value of the parameter: ");
            String value = consoleReader.readLine();

            message = service.setConfig(parameter, value, activeToken);
        } else if(selectedService == 0){
            message = service.logOut(activeToken);
            System.out.println("-------------------------------------------------------------------------------------------------------------");
        } else
            message = "wrong number...this specified number doesn't belong to a service.";
        return message;
    }

    public static String loginClient(BufferedReader consoleReader, PrinterService service) throws IOException {
        String token;

        System.out.println("Username: ");
        String login = consoleReader.readLine();
        System.out.println("Password: ");
        String password = consoleReader.readLine();
        token = service.logIn(login, password);

        return token;

    }

    public static String registerClient(BufferedReader consoleReader, PrinterService service) throws IOException {
        String message;

        System.out.println("Username: ");
        String login = consoleReader.readLine();
        System.out.println("Password: ");
        String password = consoleReader.readLine();
        message = service.Register(login, password);

        return message;
    }

    public static int selectService(BufferedReader consoleReader) {
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        System.out.println("Select the service you would like to use: ");
        System.out.println("Press 1 if you want to start the server.");
        System.out.println("Press 2 if you want to stop the server.");
        System.out.println("Press 3 if you want to restart the server.");
        System.out.println("Press 4 if you want to print a file.");
        System.out.println("Press 5 if you want to list the print queue");
        System.out.println("Press 6 if you want to move a job to the top of the queue");
        System.out.println("Press 7 if you want to get the status of the printer");
        System.out.println("Press 8 if you want to get the value of a parameter");
        System.out.println("Press 9 if you want to set the value of a parameter");
        System.out.println("Press 0 if you want to log out.");

        try{
            return Integer.parseInt(consoleReader.readLine());
        }catch (Exception e){
            return 0;
        }
    }
}
