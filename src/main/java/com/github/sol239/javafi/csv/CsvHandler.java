package com.github.sol239.javafi.csv;

import com.github.sol239.javafi.json.CsvPathsDataClass;
import com.github.sol239.javafi.json.JsonUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class CsvHandler {
    public static void readCsv(String path) {
        //String path = "C:\\Users\\david_dyn8g78\\IdeaProjects\\javafi\\src\\main\\java\\org\\example\\btc_usd_1m.csv"; // Replace with the path to your CSV file
        // C:\Users\david_dyn8g78\Downloads\archive\ETH_1min.csv
        // C:\Users\david_dyn8g78\Desktop\btc_usd_1m.csv
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            Path jsonPath = Paths.get(".", "assets", "data.json");

            System.out.println("Reading CSV file: " + path);
            System.out.println("Writing JSON file: " + jsonPath.toString());

            //TODO: deserialization does not work yes
            CsvPathsDataClass c = JsonUtils.deserialize(CsvPathsDataClass.class, jsonPath.toString());

            // print out the csv paths
            for (String csvPath : c.csvPaths) {
                System.out.println(csvPath);
            }


            c.csvPaths.add(path);
            JsonUtils.serialize(c, true, jsonPath.toString());

            System.out.println("CSV READ SUCCESS");


        } catch (IOException e) {
            System.out.println("Error reading the CSV file: " + e.getMessage());
        }
    }


    public static HashMap<Integer, String> getCsvPaths() {
        Path jsonPath = Paths.get(".", "assets", "data.json");
        try {
            CsvPathsDataClass c = JsonUtils.deserialize(CsvPathsDataClass.class, jsonPath.toString());
            HashMap<Integer, String> csvPaths = new HashMap<>();
            for (int i = 0; i < c.csvPaths.size(); i++) {
                csvPaths.put(i, c.csvPaths.get(i));
            }
            return csvPaths;
        } catch (IOException e) {
            System.out.println("Error reading the JSON file: " + e.getMessage());
            return null;
        }
    }

    public static void removeCsvPath(String path) {
        Path jsonPath = Paths.get(".", "assets", "data.json");
        try {
            CsvPathsDataClass c = JsonUtils.deserialize(CsvPathsDataClass.class, jsonPath.toString());
            c.csvPaths.remove(path);
            JsonUtils.serialize(c, true, jsonPath.toString());
        } catch (IOException e) {
            System.out.println("Error reading the JSON file: " + e.getMessage());
        }
    }


    public static void printValidCsvPaths() {
        Path jsonPath = Paths.get(".", "assets", "data.json");
        try {
            CsvPathsDataClass c = JsonUtils.deserialize(CsvPathsDataClass.class, jsonPath.toString());
            System.out.println("Valid CSV paths:");
            for (String path : c.csvPaths) {
                System.out.println(path);
            }
        } catch (IOException e) {
            System.out.println("Error reading the JSON file: " + e.getMessage());
        }
    }
}
