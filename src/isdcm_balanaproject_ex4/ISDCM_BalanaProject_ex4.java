/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package isdcm_balanaproject_ex4;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.balana.Balana;
import org.wso2.balana.ConfigurationStore;
import org.wso2.balana.PDP;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;


/**
 *
 * @author Pau
 */
public class ISDCM_BalanaProject_ex4 {

    private static Balana balana;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        if (args.length != 4) printUsageAndExit();
        
        String configLocation = args[0];
        String policyLocation = args[1];
        String requestsLocation = args[2];
        String responseLocation = args[3];
        
        PDP pdp = null;
        
        try
        {
            pdp = getConfiguredPDP(configLocation, policyLocation);
        }
        catch (Exception ex) {
            System.out.println("Could not load xacml configuration and policies. " + ex.getMessage());
            System.exit(2);
        }
        
        try 
        {            
            List<String> requestFiles = getRequestFiles(requestsLocation);
            
            for (String requestFile : requestFiles)
            {
                try 
                {
                    System.out.println("---------------------------------------------");
                    System.out.println("Processing... " + requestFile);
                    String requestContent = readRequest(requestFile);
                    String response = pdp.evaluate(requestContent);
                    System.out.println("---------------------------------------------");
                    System.out.println("RESPONSE of " + requestFile);
                    System.out.println("---------------------------------------------");
                    System.out.println(response);
                    saveResponseFile(requestFile, responseLocation, response);
                }
                catch (Exception ex)
                {
                    System.out.println("Could not execute the request: " + requestFile + ". " + ex.getMessage());
                }
            }
        }
        catch (Exception ex)
        {
            System.out.println("Could not load the requests files. " + ex.getMessage());
            System.exit(2);
        }
        System.exit(0);
    }
    
    private static String readRequest(String path) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }
    
    private static PDP getConfiguredPDP(String configurationFileLocation, String policyDirectoryLocation){
        System.setProperty(ConfigurationStore.PDP_CONFIG_PROPERTY, configurationFileLocation); 
        System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY, policyDirectoryLocation);      
        Balana balana = Balana.getInstance();
        return new PDP(balana.getPdpConfig());
    }
    
    private static List<String> getRequestFiles(String requestsDirectoryLocation)
    {
        List<String> filesPaths = new ArrayList<String>();       
        File[] files = new File(requestsDirectoryLocation).listFiles();
        for (File file : files) {
            if (file.isFile()) {
                filesPaths.add(file.getAbsolutePath());
            }
        }       
        return filesPaths;
    }
    
    private static void saveResponseFile(String inputFile, String outputPath, String content)
    {
        try {
            File file = new File(inputFile);
            String outputFilePath = outputPath + "\\" + file.getName().replace(".xml", "_responseOutput.xml");
            Files.write( Paths.get(outputFilePath), content.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException ex) {
            Logger.getLogger(ISDCM_BalanaProject_ex4.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void printUsageAndExit()
    {
        System.out.println("USAGE: ISDCM - XACML application using Balana");
        System.out.println("---------------------------------------------");
        System.out.println("Three arguments are expected:");
        System.out.println("\tFirst argument: Configuration file path");
        System.out.println("\tSecond argument: Policy directory path");
        System.out.println("\tThird argument: Requests directory path");
        System.out.println("\tFourth argument: Response directory path");
        System.out.println("---------------------------------------------");
        System.exit(1);
    }
}
