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
public class CreateGSICustomers {
    private static int contactMechId = 20881;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
        
        String existingCustFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/existingcustomer-after01312018.csv";
        List<String> existingCustomers = readInExisting(existingCustFileName);
        
        String fileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/customer-full-08292018.csv";
        List<LamsCustomer> allCustomers = readAndParse(fileName, existingCustomers);


        String outFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/GSICustomersImport-08292018.xml";
        createImportXML(allCustomers, outFileName);
    }

    private static List<LamsCustomer> readAndParse(String fileName, List<String> existingCustomers) throws FileNotFoundException {
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
            LamsCustomer cLink = parseLAMSCustomersCSV(aLine, existingCustomers);

            if (cLink != null) {
                coLinks.add(cLink);
            }
        }

        scanner.close();

        return coLinks;
    }

    private static List<String> readInExisting(String existingCustFileName) throws FileNotFoundException {
        Scanner scanner = null;
        ArrayList<String> coLinks = new ArrayList();

        scanner = new Scanner(new FileInputStream(existingCustFileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            //System.out.println(aLine);
            if (index == 1) {
                continue;
            }

            coLinks.add(aLine.trim());
        }

        scanner.close();

        return coLinks;
    }

    private static LamsCustomer parseLAMSCustomersCSV(String aLine, List<String> existingCustomers) {
        //Account No,Customer,Last Ship,Address,City,Region,Zip,Phone,Fax,Contact,Sales Rep
        //0          1        2         3       4    5      6   7     8   9       10
        String delims = "[\t]";
        String[] tokens = aLine.split(delims, -1);
        LamsCustomer customer = new LamsCustomer();

        customer.setAccountNo(tokens[0].trim());
        
        if (existingCustomers.contains(tokens[0].trim())){
            return null;
        }
        
        
        customer.setCustomer(tokens[1].trim());
        customer.setLastShip(tokens[2].trim());
        customer.setAddress(tokens[3].trim());
        customer.setCity(tokens[4].trim());
        customer.setRegion(tokens[5].trim());
        customer.setZip(tokens[6].trim());
        customer.setPhone(tokens[7].trim().replace("(", "").replace(")", "").replace("-", ""));
        customer.setFax(tokens[8].trim().replace("(", "").replace(")", "").replace("-", ""));
        customer.setContact(tokens[9].trim());
        customer.setSalesRep(tokens[10].trim());

        if (!USConstants.listOfUSStatesCode.contains(customer.getRegion())) {
            customer = null;
            System.out.println(aLine);
        }

        return customer;
    }

    private static void createImportXML(List<LamsCustomer> allCustomers, String outFileName) throws IOException, Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));

        int lineC = 1;
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\r\n");
        writer.write("<entity-engine-xml>" + "\r\n");

        for (LamsCustomer lc : allCustomers) {
            //System.out.println(lineC++ + " : customer - " + lc.getCustomer());

            String partyId = getPartyId(lc);

            writePartynPartyGroup(partyId, lc, writer);

            contactMechId = writePostalAddress(partyId, lc, writer, contactMechId);

            contactMechId = writeContactPhone(partyId, lc, writer, contactMechId);

            contactMechId = writeContactFax(partyId, lc, writer, contactMechId);

            writePartyRoles(partyId, lc, writer);
        }
        
        StringBuilder seqVItem = new StringBuilder();
        seqVItem.append("<SequenceValueItem seqName=\"ContactMech\" seqId=\"").append(Integer.toString(contactMechId + 10)).append("\"/>");
        writer.write(seqVItem.toString());
        writer.write("\r\n");
        
        writer.write("</entity-engine-xml>" + "\r\n");
        writer.close();
    }

    private static String getPartyId(LamsCustomer lc) throws Exception {
        return lc.getAccountNo();
    }

    private static String getGroupName(LamsCustomer lc) throws Exception {
        if (lc.getCustomer() == null || lc.getCustomer().isEmpty()) {
            throw new Exception();
        }
        return lc.getCustomer().replace("'", "").replace("&", "&amp;").replace("\"", "").trim();
    }

    private static void writePartynPartyGroup(String partyId, LamsCustomer lc, BufferedWriter writer) throws IOException, Exception {
        StringBuilder party = new StringBuilder("<Party partyId=\"");
        party.append(partyId).append("\" partyTypeId=\"PARTY_GROUP\" preferredCurrencyUomId=\"USD\" statusId=\"PARTY_ENABLED\" createdDate=\"\" createdByUserLogin=\"admin\"/>");
        writer.write(party.toString());
        writer.write("\r\n");

        StringBuilder partyGroup = new StringBuilder("<PartyGroup partyId=\"");
        partyGroup.append(partyId).append("\" groupName=\"").append(getGroupName(lc)).append("\" groupNameLocal=\"").append(partyId).append("\"/>");
        writer.write(partyGroup.toString());
        writer.write("\r\n");
    }

    private static int writePostalAddress(String partyId, LamsCustomer lc, BufferedWriter writer, int contactMechId) throws Exception {
        if (lc.getAddress().isEmpty()) {
            throw new Exception();
        }
        contactMechId++;

        StringBuilder cMech = new StringBuilder("<ContactMech contactMechId=\"");
        cMech.append(contactMechId).append("\" contactMechTypeId=\"POSTAL_ADDRESS\"/>");

        StringBuilder pcMech = new StringBuilder("<PartyContactMech partyId=\"").append(partyId).append("\" contactMechId=\"");
        pcMech.append(contactMechId).append("\" fromDate=\"2017-10-15 00:00:00\" />");

        StringBuilder pcMechPurpose = new StringBuilder("<PartyContactMechPurpose partyId=\"").append(partyId);
        pcMechPurpose.append("\" contactMechId=\"").append(contactMechId);
        pcMechPurpose.append("\" contactMechPurposeTypeId=\"SHIPPING_LOCATION\" fromDate=\"2017-09-26 20:57:55.0\" />");

        StringBuilder pAddress = new StringBuilder();
        pAddress.append("<PostalAddress contactMechId=\"").append(contactMechId);
        pAddress.append("\" toName=\"").append(partyId).append(" Ship To");
        pAddress.append("\" attnName=\"").append(lc.getContact().replace("'", "").replace("&", "&amp;").trim());
        pAddress.append("\" address1=\"").append(lc.getAddress().replace("\"", "").replace("&", "&amp;"));
        pAddress.append("\" city=\"").append(lc.getCity());
        pAddress.append("\" postalCode=\"").append(lc.getZip());
        pAddress.append("\" countryGeoId=\"USA\" ");
        pAddress.append("stateProvinceGeoId=\"").append(lc.getRegion()).append("\"/>");

        writer.write(cMech.toString());
        writer.write("\r\n");
        writer.write(pcMech.toString());
        writer.write("\r\n");
        writer.write(pcMechPurpose.toString());
        writer.write("\r\n");
        writer.write(pAddress.toString());
        writer.write("\r\n");

        return contactMechId;
    }

    private static int writeContactPhone(String partyId, LamsCustomer lc, BufferedWriter writer, int contactMechId) throws IOException {
        if (lc.getPhone() != null
                && !lc.getPhone().isEmpty()
                && lc.getPhone().length() >= 9) {
            contactMechId++;

            StringBuilder cMech = new StringBuilder("<ContactMech contactMechId=\"");
            cMech.append(contactMechId).append("\" contactMechTypeId=\"TELECOM_NUMBER\"/>");

            StringBuilder pcMech = new StringBuilder("<PartyContactMech partyId=\"").append(partyId).append("\" contactMechId=\"");
            pcMech.append(contactMechId).append("\" fromDate=\"2017-10-15 00:00:00\" />");

            StringBuilder pcMechPurpose = new StringBuilder("<PartyContactMechPurpose partyId=\"").append(partyId).append("\" contactMechId=\"");
            pcMechPurpose.append(contactMechId).append("\" contactMechPurposeTypeId=\"PHONE_WORK\" fromDate=\"2017-10-15 00:00:00\"/>");

            StringBuilder pTel = new StringBuilder("<TelecomNumber contactMechId=\"").append(contactMechId);
            pTel.append("\" countryCode=\"1\" ");
            pTel.append("areaCode=\"").append(getAreaCode(lc.getPhone()));
            pTel.append("\" contactNumber=\"").append(getPhoneNumber(lc.getPhone())).append("\"/>");

            writer.write(cMech.toString());
            writer.write("\r\n");
            writer.write(pcMech.toString());
            writer.write("\r\n");
            writer.write(pcMechPurpose.toString());
            writer.write("\r\n");
            writer.write(pTel.toString());
            writer.write("\r\n");
        }
        return contactMechId;
    }

    private static int writeContactFax(String partyId, LamsCustomer lc, BufferedWriter writer, int contactMechId) throws IOException {
        if (lc.getFax() != null
                && !lc.getFax().isEmpty()
                && lc.getFax().length() >= 9) {
            contactMechId++;

            StringBuilder cMech = new StringBuilder("<ContactMech contactMechId=\"");
            cMech.append(contactMechId).append("\" contactMechTypeId=\"TELECOM_NUMBER\"/>");

            StringBuilder pcMech = new StringBuilder("<PartyContactMech partyId=\"").append(partyId).append("\" contactMechId=\"");
            pcMech.append(contactMechId).append("\" fromDate=\"2017-10-15 00:00:00\" />");

            StringBuilder pcMechPurpose = new StringBuilder("<PartyContactMechPurpose partyId=\"").append(partyId).append("\" contactMechId=\"");
            pcMechPurpose.append(contactMechId).append("\" contactMechPurposeTypeId=\"FAX_NUMBER\" fromDate=\"2017-10-15 00:00:00\"/>");

            StringBuilder pTel = new StringBuilder("<TelecomNumber contactMechId=\"").append(contactMechId);
            pTel.append("\" countryCode=\"1\" ");
            pTel.append("areaCode=\"").append(getAreaCode(lc.getFax()));
            pTel.append("\" contactNumber=\"").append(getPhoneNumber(lc.getFax())).append("\"/>");

            writer.write(cMech.toString());
            writer.write("\r\n");
            writer.write(pcMech.toString());
            writer.write("\r\n");
            writer.write(pcMechPurpose.toString());
            writer.write("\r\n");
            writer.write(pTel.toString());
            writer.write("\r\n");
        }
        return contactMechId;
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

    private static String getAreaCode(String phone) {
        String rtnStr = "";
        rtnStr = phone.substring(0, 3);
        return rtnStr;
    }

    private static String getPhoneNumber(String phone) {
        String rtnStr = "";
        rtnStr = phone.substring(3);
        return rtnStr;
    }

}
