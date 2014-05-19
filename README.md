gvgai
=====

This is the framework for the General Video Game Competition 2014 - http://www.gvgai.net/

Google group - https://groups.google.com/forum/#!forum/the-general-video-game-competition

## FAQs / Troubleshooting

**1. I am getting the error `javac1.8 class not found` when running Eclipse and ANT on build.xml**
This is likely because the ANT version that is installed with your version of Eclipse is old. You can easily fix this problem by doing the following:

- Download the archive of the [latest version of ANT](http://ant.apache.org/bindownload.cgi) (Tested with  Ant 1.9.4)
- Extract the archive onto a local folder on your computer (e.g., /Users/gvgai/ant/apache-ant-1.9.4/)
- In Eclipse, go to Eclipse -> Preferences -> Ant -> Runtime
- Click on "Ant Home'' button on the right.
- Select the folder, which you extracted ANT into (e.g., /Users/gvgai/ant/apache-ant-1.9.4/)

