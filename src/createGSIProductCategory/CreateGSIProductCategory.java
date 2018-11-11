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
public class CreateGSIProductCategory {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String catFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/category.csv";
        Map<String, String> gsiCatList = readAndParseCategory(catFileName);

        String catOutFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/category.xml";
        writeOutCategoryInserts(gsiCatList, catOutFileName);
    }

    private static Map<String, String> readAndParseCategory(String catFileName) throws FileNotFoundException {
        Scanner scanner = null;
        Map<String, String> coLinks = new HashMap<>();

        scanner = new Scanner(new FileInputStream(catFileName));

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

            coLinks.put(t[0].trim(), t[1].replace("&", "&amp;").trim());
        }

        scanner.close();

        return coLinks;
    }

    private static void writeOutCategoryInserts(Map<String, String> gsiCatList, String catOutFileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(catOutFileName));

        int lineC = 1;
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\r\n");
        writer.write("<entity-engine-xml>" + "\r\n");

        for (Map.Entry<String, String> entry : gsiCatList.entrySet()) {
            String catId = entry.getKey();
            String catName = entry.getValue();
            
            writer.write("<ProductCategory productCategoryId=\"" + catId);
            writer.write("\" productCategoryTypeId=\"INTERNAL_CATEGORY\" categoryName=\"" + catName + "\"");
            writer.write(" description=\"" + catName + "\"/>" + "\r\n");
        }
        writer.write("</entity-engine-xml>" + "\r\n");
        writer.close();
    }

}
