import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.sql.*;
import java.util.*;
// Importing input output classes


public class PrinterServant extends UnicastRemoteObject implements PrinterService{
    boolean serverStatus = false; //false means server is off, true means on
    String printerOff= "Printer server is off. Start the printer server before selecting another action.";
    ArrayList<Printer> printerList;
    HashMap<String,String> parameterList;
    Connection dbConnector;
    String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "\\database.db";
    int role = 0;
    boolean userLoggedIn = false;
    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe
    String activeToken = "";
    private static final int PORT = 5099;


    private static HashMap<String, String[]> policy;

    public PrinterServant(boolean serverStatus) throws Exception {
        super(PORT, new RMISSLClientSocketFactory(),
                new RMISSLServerSocketFactory());
        printerList = new ArrayList<>();
        this.serverStatus = serverStatus;
        try{
            dbConnector = DriverManager.getConnection(url);
            Statement statement = dbConnector.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM PRINTER");

            while(resultSet.next()){
                String name = resultSet.getString("Name");
                Printer p = new Printer(name);
                printerList.add(p);
                System.out.println(name);
            }
        }catch(SQLException e){
            e.getErrorCode();
        }
        //creating a parameter array list
        parameterList = new HashMap<String,String>();
        int randomNum;
        randomNum = 3;
        for (int i = 0; i < randomNum; i++){
            String parameterName = "Parameter" + i;
            parameterList.put(parameterName, " ");
            System.out.println();
        }
        for (String i : parameterList.keySet()) {
            System.out.println(i);
        }

        // ACL policy implementation
        PolicyReader policyReader = null;
        policy = policyReader.createACL_Policy();
    }
    @Override
    public String echo(String input) throws RemoteException {
        return null;
    }

    @Override
    public String print(String filename, String printer, String token, String clientName) throws RemoteException {
        // checks :
        if(!token.equals(activeToken)) return "Session token is not valid";

        if(!userLoggedIn) return "User not logged in!";

        if (!checkUsersACL(clientName, "print")) return "Specific User doesn't have the rights to access this task!";
        // end of checks !

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

    public ArrayList<File> queue(String printer, String token, String clientName) throws RemoteException {

        // checks :
        if(!token.equals(activeToken)) return null;

        if(!userLoggedIn) return null;

        if (!checkUsersACL(clientName, "queue")) return null;
        // end of checks !
        for (int i=0;i<printerList.size();i++){
            if ((printerList.get(i).getPrinter()).equals(printer)){
                return printerList.get(i).getQueue();
            }
        }
        return null;
    }

    @Override
    public String topQueue(String printer, int job, String token, String clientName) throws RemoteException {
        // checks :
        if(!token.equals(activeToken)) return "Session token is not valid";

        if(!userLoggedIn) return "User not logged in!";

        if (!checkUsersACL(clientName, "topQueue")) return "Specific User doesn't have the rights to access this task!";
        // end of checks !

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
    public String start(String token, String clientName) throws RemoteException {
        // checks :
        if(!token.equals(activeToken)) return "Session token is not valid";

        if(!userLoggedIn) return "User not logged in!";

        if (!checkUsersACL(clientName, "start")) return "Specific User doesn't have the rights to access this task!";
        // end of checks !

        if (!serverStatus){
            serverStatus = true;
            // when the print server starts working it automatically starts all the printers as well!
            for (int i = 0; i < printerList.size(); i++){
                printerList.get(i).setStatus(true);
            }
            return "The server starts working now";
        }else {
            return "The server has been working already.";
        }
    }

    @Override
    public String stop(String token, String clientName) throws RemoteException {

        // checks :
        if(!token.equals(activeToken)) return "Session token is not valid";

        if(!userLoggedIn) return "User not logged in!";

        if (!checkUsersACL(clientName, "stop")) return "Specific User doesn't have the rights to access this task!";
        // end of checks !
        if (serverStatus){
            serverStatus=false;
            return "The server is stopped now";

        }else {
            return "The server is stopped already.";

        }
    }

    @Override
    public String restart(String token, String clientName) throws RemoteException {
        // checks :
        if(!token.equals(activeToken)) return "Session token is not valid";

        if(!userLoggedIn) return "User not logged in!";

        if (!checkUsersACL(clientName, "restart")) return "Specific User doesn't have the rights to access this task!";
        // end of checks !

        if (serverStatus){
            //restart

            return "The server has been restarted now!";

        }else {
           return "The server is off, please start the server instead!";
        }
    }

    @Override
    public String status(String printer, String token, String clientName) throws RemoteException {
        // checks :
        if(!token.equals(activeToken)) return "Session token is not valid";

        if(!userLoggedIn) return "User not logged in!";

        if (!checkUsersACL(clientName, "status")) return "Specific User doesn't have the rights to access this task!";
        // end of checks !

        for(int i = 0; i < printerList.size(); i++){
            if ((printerList.get(i).getPrinter()).equals(printer)) {
                if (printerList.get(i).getStatus()){
                    return printer + " is ON.";
                }
                else{
                    return printerOff;
                }
            }
        }
        return "Did not found the selected printer in the list";
    }

    @Override
    public String readConfig(String parameter, String token, String clientName) throws RemoteException {
        // checks :
        if(!token.equals(activeToken)) return "Session token is not valid";

        if(!userLoggedIn) return "User not logged in!";

        if (!checkUsersACL(clientName, "readConfig")) return "Specific User doesn't have the rights to access this task!";
        // end of checks !

        String value = parameterList.get(parameter);
        if (value.equals(" ") || value.equals(null)){
            return "Did not found the parameter in the list.";
        }else {
            return value;
        }

    }

    @Override
    public String setConfig(String parameter, String value, String token, String clientName) throws RemoteException {
        // checks :
        if(!token.equals(activeToken)) return "Session token is not valid";

        if(!userLoggedIn) return "User not logged in!";

        if (!checkUsersACL(clientName, "setConfig")) return "Specific User doesn't have the rights to access this task!";
        // end of checks !

        for (String i : parameterList.keySet()) {
            if (i.equals(parameter)){
                parameterList.put(i, value);
                return "Value set";
            }
        }
        return "Did not found the parameter in the list.";
    }

    @Override
    public String logOut(String token) throws RemoteException {
        if(!token.equals(activeToken)) return "Session token is not valid";
        activeToken = "";
        return "Log out";
    }

    public void queryDone() throws SQLException {
        Statement statement = dbConnector.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM User");
        while(resultSet.next()){
            System.out.println(resultSet.getString("Login") + " " + resultSet.getString("Password"));
        }
    }
    @Override
    public String logIn(String login, String password) throws RemoteException {
        try{
            User newUser = new User(login, password, (short) 0);

            Statement statement = dbConnector.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM User WHERE Login = '" + login + "'");

            while(resultSet.next()){
                String dbPassword = resultSet.getString("Password");
                int dbRole = resultSet.getInt("Role");

                if(dbPassword.equals(newUser.password)){
                    role = dbRole;
                    userLoggedIn = true;
                    activeToken = generateNewToken();
                    return activeToken;
                }
            }
        }catch(Exception e){
            return e.getMessage();
        }
        return "Not logged in";
    }

    @Override
    public String Register(String login, String password) throws RemoteException {
        try{
            User newUser = new User(login, password, (short) 0);

            Statement statement = dbConnector.createStatement();

            boolean execute = statement.execute("INSERT INTO User (Login, Password, Role)\n" +
                                                    "VALUES ('" + newUser.login + "', '" + newUser.password + "'," + newUser.role + ");");
            if(!execute) return "Registered";
        }catch(Exception e){
            return e.getMessage();
        }
        return "Execute failed";
    }

    public boolean checkIfPrinterIsOn(){
        return serverStatus;
    }

    @Override
    public String toStringQueue(String printer, String token, String clientName) throws RemoteException {
        if(!token.equals(activeToken)) return "Session token is not valid";
        if(!userLoggedIn) return "User not logged in!";
        ArrayList<File> queue = queue(printer, token,clientName);
        String s = "";
        for (int i=0;i<queue.size();i++){
            s += queue.get(i).getJobNumber() + "   " + queue.get(i).getFileName() + "\n";
        }
        return s;
    }
    private static boolean checkUsersACL(String clientUsername, String functionName){
        String[] functionEntry = new String[8];
        functionEntry = policy.get(functionName);
        System.out.println(functionName);
        System.out.println(Arrays.toString(functionEntry));

        boolean userAccess = false;

        for (int i = 0; i < 8; i++){
            if (clientUsername.equals(functionEntry[i])){
                userAccess = true;
            }
        }

        System.out.println(userAccess + " " + clientUsername + " " + functionName);
        return userAccess;
    }
    private static String generateNewToken() {
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}

