# Subtitle Player (*VTT)
Subtitle Player is a JavaFX application that allows you to play video files with synchronized subtitles. This README provides instructions on how to run the compiled JAR file of the application.

# Prerequisites
To run the Subtitle Player JAR file, you need to have the following installed on your machine:

* Java Development Kit (JDK)
* JavaFX SDK

# Installation
1. Install the JDK on your machine if you haven't already. You can download it from the official Oracle website: https://www.oracle.com/java/technologies/javase-jdk11-downloads.html
2. Install the JavaFX SDK by following the instructions provided by Oracle. You can download it from the official JavaFX website: https://openjfx.io/

# Usage
1. Clone this repository or download the source code.

2. Compile the source code using the following command:  
`javac --module-path /path/to/javafx-sdk-<version>/lib --add-modules javafx.controls,javafx.fxml -d out src/com/example/Program.java`  
Replace /path/to/javafx-sdk-<version>/lib with the actual path to the lib directory of your JavaFX SDK installation.

3. Navigate to the out directory where the compiled .class files are located.

4. Create a manifest file named manifest.txt and add the following content:  
`Main-Class: com.example.Program`  
Replace com.example.Program with the actual package and class name of the main class in your project.

5. Create a JAR file using the following command:  
`jar cvfm SubtitlePlayer.jar manifest.txt com/example/*.class`
Replace SubtitlePlayer.jar with the desired name for your JAR file.

6. To run the compiled JAR file, use the following command:  
`java --module-path /path/to/javafx-sdk-<version>/lib --add-modules javafx.controls,javafx.fxml -jar SubtitlePlayer.jar`  
Replace /path/to/javafx-sdk-<version>/lib with the actual path to the lib directory of your JavaFX SDK installation.  
Make sure to provide the correct path to the JavaFX SDK and specify the main class using the -jar option.

# Contributing
Contributions to the Subtitle Player project are welcome. If you encounter any issues or have suggestions for improvements, please feel free to open an issue or submit a pull request.  
### by Masalytin