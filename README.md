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

- Java 17 or higher
- PostgreSQL
- Gradle

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/sol239/java-fi
   ```
   
## Usage

1. run ServerParalel.java
2. run Client.java
3. run the commands in the client by typing them in the console and then pressing enter.

### Example 1 - inserting historical data

> run ServerParalel.java

> run Client.java

> insert -t=btc_1min -p=C:\csv\PROCESSED\BTCUSD_1MIN.csv

---

### Example 2 - creating columns with indicators

The command below will create a column with the RSI indicator with a period of 14 for the table btc_1min.

> run ServerParalel.java

> run Client.java
> st -t=btc_1min -i=rsi:14

---

### Example 3 - backtesting a strategy

> run ServerParalel.java

> run Client.java

> bt -t=btc -st=C:\Users\david_dyn8g78\IdeaProjects\java-fi\assets\setups\setup_1.json -s=C:\Users\david_dyn8g78\IdeaProjects\java-fi\assets\strategies\rsi_strategy.json -r=C:\Users\david_dyn8g78\IdeaProjects\java-fi\assets\results\trades.json

<div style="border-left: 4px solid orange; padding: 10px;">
  <strong>⚠️ Warning:</strong> Strategy columns must be present in the table before running the backtest command. Otherwise, the backtest will not work.
</div>

---

## Commands

### `bt`
**Usage:** `bt [OPTION]...`
The command to backtest strategies. For more info look into `USER.md`.
Does not support backtesting multiple tables at once.
**Example:** `TODO`
**Options:**
* `-h`, `--help`: Show help message.
* `-t=TABLE_NAME`, `--tables=TABLE_NAME`: Specify the table to backtest.
* `-s=STRATEGY_PATH`, `--strategy=STRATEGY_PATH`: Specify the path to the strategy file.

### `clean`
**Usage:** `clean [OPTION]...`
The command cleans the database - removes ALL strategy and indicator columns.
**Options:**
* `-h`, `--help`: Show help message.

### `cn`
**Usage:** `cn [OPTION]...`
Checks the connection to the server.
**Options:**
* `-h`, `--help`: Show help message.

### `config`
**Usage:** `config [OPTION]...`
The command displays the current configuration.
**Options:**
* `-h`, `--help`: Show help message.
* `-u=URL`, `--url=URL`: Set the server URL.
* `-p=PASSWORD`, `--password=PASSWORD`: Set the server password.
* `-n=USERNAME`, `--username=USERNAME`: Set the server username.

### `db`
**Usage:** `db [OPTION]...`
The command to check connection to the database.
**Options:**
* `-h`, `--help`: Show help message.

### `del`
**Usage:** `del [OPTION]... [TABLE]...`
The command to delete specified `TABLE`(s) from the database.
**Options:**
* `-h`, `--help`: Show help message.

### `exit`
**Usage:** `exit [OPTION]...`
The command to exit the application and close the connection to the server.
**Options:**
* `-h`, `--help`: Show help message.

### `help`
**Usage:** `help [OPTION]...`
The command prints the help information for the application.
**Options:**
* `-h`, `--help`: Show help message.

### `insert`
**Usage:** `insert [OPTION]...`
The command inserts data from a CSV file to the database.
**Options:**
* `-h`, `--help`: Show help message.
* `-t`, `--table=TABLE_NAME`: Specify the target table name.
* `-p`, `--path=CSV_FILE_PATH`: Specify the path to the CSV file.

### `st`
**Usage:** `st [OPTION]...`
The command updates the tables with calculated instrument values.
**Example:** `st -t=btc_d,solx -i=rsi:14;sma:30`
**Options:**
* `-h`, `--help`: Show help message.
* `-t=TABLE_NAME1,TABLE_NAME2,...`, `--tables=TABLE_NAME1,TABLE_NAME2,...`: Specify the tables to update.
* `-i=INS_CMD1,INS_CMD2,...`, `--instruments=INS_CMD1,INS_CMD2,...`: Specify the instruments to calculate (e.g., `rsi:14`, `sma:30`).

### `tb`
**Usage:** `tb`
The command to show all available tables.

