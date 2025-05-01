package com.github.sol239.javafi.utils.backtesting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class Strategy {
    public String openClause;
    public String closeClause;
    public Setup setup;


    public Strategy(String openClause, String closeClause,  Setup setup) {
        this.openClause = openClause;
        this.closeClause = closeClause;
        this.setup = setup;
    }

    public Strategy(Setup setup) {
        this.setup = setup;
    }

    public void loadClausesFromJson(String jsonPath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(new File(jsonPath));
            this.openClause = jsonNode.get("openClause").asText();
            this.closeClause = jsonNode.get("closeClause").asText();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Strategy:\nopenClause = " + openClause + "\n" +
                "closeClause = " + closeClause + "\n" +
                "setup = " + setup.toString() + "\n";
    }
}
