package com.github.sol239.javafi.cmd.Commands;

import com.github.sol239.javafi.DataObject;
import com.github.sol239.javafi.cmd.Command;
import com.github.sol239.javafi.instrument.java.InstrumentDemo;
import com.github.sol239.javafi.instrument.java.InstrumentExecutor;

import java.util.*;

public class StCommand implements Command {
    @Override
    public String getName() {
        return "st";
    }

    @Override
    public String getDescription() {
        return "Usage: st [OPTION]...\n" +
                "The command updates the tables with calculated instrument values\n" +
                "Example: st -t=btc_d,solx -i=rsi:14;sma:30\n" +
                getParameters();
    }


    public String getParameters() {
        return "Options:\n" +
                "  -h, --help\n" +
                "  -t=TABLE_NAME1,TABLE_NAME2,... , --tables=TABLE_NAME1,TABLE_NAME2,...\n" +
                "  -i=INS_CMD1,INS_CMD2,... , --instruments=INS_CMD1,INS_CMD2,...";
    }

    @Override
    public DataObject run(List<String> args, List<String> flags) {

        List<String> tables = new ArrayList<>();
        List<String> instrumentStrings = new ArrayList<>();

        for (String flag : flags) {
            if (flag.equals("-h") || flag.equals("--help")) {
                return new DataObject(200, "server", this.getDescription());
            } else if (flag.startsWith("-t=") || flag.startsWith("--tables=")) {
                try {
                    tables = Arrays.asList(flag.split("=")[1].split(","));
                } catch (Exception e) {
                    return new DataObject(400, "server", "Error parsing tables.");
                }
            } else if (flag.startsWith("-i=") || flag.startsWith("--instruments=")) {
                try {
                    instrumentStrings = Arrays.asList(flag.split("=")[1].split(";"));
                } catch (Exception e) {
                    return new DataObject(400, "server", "Error parsing instruments.");
                }
            }
        }

        try {
            Map<String, Double[]> instruments = new LinkedHashMap<>();

            for (String instrument : instrumentStrings) {
                String[] instrumentSplit = instrument.split(":");
                String instrumentName = instrumentSplit[0];
                Double[] instrumentParams = Arrays.stream(instrumentSplit[1].split(",")).map(Double::parseDouble).toArray(Double[]::new);
                instruments.put(instrumentName, instrumentParams);
            }
            InstrumentExecutor.runInstruments(tables, instruments);
        } catch (Exception e) {
            return new DataObject(400, "server", "Error executing instruments.");
        }

        return new DataObject(200, "server", "Instruments executed successfully.");


    }
}
