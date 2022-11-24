import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class PolicyReader {

    public static HashMap<String, String[]> createACL_Policy() throws Exception{
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        String ACL_FileName = " ";
        System.out.println("--------------------------------------------------------------------------------------------------");
        System.out.println("Do you want to test the ACL policy before of after the changes in the company?");
        System.out.println("Press [C] for the ACL policy after the changes, otherwise the default implementation will be used.");
        String choice = consoleReader.readLine();
        if (choice.equals("C")){
            ACL_FileName = "AccessControlListChanges.txt";
        }else{
            ACL_FileName = "AccessControlList.txt";
        }
        return readACL_List(ACL_FileName);
    }

    private static HashMap<String, String[]> readACL_List(String FileName) throws FileNotFoundException {

        HashMap<String, String[]> policyMap = new HashMap<String, String[]>();

        //reading the Access Control List txt file
        File AccessControlFile = new File(FileName);

        Scanner AccessControlReader = new Scanner(AccessControlFile);

        while(AccessControlReader.hasNextLine()){
        //    AccessControlReader.nextLine();
            String[] line = AccessControlReader.nextLine().trim().split(":");
            String operation = line[0];
        //    System.out.println(operation + ": ");
            String[] ACL_names = new String[8];
            for (int i = 1; i < line.length; i++){
                ACL_names[i-1] = line[i];
         //       System.out.println(ACL_names[i-1]);
            }
            policyMap.put(operation, ACL_names);
        }
        AccessControlReader.close();
        return policyMap;
    }

}
