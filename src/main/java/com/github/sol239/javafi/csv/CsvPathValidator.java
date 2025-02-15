package com.github.sol239.javafi.csv;

public class CsvPathValidator {

    public static boolean isValid(String path) {
        return path.endsWith(".csv");
    }

}
