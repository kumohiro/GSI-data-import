/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package createGSIColorFeatures;

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
public class CreateGSIColorFeatures {

    private static int pFeatureId = 10000;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String existingColorFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/existing-color-PRODUCT_FEATURE-after01312018.csv";
        List<String> existingColors = readExistingColors(existingColorFileName);

        String fileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/color.csv";
        List<LamsProductColor> allColors = readAndParse(fileName);

        String outFileName = "/home/seanc/Desktop/GSI/gsi_production/exported-data/data-08292018/color.csv.xml";
        createImportXML(allColors, existingColors, outFileName);
    }

    private static List<LamsProductColor> readAndParse(String fileName) throws FileNotFoundException {
        Scanner scanner = null;
        List<LamsProductColor> coLinks = new ArrayList();

        scanner = new Scanner(new FileInputStream(fileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            System.out.println(index + ": " +aLine);
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

    private static void createImportXML(List<LamsProductColor> allColors, List<String> existingColors, String outFileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));

        int lineC = 1;
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\r\n");
        writer.write("<entity-engine-xml>" + "\r\n");

        for (LamsProductColor lc : allColors) {
            System.out.println(lineC++ + " : color - " + lc.getColorID() + " : " + lc.getCOLOR());

            int fId = pFeatureId + Integer.parseInt(lc.getColorID());

            if (existingColors.contains(String.valueOf(fId))) {
                continue;
            }

            writer.write("<ProductFeature productFeatureId=\"");
            writer.write(String.valueOf(fId));

            writer.write("\" productFeatureTypeId=\"COLOR\" productFeatureCategoryId=\"10000\" description=\"");
            writer.write(lc.getCOLOR());
            writer.write("\"/>" + "\r\n");

            writer.write("<SequenceValueItem seqName=\"ProductFeature\" seqId=\"");
            writer.write(String.valueOf(fId + 1));
            writer.write("\"/>" + "\r\n");

            writer.write("\r\n");
        }

        writer.write("</entity-engine-xml>" + "\r\n");
        writer.close();
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

    private static List<String> readExistingColors(String existingColorFileName) throws FileNotFoundException {
        Scanner scanner = null;
        List<String> existingColors = new ArrayList<>();

        scanner = new Scanner(new FileInputStream(existingColorFileName));

        int index = 0;
        while (scanner.hasNextLine()) {
            String aLine = scanner.nextLine();
            index++;
            if (index == 1) {
                continue;
            }

            String delims = "[,]";
            String[] tokens = aLine.split(delims, -1);
            
            existingColors.add(tokens[0].trim());
        }

        scanner.close();
        
        return existingColors;
    }
    

}
