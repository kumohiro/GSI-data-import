/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package createGSIAssociationProduct;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author seanc
 */
public class CreateGSIAssociationProduct {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String prodIdInternalNameFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/prodId-InternalName.csv";
        Map<String, String> pIdName = readAndParsePIdInternalName(prodIdInternalNameFileName);
        
        String associationOutFile = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/product-association.xml";
        writeOutXml(pIdName, associationOutFile);
    }
    
    private static Map<String, String> readAndParsePIdInternalName(String prodIdInternalNameFileName) throws FileNotFoundException {
        Scanner scanner = null;
        Map<String, String> coLinks = new HashMap<>();

        scanner = new Scanner(new FileInputStream(prodIdInternalNameFileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            //System.out.println(aLine);
            if (index == 1) {
                continue;
            }
            String delims = "[,]";
            String[] t = aLine.split(delims, -1);

            coLinks.put(t[0].trim(), t[1].trim());
        }

        scanner.close();

        return coLinks;
    }

    private static void writeOutXml(Map<String, String> pIdName, String associationOutFile) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(associationOutFile));

        int lineC = 1;
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\r\n");
        writer.write("<entity-engine-xml>" + "\r\n");

        for (String prodId : pIdName.keySet()) {
            
        }
        
        writer.write("</entity-engine-xml>" + "\r\n");
        writer.close();
    }

}
