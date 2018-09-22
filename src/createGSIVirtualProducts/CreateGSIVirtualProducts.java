/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package createGSIVirtualProducts;

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
public class CreateGSIVirtualProducts {

    private static int prodIdSeq = 10000; //<-- no need to change for we use existing prod ids and the sequence value will update plus this
    private static int prodDimentionSeq = 351890; //<-- check the value from DB

    public static void main(String[] args) throws FileNotFoundException, IOException {
        String existingFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/existing-PRODUCT-after01312018.csv";
        List<String> existingVirtualProdIds = readExistingProdIds(existingFileName);

        String fileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/products-simple.csv";
        List<LamsProduct> allVirtualProds = readAndParse(fileName);

        String outFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/products.csv.xml";
        createImportXML(allVirtualProds, existingVirtualProdIds, outFileName);
    }

    private static List<LamsProduct> readAndParse(String fileName) throws FileNotFoundException {
        Scanner scanner = null;
        List<LamsProduct> coLinks = new ArrayList();

        scanner = new Scanner(new FileInputStream(fileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            System.out.println(aLine);
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

    private static void createImportXML(List<LamsProduct> allProds, List<String> existingVirtualProdIds, String outFileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));

        int lineC = 1;
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\r\n");
        writer.write("<entity-engine-xml>" + "\r\n");

        for (LamsProduct lc : allProds) {
            System.out.println(lineC++ + " : product id - " + lc.getProductId());
            if (existingVirtualProdIds.contains(lc.getProductId())) {
                continue;
            }

            writer.write("<Product productId=\"");
            writer.write(lc.getProductId());
            writer.write("\" productTypeId=\"FINISHED_GOOD\" internalName=\"");
            writer.write(lc.getProductName());
            writer.write("\" productName=\"");
            writer.write(lc.getProductName());
            writer.write("\" inventoryItemTypeId=\"NON_SERIAL_INV_ITEM\" isVirtual=\"Y\" isVariant=\"N\" virtualVariantMethodEnum=\"VV_FEATURETREE\" billOfMaterialLevel=\"0\" createdDate=\"\" createdByUserLogin=\"admin\" lastModifiedDate=\"\" lastModifiedByUserLogin=\"admin\" inShippingBox=\"N\" lotIdFilledIn=\"Allowed\" lastUpdatedStamp=\"\" lastUpdatedTxStamp=\"\" createdStamp=\"\" createdTxStamp=\"\"/>");
            writer.write("\r\n");

            writer.write("<SequenceValueItem seqName=\"Product\" seqId=\"");
            writer.write(String.valueOf(prodIdSeq + Integer.valueOf(lc.getProductId())));
            writer.write("\"/>" + "\r\n");

            writer.write("<ProductDimension dimensionId=\"");
            writer.write(String.valueOf(prodDimentionSeq));
            writer.write("\" productId=\"" + lc.getProductId());
            writer.write("\" productType=\"Finished Good\" internalName=\"" + lc.getProductName());
            writer.write("\" lastUpdatedStamp=\"\" lastUpdatedTxStamp=\"\" createdStamp=\"\" createdTxStamp=\"\"/>");
            writer.write("\r\n");

            writer.write("<SequenceValueItem seqName=\"ProductDimension\" seqId=\"");
            prodDimentionSeq = prodDimentionSeq + 10;
            writer.write(String.valueOf(prodDimentionSeq));
            writer.write("\"/>" + "\r\n");

            writer.write("\r\n");

        }
        writer.write("</entity-engine-xml>" + "\r\n");
        writer.close();
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

    private static List<String> readExistingProdIds(String existingFileName) throws FileNotFoundException {
        Scanner scanner = null;
        List<String> existingIds = new ArrayList<>();

        scanner = new Scanner(new FileInputStream(existingFileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            System.out.println(aLine);
            if (index == 1) {
                continue;
            }

            String delims = "[,]";
            String[] tokens = aLine.split(delims, -1);

            existingIds.add(tokens[0].trim());
        }

        scanner.close();
        return existingIds;
    }
}
