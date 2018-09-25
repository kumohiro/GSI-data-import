/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package createGSIInventory;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author seanc
 */
public class CreateGSIInventory {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
        String inventoryFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/InventoryItem2.csv";
        List<GSIInventory> invList = readAndParseInventory(inventoryFileName);

        String prodIdInternalNameFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/prodId-InternalName.csv";
        Map<String, String> pIdName = readAndParsePIdInternalName(prodIdInternalNameFileName);

        String outFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/inventory.xml";
        writeOutInventoryXML(invList, pIdName, outFileName);
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

            if (t[1].toLowerCase().contains("afb silky ")) {
                //System.out.println(t[1].trim().replaceAll(" \"\"", "").replaceAll("\"", ""));
            }

            coLinks.put(t[0].trim(), t[1].trim().replaceAll(" \"\"", "").replaceAll("\"", ""));
        }

        scanner.close();

        return coLinks;
    }

    private static List<GSIInventory> readAndParseInventory(String inventoryFileName) throws FileNotFoundException {
        //empty	Name	Color	init	in	out	rtn	adj	stock	onOrder	total
        //0     1       2       3       4       5       6       7       8       9       10
        Scanner scanner = null;
        List<GSIInventory> coLinks = new ArrayList<>();

        scanner = new Scanner(new FileInputStream(inventoryFileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            //System.out.println(aLine);
            if (index == 1) {
                continue;
            }
            String delims = "[\t]";
            String[] t = aLine.split(delims, -1);

            if (!t[1].trim().isEmpty()) {
                GSIInventory gInv = new GSIInventory();
                gInv.setEmpty(t[0].trim());
                gInv.setName(t[1].trim().replaceAll("\"", ""));
                gInv.setColor(t[2].trim());
                gInv.setInit(t[3].trim());
                gInv.setIn(t[4].trim());
                gInv.setOut(t[5].trim());
                gInv.setRtn(t[6].trim());
                gInv.setAdj(t[7].trim());
                gInv.setStock(t[8].trim());
                gInv.setOnOrder(t[9].trim());
                gInv.setTotal(t[10].trim());

                coLinks.add(gInv);
            }
        }

        scanner.close();

        return coLinks;
    }

    private static void writeOutInventoryXML(List<GSIInventory> invList, Map<String, String> pIdName, String outFileName) throws IOException, Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));

        int lineC = 1;
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\r\n");
        writer.write("<entity-engine-xml>" + "\r\n");

        for (GSIInventory gInv : invList) {

            String pId = getKeyByValue(pIdName, gInv.getName() + " - " + gInv.getColor());
            if (pId == null || pId.isEmpty()) {
                //throw new Exception(gInv.getName() + " - " + gInv.getColor() + " : " + "product id not found");
                
                System.out.println("ERROR: " + gInv.getName() + " - " + gInv.getColor() + " : " + "product id not found");
            }
            //System.out.println(gInv.getName() + " - " + gInv.getColor() + " : " + pId);

        }

        writer.write("</entity-engine-xml>" + "\r\n");
        writer.close();
    }

    private static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

}
