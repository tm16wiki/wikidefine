# Wikidefine

Wikidefine extracts well formed definition texts from Wikipedia dump files or via web crawler.

## Getting Started

Just clone the repo, run maven and you're ready to go.

### Prerequisites

You need to have a JDK (we tested with 1.8), git and maven installed. Beware when using openjdk you'll need the openjfx package also installed.

### Installing

Clone the repository:
```
git clone git@github.com:tm16wiki/wikidefine.git
```

Then run maven to download the dependencies:
```
cd wikidefine
mvn install
```

You can now run Wikidefine in your shell:
```
java -jar target/WikiDefine-*.jar
```

or start the GUI:
```
java -jar target/WikiDefine-*.jar -gui
```

## Using Wikidefine

There are two ways to use Wikidefine: Command line and GUI.

## Command line

To start Wikidefine in your shell run
```
java -jar target/WikiDefine-*.jar
```

### Configuration

You will need to create a new definition at first.
Please name it default as the default configuration will be selected the next time you run Wikidefine.
Then please specify the language of your Wikipedia dumps (de|en). Afterwards please type in the absolute filepath to the Wikipedia XML dump file.
You will then be asked for a database. Type in the address of your PostgreSQL database or the filepath to your sqlite database.
It's also possible to enter a filepath to a sqlite database which does not exist yet - Wikidefine will automatically create a new sqlite database.
To finalize the database configuration you will need to enter the database user and password unless you selected a sqlite database.

### Commands - File dump extraction

After you finished your configuration you can run `fd` to start the filedump extractor. There are following configuration options available:
|Option|Description|Command|Default value|Example|
|---	|---	|---	|---	|---	|
|threads|Number of used threads.|st|4|st 4|
|maximum|Maximum number of definitions to extract.|sm|Integer.MAX_VALUE|sm 5000|
|filepath|Path to Wikipedia XML dump file|sp|filepath specified in main configuration|sp /home/user/wikidefine/dewikidump.xml|
|db path|Path to database|Not changeable|database path specified in main configuration|Not changeable (please edit the main configuration)|
|db export|Specifies whether or not the definitions are stored in the database.|se|true|se|
|verbose|Displays accepted and rejected definitions when true|sv|true|sv|
|show stats|Shows running time, number of prefiltered pages, number of accepted definitions and number of rejected definitions after completing.|ss|false|ss|

When the configuration is ready please run `run` to start the extraction process.

To go back to the main menu please enter `exit`.

### Commands - Web definition crawler

Run `wd` from main menu to enter the web definition crawler subshell. There is only one configuration option available.

|Option|Description|Command|Default value|Example|
|---	|---	|---	|---	|---	|
|language|Wikipedia location to crawl from.|sl|language specified in main configuration|sl en|

To crawl a Wikipedia page just enter `d "page to crawl"`, for example: `d Wikipedia`. If your term consists of two or more words please use quotation marks, for example: `d "hard disk drive"`.

To go back to the main menu please enter `exit`.

## GUI

To start Wikidefine in your shell run
```
java -jar target/WikiDefine-*.jar
```

### Configuration

In the first input field you will need to specify the path to the Wikipedia XML dump file. Press "Open" to open a file select dialogue.
Under that you can specify the maximum number of pages to extract, turn off statistics, verbose output and database export. On the right size you can select the number of thread to use and your desired language.
After you configured your database (if database export is selected) you can press the Run-button on the right hand to start the extraction process. You will see a progress bar on the bottom border.

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* René Brückner
* Christian Frommert

## License

This project is licensed under the MIT License - see the [LICENSE.MD](LICENSE.MD) file for details.
