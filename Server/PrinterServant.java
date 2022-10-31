import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

public class PrinterServant extends UnicastRemoteObject implements PrinterService{
    boolean serverStatus = false; //false means server is off, true means on
    String printerOff= "Printer server is off. Start the printer server before selecting another action.";
    ArrayList<Printer> printerList;
    HashMap<String,String> parameterList;
    Connection dbConnector;
    String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "\\database.db";
    int role = 0;
    boolean userLoggedIn = false;
    //
    public PrinterServant(boolean serverStatus) throws RemoteException {
        super();
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
    public String print(String filename, String printer) throws RemoteException {
        if(!userLoggedIn) return "User not logged in!";
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
        if(!userLoggedIn) return null;
        for (int i=0;i<printerList.size();i++){
            if ((printerList.get(i).getPrinter()).equals(printer)){
                return printerList.get(i).getQueue();
            }
        }
        return null;
    }

    @Override
    public String topQueue(String printer, int job) throws RemoteException {
        if(!userLoggedIn) return "User not logged in!";
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
        if(!userLoggedIn) return "User not logged in!";
        if (!serverStatus){
            serverStatus = true;
            return "The server starts working now";

        }else {
            return "The server has been working already.";
        }
    }

    @Override
    public String stop() throws RemoteException {
        if(!userLoggedIn) return "User not logged in!";
        if (serverStatus){
            serverStatus=false;
            return "The server is stopped now";

        }else {
            return "The server is stopped already.";

        }
    }

    @Override
    public String restart() throws RemoteException {
        if(!userLoggedIn) return "User not logged in!";
        if (serverStatus){
            //restart

            return "The server has been restarted now!";

        }else {
           return "The server is off, please start the server instead!";
        }
    }

    @Override
    public String status(String printer) throws RemoteException {
        if(!userLoggedIn) return "User not logged in!";
        return printer; //printer.status(); //printer status method needed
    }

    @Override
    public String readConfig(String parameter) throws RemoteException {
        if(!userLoggedIn) return "User not logged in!";
        String value = parameterList.get(parameter);
        if (value.equals(" ") || value.equals(null)){
            return "Did not found the parameter in the list.";
        }else {
            return value;
        }

    }

    @Override
    public String setConfig(String parameter, String value) throws RemoteException {
        if(!userLoggedIn) return "User not logged in!";
        for (String i : parameterList.keySet()) {
            if (i.equals(parameter)){
                parameterList.put(i, value);
                return "Value set";
            }
        }
        return "Did not found the parameter in the list.";
    }

    @Override
    public String logOut() throws RemoteException {
    //    if(!userLoggedIn) return "User not logged in!";
    //    userLoggedIn = false;
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
                int dbRole = resultSet.getInt("Role");


                if(dbPassword.equals(newUser.password)){
                    role = dbRole;
                    userLoggedIn = true;
                    return newUser.password + ", " + dbPassword;
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
    public String toStringQueue(String printer) throws RemoteException {
        if(!userLoggedIn) return "User not logged in!";
        ArrayList<File> queue = queue(printer);
        String s = "";
        for (int i=0;i<queue.size();i++){
            s += queue.get(i).getJobNumber() + "   " + queue.get(i).getFileName() + "\n";
        }
        return s;
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
}
