/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package createGSIVarientProducts;

import createGSIColorFeatures.LamsProductColor;
import createGSIVirtualProducts.LamsProduct;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 *
 * @author seanc
 */
public class CreateGSIVarientProducts {

    private static int pDimension = 352520; // <-- last sequence value item udpate of virtual prod insert
    private static int ppChange = 324400;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, Exception {
        String existingFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/existing-PRODUCT-after01312018.csv";
        List<String> existingProdIds = readExistingProdIds(existingFileName);
        
        String virtualProdFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/products-simple.csv";
        List<LamsProduct> allVirtualProds = readAndParseProduct(virtualProdFileName);

        String prodColorFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/productscolor.csv";
        List<LamsProductColorVarient> allVarientProds = readAndParseProductColorVarient(prodColorFileName);

        String fileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/color.csv";
        List<LamsProductColor> allColors = readAndParseColor(fileName);

        String outFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/productscolor.csv.xml";
        createImportXML(allVarientProds, allVirtualProds, allColors, existingProdIds, outFileName);
    }

    private static List<LamsProduct> readAndParseProduct(String fileName) throws FileNotFoundException {
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
            System.out.println(aLine);
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

    private static List<LamsProductColor> readAndParseColor(String fileName) throws FileNotFoundException {
        Scanner scanner = null;
        List<LamsProductColor> coLinks = new ArrayList();

        scanner = new Scanner(new FileInputStream(fileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            System.out.println(aLine);
            if (index == 1) {
                continue;
            }
            LamsProductColor cLink = parseLAMSColorsCSV(aLine);

            if (cLink != null) {
                coLinks.add(cLink);
            }
        }

        scanner.close();

        return coLinks;
    }

    private static LamsProductColor parseLAMSColorsCSV(String aLine) {
        //﻿ColorID,COLOR,Active,NText1,NText2,NText3,NNumber1,NNumber2,NNumber3,ColorPicture,ColorComposite,ColorCompDesp,ColorDescription,LastUser,LastModifiedDateTime,CreditMemoCount
        //0        1     2      3      4      5      6        7        8        9            10             11            12               13       14                   15
        String delims = "[,]";
        String[] tokens = aLine.split(delims, -1);
        LamsProductColor color = new LamsProductColor();

        color.setColorID(tokens[0].trim());
        color.setCOLOR(tokens[1].replace("\"", "").trim());

        return color;
    }

    private static void createImportXML(List<LamsProductColorVarient> allVarientProds, 
            List<LamsProduct> allVirtualProds, 
            List<LamsProductColor> allColors, 
            List<String> existingProdIds, 
            String outFileName) throws Exception {

        BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));

        int lineC = 1;
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\r\n");
        writer.write("<entity-engine-xml>" + "\r\n");

        for (LamsProductColorVarient pcv : allVarientProds) {
            String productId = pcv.getProductID() + "-" + pcv.getProductID2();
            
            if (existingProdIds.contains(productId)) continue;

            String internalName = getInternalName(pcv.getProductID(), allVirtualProds);
            String colorName = getColorName(pcv.getColorID(), allColors);
            String internalProductName = internalName + " - " + colorName;

            int colorFeatureId = Integer.parseInt(pcv.getColorID()) + 10000;

            writer.write("<Product productId=\"" + productId + "\" productTypeId=\"FINISHED_GOOD\" ");
            writer.write("internalName=\"" + internalProductName + "\" productName=\"" + internalProductName + "\" ");
            writer.write("inventoryItemTypeId=\"NON_SERIAL_INV_ITEM\" isVirtual=\"N\" isVariant=\"Y\" virtualVariantMethodEnum=\"\" billOfMaterialLevel=\"0\" inShippingBox=\"N\" lotIdFilledIn=\"Allowed\" />");
            writer.write("\r\n");

            writer.write("<ProductDimension dimensionId=\"" + String.valueOf(pDimension) + "\" productId=\"" + productId + "\" productType=\"Finished Good\" internalName=\"" + internalProductName + "\"/>");
            writer.write("\r\n");

            writer.write("<ProductAssoc productId=\"" + pcv.getProductID() + "\" productIdTo=\"" + productId + "\" productAssocTypeId=\"PRODUCT_VARIANT\" fromDate=\"2018-04-10 20:22:44.0\"/>");
            writer.write("\r\n");

            writer.write("<ProductFeatureAppl productId=\"" + productId + "\" productFeatureId=\"" + String.valueOf(colorFeatureId) + "\" productFeatureApplTypeId=\"STANDARD_FEATURE\" fromDate=\"2018-04-10 20:20:14.0\"/>");
            writer.write("\r\n");

            writer.write("<ProductPrice productId=\"" + productId + "\" productPriceTypeId=\"DEFAULT_PRICE\" productPricePurposeId=\"PURCHASE\" currencyUomId=\"USD\" productStoreGroupId=\"_NA_\" fromDate=\"2018-04-10 20:20:41.0\" price=\"" + pcv.getUnitPrice1() + "\" taxInPrice=\"Y\"/>");
            writer.write("\r\n");

            writer.write("<ProductPriceChange productPriceChangeId=\"" + String.valueOf(ppChange) + "\" productId=\"" + productId + "\" productPriceTypeId=\"DEFAULT_PRICE\" productPricePurposeId=\"PURCHASE\" currencyUomId=\"USD\" productStoreGroupId=\"_NA_\" fromDate=\"2018-04-10 20:20:41.0\" price=\"" + pcv.getUnitPrice1() + "\"/>");
            writer.write("\r\n");

            pDimension = pDimension + 10;
            writer.write("<SequenceValueItem seqName=\"ProductDimension\" seqId=\"" + String.valueOf(pDimension) + "\"/>");
            writer.write("\r\n");

            ppChange = ppChange + 10;
            writer.write("<SequenceValueItem seqName=\"ProductPriceChange\" seqId=\"" + String.valueOf(ppChange) + "\"/>");
            writer.write("\r\n");
            writer.write("\r\n");
        }

        writer.write("</entity-engine-xml>" + "\r\n");
        writer.close();
    }

    private static String getInternalName(String productID,
            List<LamsProduct> allVirtualProds) throws Exception {

        String rtnString = null;

        Optional<LamsProduct> matchingProduct = allVirtualProds.stream().
                filter(p -> p.getProductId().equals(productID)).
                findFirst();

        LamsProduct prod = matchingProduct.orElse(null);

        if (prod == null) {
            throw new Exception();
        } else {
            rtnString = prod.getProductName();
        }

        return rtnString;
    }

    private static String getColorName(String colorID,
            List<LamsProductColor> allColors) throws Exception {
        String rtnString = null;

        Optional<LamsProductColor> matchingColor = allColors.stream().
                filter(p -> p.getColorID().equals(colorID)).
                findFirst();

        LamsProductColor color = matchingColor.orElse(null);

        if (color == null) {
            System.out.println("## color id = " + colorID);
            
            throw new Exception();
        } else {
            rtnString = color.getCOLOR();
        }

        return rtnString;
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
