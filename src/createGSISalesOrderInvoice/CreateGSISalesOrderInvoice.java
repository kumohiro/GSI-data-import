/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package createGSISalesOrderInvoice;

import createGSIEmployees.LamsEmployee;
import createGSIVarientProducts.LamsProductColorVarient;
import createGSIVirtualProducts.LamsProduct;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author seanc
 */
public class CreateGSISalesOrderInvoice {

    private static int inventorySeq = 10000;
    private static int paymentPrefSeq = 10000;
    private static int shipmentSeq = 10000;
    private static int itemIssuenceSeq = 10000;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
        String orderFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/products/orders-from2011-noInv0-trimed-tab.txt";
        List<LambsOrder> allOrders = readAndParseOrder(orderFileName);

        String virtualProdFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/products/products-simple.csv";
        List<LamsProduct> allVirtualProds = readAndParseProduct(virtualProdFileName);

        String prodColorFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/products/productscolor.csv";
        List<LamsProductColorVarient> allVarientProds = readAndParseProductColorVarient(prodColorFileName);

        String customerFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/products/customer-id-account-db.csv";
        List<LamsCustomerIdAccount> allCustomers = readAndParseCustomer(customerFileName);

        String employeeFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/products/employees.csv";
        List<LamsEmployee> allEmps = readAndParseEmployee(employeeFileName);

        String orderDetailFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/products/orderDetail-from2011.csv";
        List<LambsOrderDetail> allOrderDetails = readAndParseOrderDetails(orderDetailFileName);

        String partyContactMechFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/products/partyId_contactMechId.csv";
        Map<String, String> pContactMech = readAndParsePartyContactMech(partyContactMechFileName);

        String prodIdInternalNameFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/products/prodId-InternalName.csv";
        Map<String, String> pIdName = readAndParsePIdInternalName(prodIdInternalNameFileName);

        createImportXML(allEmps, allVirtualProds, allVarientProds, allCustomers, allOrders, allOrderDetails, pContactMech, pIdName);
    }

    private static void createImportXML(List<LamsEmployee> allEmps,
            List<LamsProduct> allVirtualProds,
            List<LamsProductColorVarient> allVarientProds,
            List<LamsCustomerIdAccount> allCustomers,
            List<LambsOrder> allOrders,
            List<LambsOrderDetail> allOrderDetails,
            Map<String, String> partyContactMech,
            Map<String, String> pIdName) throws IOException, Exception {

        String outFileNameBase = "/home/seanc/Desktop/GSI/gsi_production/oCreate/orderInput";
        String outFileName = "";
        int orderCount = 0;
        BufferedWriter writer = null;
        int fileSequence = 0;
        int maxNumOrdersPerFile = 10000;

        for (LambsOrder od : allOrders) {

            if (orderCount % maxNumOrdersPerFile == 0) {
                //create new writer with new file name
                outFileName = outFileNameBase + fileSequence++ + ".xml";
                writer = new BufferedWriter(new FileWriter(outFileName));

                int lineC = 1;
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\r\n");
                writer.write("<entity-engine-xml>" + "\r\n");
            }

            String oCustomerId = getDTMCustomerId(od, allCustomers);
            if (oCustomerId == null || oCustomerId.isEmpty()) {
                throw new Exception("oCustomerId is null - " + oCustomerId);
            }

            String contactMechId = getContactMechId(oCustomerId, partyContactMech);
            if (contactMechId == null || contactMechId.isEmpty()) {
                throw new Exception("ContactMechId is null - " + oCustomerId);
            }
            List<LambsOrderDetail> oItems = getOrderItems(od.getOrderID(), allOrderDetails);
            String empId = getEmpLoginName(od, allEmps);

            //System.out.println("Processing... " + od.getOrderID());
            writeOutOrder(od, oCustomerId, contactMechId, oItems, empId, allVarientProds, writer, pIdName);

            writer.write("\r\n");
            orderCount++;

            if (orderCount % maxNumOrdersPerFile == 0 && orderCount != 0) {
                //close the current writer
                writer.write("</entity-engine-xml>" + "\r\n");
                writer.close();
            }
        }

        writer.write("</entity-engine-xml>" + "\r\n");
        writer.close();
    }

    private static void writeOutOrder(LambsOrder od,
            String oCustomerId,
            String contactMechId,
            List<LambsOrderDetail> oItems,
            String empId,
            List<LamsProductColorVarient> allVarientProds,
            BufferedWriter writer,
            Map<String, String> pIdName) throws IOException, Exception {

        writer.write("<OrderHeader orderId=\"" + od.getOrderID() + "\" orderTypeId=\"SALES_ORDER\" salesChannelEnumId=\"AFFIL_SALES_CHANNEL\" ");
        writer.write("orderDate=\"" + od.getOrderDate() + "\" priority=\"2\" entryDate=\"" + od.getOrderDate() + "\" statusId=\"ORDER_COMPLETED\" ");
        writer.write("createdBy=\"" + empId + "\" currencyUom=\"USD\" originFacilityId=\"10000\" productStoreId=\"10000\" ");
        writer.write("remainingSubTotal=\"" + od.getSubTotal() + "\" grandTotal=\"" + od.getSubTotal() + "\" invoicePerShipment=\"Y\" ");
        writer.write("lastUpdatedStamp=\"" + od.getOrderDate() + "\" lastUpdatedTxStamp=\"" + od.getOrderDate() + "\" createdStamp=\"" + od.getOrderDate() + "\" createdTxStamp=\"" + od.getOrderDate() + "\"/>");
        writer.write("\r\n");

        writer.write("<OrderContactMech orderId=\"" + od.getOrderID() + "\" contactMechPurposeTypeId=\"SHIPPING_LOCATION\" contactMechId=\"" + contactMechId + "\" />");
        writer.write("\r\n");

        Map<String, String> itemToSequenceId = new HashMap<>();

        int orderItemSeqId = 1;
        for (LambsOrderDetail odItem : oItems) {
            String productId = getProdId(odItem, allVarientProds);
            String qty = odItem.getOrderQty();
            String uPrice = odItem.getUnitPrice();
            String itemDescription = pIdName.get(productId);

            itemToSequenceId.put(productId, String.format("%05d", orderItemSeqId));

            //order item
            writer.write("<OrderItem orderId=\"" + od.getOrderID() + "\" orderItemSeqId=\"" + String.format("%05d", orderItemSeqId++) + "\" orderItemTypeId=\"PRODUCT_ORDER_ITEM\" ");
            writer.write("productId=\"" + productId + "\" prodCatalogId=\"WHOLESALE_CATALOG\" isPromo=\"N\" ");
            writer.write("quantity=\""
                    + qty
                    + "\" selectedAmount=\"0.000000\" unitPrice=\""
                    + uPrice
                    + "\" unitListPrice=\"0.000\" isModifiedPrice=\"N\" "
                    + "itemDescription=" + itemDescription.replace("&", "&amp;").replace("\"\"", " &quot;")
                    + " correspondingPoId=\"\" statusId=\"ITEM_COMPLETED\" />");
            writer.write("\r\n");
        }

        //order role
        writer.write("<OrderRole orderId=\"" + od.getOrderID() + "\" partyId=\"10000\" roleTypeId=\"BILL_FROM_VENDOR\" />" + "\r\n");
        writer.write("<OrderRole orderId=\"" + od.getOrderID() + "\" partyId=\"" + oCustomerId + "\" roleTypeId=\"BILL_TO_CUSTOMER\" />" + "\r\n");
        writer.write("<OrderRole orderId=\"" + od.getOrderID() + "\" partyId=\"" + oCustomerId + "\" roleTypeId=\"PLACING_CUSTOMER\" />" + "\r\n");
        writer.write("<OrderRole orderId=\"" + od.getOrderID() + "\" partyId=\"" + oCustomerId + "\" roleTypeId=\"SHIP_TO_CUSTOMER\" />" + "\r\n");

        //payment preference
        writer.write("<OrderPaymentPreference orderPaymentPreferenceId=\"" + Integer.toString(paymentPrefSeq++)
                + "\" orderId=\"" + od.getOrderID()
                + "\" paymentMethodTypeId=\"EXT_OFFLINE\" presentFlag=\"N\" swipedFlag=\"N\" overflowFlag=\"N\" maxAmount=\"" + od.getInvoiceSum()
                + "\" statusId=\"PAYMENT_NOT_RECEIVED\" createdDate=\"" + od.getOrderDate() + "\" createdByUserLogin=\"admin\" />");
        writer.write("\r\n");

        //shipment
        /*
        writer.write("<OrderItemShipGroup orderId=\"" + od.getOrderID()
                + "\" shipGroupSeqId=\"00001\" carrierRoleTypeId=\"CARRIER\" contactMechId=\"" + contactMechId
                + "\" shippingInstructions=\"\" maySplit=\"N\" isGift=\"N\" />");
        writer.write("\r\n");

        writer.write("<Shipment shipmentId=\"" + shipmentSeq + "\" shipmentTypeId=\"SALES_SHIPMENT\" statusId=\"SHIPMENT_SHIPPED\" primaryOrderId=\"" + od.getOrderID() + "\" "
                + "primaryShipGroupSeqId=\"00001\" estimatedShipCost=\"0.00\" originFacilityId=\"10000\" originContactMechId=\"10003\" "
                + "destinationContactMechId=\"" + contactMechId + "\" partyIdTo=\"" + oCustomerId + "\" partyIdFrom=\"10000\" "
                + "createdDate=\"" + od.getOrderDate() + "\" createdByUserLogin=\"admin\" />");
        writer.write("\r\n");
        */

        //invoice
        writer.write("<Invoice invoiceId=\"" + od.getInvoiceNum() + "\" invoiceTypeId=\"SALES_INVOICE\" partyIdFrom=\"10000\" "
                + "partyId=\"" + oCustomerId + "\" statusId=\"INVOICE_PAID\" "
                + "invoiceDate=\"" + od.getOrderDate() + "\" currencyUomId=\"USD\" "
                + "/>");
        writer.write("\r\n");
        //invoice role
        writer.write("<InvoiceRole invoiceId=\"" + od.getInvoiceNum() + "\" partyId=\"10000\" roleTypeId=\"BILL_FROM_VENDOR\" />");
        writer.write("\r\n");
        writer.write("<InvoiceRole invoiceId=\"" + od.getInvoiceNum() + "\" partyId=\"" + oCustomerId + "\" roleTypeId=\"BILL_TO_CUSTOMER\" />");
        writer.write("\r\n");
        writer.write("<InvoiceRole invoiceId=\"" + od.getInvoiceNum() + "\" partyId=\"" + oCustomerId + "\" roleTypeId=\"PLACING_CUSTOMER\" />");
        writer.write("\r\n");
        writer.write("<InvoiceRole invoiceId=\"" + od.getInvoiceNum() + "\" partyId=\"" + oCustomerId + "\" roleTypeId=\"SHIP_TO_CUSTOMER\" />");
        writer.write("\r\n");

        int shipItemSeqId = 1;
        int invoiceItemSeq = 1;
        for (LambsOrderDetail odItem : oItems) {

            String productId = getProdId(odItem, allVarientProds);
            String itemDescription = pIdName.get(productId);
            String uPrice = odItem.getUnitPrice();

            int orderQty = 0;
            if (odItem.getOrderQty() != null && !odItem.getOrderQty().isEmpty()) {
                orderQty = Integer.parseInt(odItem.getOrderQty());
            }
            int backQty = 0;
            if (odItem.getBackQty() != null && !odItem.getBackQty().isEmpty()) {
                backQty = Integer.parseInt(odItem.getBackQty());
            }
            int orderShipped = orderQty - backQty;

            if (orderShipped > 0) {
                //inventory item
                /*
                writer.write("<InventoryItem inventoryItemId=\"" + Integer.toString(inventorySeq)
                        + "\" inventoryItemTypeId=\"NON_SERIAL_INV_ITEM\" productId=\"" + productId
                        + "\" ownerPartyId=\"10000\" facilityId=\"10000\" quantityOnHandTotal=\"-" + String.valueOf(orderShipped)
                        + "\" availableToPromiseTotal=\"-" + String.valueOf(orderShipped)
                        + "\" accountingQuantityTotal=\"-" + String.valueOf(orderShipped)
                        + "\" unitCost=\"" + uPrice
                        + "\" currencyUomId=\"USD\" />");
                writer.write("\r\n");
                */

                //shipment items
                /*
                writer.write("<ShipmentItem shipmentId=\"" + Integer.toString(shipmentSeq) + "\" shipmentItemSeqId=\"" + String.format("%05d", shipItemSeqId)
                        + "\" productId=\"" + productId
                        + "\" quantity=\"" + String.valueOf(orderShipped)
                        + "\" />");
                writer.write("\r\n");
                */

                //item issuence
                /*
                writer.write("<ItemIssuance itemIssuanceId=\"" + Integer.toString(itemIssuenceSeq) + "\" orderId=\"" + od.getOrderID()
                        + "\" orderItemSeqId=\"" + itemToSequenceId.get(productId)
                        + "\" shipGroupSeqId=\"00001\" inventoryItemId=\"" + Integer.toString(inventorySeq)
                        + "\" shipmentId=\"" + Integer.toString(shipmentSeq) + "\" shipmentItemSeqId=\"" + String.format("%05d", shipItemSeqId)
                        + "\" issuedDateTime=\"" + od.getOrderDate() + "\" issuedByUserLoginId=\"admin\" quantity=\"" + String.valueOf(orderShipped)
                        + "\" />");
                writer.write("\r\n");
                */
/*
                
<InvoiceItem invoiceId="219594" invoiceItemSeqId="00001" invoiceItemTypeId="INV_FPROD_ITEM" productId="1942-22100" quantity="2" amount="46.50" description="New InRemy 10  &quot; - Natural" />
<OrderItemBilling orderId="144067" orderItemSeqId="00001" invoiceId="219594" invoiceItemSeqId="00001" quantity="2" amount="46.50" />
                */
                //invoice items
                writer.write("<InvoiceItem invoiceId=\"" + od.getInvoiceNum() + "\" invoiceItemSeqId=\"" + String.format("%05d", invoiceItemSeq)
                        + "\" invoiceItemTypeId=\"INV_FPROD_ITEM"
                        + "\" productId=\"" + productId
                        + "\" quantity=\"" + String.valueOf(orderShipped) + "\" amount=\"" + uPrice + "\" "
                        + "description=" + itemDescription.replace("&", "&amp;").replace("\"\"", " &quot;") + " />");
                writer.write("\r\n");

                //order item billing
                writer.write("<OrderItemBilling orderId=\"" + od.getOrderID()
                        + "\" orderItemSeqId=\"" + itemToSequenceId.get(productId)
                        + "\" invoiceId=\"" + od.getInvoiceNum() + "\" invoiceItemSeqId=\"" + String.format("%05d", invoiceItemSeq++)
                        + "\" quantity=\"" + String.valueOf(orderShipped)
                        + "\" amount=\"" + uPrice + "\" />");
                writer.write("\r\n");

                itemIssuenceSeq++;
                inventorySeq++;
                shipItemSeqId++;
            }
        }
        //invoice items
        writer.write("<InvoiceItem invoiceId=\"" + od.getInvoiceNum() + "\" invoiceItemSeqId=\"" + String.format("%05d", invoiceItemSeq++)
                + "\" invoiceItemTypeId=\"INV_SHIPPING_CHARGES\" "
                + " amount=\"" + od.getFreightCharge() + "\" "
                + "description=\"" + "Shipping Charge" + " \"/>");
        writer.write("\r\n");

        //invoice contact
        writer.write("<InvoiceContactMech invoiceId=\"" + od.getInvoiceNum() + "\" contactMechPurposeTypeId=\"PAYMENT_LOCATION\" contactMechId=\"10000\" />");
        writer.write("\r\n");

        /*
        //payment
        BigDecimal invSum = new BigDecimal(od.getInvoiceSum());
        BigDecimal invBal = new BigDecimal(od.getInvBalance());
        BigDecimal collectionSum = new BigDecimal(od.getCollectionSum());

        if (invSum.subtract(collectionSum).doubleValue() != invBal.doubleValue()) {
            System.out.println(invSum);
            System.out.println(collectionSum);
            System.out.println(invSum.subtract(collectionSum));
            System.out.println(invBal);

            throw new Exception("invSum - collectionSum != invBal : invoice id " + invSum + " - " + collectionSum + " = " + invBal + " " + od.getInvoiceNum());
        }

        DecimalFormat df = new DecimalFormat("0.00");
        if (collectionSum.doubleValue() != 0) {
            writer.write("<Payment paymentId=\"" + String.valueOf(paymentSeq) + "\" paymentTypeId=\"CUSTOMER_PAYMENT\" paymentMethodTypeId=\"COMPANY_CHECK\" "
                    + "paymentMethodId=\"10000\" partyIdFrom=\"" + oCustomerId + "\" partyIdTo=\"10000\" statusId=\"PMNT_RECEIVED\" "
                    + "effectiveDate=\"" + od.getOrderDate() + "\" amount=\"" + df.format(collectionSum) + "\" currencyUomId=\"USD\" />");
            writer.write("\r\n");

            //payment application
            writer.write("<PaymentApplication paymentApplicationId=\"" + String.valueOf(paymentApplicationSeq++) + "\" paymentId=\"" + String.valueOf(paymentSeq++)
                    + "\" invoiceId=\"" + od.getInvoiceNum()
                    + "\" amountApplied=\"" + df.format(collectionSum) + "\" />");
            writer.write("\r\n");
        }
         */
        shipmentSeq++;

        writer.write("\r\n");
    }

    private static List<LamsCustomerIdAccount> readAndParseCustomer(String fileName) throws FileNotFoundException {
        Scanner scanner = null;
        ArrayList<LamsCustomerIdAccount> coLinks = new ArrayList();

        scanner = new Scanner(new FileInputStream(fileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            //System.out.println(aLine);
            if (index == 1) {
                continue;
            }
            LamsCustomerIdAccount cLink = parseLAMSCustomersCSV(aLine);

            if (cLink != null) {
                coLinks.add(cLink);
            }
        }

        scanner.close();

        return coLinks;
    }

    private static LamsCustomerIdAccount parseLAMSCustomersCSV(String aLine) {
        String delims = "[,]";
        String[] tokens = aLine.split(delims, -1);
        LamsCustomerIdAccount customer = new LamsCustomerIdAccount();

        //System.out.println(aLine);
        customer.setCustID(tokens[0].trim());
        customer.setAccountNumber(tokens[1].trim());
        customer.setCompanyName(tokens[2].trim());

        return customer;
    }

    private static List<LamsProduct> readAndParseProduct(String fileName) throws FileNotFoundException {
        Scanner scanner = null;
        List<LamsProduct> coLinks = new ArrayList();

        scanner = new Scanner(new FileInputStream(fileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            //System.out.println(aLine);
            if (index == 1) {
                continue;
            }
            LamsProduct cLink = parseLAMSProductsCSV(aLine);

            if (cLink != null) {
                coLinks.add(cLink);
            }
        }

        scanner.close();

        return coLinks;
    }

    private static LamsProduct parseLAMSProductsCSV(String aLine) {
        String delims = "[,]";
        String[] tokens = aLine.split(delims, -1);
        LamsProduct color = new LamsProduct();

        if (tokens.length < 2) {
            System.out.println("what?");
        }

        color.setProductId(tokens[0].trim());

        String pName = tokens[1];
        if (pName.charAt(0) == '"') {
            pName = pName.substring(1);
        }
        if (pName.charAt(pName.length() - 1) == '"') {
            pName = pName.substring(0, pName.length() - 1);
        }

        pName = pName.replace("&", "&amp;");
        color.setProductName(pName.replace("\"\"", " &quot;").trim());

        return color;
    }

    private static List<LamsProductColorVarient> readAndParseProductColorVarient(String prodColorFileName) throws FileNotFoundException {
        Scanner scanner = null;
        List<LamsProductColorVarient> coLinks = new ArrayList();

        scanner = new Scanner(new FileInputStream(prodColorFileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            //System.out.println(aLine);
            if (index == 1) {
                continue;
            }
            LamsProductColorVarient cLink = parseLAMSProductColorsCSV(aLine);

            if (cLink != null) {
                coLinks.add(cLink);
            }
        }

        scanner.close();

        return coLinks;
    }

    private static LamsProductColorVarient parseLAMSProductColorsCSV(String aLine) {
        String delims = "[,]";
        String[] tokens = aLine.split(delims, -1);
        LamsProductColorVarient pc = new LamsProductColorVarient();

        if (tokens.length < 2) {
            System.out.println("what?");
        }
        //0             1               2       3               4       5
        //ProductID2	ProductID	Color	Inventory	ColorID	UnitPrice1	UnitPrice2	UnitPrice3	Inv1	Commit1	AfterCommit1	POIssue1	AfterPOIssue1	ReOrder1	RMAQty1	Inv1Active	LastUser	LastModifiedDateTime

        pc.setProductID2(tokens[0].trim());
        pc.setProductID(tokens[1].trim());
        pc.setColor(tokens[2].trim());
        pc.setColorID(tokens[4].trim());
        pc.setUnitPrice1(tokens[5].trim());

        return pc;
    }

    private static List<LamsEmployee> readAndParseEmployee(String fileName) throws FileNotFoundException {
        Scanner scanner = null;
        ArrayList<LamsEmployee> coLinks = new ArrayList();

        scanner = new Scanner(new FileInputStream(fileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            //System.out.println(aLine);
            if (index == 1) {
                continue;
            }
            LamsEmployee cLink = parseLAMSEmployeesCSV(aLine);

            if (cLink != null) {
                coLinks.add(cLink);
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

        lemp.setActive(tokens[16].trim());
        lemp.setCellPhoneNo(tokens[21].trim());
        lemp.setEmail(tokens[23].trim());

        return lemp;
    }

    private static List<LambsOrder> readAndParseOrder(String orderFileName) throws FileNotFoundException {
        Scanner scanner = null;
        ArrayList<LambsOrder> coLinks = new ArrayList();

        scanner = new Scanner(new FileInputStream(orderFileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;

            //System.out.println(aLine);
            if (index == 1) {
                continue;
            }
            LambsOrder cLink = parseLAMBSOrderCSV(aLine);

            if (cLink != null) {
                coLinks.add(cLink);
            }
        }

        scanner.close();

        return coLinks;
    }

    private static List<LambsOrderDetail> readAndParseOrderDetails(String orderDetailFileName) throws FileNotFoundException {
        Scanner scanner = null;
        ArrayList<LambsOrderDetail> coLinks = new ArrayList();

        scanner = new Scanner(new FileInputStream(orderDetailFileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            //System.out.println(aLine);
            if (index == 1) {
                continue;
            }
            LambsOrderDetail cLink = parseLAMBSOrderDetailCSV(aLine);

            if (cLink != null) {
                coLinks.add(cLink);
            }
        }

        scanner.close();

        return coLinks;
    }

    private static LambsOrder parseLAMBSOrderCSV(String aLine) {
        String delims = "[\t]";
        String[] t = aLine.split(delims, -1);

        LambsOrder od = new LambsOrder();
//0              1        2              3                4              5               6                 7    8           9
//OrderID	CustID	EmployeeID	EmployeeName	SalesCommission	PONumber	OrderDate	Confirm	ComPaid	RequiredDate
//10             1       2                3       4             5                6         7             8              9 
//ShippedDate	ShipVia	FreightCharge	Zone	SalesTaxRate	Discountt	COD	OrderTypeId	ShipRegion	ShipZipCode	
//20             1               2         3               4                 5             6                7            8      9
//Weight	InvoiceNum	CTNS	XDiscount	InvStatus	AddToInvDesp	AddToInvAmount	TAmtAftInvTag	Xsales1	Xsales2	
//30             1       2               3               4                5              6                 7                    8       9
//Xsales3	Xsales4	XSales1Percent	XSales2Percent	XSales3Percent	XSales4Percent	EmployeeID2	SalesCommission2	StoreNo	InvoiceInt	
//40                    1                2               3                 4                5                     6                      7               8                    9
//EstimateFreightCharge	AddToCODTagDesp	AddToCODTagAMT	MinusToInvDesp	MinusToInvAMT	MinusToCodTagDesp	MinusToCodTagAMT	InvoiceSum	CollectionSum	InvBalance	
//50             1                 2               3               4                5                    6               7               8                       9      
//PostSum	PostInvBalance	CreditSum	PostCheckSum	CustomerEMail	ShipChargePercent	InvoiceDueDate	CustomerRelated	CustomerZoneCategory	TermID	
//60                     1               2                  3              4      5               6              7               8
//PercentDiscount	PcsDiscount	InvGroupNo	DCPercent	PCPCS	SubTotal	OrderDiscount	LastUser	LastModifiedDateTime

        od.setOrderID(t[0].trim());

        od.setCustID(t[1].trim());
        od.setEmployeeID(t[2].trim());
        od.setPONumber(t[5].trim());
        od.setOrderDate(t[6].trim());

        od.setShippedDate(t[10].trim());

        od.setFreightCharge(t[12].trim());

        od.setInvoiceNum(t[21].trim());
        od.setInvStatus(t[24].trim());

        od.setInvoiceSum(t[47].trim());
        od.setCollectionSum(t[48].trim());
        od.setInvBalance(t[49].trim());

        od.setPostSum(t[50].trim());
        od.setPostInvBalance(t[51].trim());
        od.setCreditSum(t[52].trim());
        od.setPostCheckSum(t[53].trim());

        od.setTermID(t[59].trim());

        return od;
    }

    private static LambsOrderDetail parseLAMBSOrderDetailCSV(String aLine) {
        String delims = "[,]";
        String[] t = aLine.split(delims, -1);

        LambsOrderDetail od = new LambsOrderDetail();

        //ID,OrderID,ProductID,Description,UnitPrice,Quantity,Discount,Color,LastPrice,Inventory,
        //0  1       2           3           4          5         6       7      8        9             
        //OrderQty,BackQty,BackOrderYN,OrderStatus,ColorID,Q1,SubTotal,SpecialItem,OrderDStatus,BackOrderID,
        //10         1          2           3         4    5  6        7           8            9                      
        //LastUser,LastModifiedDateTime,upsize_ts
        //20       1                    2     
        od.setID(t[0].trim());
        od.setOrderID(t[1].trim());
        od.setProductID(t[2].trim());
        od.setUnitPrice(t[4].trim());
        od.setQuantity(t[5].trim());
        od.setDiscount(t[6].trim());
        od.setColor(t[7].trim());
        od.setOrderQty(t[10].trim());
        od.setBackQty(t[11].trim());
        od.setColorID(t[14].trim());
        od.setSubTotal(t[16].trim());
        od.setOrderStatus(t[18].trim());

        return od;
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

    private static Map<String, String> readAndParsePartyContactMech(String partyContactMechFileName) throws FileNotFoundException {
        Scanner scanner = null;
        Map<String, String> coLinks = new HashMap<>();

        scanner = new Scanner(new FileInputStream(partyContactMechFileName));

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

    private static String getDTMCustomerId(LambsOrder od, List<LamsCustomerIdAccount> allCustomers) throws Exception {
        String DTMId = null;
        for (LamsCustomerIdAccount c : allCustomers) {
            if (c.getCustID().equalsIgnoreCase(od.getCustID())) {
                DTMId = c.getAccountNumber();
            }
        }
        if (DTMId == null) {
            throw new Exception("No customer id");
        }

        return DTMId;
    }

    private static String getContactMechId(String oCustomerId, Map<String, String> partyContactMech) {
        return partyContactMech.get(oCustomerId);
    }

    private static List<LambsOrderDetail> getOrderItems(String orderID, List<LambsOrderDetail> allOrderDetails) {
        List<LambsOrderDetail> orderDetails = new ArrayList<>();

        allOrderDetails.stream().filter((od) -> (od.getOrderID().equalsIgnoreCase(orderID))).forEach((od) -> {
            orderDetails.add(od);
        });

        return orderDetails;
    }

    private static String getEmpLoginName(LambsOrder od, List<LamsEmployee> allEmps) throws Exception {
        String empLogin = null;

        for (LamsEmployee le : allEmps) {
            if (od.getEmployeeID().equalsIgnoreCase(le.getEmployeeID().replace("EMP-", ""))) {
                empLogin = getUserLogin(le);
            }
        }

        if (empLogin == null) {
            throw new Exception("user login not found");
        }

        return empLogin;
    }

    private static String getUserLogin(LamsEmployee lc) {
        return (lc.getFirstName().toLowerCase() + lc.getLastName().toLowerCase().substring(0, 1)).replace(" ", "").replace(".", "");
    }

    private static String getProdId(LambsOrderDetail odItem, List<LamsProductColorVarient> allVarientProds) throws Exception {
        String prodId = null;

        for (LamsProductColorVarient pcv : allVarientProds) {
            if (odItem.getProductID().equalsIgnoreCase(pcv.getProductID())
                    && odItem.getColorID().equalsIgnoreCase(pcv.getColorID())) {
                prodId = pcv.getProductID() + "-" + pcv.getProductID2();
            }
        }

        if (prodId == null) {
            throw new Exception("Prod Id not found");
        }

        return prodId;
    }

}
