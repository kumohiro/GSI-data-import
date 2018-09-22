/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package createGSIEmployees;

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
public class createGSIEmployees {
    private static int contactMechId = 21033;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String existingCustFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/existingPARTY_ID-after01312018.csv";
        List<String> existingCustomers = readInExisting(existingCustFileName);

        String fileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/employees.csv";
        List<LamsEmployee> allEmps = readAndParse(fileName, existingCustomers);

        String outFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/employees-08292018.xml";
        createImportXML(allEmps, outFileName);
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

    private static List<LamsEmployee> readAndParse(String fileName, List<String> existingCustomers) throws FileNotFoundException {
        Scanner scanner = null;
        ArrayList<LamsEmployee> coLinks = new ArrayList();

        scanner = new Scanner(new FileInputStream(fileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            //System.out.println(index + ": " + aLine);
            if (index == 1) {
                continue;
            }
            LamsEmployee cLink = parseLAMSEmployeesCSV(aLine);

            if (cLink != null) {
                if (!existingCustomers.contains(cLink.getEmployeeID())) {
                    //System.out.println (cLink.getEmployeeID());
                    coLinks.add(cLink);
                }

            }
        }

        scanner.close();

        return coLinks;
    }

    private static LamsEmployee parseLAMSEmployeesCSV(String aLine) {
//0             1               2               3       7       8        9      10      11              12              13      16      21      23      
//EmployeeID	LastName	FirstName	Title	Address	City	State	ZipCode	HomePhone	Extension	Photo	Active	cell    Email
        String delims = "[,]";
        String[] tokens = aLine.split(delims, -1);

        LamsEmployee lemp = new LamsEmployee();

        lemp.setEmployeeID("EMP-" + tokens[0].trim());

        lemp.setFirstName(tokens[2].trim());
        lemp.setLastName(tokens[1].trim());

        lemp.setTitle(tokens[3].trim());

        boolean addUnknown = false;
        if (tokens[7].trim().equalsIgnoreCase("unknown")) {
            addUnknown = true;
        }
        lemp.setAddress(getAddress(tokens[7].trim(), addUnknown));
        lemp.setCity(getCity(tokens[8].trim(), addUnknown));
        lemp.setState(getState(tokens[9].trim(), addUnknown));
        lemp.setZipCode(getZip(tokens[10].trim(), addUnknown));

        lemp.setActive(tokens[16].trim());
        lemp.setCellPhoneNo(tokens[21].trim());
        lemp.setEmail(tokens[23].trim());

        return lemp;
    }

    private static String getAddress(String trim, boolean addUnknown) {
        String rtnAddress = "7112 Alondra Blvd.";
        if (addUnknown) {
            return rtnAddress;
        } else {
            return trim;
        }
    }

    private static String getCity(String trim, boolean addUnknown) {
        String rtnAddress = "Paramount";
        if (addUnknown) {
            return rtnAddress;
        } else {
            return trim;
        }
    }

    private static String getState(String trim, boolean addUnknown) {
        String rtnAddress = "CA";
        if (addUnknown) {
            return rtnAddress;
        } else {
            return trim;
        }
    }

    private static String getZip(String trim, boolean addUnknown) {
        String rtnAddress = "90723";
        if (addUnknown) {
            return rtnAddress;
        } else {
            return trim;
        }
    }

    private static void createImportXML(List<LamsEmployee> allEmps, String outFileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));
        //int contactMechId = 21033;

        int lineC = 1;
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\r\n");
        writer.write("<entity-engine-xml>" + "\r\n");

        for (LamsEmployee lc : allEmps) {
            //System.out.println(lineC++ + " : customer - " + lc.getCustomer());
            writePartyNPerson(lc, writer);

            writePostalContact(lc, writer, contactMechId);
            contactMechId = contactMechId + 10;

            if (lc.getCellPhoneNo() != null && !lc.getCellPhoneNo().isEmpty()) {
                writePhoneContact(lc, writer, contactMechId);
                contactMechId = contactMechId + 10;
            }

            if (lc.getEmail() != null && !lc.getEmail().isEmpty()) {
                writeEmailContact(lc, writer, contactMechId);
                contactMechId = contactMechId + 10;
            }

            writer.write("\r\n");
        }

        StringBuilder seqVItem = new StringBuilder();
        seqVItem.append("<SequenceValueItem seqName=\"ContactMech\" seqId=\"").append(Integer.toString(contactMechId + 10)).append("\"/>");
        writer.write(seqVItem.toString());
        writer.write("\r\n");

        writer.write("</entity-engine-xml>" + "\r\n");
        writer.close();
    }

    private static void writePartyNPerson(LamsEmployee lc, BufferedWriter writer) throws IOException {
        System.out.println("Processing... " + lc.getEmployeeID());

        StringBuilder party = new StringBuilder("<Party partyId=\"");
        party.append(lc.getEmployeeID())
                .append("\" partyTypeId=\"PERSON\" statusId=\"")
                .append("PARTY_ENABLED\" createdDate=\"\" createdByUserLogin=\"admin\"/>");
        writer.write(party.toString());
        writer.write("\r\n");

        StringBuilder person = new StringBuilder("<Person partyId=\"");
        person.append(lc.getEmployeeID())
                .append("\" firstName=\"").append(lc.getFirstName())
                .append("\" lastName=\"").append(lc.getLastName())
                .append("\"/>");
        writer.write(person.toString());
        writer.write("\r\n");

        String partyRole = "<PartyRole partyId=\""
                + lc.getEmployeeID()
                + "\" roleTypeId=\"EMPLOYEE\"/>";
        writer.write(partyRole);
        writer.write("\r\n");

        String productStoreRole = "<ProductStoreRole partyId=\""
                + lc.getEmployeeID()
                + "\" roleTypeId=\"EMPLOYEE\" productStoreId=\"10000\" fromDate=\"2018-04-28 15:40:17.0\" />";

        writer.write(productStoreRole);
        writer.write("\r\n");

        String loginName = getUserLogin(lc);

        StringBuilder userLogin = new StringBuilder("<UserLogin userLoginId=\"");
        userLogin.append(loginName).append("\" currentPassword=\"{SHA}5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8\" enabled=\"");
        if (lc.getActive().equals("0")) {
            userLogin.append("N")
                    .append("\" hasLoggedOut=\"Y\" requirePasswordChange=\"Y\" disabledBy=\"admin\" partyId=\"");
        } else {
            userLogin.append("Y")
                    .append("\" hasLoggedOut=\"Y\" requirePasswordChange=\"Y\" partyId=\"");
        }
        userLogin.append(lc.getEmployeeID()).append("\"/>");
        writer.write(userLogin.toString());
        writer.write("\r\n");

        if (lc.getTitle().toLowerCase().startsWith("ware")
                || lc.getTitle().toLowerCase().startsWith("sale")) {
            StringBuilder userLoginSecurityGroup = new StringBuilder("<UserLoginSecurityGroup userLoginId=\"");
            userLoginSecurityGroup.append(loginName).append("\" groupId=\"");
            if (lc.getTitle().toLowerCase().startsWith("ware")) {
                userLoginSecurityGroup.append("WAREHOUSE PERSON");
            } else if (lc.getTitle().toLowerCase().startsWith("sale")) {
                userLoginSecurityGroup.append("SALES PERSON");
            }
            userLoginSecurityGroup.append("\" fromDate=\"2018-04-28 15:40:17.0\" />");

            writer.write(userLoginSecurityGroup.toString());
            writer.write("\r\n");
        }

    }

    private static String getUserLogin(LamsEmployee lc) {
        return (lc.getFirstName().toLowerCase() + lc.getLastName().toLowerCase().substring(0, 1)).replace(" ", "").replace(".", "");
    }

    private static void writePostalContact(LamsEmployee lc, BufferedWriter writer, int contactMechId) throws IOException {
        StringBuilder contactMech = new StringBuilder();
        contactMech.append("<ContactMech contactMechId=\"").append(Integer.toString(contactMechId));
        contactMech.append("\" contactMechTypeId=\"POSTAL_ADDRESS\"/>");
        writer.write(contactMech.toString());
        writer.write("\r\n");

        StringBuilder postalAdd = new StringBuilder();
        postalAdd.append("<PostalAddress contactMechId=\"").append(Integer.toString(contactMechId)).append("\" ");
        postalAdd.append("toName=\"").append(lc.getFirstName()).append(" ").append(lc.getLastName()).append("\" ");
        postalAdd.append("address1=\"").append(lc.getAddress()).append("\" ");
        postalAdd.append("city=\"").append(lc.getCity()).append("\" ");
        postalAdd.append("postalCode=\"").append(lc.getZipCode()).append("\" ");
        postalAdd.append("countryGeoId=\"USA\" ");
        postalAdd.append("stateProvinceGeoId=\"").append(lc.getState()).append("\"/>");
        writer.write(postalAdd.toString());
        writer.write("\r\n");

        StringBuilder partyContactMech = new StringBuilder();
        partyContactMech.append("<PartyContactMech partyId=\"").append(lc.getEmployeeID()).append("\" ");
        partyContactMech.append("contactMechId=\"").append(Integer.toString(contactMechId)).append("\" ");
        partyContactMech.append("roleTypeId=\"EMPLOYEE\" fromDate=\"2018-04-28 15:40:17.0\"/>");
        writer.write(partyContactMech.toString());
        writer.write("\r\n");

        StringBuilder partyContactMechPurpose = new StringBuilder();
        partyContactMechPurpose.append("<PartyContactMechPurpose partyId=\"").append(lc.getEmployeeID()).append("\" ");
        partyContactMechPurpose.append("contactMechId=\"").append(Integer.toString(contactMechId)).append("\" ");
        partyContactMechPurpose.append("contactMechPurposeTypeId=\"GENERAL_LOCATION\" fromDate=\"2018-04-28 15:40:17.0\"/>");
        writer.write(partyContactMechPurpose.toString());
        writer.write("\r\n");

        partyContactMechPurpose = new StringBuilder();
        partyContactMechPurpose.append("<PartyContactMechPurpose partyId=\"").append(lc.getEmployeeID()).append("\" ");
        partyContactMechPurpose.append("contactMechId=\"").append(Integer.toString(contactMechId)).append("\" ");
        partyContactMechPurpose.append("contactMechPurposeTypeId=\"SHIPPING_LOCATION\" fromDate=\"2018-04-28 15:40:17.0\"/>");
        writer.write(partyContactMechPurpose.toString());
        writer.write("\r\n");
    }

    private static void writePhoneContact(LamsEmployee lc, BufferedWriter writer, int contactMechId) throws IOException {
        StringBuilder contactMech = new StringBuilder();
        contactMech.append("<ContactMech contactMechId=\"").append(Integer.toString(contactMechId));
        contactMech.append("\" contactMechTypeId=\"TELECOM_NUMBER\"/>");
        writer.write(contactMech.toString());
        writer.write("\r\n");

        StringBuilder telNumber = new StringBuilder();
        telNumber.append("<TelecomNumber contactMechId=\"").append(Integer.toString(contactMechId)).append("\" ");
        telNumber.append("countryCode=\"1\" ");
        telNumber.append("areaCode=\"").append(getAreaCode(lc.getCellPhoneNo())).append("\" ");
        telNumber.append("contactNumber=\"").append(getContactNumber(lc.getCellPhoneNo())).append("\" />");
        writer.write(telNumber.toString());
        writer.write("\r\n");

        StringBuilder partyContactMech = new StringBuilder();
        partyContactMech.append("<PartyContactMech partyId=\"").append(lc.getEmployeeID()).append("\" ");
        partyContactMech.append("contactMechId=\"").append(Integer.toString(contactMechId)).append("\" ");
        partyContactMech.append("roleTypeId=\"EMPLOYEE\" fromDate=\"2018-04-28 15:40:17.0\"/>");
        writer.write(partyContactMech.toString());
        writer.write("\r\n");

        StringBuilder partyContactMechPurpose = new StringBuilder();
        partyContactMechPurpose.append("<PartyContactMechPurpose partyId=\"").append(lc.getEmployeeID()).append("\" ");
        partyContactMechPurpose.append("contactMechId=\"").append(Integer.toString(contactMechId)).append("\" ");
        partyContactMechPurpose.append("contactMechPurposeTypeId=\"PHONE_MOBILE\" fromDate=\"2018-04-28 15:40:17.0\"/>");
        writer.write(partyContactMechPurpose.toString());
        writer.write("\r\n");
    }

    private static void writeEmailContact(LamsEmployee lc, BufferedWriter writer, int contactMechId) throws IOException {
        StringBuilder contactMech = new StringBuilder();
        contactMech.append("<ContactMech contactMechId=\"").append(Integer.toString(contactMechId));
        contactMech.append("\" contactMechTypeId=\"EMAIL_ADDRESS\" ");
        contactMech.append("infoString=\"").append(lc.getEmail()).append("\"/>");
        writer.write(contactMech.toString());
        writer.write("\r\n");

        StringBuilder partyContactMech = new StringBuilder();
        partyContactMech.append("<PartyContactMech partyId=\"").append(lc.getEmployeeID()).append("\" ");
        partyContactMech.append("contactMechId=\"").append(Integer.toString(contactMechId)).append("\" ");
        partyContactMech.append("roleTypeId=\"EMPLOYEE\" fromDate=\"2018-04-28 15:40:17.0\"/>");
        writer.write(partyContactMech.toString());
        writer.write("\r\n");

        StringBuilder partyContactMechPurpose = new StringBuilder();
        partyContactMechPurpose.append("<PartyContactMechPurpose partyId=\"").append(lc.getEmployeeID()).append("\" ");
        partyContactMechPurpose.append("contactMechId=\"").append(Integer.toString(contactMechId)).append("\" ");
        partyContactMechPurpose.append("contactMechPurposeTypeId=\"PRIMARY_EMAIL\" fromDate=\"2018-04-28 15:40:17.0\"/>");
        writer.write(partyContactMechPurpose.toString());
        writer.write("\r\n");
    }

    private static String getAreaCode(String cellPhoneNo) {
        String delims = "[-]";
        String[] tokens = cellPhoneNo.split(delims, -1);
        return tokens[0].trim();
    }

    private static String getContactNumber(String cellPhoneNo) {
        String delims = "[-]";
        String[] tokens = cellPhoneNo.split(delims, -1);
        return tokens[1].trim() + "-" + tokens[2].trim();
    }

}
