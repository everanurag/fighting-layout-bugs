package com.googlecode.fightinglayoutbugs.helpers;

import org.junit.Test;

import java.awt.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RectangularRegionUnitTest {

    @Test
    public void testAsRectangle() {
        RectangularRegion rr = new RectangularRegion(10, 20, 15, 27);
        Rectangle r = rr.asRectangle();
        assertThat(r.x, is(10));
        assertThat(r.y, is(20));
        assertThat(r.width, is(6));
        assertThat(r.height, is(8));
    }
}
