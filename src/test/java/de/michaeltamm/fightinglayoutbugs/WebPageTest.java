package de.michaeltamm.fightinglayoutbugs;

import org.testng.annotations.Test;

import static de.michaeltamm.fightinglayoutbugs.HamcrestHelper.*;

public class WebPageTest extends TestUsingSelenium {

    @Test
    public void testGetFlashMoviePixelsWithChrome() {
        WebPage pageWithFlashMovie = getWebPageFor("/page_with_flash_movie.html").usingChromeDriver();
        checkPageWithFlashMovie(pageWithFlashMovie);
    }

    @Test
    public void testGetFlashMoviePixelsWithFirefox() {
        WebPage pageWithFlashMovie = getWebPageFor("/page_with_flash_movie.html").usingFirefoxDriver();
        checkPageWithFlashMovie(pageWithFlashMovie);
    }

    @Test
    public void testGetFlashMoviePixelsWithInternetExplorer() {
        WebPage pageWithFlashMovie = getWebPageFor("/page_with_flash_movie.html").usingInternetExplorerDriver();
        checkPageWithFlashMovie(pageWithFlashMovie);
    }

    @Test
    public void testGetFlashMoviePixelsWithDefaultSelenium() {
        WebPage pageWithFlashMovie = getWebPageFor("/page_with_flash_movie.html").usingDefaultSelenium();
        checkPageWithFlashMovie(pageWithFlashMovie);
    }

    private void checkPageWithFlashMovie(WebPage testPage) {
        boolean[][] flashMoviePixels = testPage.getFlashMoviePixels();
        assertThat(flashMoviePixels[45][105], is(false));
        assertThat(flashMoviePixels[55][105], is(false));
        assertThat(flashMoviePixels[45][115], is(false));
        assertThat(flashMoviePixels[55][115], is(true));
        assertThat(flashMoviePixels[49][109], is(false));
        assertThat(flashMoviePixels[50][109], is(false));
        assertThat(flashMoviePixels[49][110], is(false));
        assertThat(flashMoviePixels[50][110], is(true));
    }
}
