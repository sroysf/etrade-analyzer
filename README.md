# Overview

This is a small utility that takes multiple ETrade portfolio export files and aggregates them into a single output file for analysis.

Investments that appear in multiple accounts show up as a single aggregate amount.

Similarly, the investments are categorized based on the category definitions in resources/categories.props. The category totals are also
aggregated and added to the output file.

To obtain a portfolio export file, click on an account, then click the "Export to Excel" button.

- Categories are defined in /src/main/resources/categories.props
- See EtradeExportFilesProcessor.java for hard coded file paths

The intent is to then be able to quickly do further overall portfolio analysis by importing the resulting file into Excel. 

# Build

Build the project with

    $ mvn install

Once the project is installed in your local repository, you can add it as a dependency in other projects using normal maven dependency declarations.

# Run

	$ sh target/bin/appmain