package com.github.sol239.javafi.utils.backtesting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * A class that represents a trading strategy.
 */
public class Strategy {

    /**
     * SQL WHERE clause for opening a trade.
     */
    public String openClause;

    /**
     * SQL WHERE clause for closing a trade.
     */
    public String closeClause;

    /**
     * Setup for the strategy.
     */
    public Setup setup;

    /**
     * Constructor for Strategy.
     *
     * @param openClause SQL WHERE clause for opening a trade.
     * @param closeClause SQL WHERE clause for closing a trade.
     * @param setup Setup for the strategy.
     */
    public Strategy(String openClause, String closeClause,  Setup setup) {
        this.openClause = openClause;
        this.closeClause = closeClause;
        this.setup = setup;
    }

    /**
     * Constructor for Strategy.
     * @param setup Setup for the strategy.
     */
    public Strategy(Setup setup) {
        this.setup = setup;
    }

    /**
     * Loads the open and close clauses from a JSON file.
     * @param jsonPath Path to the JSON file.
     */
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

    /**
     * Returns a string representation of the strategy.
     * @return String representation of the strategy.
     */
    @Override
    public String toString() {
        return "Strategy:\nopenClause = " + openClause + "\n" +
                "closeClause = " + closeClause + "\n" +
                "setup = " + setup.toString() + "\n";
    }
}
