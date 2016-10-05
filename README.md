gvgai
=====

This is the framework for the General Video Game Competition 2014 - http://www.gvgai.net/

Google group - https://groups.google.com/forum/#!forum/the-general-video-game-competition

## FAQs / Troubleshooting

**1. How do I upload my controller? What files or folder structure do I need? 
First of all, your controller ```Agent.java``` and any auxiliary files you create should be in a single package folder with your username. For example, if your username is "abc", you should have a package folder named "abc" in the project. Your entire project layout should look something like this:

```groovy
- abc
	|- Agent.java
	|- MyAdditionalFile1.java
	|- MyAdditionalFile2.java
- controllers
- core
- ontology
- tools
```

Then, all you need to do is to zip and upload the "abc" folder. No other folders/files are necessary.


**2. I am getting the error `javac1.8 class not found` when running Eclipse and ANT on build.xml**
This is likely because the ANT version that is installed with your version of Eclipse is old. You can easily fix this problem by doing the following:

- Download the archive of the [latest version of ANT](http://ant.apache.org/bindownload.cgi) (Tested with  Ant 1.9.4)
- Extract the archive onto a local folder on your computer (e.g., /Users/gvgai/ant/apache-ant-1.9.4/)
- In Eclipse, go to Eclipse -> Preferences -> Ant -> Runtime
- Click on "Ant Home'' button on the right.
- Select the folder, which you extracted ANT into (e.g., /Users/gvgai/ant/apache-ant-1.9.4/)

