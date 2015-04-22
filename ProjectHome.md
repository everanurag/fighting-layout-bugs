This is the home page of _Fighting Layout Bugs_ - a library for automatic detection of layout bugs in web pages.

It was publicly announced on 20th of October 2009 at the Google Test Automation Conference in Zurich:

http://www.youtube.com/watch?v=WY3C6FHqSqQ

Other presentations:
  * [QCon London, September 2010](http://www.infoq.com/presentations/Fighting-Layout-Bugs)
  * [webinale, June 2011 (german)](http://www.slideshare.net/MichaelTamm/fighting-layout-bugs)
  * [Java User Group Berlin Brandenburg, November 2011 (german)](http://www.slideshare.net/MichaelTamm/fighting-layout-bugs-jugbb-2011)

It can be used with Firefox, Google Chrome, Safari, and Internet Explorer via the [WebDriver](http://selenium.googlecode.com/svn/trunk/docs/api/java/org/openqa/selenium/WebDriver.html) implementations provided by [Selenium 2](http://selenium.googlecode.com/).

## What does it offer? ##

Currently there are the following detectors:

  * DetectInvalidImageUrls
  * DetectTextNearOrOverlappingHorizontalEdge
  * DetectTextNearOrOverlappingVerticalEdge
  * DetectTextWithTooLowContrast
  * DetectElementsWithInvisibleFocus (not enabled by default)

## How to get started? ##

  * If you use [Maven](http://maven.apache.org), add the following dependency to your `pom.xml` file:
```
    <dependency>
        <groupId>com.googlecode.fighting-layout-bugs</groupId>
        <artifactId>fighting-layout-bugs</artifactId>
        <version>0.6</version>
    </dependency>
```
Alternatively if you do not use Maven, you can download the [fighting-layout-bugs-0.6-jar-with-dependencies.jar](https://drive.google.com/file/d/0B2szJKihLd7iRkYwYnpGTEhUalE/view) and add it to the classpath of your Java project. It includes all needed dependencies like the Selenium 2.44 classes and resources.
  * Create a test like this:
```
    FirefoxDriver driver = new FirefoxDriver();
    try {
        String testPageUrl = "http://www.test.de/";
        driver.get(testPageUrl);
        WebPage webPage = new WebPage(driver);
        FightingLayoutBugs flb = new FightingLayoutBugs();
        final Collection<LayoutBug> layoutBugs = flb.findLayoutBugsIn(webPage);
        System.out.println("Found " + layoutBugs.size() + " layout bug(s).");
        for (LayoutBug bug : layoutBugs) {
            System.out.println(bug);
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        driver.quit();
    }
```

## Feedback ##

Feedback is highly appreciated. Let us know, if you experience any problems like:
  * a detector throws an exception,
  * you got a false alarm,
  * you have a layout bug, which is not found.
Just drop us a note: [mailto:fighting-layout-bugs@googlegroups.com](mailto:fighting-layout-bugs@googlegroups.com)

Of course contributions are welcome too, if you like.