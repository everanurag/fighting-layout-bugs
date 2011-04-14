package de.michaeltamm.fightinglayoutbugs;

import org.junit.Test;

import static de.michaeltamm.fightinglayoutbugs.HamcrestHelper.assertThat;
import static de.michaeltamm.fightinglayoutbugs.HamcrestHelper.is;

public class WebPageTest extends TestUsingSelenium {

    @Test
    public void test() {
        WebPage pageWithFlashMovie = getWebPageFor("/page_with_flash_movie.html");
        boolean[][] flashMoviePixels = pageWithFlashMovie.getFlashMovieAndIframePixels();
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
