/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package createPayments;

import createGSISalesOrderInvoice.LambsOrder;
import createGSISalesOrderInvoice.LamsCustomerIdAccount;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.math.BigDecimal;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author seanc
 */
public class CreateGSIPayments {

    private static int paymentApplicationSeq = 10000;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
        // TODO code application logic here
        String paymentFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/products/payments.csv";
        String outFileBase = "/home/seanc/Desktop/GSI/gsi_production/paymentCreate/paymentInput";
        String customerFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/products/customer-id-account-db.csv";
        String orderFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/products/orders-from2011-noInv0-trimed-tab.txt";

        List<LambsOrder> allOrders = readAndParseOrder(orderFileName);

        List<LamsCustomerIdAccount> allCustomers = readAndParseCustomer(customerFileName);

        List<LambsPayment> payments = readAndParsePayments(paymentFileName);

        checkPayments(payments, allCustomers, allOrders, outFileBase);

        //writeOutPaymentXML(outFileBase, payments, allCustomers, allOrders);
    }

    private static List<LambsPayment> readAndParsePayments(String paymentFileName) throws FileNotFoundException {
        Scanner scanner = null;
        List<LambsPayment> coLinks = new ArrayList();

        scanner = new Scanner(new FileInputStream(paymentFileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            //System.out.println(aLine);
            if (index == 1) {
                continue;
            }
            LambsPayment cLink = parseLAMSPaymentsCSV(aLine);

            if (cLink != null) {
                coLinks.add(cLink);
            }
        }

        scanner.close();

        return coLinks;
    }

    private static LambsPayment parseLAMSPaymentsCSV(String aLine) {
        String delims = "[,]";
        String[] t = aLine.split(delims, -1);

        LambsPayment lp = new LambsPayment();
        //0          1       2       3           4            5                6        7            8       9
        //PaymentID,OrderID,CustID,PaymentDate,PaymentAmount,PaymentMethodID,CheckBank,CheckNumber,CardName,CreditCardNumber,
        //10          11    2        3               4                5           6         7        8        9
        //PaymentDone,ARID,CustID2,CreditCardHolder,CreditCardExpDate,MasterIDNo,InvoiceNo,CreditID,Lastuser,LastModifiedDateTime,
        //20
        //StoreNo

        lp.setPaymentID(t[0].trim());
        lp.setOrderID(t[1].trim());
        lp.setCustID(t[2].trim());
        lp.setPaymentDate(t[3].trim());
        lp.setPaymentAmount(t[4].trim());
        lp.setPaymentMethodID(t[5].trim());

        lp.setCheckNumber(t[7].trim());

        lp.setPaymentDone(t[10].trim());

        lp.setInvoiceNo(t[16].trim());

        /*
        if (Integer.parseInt(lp.getOrderID()) < 161039) {
            lp = null;
        }
         */
        return lp;
    }

    private static void writeOutPaymentXML(String outFileNameBase,
            List<LambsPayment> payments,
            List<LamsCustomerIdAccount> allCustomers,
            List<LambsOrder> allOrders) throws IOException, Exception {
        String outFileName = "";
        int orderCount = 0;
        BufferedWriter writer = null;
        int fileSequence = 0;

        int paymentApplicationSeq = 0;

        for (LambsPayment od : payments) {
            String balance = getInvoiceBalance(allOrders, od.getOrderID());

            if (orderCount % 5000 == 0) {
                //create new writer with new file name
                outFileName = outFileNameBase + fileSequence++ + ".xml";
                writer = new BufferedWriter(new FileWriter(outFileName));

                int lineC = 1;
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\r\n");
                writer.write("<entity-engine-xml>" + "\r\n");
            }

            String oCustomerId = getDTMCustomerId(od.getCustID(), allCustomers);

            //payment
            DecimalFormat df = new DecimalFormat("0.00");
            writer.write("<Payment paymentId=\"" + od.getPaymentID() + "\" paymentTypeId=\"CUSTOMER_PAYMENT\" paymentMethodTypeId=\"COMPANY_CHECK\" "
                    + "paymentMethodId=\"10000\" partyIdFrom=\"" + oCustomerId + "\" partyIdTo=\"10000\" statusId=\"PMNT_RECEIVED\" "
                    + "effectiveDate=\"" + od.getPaymentDate() + "\" amount=\"" + od.getPaymentAmount() + "\" currencyUomId=\"USD\" />");
            writer.write("\r\n");

            //payment application
            writer.write("<PaymentApplication paymentApplicationId=\"" + String.valueOf(paymentApplicationSeq++) + "\" paymentId=\"" + od.getPaymentID()
                    + "\" invoiceId=\"" + od.getInvoiceNo()
                    + "\" amountApplied=\"" + od.getPaymentAmount() + "\" />");
            writer.write("\r\n");

            writer.write("\r\n");
            orderCount++;

            if (orderCount % 5000 == 0 && orderCount != 0) {
                //close the current writer
                writer.write("</entity-engine-xml>" + "\r\n");
                writer.close();
            }

        }
        writer.write("</entity-engine-xml>" + "\r\n");
        writer.close();
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

    private static String getDTMCustomerId(String lcustId, List<LamsCustomerIdAccount> allCustomers) throws Exception {
        String DTMId = null;
        for (LamsCustomerIdAccount c : allCustomers) {
            if (c.getCustID().equalsIgnoreCase(lcustId)) {
                DTMId = c.getAccountNumber();
            }
        }
        if (DTMId == null) {
            throw new Exception("No customer id");
        }

        return DTMId;
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

    private static String getInvoiceBalance(List<LambsOrder> allOrders, String orderID) throws Exception {
        String remainBalance = null;

        for (LambsOrder lo : allOrders) {
            if (lo.getOrderID().equalsIgnoreCase(orderID)) {
                remainBalance = lo.getInvBalance();
            }
        }

        if (remainBalance == null || remainBalance.isEmpty()) {
            throw new Exception("Balance is null or empty for orderID: " + orderID);
        }

        return remainBalance;
    }

    private static void checkPayments(List<LambsPayment> payments,
            List<LamsCustomerIdAccount> allCustomers,
            List<LambsOrder> allOrders,
            String outFileNameBase) throws Exception {

        String outFileName = "";
        int orderCount = 0;
        BufferedWriter writer = null;
        int fileSequence = 0;

        for (LambsOrder lo : allOrders) {
            String invoiceId = lo.getInvoiceNum();
            if (orderCount % 50000 == 0) {
                //create new writer with new file name
                outFileName = outFileNameBase + fileSequence++ + ".xml";
                writer = new BufferedWriter(new FileWriter(outFileName));

                int lineC = 1;
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\r\n");
                writer.write("<entity-engine-xml>" + "\r\n");
            }

            List<LambsPayment> invPayments = getPayments(lo, payments, allCustomers, writer);

            orderCount++;

            if (orderCount % 50000 == 0 && orderCount != 0) {
                //close the current writer
                writer.write("</entity-engine-xml>" + "\r\n");
                writer.close();
            }
        }
        writer.write("</entity-engine-xml>" + "\r\n");
        writer.close();
    }

    private static List<LambsPayment> getPayments(LambsOrder lo,
            List<LambsPayment> payments,
            List<LamsCustomerIdAccount> allCustomers,
            BufferedWriter writer) throws Exception {

        List<LambsPayment> rtnPayments = new ArrayList<>();

        BigDecimal invoiceTotal = new BigDecimal(lo.getInvoiceSum());
        BigDecimal invoiceBalance = new BigDecimal(lo.getInvBalance());

        BigDecimal paymentTotal = new BigDecimal("0.00");

        String oCustomerId = getDTMCustomerId(lo.getCustID(), allCustomers);

        for (LambsPayment lp : payments) {
            //System.out.println(lp.getInvoiceNo() + " : " + lo.getInvoiceNum());

            if (lp.getInvoiceNo().equalsIgnoreCase(lo.getInvoiceNum())) {
                rtnPayments.add(lp);

                BigDecimal paymentAmt = new BigDecimal(lp.getPaymentAmount());
                paymentTotal = paymentTotal.add(paymentAmt);

                //payment
                writer.write("<Payment paymentId=\"" + lp.getPaymentID() + "\" paymentTypeId=\"CUSTOMER_PAYMENT\" paymentMethodTypeId=\"COMPANY_CHECK\" "
                        + "paymentMethodId=\"10000\" partyIdFrom=\"" + oCustomerId + "\" partyIdTo=\"10000\" statusId=\"PMNT_RECEIVED\" "
                        + "effectiveDate=\"" + lp.getPaymentDate() + "\" amount=\"" + lp.getPaymentAmount() + "\" currencyUomId=\"USD\" />");
                writer.write("\r\n");

                //payment application
                writer.write("<PaymentApplication paymentApplicationId=\"" + String.valueOf(paymentApplicationSeq++) + "\" paymentId=\"" + lp.getPaymentID()
                        + "\" invoiceId=\"" + lp.getInvoiceNo()
                        + "\" amountApplied=\"" + lp.getPaymentAmount() + "\" />");

                writer.write("\r\n");

                writer.write("\r\n");
            }
        }

        if (invoiceBalance.compareTo(BigDecimal.ZERO) != 0) {
            writer.write("<Invoice invoiceId=\"" + lo.getInvoiceNum()
                    + "\" invoiceTypeId=\"SALES_INVOICE\" partyIdFrom=\"10000\" partyId=\"" + oCustomerId
                    + "\" statusId=\"INVOICE_IN_PROCESS\" currencyUomId=\"USD\" />");
            writer.write("\r\n");

            writer.write("\r\n");
        }

        return rtnPayments;
    }
}
