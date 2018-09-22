/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package createGSIVirtualProducts;

/**
 *
 * @author seanc
 */
public class SubStringTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String pName = "\"hello seanc\" and chung\"";
        if (pName.charAt(0) == '"'){
            pName = pName.substring(1);
        }
        if (pName.charAt(pName.length()-1) == '"'){
            pName = pName.substring(0,pName.length()-1);
        }
        
        System.out.println(pName);
    }
    
}
