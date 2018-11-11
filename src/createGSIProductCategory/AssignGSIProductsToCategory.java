/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package createGSIProductCategory;

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
public class AssignGSIProductsToCategory {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // ProductID	ProductName	ProdCateID
        String inFile = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/product-categoryId.csv";
        Map<String,String> pCategoryMap = readAndParseProductAndCategory(inFile);

        String outFile = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/product-categoryId.xml";
        writeOutProdAndCategory(pCategoryMap, outFile);
    }

    private static Map<String, String> readAndParseProductAndCategory(String inFile) throws FileNotFoundException {
        Scanner scanner = null;
        Map<String, String> coLinks = new HashMap<>();

        scanner = new Scanner(new FileInputStream(inFile));

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

    private static void writeOutProdAndCategory(Map<String, String> pCategoryMap, String outFile) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

        int lineC = 1;
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\r\n");
        writer.write("<entity-engine-xml>" + "\r\n");
        
        /*
    <ProductCategoryMember productCategoryId="5" productId="10" fromDate="2010-01-01 00:00:00.0"/>

    */

        for (Map.Entry<String, String> entry : pCategoryMap.entrySet()) {
            String prodId = entry.getKey();
            String catId = entry.getValue();
            
            writer.write("<ProductCategoryMember productCategoryId=\"" + catId);
            writer.write("\" productId=\"" + prodId + "\"");
            writer.write(" fromDate=\"2010-01-01 00:00:00.0\"/>" + "\r\n");
        }
        writer.write("</entity-engine-xml>" + "\r\n");
        writer.close();
    }
    
}
