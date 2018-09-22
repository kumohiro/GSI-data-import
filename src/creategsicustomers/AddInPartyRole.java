/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package creategsicustomers;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author seanc
 */
public class AddInPartyRole {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, Exception {
    String fileName = "/home/seanc/Desktop/data/import/CustomersNotImported.csv";
        List<LamsCustomer> allCustomers = readAndParse(fileName);

        String outFileName = "/home/seanc/Desktop/data/import/CustomersNotImported-Roles.xml";
        createImportXML(allCustomers, outFileName);
    }

    private static List<LamsCustomer> readAndParse(String fileName) throws FileNotFoundException {
        Scanner scanner = null;
        ArrayList<LamsCustomer> coLinks = new ArrayList();

        scanner = new Scanner(new FileInputStream(fileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            //System.out.println(aLine);
            if (index == 1) {
                continue;
            }
            LamsCustomer cLink = parseLAMSCustomersCSV(aLine);

            if (cLink != null) {
                coLinks.add(cLink);
            }
        }

        scanner.close();

        return coLinks;
    }

    private static LamsCustomer parseLAMSCustomersCSV(String aLine) {
        //Account No,Customer,Last Ship,Address,City,Region,Zip,Phone,Fax,Contact,Sales Rep
        //0          1        2         3       4    5      6   7     8   9       10
        String delims = "[,]";
        String[] tokens = aLine.split(delims, -1);
        LamsCustomer customer = new LamsCustomer();

        customer.setAccountNo(tokens[0].trim());
        

        return customer;
    }

    private static void createImportXML(List<LamsCustomer> allCustomers, String outFileName) throws IOException, Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));
        int contactMechId = 10020;

        int lineC = 1;
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\r\n");
        writer.write("<entity-engine-xml>" + "\r\n");
        
        for (LamsCustomer lc : allCustomers) {
            //System.out.println(lineC++ + " : customer - " + lc.getCustomer());

            String partyId = getPartyId(lc);

            writePartyRoles(partyId, lc, writer);
        }
        writer.write("</entity-engine-xml>" + "\r\n");
        writer.close();
    }

    private static String getPartyId(LamsCustomer lc) throws Exception {
        return lc.getAccountNo();
    }

    private static void writePartyRoles(String partyId, LamsCustomer lc, BufferedWriter writer) throws IOException {
        StringBuilder pRole = new StringBuilder();
        pRole.append("<PartyRole partyId=\"").append(partyId).append("\" roleTypeId=\"BILL_TO_CUSTOMER\"/>");
        writer.write(pRole.toString());
        writer.write("\r\n");

        pRole = new StringBuilder();
        pRole.append("<PartyRole partyId=\"").append(partyId).append("\" roleTypeId=\"CUSTOMER\"/>");
        writer.write(pRole.toString());
        writer.write("\r\n");

        pRole = new StringBuilder();
        pRole.append("<PartyRole partyId=\"").append(partyId).append("\" roleTypeId=\"END_USER_CUSTOMER\"/>");
        writer.write(pRole.toString());
        writer.write("\r\n");

        pRole = new StringBuilder();
        pRole.append("<PartyRole partyId=\"").append(partyId).append("\" roleTypeId=\"PLACING_CUSTOMER\"/>");
        writer.write(pRole.toString());
        writer.write("\r\n");

        pRole = new StringBuilder();
        pRole.append("<PartyRole partyId=\"").append(partyId).append("\" roleTypeId=\"SHIP_TO_CUSTOMER\"/>");
        writer.write(pRole.toString());
        writer.write("\r\n");

        pRole = new StringBuilder();
        pRole.append("<PartyRole partyId=\"").append(partyId).append("\" roleTypeId=\"_NA_\"/>");
        writer.write(pRole.toString());
        writer.write("\r\n");
    }
}
