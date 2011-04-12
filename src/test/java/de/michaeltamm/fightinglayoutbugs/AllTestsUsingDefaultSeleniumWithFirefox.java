package de.michaeltamm.fightinglayoutbugs;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Michael Tamm
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SimpleTextDetectorTest.class,
    AnimationAwareTextDetectorTest.class,
    SimpleEdgeDetectorTest.class,
    DetectInvalidImageUrlsTest.class,
    DetectNeedsHorizontalScrollingTest.class,
    DetectTextNearOrOverlappingVerticalEdgeTest.class,
    DetectTextWithTooLowContrastTest.class,
    WebPageTest.class
})
public class AllTestsUsingDefaultSeleniumWithFirefox {

    @BeforeClass
    public static void freezeTestWebPageFactory() {
        TestWebPageFactory.UsingDefaultSeleniumWithFirefox.freeze();
    }

    @AfterClass
    public static void meltTestWebPageFactory() {
        TestWebPageFactory.UsingDefaultSeleniumWithFirefox.melt();
    }

}
