# RuleBuilder

RuleBuilder (current version 2.0.2) is a Java application for visualization of rules written in the [BioNetGen language](http://www.bionetgen.org) (BNGL).

### Compiling RuleBuilder

RuleBuilder is built using [Apache Maven](https://maven.apache.org/) and Java v1.6 or greater.  Simply navigate to the RuleBuilder root directory and execute

``mvn clean compile package``

The resulting executable JAR file can be found in the ``target/`` subdirectory as ``RuleBuilder-2.0.2.jar``

### Running RuleBuilder
This file can be executed from a Unix-like terminal with the command 

``java -jar RuleBuilder-2.0.2.jar``

Alternatively, navigating to the target/ directory and clicking the icon should initialize the program.

### Using RuleBuilder
Once the program is running, BNGL strings can be written in the text box at the bottom of the GUI, or the user can construct visual representations of BNGL patterns or rules using the buttons at the top of the GUI
