# java-fi

java-fi is a client-server application that allows backtesting of trading strategies using historical data.
Various indicators are available for use, and the application is designed to be extensible, allowing users
to add their own indicators and strategies.

The application is built using Java and PostgreSQL, and it provides a command-line interface for interacting
with the server.

---

## Features

### Calculationg Indicators

- Creating columns with the indicator values.

### Backtesting

- Allows you to backtest your strategies using historical data.

---

## Getting Started

### Prerequisites

- Java 23
- PostgreSQL
- Gradle

### Installation & Usage

1. Clone the repository:
   ```bash
   git clone https://github.com/sol239/java-fi
   ```
2. Navigate to the project directory:
   ```bash
    cd java-fi
    ```
3. Build the project using Gradle:
    ```bash
    gradle clean build
    ```
    - Some of the tests will fail because of missing database credentials.
4. Run the server using:
    ```bash
    gradle server
   
    # or
   
   ./gradlew server
    ```

5. Run the client using:
    ```bash
    gradle client
   
    # or
   
    ./gradlew client
    ```

---

### Example 1 - inserting historical data

> gradle server

> gradle client

> insert -t=btc_1min -p=path_to_csv_dir\BTCUSD_1MIN.csv

---

### Example 2 - creating columns with indicators

The commands below will create a column with the RSI indicator with a period of 14 for the table btc_1min.

> gradle server

> gradle client

> st -t=btc_1min -i=rsi:14

---

### Example 3 - backtesting a strategy

> gradle server

> gradle client

> bt -t=btc -st=path_to_json_setup_dir\setup_1.json -s=path_to_strategy_dir\rsi_strategy.json
> -r=path_to_result_dir\trades.json

> [!IMPORTANT]
> Strategy columns must be present in the table before running the backtest commands. Otherwise, the backtest will not work.

### Example 4 - setting up the database configuration

> gradle server

> gradle client

> config -u=URL -p=PASSWORD -n=USERNAME

- The URL should be in the format: `jdbc:postgresql://<host>:<port>/<database_name>`
- For example: `jdbc:postgresql://localhost:5432/postgres`
- Without this step, the application will not be able to connect to the database.

---

### CSV Format

- You can find an example in ./assets/csv/BTCUSD_1D.csv

**Schema**

| id INT | timestamp BIGINT | open DOUBLE PRECISION | high DOUBLE PRECISION | low DOUBLE PRECISION | close DOUBLE PRECISION | volume DOUBLE PRECISION | date TIMESTAMP |
|--------|------------------|-----------------------|-----------------------|----------------------|------------------------|-------------------------|----------------|

**Example**

| id  | date                | open   | high   | low    | close  | volume     | timestamp  |
|-----|---------------------|--------|--------|--------|--------|------------|------------|
| 1   | 2014-11-28 00:00:00 | 363.59 | 381.34 | 360.57 | 376.28 | 3220878.18 | 1417132800 |
| 2   | 2014-11-29 00:00:00 | 376.42 | 386.60 | 372.25 | 376.72 | 2746157.05 | 1417219200 |
| ... | ...                 | ...    | ...    | ...    | ...    | ...        | ...        |

---

## Creating instrument columns

- If you want to backtest a strategy based on an instrument, you need to create a column with the instrument values.
- You can do this by using the command:
    - `st -t=btc_1min -i=rsi:14`
    - `st -t=btc_1min -i=sma:30`
    - `st -t=btc_1min -i=macd:12,26,9,10` - instrument arguments are separated by commas

---

## Backtesting a strategy

### Running a backtest

- You can run a backtest using the command:
    `bt -t=btc_1min -st=path_to_json_setup_dir\setup_1.json -s=path_to_strategy_dir\rsi_strategy.json -r=path_to_result_dir\trades.json`

### Setup file

- You can find an example in ./assets/setup/setup_1.json
- Below is an example of a setup file:
    ```json
    {
        "balance": 10000.0,
        "leverage": 12.0,
        "fee": 0.0025,
        "takeProfit": 1.01,
        "stopLoss": 0.99,
        "amount": 500.0,
        "maxTrades": 5,
        "delaySeconds": 10800,
        "dateRestriction": "",
        "tradeLifeSpanSeconds": 90000
    }
    ```
- All the field are mandatory.
- **Fields**:
  - balance: The initial balance of the trading account.
  - leverage: The leverage to be used for trading.
  - fee: The trading fee charged by the broker. It is multiplier of the price. e.g. 0.0025 means 0.25% fee.
  - takeProfit: The take profit level for each trade (as a multiplier of the entry price).
  - stopLoss: The stop loss level for each trade (as a multiplier of the entry price).
  - amount: The amount to be traded in each trade.
  - maxTrades: The maximum number of concurrent open trades.
  - delaySeconds: The delay in seconds between each trade.
  - dateRestriction: A date restriction for trading. It is a SQL WHERE clause. 
    e.g. `WHERE date >= '2023-07-19 00:00:00.000000' AND date <= '2023-07-30 00:00:00.000000'`
  - tradeLifeSpanSeconds: The maximum lifespan of a trade in seconds. After this time, the trade will be closed for actual price.

---

### Strategy file
- You can find an example in ./assets/strategy/rsi_strategy.json
- Below is an example of a strategy file:
    ```json
    {
      "openClause": "WHERE rsi_14_ins_ <= 30",
      "closeClause": "WHERE rsi_14_ins_ >= 80"
    }
    ```

- **Fields**: 
- openClause: The SQL WHERE clause for opening a trade. It is used to filter the rows in the table.
- closeClause: The SQL WHERE clause for closing a trade. It is used to filter the rows in the table.

> [!IMPORTANT] 
> The instrument names in clauses are in format: `<instrument_name>_<arguments>_ins_`. You can always check column names
> in the database to see the correct names.

---

### Result file
- You can find an example in ./assets/result/trades.json
- It is a simple serialization of a List of instances of `Trade` class.

---

## Contact

- If you have any questions or suggestions, feel free to contact me at email: `david.valek17@gmail.com`
