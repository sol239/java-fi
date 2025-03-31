package com.github.sol239.javafi.utils.backtesting;

public class Strategy {
    public String openClause;
    public String closeClause;
    public Setup setup;


    public Strategy(String openClause, String closeClause,  Setup setup) {
        this.openClause = openClause;
        this.closeClause = closeClause;
        this.setup = setup;
    }
}
