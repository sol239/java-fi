# java-fi

java-fi is a client-server application that allows backtesting of trading strategies using historical data.
Various indicators are available for use, and the application is designed to be extensible, allowing users
to add their own indicators and strategies.

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
    ```

5. Run the client using:
    ```bash
    gradle client
    ```

---

### Example 0 - running the server and client

> [!IMPORTANT]  
> The client will not connect to the server neither it will start server if the server is not running.

```bash
gradle server
```

```bash
gradle client
```

> [!TIP]
> Use this instead to not print gradle additional information: `gradle client --console=plain`


---

> [!NOTE]
> All examples below are run in the client console. The server must be running before running the client.
> Examples below use the demo data located in the `assets` directory. You can use your own data by replacing the paths.

---

### Example 1 - help command

> help 

### Example 2 - inserting historical data

> insert -t=btc -p=assets/csv/BTCUSD_1MIN.csv

---

### Example 3 - printing the table

> tb btc

---

### Example 4 - checking the connection to the server

> cn

---

### Example 5 - checking the connection to the database

> db

---

### Example 6 - printing available instruments

> inst

---

### Example 7 - printing the help for a specific command

> insert -h

or:

> insert --help

---

### Example 8 - creating columns with indicators

The commands below will create a column with the RSI indicator with a period of 14 for the table btc.

> st -t=btc -i=rsi:14

---

### Example 9 - backtesting a strategy


> bt -t=btc -st=assets\setups\setup_1.json -s=assets\strategies\rsi_strategy.json -r=assets\trades\trades.json

The output of the given command should be:
```text
Winning trades: 18 | Losing trades: 23 | Total trades: 41 | Win rate: 43.90% | Profit: 11165.89 USD
```

> [!IMPORTANT]
> Strategy columns must be present in the table before running the backtest commands. Otherwise, the backtest will not work.
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
    - `st -t=btc -i=rsi:14`
    - `st -t=btc -i=sma:30`
    - `st -t=btc -i=macd:12,26,9,10` - instrument arguments are separated by commas
- You can see how to correctly use instruments by using the command `inst` in the client console.
---

## Backtesting a strategy

### Running a backtest

- You can run a backtest using the command:
    `bt -t=btc -st=assets\setups\setup_1.json -s=assets\strategies\rsi_strategy.json -r=assets\trades\trades.json`
- **Fields**:
  - `-t`: The name of the table to be used for backtesting.
  - `-st`: The path to the setup file.
  - `-s`: The path to the strategy file.
  - `-r`: The path to the result file.
- Below you can find the description of the setup and strategy files.
---

### .env file

- `.env` is located in the root directory of the project.

    ```text
    PORT=1100
    DB_URL=jdbc:h2:./java_fi
    ```
- **Fields**:
  - `PORT`: The port on which the server will run. Default is 1100.
  - `DB_URL`: The URL of the database. Default is `jdbc:h2:./java_fi`.

---

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
      "closeClause": "WHERE rsi_14_ins_ >= 80",
      "stopLoss": false,
      "takeProfit": true
    }
    ```

- **Fields**: 
- `openClause`: The SQL WHERE clause for opening a trade. It is used to filter the rows in the table.
- `closeClause`: The SQL WHERE clause for closing a trade. It is used to filter the rows in the table.
- `stopLoss`: A boolean value indicating whether to use stop loss or not.
- `takeProfit`: A boolean value indicating whether to use take profit or not.

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

> [!IMPORTANT]  
> To be implemented in Advanced Java course.
> - Spring Boot Backend as server
> - JavaScript Frontend as client
>   - Better visualization
>   - Better user experience
> - Using Advanced Java course techniques (Annotations, Modules, etc.)
> - Focus on performance - reworking the entire codebase where necessary
> - Dockerize the application
> - and more ...