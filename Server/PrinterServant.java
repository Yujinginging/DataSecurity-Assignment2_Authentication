import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.*;

public class PrinterServant extends UnicastRemoteObject implements PrinterService{
    boolean serverStatus = false; //false means server is off, true means on
    String printerOff= "Printer server is off. Start the printer server before selecting another action.";
    ArrayList<Printer> printerList;
    HashMap<String,String> parameterList;
    Connection dbConnector;
    String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "\\database.db";
    Map<String, Integer[]> roles = new HashMap<String, Integer[]>();;
    String currentRole;
    Integer[] priv = {0,0,0,0,0,0,0,0,0};
    boolean userLoggedIn = false;
    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe
    String activeToken = "";
    private static final int PORT = 5099;

    //
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
            ResultSet getRoles = statement.executeQuery("SELECT * FROM ROLE");


            while(getRoles.next()){
                String name = resultSet.getString("Name");
                int print = resultSet.getInt("print");
                int queue = resultSet.getInt("queue");
                int topQueue = resultSet.getInt("topQueue");
                int start = resultSet.getInt("start");
                int stop = resultSet.getInt("stop");
                int restart = resultSet.getInt("restart");
                int status = resultSet.getInt("status");
                int readConfig = resultSet.getInt("readConfig");
                int setConfig = resultSet.getInt("setConfig");

                List<Integer> priv = new ArrayList<Integer>();
                priv.add(print);
                priv.add(queue);
                priv.add(topQueue);
                priv.add(start);
                priv.add(stop);
                priv.add(restart);
                priv.add(status);
                priv.add(readConfig);
                priv.add(setConfig);
                priv.toArray();
                Integer[] arr = new Integer[priv.size()];
                arr = priv.toArray(arr);
                roles.put(name, arr);
                System.out.println(name + ", " + priv.toString());
            }


        }catch(SQLException e){
            System.out.println(e.getErrorCode());
        }
        //creating a parameter array list
        parameterList = new HashMap<String,String>();
        int randomNum;
        int upperBound = 10;
        //randomNum = rand.nextInt(upperBound);
        randomNum = 3;
        for (int i = 0; i < randomNum; i++){
            String parameterName = "Parameter" + i;
            parameterList.put(parameterName, " ");
            System.out.println();
        }
        for (String i : parameterList.keySet()) {
            System.out.println(i);
        }

    }
    @Override
    public String echo(String input) throws RemoteException {
        return null;
    }

    @Override
    public String print(String filename, String printer, String token) throws RemoteException {
        if(!token.equals(activeToken)) return "Session token is not valid";
        if(!userLoggedIn) return "User not logged in!";
        if(priv[0] == 0) return "Specific User doesn't have the rights to access this task!";
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
    public ArrayList<File> queue(String printer, String token) throws RemoteException {
        if(!token.equals(activeToken)) return null;
        if(!userLoggedIn) return null;
        if(priv[1] == 0) return null;
        for (int i=0;i<printerList.size();i++){
            if ((printerList.get(i).getPrinter()).equals(printer)){
                return printerList.get(i).getQueue();
            }
        }
        return null;
    }

    @Override
    public String topQueue(String printer, int job, String token) throws RemoteException {
        if(!token.equals(activeToken)) return "Session token is not valid";
        if(!userLoggedIn) return "User not logged in!";
        if(priv[2] == 0) return "Specific User doesn't have the rights to access this task!";
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
    public String start(String token) throws RemoteException {
        if(!token.equals(activeToken)) return "Session token is not valid";
        if(!userLoggedIn) return "User not logged in!";
        if(priv[3] == 0) return "Specific User doesn't have the rights to access this task!";
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
    public String stop(String token) throws RemoteException {
        if(!token.equals(activeToken)) return "Session token is not valid";
        if(!userLoggedIn) return "User not logged in!";
        if(priv[4] == 0) return "Specific User doesn't have the rights to access this task!";
        if (serverStatus){
            serverStatus=false;
            return "The server is stopped now";

        }else {
            return "The server is stopped already.";

        }
    }

    @Override
    public String restart(String token) throws RemoteException {
        if(!token.equals(activeToken)) return "Session token is not valid";
        if(!userLoggedIn) return "User not logged in!";
        if(priv[5] == 0) return "Specific User doesn't have the rights to access this task!";

        if (serverStatus){
            //restart

            return "The server has been restarted now!";

        }else {
           return "The server is off, please start the server instead!";
        }
    }

    @Override
    public String status(String printer, String token) throws RemoteException {
        if(!token.equals(activeToken)) return "Session token is not valid";
        if(!userLoggedIn) return "User not logged in!";
        if(priv[6] == 0) return "Specific User doesn't have the rights to access this task!";
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
    public String readConfig(String parameter, String token) throws RemoteException {
        if(!token.equals(activeToken)) return "Session token is not valid";
        if(!userLoggedIn) return "User not logged in!";
        if(priv[7] == 0) return "Specific User doesn't have the rights to access this task!";
        String value = parameterList.get(parameter);
        if (value.equals(" ") || value.equals(null)){
            return "Did not found the parameter in the list.";
        }else {
            return value;
        }

    }

    @Override
    public String setConfig(String parameter, String value, String token) throws RemoteException {
        if(!token.equals(activeToken)) return "Session token is not valid";
        if(!userLoggedIn) return "User not logged in!";
        if(priv[8] == 0) return "Specific User doesn't have the rights to access this task!";
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
    //    if(!userLoggedIn) return "User not logged in!";
    //    userLoggedIn = false;
        if(!token.equals(activeToken)) return "Session token is not valid";
        activeToken = "";
        for(int i=0;i<priv.length;i++)
        {
            priv[i] = 0;
        }
        return "Log out";
    }

    @Override
    public String logIn(String login, String password) throws RemoteException {
        try{
            User newUser = new User(login, password, (short) 0);

            Statement statement = dbConnector.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM User WHERE Login = '" + login + "'");

            while(resultSet.next()){
                String dbPassword = resultSet.getString("Password");
                String dbRole = resultSet.getString("Roles");


                if(dbPassword.equals(newUser.password)){
                    currentRole = dbRole;
                    userLoggedIn = true;
                    activeToken = generateNewToken();
                    String[] arrOfStr = currentRole.split(";", 0);

                    for (String a : arrOfStr)
                    {
                        for(int i=0;i<priv.length;i++)
                        {
                            priv[i] = priv[i] + roles.get(a)[i];
                        }
                    }
                    return activeToken;
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
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
    public String toStringQueue(String printer, String token) throws RemoteException {
        if(!token.equals(activeToken)) return "Session token is not valid";
        if(!userLoggedIn) return "User not logged in!";
        ArrayList<File> queue = queue(printer, token);
        String s = "";
        for (int i=0;i<queue.size();i++){
            s += queue.get(i).getJobNumber() + "   " + queue.get(i).getFileName() + "\n";
        }
        return s;
    }

    @Override
    public String ChangeRole(String user, String newRole, String token) throws RemoteException {
        if(!token.equals(activeToken)) return "Session token is not valid";
        if(!userLoggedIn) return "User not logged in!";
        if(!currentRole.contains("Admin")) return "Specific User doesn't have the rights to access this task!";

        try{
            Statement statement = dbConnector.createStatement();
            String[] arrOfStr = newRole.split(";", 0);

            for (String a : arrOfStr)
            {
                if(!roles.containsKey(a)) return "One of roles doesn't exists " + a;
            }

            boolean execute = statement.execute("UPDATE User SET Roles='"+newRole+"' WHERE Login = '"+user+"'");
            if(!execute) return "Role changed";
        }catch(Exception e){
            return e.getMessage();
        }
        return "Execute failed";
    }

    private static String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String generateNewToken() {
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
