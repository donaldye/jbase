package com.jj.test.mlp;


import java.io.*;
import java.util.*;

/**
 * Created by donald on 7/7/2017.
 */

// This test should take around 45 minutes to 1 hour. Read all of the instructions before you being. It will help you plan ahead.

/* TODO: Implement a net positions calculator
 * This NetPositionCalculator should read in test_data.csv
 * and output a file in the format of net_positions_expected.csv
 *
 * Here is a sample of net positions:
 * TRADER   BROKER  SYMBOL  QUANTITY    PRICE
 * Joe      ML      IBM.N     100         50
 * Joe      DB      IBM.N    -50          50
 * Joe      CS      IBM.N     30          30
 * Mike     CS      AAPL.N    100         20
 * Mike     BC      AAPL.N    200         20
 * Debby    BC      NVDA.N    500         20
 *
 * Expected Output:
 * TRADER   SYMBOL  QUANTITY
 * Joe      IBM.N     80
 * Mike     AAPL.N    300
 * Debby    NVDA.N    500
 */

/* TODO: Implement a boxed position calculator
 * This BoxedPositionCalculator should read in test_data.csv
 * and output a file the format of boxed_positions_expected.csv
 *
 * Boxed positions are defined as:
 * A trader has long (quantity > 0) and short (quantity < 0) positions for the same symbol at different brokers.
 *
 * This is an example of a boxed position:
 * TRADER   BROKER  SYMBOL  QUANTITY    PRICE
 * Joe      ML      IBM.N     100         50      <------Has at least one positive quantity for Trader = Joe and Symbol = IBM
 * Joe      DB      IBM.N    -50          50      <------Has at least one negative quantity for Trader = Joe and Symbol = IBM
 * Joe      CS      IBM.N     30          30
 *
 * Expected Output:
 * TRADER   SYMBOL  QUANTITY
 * Joe      IBM.N     50        <------Show the minimum quantity of all long positions or the absolute sum of all short positions. ie. minimum of (100 + 30) and abs(-50) is 50
 *
 * This is NOT a boxed position. Since no trader has both long and short positions at different brokers.
 * TRADER   BROKER  SYMBOL  QUANTITY    PRICE
 * Joe      ML      IBM.N     100         50
 * Joe      DB      IBM.N     50          50
 * Joe      CS      IBM.N     30          30
 * Mike     DB      IBM.N    -50          50
 *
 */

/* TODO: Write tests to ensure your code works
 * Feel free to write as many or as few tests as you feel necessary to ensure that your
 * code is correct and stable.
 */

/*
 * How we review this test:
 * We look for clean, readable code, that is well designed and solves the problem.
 * As for testing, we simply look for completeness.
 *
 * Some assumptions you can make when implementing:
 * 1) The file is always valid, you do not need to validate the file in any way
 * 2) You may write all classes in this one file
 */
public class PositionCalculator
{
    public final static String DELIMITER = ",";
    public final static Integer ZERO = new Integer(0);
    public final static String HEADER = "TRADER,SYMBOL,QUANTITY";

    // these two generator methods look very similiar but haven't got time to refactor to make reusable codes.
    //  So just to make it working and readable
    public static void generateNetPositionFile(String sourceFilePath, String  targetFilePath)
            throws IOException
    {
        //trader,broker,symbol,quantity,price
        //Anna,DB,IBM.N,100,12
        File inputFile = new File(sourceFilePath);
        Scanner input = null;
        //use LinkedHashMap to maintain insertion order
        Map<String, Integer> netPositionMap = new LinkedHashMap<String, Integer>();

        try
        {
            input = new Scanner(inputFile);

            int i = 0;
            while (input.hasNextLine())
            {
                i++;
                String line = input.nextLine();
                System.out.println(line);
                //skip header row
                if (i == 1) {
                    continue;
                }
                String[] result = line.split(DELIMITER);

                //assume we know what the columns are and their column indexes
                //  otherwise we'll configure enum {trader, ...} to define what to look for and then store their column indexes
                String trader = result[0];
                String secId = result[2];
                Integer quantity = Integer.valueOf(result[3]);

                //don't bother to define Custom Object as key. Just concatenating String as key
                String key = trader + "," + secId;
                Integer existingQuan = netPositionMap.get(key);
                if (existingQuan == null)
                {
                    netPositionMap.put(key, quantity);
                }
                else
                {
                    netPositionMap.put(key, existingQuan + quantity);
                }
            }
        }
        finally
        {
            if (input != null)
            {
                input.close();
            }
        }

        //StringBuilder sb = new StringBuilder();
        BufferedWriter bufferedWriter = null;
        try
        {
            FileWriter writer = new FileWriter(targetFilePath, false);
            bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(HEADER);
            bufferedWriter.newLine();
            for (Map.Entry<String, Integer> entry : netPositionMap.entrySet())
            {
                String[] keys = entry.getKey().split(DELIMITER);
                Integer quantity = entry.getValue();
                String line = keys[0] + DELIMITER + keys[1] + DELIMITER + quantity;
                bufferedWriter.write(line);
                bufferedWriter.newLine();
                //sb.append(keys[0] + DELIMITER + keys[1] + DELIMITER + quantity + "\r\n");
            }
        }
        finally
        {
            if (bufferedWriter != null)
            {
                bufferedWriter.close();
            }
        }
    }

    // these two generator methods look very similiar but haven't got time to refactor to make reusable codes.
    //  So just to make it working and readable
    public static void generateBoxedPositionFile(String sourceFilePath, String  targetFilePath)
            throws IOException
    {
        File inputFile = new File(sourceFilePath);
        Scanner input = null;

        Map<String, Integer> longPositionMap = new LinkedHashMap<String, Integer>();
        Map<String, Integer> shortPositionMap = new LinkedHashMap<String, Integer>();

        try
        {
            input = new Scanner(inputFile);

            int i = 0;
            while (input.hasNextLine())
            {
                i++;
                String line = input.nextLine();
                System.out.println(line);
                //skip header row
                if (i == 1)
                {
                    continue;
                }
                String[] result = line.split(DELIMITER);

                //assume we know what the columns are and their column indexes
                //  otherwise we'll configure enum {trader, ...} to define what to look for and then store their column indexes
                String trader = result[0];
                String secId = result[2];
                Integer quantity = Integer.valueOf(result[3]);

                //don't bother to define Custom Object as key. Just concatenating String as key
                String key = trader + "," + secId;
                if (quantity.compareTo(ZERO) > 0)
                {
                    Integer existingQuan = longPositionMap.get(key);
                    if (existingQuan == null)
                    {
                        longPositionMap.put(key, quantity);
                    }
                    else
                    {
                        longPositionMap.put(key, existingQuan + quantity);
                    }
                }
                else if (quantity.compareTo(ZERO) < 0)
                {
                    Integer existingQuan = shortPositionMap.get(key);
                    if (existingQuan == null)
                    {
                        shortPositionMap.put(key, Math.abs(quantity));
                    }
                    else
                    {
                        shortPositionMap.put(key, existingQuan + Math.abs(quantity));
                    }
                }
            }
        }
        finally
        {
            if (input != null)
            {
                input.close();
            }
        }

        // find the common keys
        Set<String> s = new HashSet<String>(longPositionMap.keySet());
        s.retainAll(shortPositionMap.keySet());

        BufferedWriter bufferedWriter = null;

        try
        {
            FileWriter writer = new FileWriter(targetFilePath, false);
            bufferedWriter = new BufferedWriter(writer);

            bufferedWriter.write(HEADER);
            bufferedWriter.newLine();
            for (String keyString : s)
            {
                String[] keys = keyString.split(DELIMITER);
                Integer longQuan = longPositionMap.get(keyString);
                Integer shortQuan = shortPositionMap.get(keyString);
                String line = keys[0] + DELIMITER + keys[1] + DELIMITER + (longQuan.compareTo(shortQuan) < 0 ? longQuan : shortQuan);
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
        }
        finally
        {
            if (bufferedWriter != null)
            {
                bufferedWriter.close();
            }
        }
    }

    // assuming positions are in insertion order
    //  This works for the sample data but this might not be working for comparing other BoxedPositionFile due to long/short ordering
    //  Instead of String comparison, ideally we should be comparing row by row in TreeMap
    public static boolean fileEquals(String outputFile, String expectedFile)
            throws FileNotFoundException
    {
        File file1 = new File(outputFile);
        Scanner input = new Scanner(file1);

        StringBuilder sb1 = new StringBuilder();
        while (input.hasNextLine())
        {
            sb1.append(input.nextLine());
        }
        input.close();

        File file2 = new File(expectedFile);
        input = new Scanner(file2);
        StringBuilder sb2 = new StringBuilder();
        while (input.hasNextLine())
        {
            sb2.append(input.nextLine());
        }
        input.close();

        //or do assertEquals(string1, string2) like JUnit
        return sb1.toString().equals(sb2.toString());
    }

    public static void runTestCase()
    {
        String tradeFile = "C:\\Users\\yejaz\\IdeaProjects\\util\\data\\test_data.csv";
        String netPositionFile = "C:\\Users\\yejaz\\IdeaProjects\\util\\data\\net_positions_output.csv";
        String boxPositionFile = "C:\\Users\\yejaz\\IdeaProjects\\util\\data\\boxed_positions_output.csv";

        try
        {
            generateNetPositionFile(tradeFile, netPositionFile);
            String expectedNetPositionFile = "C:\\Users\\yejaz\\IdeaProjects\\util\\data\\net_positions_expected.csv";

            System.out.println("NetPositionFile is expected ? " + fileEquals(netPositionFile, expectedNetPositionFile));
        }
        catch (Exception excep)
        {
            System.out.println("failure in generateNetPositionFile");
        }

        try
        {
            generateBoxedPositionFile(tradeFile, boxPositionFile);
            String expectedBoxPositionFile = "C:\\Users\\yejaz\\IdeaProjects\\util\\data\\boxed_positions_expected.csv";

            System.out.println("BoxedPositionFile is expected ? " + fileEquals(boxPositionFile, expectedBoxPositionFile));
        }
        catch (Exception excep)
        {
            System.out.println("failure in generateBoxPositionFile");
        }
    }

    public static void main(String[] args)
    {
        runTestCase();
    }

}
