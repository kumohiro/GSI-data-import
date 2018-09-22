/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author seanc
 */
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.util.Pair;

public class test {

    private static boolean containsItem(List<Pair<String, Integer>> list, Pair<String, Integer> item) {
        boolean contains = false;

        for (Pair<String, Integer> i : list) {
            if (i.getKey().equalsIgnoreCase(item.getKey())) {
                contains = true;
                break;
            }
        }

        return contains;
    }

    private static void updateItemList(List<Pair<String, Integer>> list, Pair<String, Integer> item) {
        int idx = 0;
        Pair<String, Integer> updateTo = null;

        for (Pair<String, Integer> i : list) {
            if (i.getKey().equalsIgnoreCase(item.getKey())) {
                updateTo = new Pair(item.getKey(), i.getValue() + item.getValue());
                break;
            }
            idx++;
        }

        list.set(idx, updateTo);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        List<Pair<String, Integer>> list = new ArrayList<>();
        Pair<String, Integer> item = null;

        item = new Pair<>("seanc", 1);
        list.add(item);

        item = new Pair<>("chungs", 1);
        list.add(item);

        item = new Pair<>("unho", 1);
        list.add(item);

        for (Pair<String, Integer> i : list) {
            System.out.println(i);
        }
        System.out.println("---------------------------------");

        item = new Pair<>("unho", 3);
        if (containsItem(list, item)) {
            updateItemList(list, item);
        } else {
            list.add(item);
        }

        item = new Pair<>("unhochung", 3);
        if (containsItem(list, item)) {
            updateItemList(list, item);
        } else {
            list.add(item);
        }

        for (Pair<String, Integer> i : list) {
            System.out.println(i);
        }

        Collections.sort(list, new Comparator<Pair<String, Integer>>() {
            @Override
            public int compare(final Pair<String, Integer> o1, final Pair<String, Integer> o2) {
                if (o1.getValue() > o2.getValue()){
                    return -1;
                } else if (o1.getValue() < o2.getValue()){
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        
        System.out.println("--------------------------------------");
        
        for (Pair<String, Integer> i : list) {
            System.out.println(i);
        }
    }

}
