package com.github.sol239.javafi.utils.csv;

import java.util.HashMap;
import java.util.Scanner;

public class CmdController {

    public static final HashMap<String, String> mainMenu = new HashMap<>()
    {
        {
            put("1", "ADD CSV");
            put("2", "ANALYSE CSV");
            put("3", "SETTINGS");
            put("4", "EXIT");
        }
    };

    public static final HashMap<String, String> addCsvMenu = new HashMap<>()
    {
        {
            put("1", "ADD CSV");
        }
    };

    public static final HashMap<String, String> analyseCsv = new HashMap<>()
    {
        {
            put("1", "ANALYSE CSV");
        }
    };

    public static final HashMap<String, String> settings = new HashMap<>()
    {
        {
            put("1", "VIEW DATASETS");
        }
    };



    public static final HashMap<Integer, String> menus = new HashMap<>()
    {
        {
            put(0, "MAIN MENU");
            put(1, "ADD CSV");
            put(2, "ANALYSE CSV");
            put(3, "SETTINGS");
            put(4, "EXIT");
        }
    };

     private int currentMenu;


    public CmdController() {
        this.currentMenu = 0;
    }

    public void setCurrentMenu(int menu) {
        this.currentMenu = menu;
    }

    public int getCurrentMenu() {
        return this.currentMenu;
    }

    public static  <T> void printCommands(HashMap<T, String> commands) {
        for (T key : commands.keySet()) {
            System.out.println(key + " - " + commands.get(key));
        }

        System.out.print("\033[H\033[2J");
        System.out.flush();


    }

    public void runCommand(int command) {
        if (this.currentMenu == 1) {
            if (command == 1) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter the path to the CSV file: ");
                String path = scanner.nextLine();
                System.out.println("Path: " + path);

                CsvHandler.readCsv(path);
            }
        }
    }

    public void Controller() {
        switch (this.currentMenu) {
            case 0 -> {printCommands(mainMenu);}
            case 1 -> printCommands(addCsvMenu);
            case 2 -> printCommands(analyseCsv);
            case 3 -> printCommands(settings);
        }

    }



}
