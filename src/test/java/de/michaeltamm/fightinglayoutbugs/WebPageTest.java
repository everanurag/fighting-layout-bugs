package de.michaeltamm.fightinglayoutbugs;

import org.junit.Test;

import java.util.Collection;

import static de.michaeltamm.fightinglayoutbugs.HamcrestHelper.assertThat;
import static de.michaeltamm.fightinglayoutbugs.HamcrestHelper.is;
import static de.michaeltamm.fightinglayoutbugs.TestHelper.asList;

public class WebPageTest extends TestUsingSelenium {

    @Test
    public void testGetRectangularRegions() {
        WebPage pageWithFlashMovie = getWebPageFor("/page_with_flash_movie.html");
        Collection<RectangularRegion> rectangularRegions = pageWithFlashMovie.getRectangularRegionsCoveredBy(asList("embed"));
        assertThat(rectangularRegions.size(), is(1));
        RectangularRegion flashMovie = rectangularRegions.iterator().next();
        assertThat(flashMovie.x1, is(50));
        assertThat(flashMovie.y1, is(110));
        assertThat(flashMovie.x2, is(50 + 793 - 1));
        assertThat(flashMovie.y2, is(110 + 225 - 1));
    }

}
