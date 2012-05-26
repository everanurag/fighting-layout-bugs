/*
 * Copyright 2009-2012 Michael Tamm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.fightinglayoutbugs.helpers;

import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static com.googlecode.fightinglayoutbugs.helpers.FileHelper.createParentDirectoryIfNeeded;

/**
 * @author Michael Tamm
 */
public class ImageHelper {

    public static BufferedImage urlToImage(URL imageUrl) {
        BufferedImage image;
        try {
            image = ImageIO.read(imageUrl);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image from URL: " + imageUrl, e);
        }
        return image;
    }

    public static BufferedImage fileToImage(File imageFile) {
        BufferedImage image;
        try {
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image file: " + imageFile, e);
        }
        return image;
    }

    public static int[][] urlToPixels(URL imageUrl) {
        BufferedImage image = urlToImage(imageUrl);
        int[][] pixels = imageToPixels(image);
        return pixels;
    }

    public static int[][] fileToPixels(File imageFile) {
        BufferedImage image = fileToImage(imageFile);
        int[][] pixels = imageToPixels(image);
        return pixels;
    }

    public static int[][] pngToPixels(byte[] png) {
        InputStream in = new ByteArrayInputStream(png);
        try {
            BufferedImage image = ImageIO.read(in);
            return imageToPixels(image);
        } catch (IOException e) {
            throw new RuntimeException("Should never happen.", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static void pixelsToPngFile(int[][] pixels, File pngFile) {
        BufferedImage image = pixelsToImage(pixels);
        createParentDirectoryIfNeeded(pngFile);
        imageToPngFile(image, pngFile);
    }

    public static void imageToPngFile(BufferedImage image, File pngFile) {
        try {
            ImageIO.write(image, "png", pngFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write image to file: " + pngFile, e);
        }
    }

    public static void pixelsToPngFile(boolean[][] pixels, File pngFile) {
        BufferedImage image = pixelsToImage(pixels);
        createParentDirectoryIfNeeded(pngFile);
        imageToPngFile(image, pngFile);
    }

    public static int[][] imageToPixels(BufferedImage image) {
        if (image == null) {
            return null;
        }
        int w = image.getWidth();
        int h = image.getHeight();
        int[][] pixels = new int[w][h];
        Raster raster = image.getRaster();
        if (raster.getTransferType() == DataBuffer.TYPE_BYTE) {
            byte[] bytes = (byte[]) raster.getDataElements(0, 0, w, h, null);
            int bytesPerPixel = (bytes.length / (w*h));
            ColorModel colorModel = image.getColorModel();
            byte[] buf = new byte[bytesPerPixel];
            for (int x = 0; x < w; ++x) {
                for (int y = 0; y < h; ++y) {
                    System.arraycopy(bytes, (x + y * w) * bytesPerPixel, buf, 0, bytesPerPixel);
                    pixels[x][y] = colorModel.getRGB(buf) & 0xFFFFFF;
                }
            }
            return pixels;
        } else {
            throw new RuntimeException("transfer type " + raster.getTransferType() + " not implemented yet");
        }
    }

    public static BufferedImage pixelsToImage(int[][] pixels) {
        if (pixels == null) {
            return null;
        }
        int w = pixels.length;
        if (w > 0) {
            int h = pixels[0].length;
            if (h > 0) {
                BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < w; ++x) {
                    int[] column = pixels[x];
                    for (int y = 0; y < h; ++y) {
                        image.setRGB(x, y, column[y]);
                    }
                }
                return image;
            } else {
                throw new IllegalArgumentException("pixels[0].length must not be 0.");
            }
        } else {
            throw new IllegalArgumentException("pixels.length must not be 0.");
        }
    }

    public static BufferedImage pixelsToImage(boolean[][] pixels) {
        if (pixels == null) {
            return null;
        }
        int w = pixels.length;
        if (w > 0) {
            int h = pixels[0].length;
            if (h > 0) {
                BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < w; ++x) {
                    boolean[] column = pixels[x];
                    for (int y = 0; y < h; ++y) {
                        image.setRGB(x, y, column[y] ? 0 : 0xFFFFFF);
                    }
                }
                return image;
            } else {
                throw new IllegalArgumentException("pixels[0].length must not be 0.");
            }
        } else {
            throw new IllegalArgumentException("pixels.length must not be 0.");
        }
    }

    public static int[][] copyOf(int[][] pixels) {
        int[][] copy;
        if (pixels == null) {
            copy = null;
        } else {
            int w = pixels.length;
            if (w == 0) {
                copy = new int[0][];
            } else {
                int h = pixels[0].length;
                copy = new int[w][h];
                if (h > 0) {
                    for (int x = 0; x < w; ++x) {
                        System.arraycopy(pixels[x], 0, copy[x], 0, h);
                    }
                }
            }
        }
        return copy;
    }

    /**
     * Returns {@code null} if the given subImage can not be found in the given image.
     */
    @Nullable
    public static RectangularRegion findFirstSubImageInImage(@Nonnull BufferedImage subImage, @Nonnull BufferedImage image) {
        List<RectangularRegion> temp = findSubImageInImage(subImage, image, 1);
        return (temp.isEmpty() ? null : temp.get(0));
    }

    /**
     * Returns all rectangular regions where the given {@code subImage} is found in the given {@code image}.
     * Returns an empty collection if no occurrence is found.
     * Pixels of {@code subImage} with an alpha value lower than 255 are ignored.
     */
    @Nonnull
    public static Collection<RectangularRegion> findSubImageInImage(@Nonnull BufferedImage subImage, @Nonnull BufferedImage image) {
        return findSubImageInImage(subImage, image, Integer.MAX_VALUE);
    }

    private static List<RectangularRegion> findSubImageInImage(BufferedImage subImage, BufferedImage image, int max) {
        Map<Integer, List<Point>> rgb2offsets = new HashMap<Integer, List<Point>>();
        int sw = subImage.getWidth();
        int sh = subImage.getHeight();
        for (int x = 0; x < sw; ++x) {
            for (int y = 0; y < sh; ++y) {
                int argb = subImage.getRGB(x, y);
                int a = argb >>> 24;
                if (a == 255) {
                    Integer rgb = argb & 0xFFFFFF;
                    List<Point> offsets = rgb2offsets.get(rgb);
                    if (offsets == null) {
                        offsets = new ArrayList<Point>();
                        rgb2offsets.put(rgb, offsets);
                    }
                    offsets.add(new Point(x, y));
                }
            }
        }
        List<RectangularRegion> result = new ArrayList<RectangularRegion>();
        int w = image.getWidth();
        int h = image.getHeight();
        int[][] p = new int[w][h];
        Raster raster = image.getRaster();
        if (raster.getTransferType() == DataBuffer.TYPE_BYTE) {
            byte[] bytes = (byte[]) raster.getDataElements(0, 0, w, h, null);
            int bytesPerPixel = (bytes.length / (w * h));
            ColorModel colorModel = image.getColorModel();
            byte[] buf = new byte[bytesPerPixel];
            for (int x = 0; x < w; ++x) {
                for (int y = 0; y < h; ++y) {
                    System.arraycopy(bytes, (x + y * w) * bytesPerPixel, buf, 0, bytesPerPixel);
                    p[x][y] = colorModel.getRGB(buf) & 0xFFFFFF;
                }
            }
        } else if (raster.getTransferType() == DataBuffer.TYPE_INT) {
            for (int x = 0; x < w; ++x) {
                p[x] = (int[]) raster.getDataElements(x, 0, 1, h, null);
            }
        } else {
            throw new RuntimeException("findSubImageInImage not implemented for image transfer type " + raster.getTransferType() + " yet.");
        }
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                Iterator<Map.Entry<Integer, List<Point>>> i = rgb2offsets.entrySet().iterator();
                compareWithSubImageLoop:
                while (i.hasNext()) {
                    Map.Entry<Integer, List<Point>> mapEntry = i.next();
                    int expectedRgb = mapEntry.getKey();
                    for (Point offset : mapEntry.getValue()) {
                        int xx = x + offset.x;
                        int yy = y + offset.y;
                        if (xx >= w || yy >= h || expectedRgb != p[xx][yy]) {
                            break compareWithSubImageLoop;
                        }
                    }
                    if (!i.hasNext()) {
                        result.add(new RectangularRegion(x, y, x + (sw - 1), y + (sh - 1)));
                        if (result.size() == max) {
                            return result;
                        }
                    }
                }
            }
        }
        return result;
    }

    public static void applyConvolutionFilter(int[][] pixels, float[][] kernel) {
        if (kernel == null) {
            throw new IllegalArgumentException("Method parameter kernel must not be null.");
        }
        int kernelSize = kernel.length;
        if (kernelSize % 2 == 0) {
            throw new IllegalArgumentException("Method parameter kernel must have odd size. (e.g. 3, 5, 7, ...)");
        }
        if (kernel[0].length != kernelSize) {
            throw new IllegalArgumentException("Method parameter kernel must have square dimensions. (e.g. 3x3, 5x5, 7x7, ...)");
        }
        if (pixels != null) {
            int w = pixels.length;
            if (w > 0) {
                int h = pixels[0].length;
                if (h > 0) {
                    int[][] r = new int[w][h];
                    int[][] g = new int[w][h];
                    int[][] b = new int[w][h];
                    int[][] a = splitIntoChannels(pixels, r, g, b);
                    if (a != null) {
                        applyConvolutionFilterToChannel(a, kernel);
                    }
                    applyConvolutionFilterToChannel(r, kernel);
                    applyConvolutionFilterToChannel(g, kernel);
                    applyConvolutionFilterToChannel(b, kernel);
                    if (a != null) {
                        combineChannels(a, r, g, b, pixels);
                    } else {
                        combineChannels(r, g, b, pixels);
                    }
                }
            }
        }
    }

    public static void combineChannels(int[][] a, int[][] r, int[][] g, int[][] b, int[][] pixels) {
        int w = pixels.length;
        int h = pixels[0].length;
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                pixels[x][y] = (a[x][y] << 24) | (r[x][y] << 16) | (g[x][y] << 8) | b[x][y];
            }
        }
    }

    public static void combineChannels(int[][] r, int[][] g, int[][] b, int[][] pixels) {
        int w = pixels.length;
        int h = pixels[0].length;
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                pixels[x][y] = (r[x][y] << 16) | (g[x][y] << 8) | b[x][y];
            }
        }
    }

    /**
     * If <code>pixels</code> has alpha channel, another <code>int[][]</code> array is allocated,
     * filled with the alpha values, and returned, otherwise <code>null</code> is returned.
     */
    public static int[][] splitIntoChannels(int[][] pixels, int[][] r, int[][] g, int[][] b) {
        int[][] a = null;
        int w = pixels.length;
        int h = pixels[0].length;
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                int p = pixels[x][y];
                int alpha = p >>> 24;
                if (alpha > 0) {
                    if (a == null) {
                        a = new int[w][h];
                    }
                    a[x][y] = alpha;
                }
                r[x][y] = (p & 0xFF0000) >> 16;
                g[x][y] = (p & 0xFF00) >> 8;
                b[x][y] = (p & 0xFF);
            }
        }
        return a;
    }

    public static void applyConvolutionFilterToChannel(int[][] channel, float[][] kernel) {
        int w = channel.length;
        int h = channel[0].length;
        int kernelSize = kernel.length;
        int r = (kernelSize / 2);
        int xx, yy;
        float n, d;
        // for each column ...
        for (int x = 0; x < w; ++x) {
            // for each row ...
            for (int y = 0; y < h; ++y) {
                n = d = 0;
                // for each kernel column ...
                for (int i = 0; i < kernelSize; ++i) {
                    xx = x + (i - r);
                    if (0 <= xx && xx < w) {
                        // for each kernel row ...
                        for (int j = 0; j < kernelSize; ++j) {
                            yy = y + (j - r);
                            if (0 <= yy && yy < h) {
                                float k = kernel[i][j];
                                int oldValue = channel[xx][yy];
                                assert 0 <= oldValue && oldValue <= 255;
                                n += k * oldValue;
                                d += k;
                            }
                        }
                    }
                }
                int newValue = Math.round(n / d);
                assert 0 <= newValue && newValue <= 255;
                channel[x][y] = newValue;
            }
        }
    }

    public static void gaussianBlur(int[][] pixels, float sigma) {
        int kernelSize = (int) Math.ceil(6 * sigma);
        if (kernelSize % 2 == 0) {
            ++kernelSize;
        }
        if (kernelSize < 3) {
            kernelSize = 3;
        }
        float[][] kernel = new float[kernelSize][kernelSize];
        int m = kernelSize / 2;
        double q = 2 * sigma * sigma;
        double f = 1 / (Math.PI * q);
        for (int x = 0; x < kernelSize; ++x) {
            int dx = x - m;
            for (int y = 0; y < kernelSize; ++y) {
                int dy = y - m;
                kernel[x][y] = (float) (f * Math.exp(- ((dx * dx + dy * dy) / q)));
            }
        }
        applyConvolutionFilter(pixels, kernel);
    }

    /**
     * Blends pixelsWithAlpha into pixels.
     */
    public static void blend(int[][] pixels, int[][] pixelsWithAlpha) {
        if (pixels != null && pixelsWithAlpha != null) {
            int w = Math.min(pixels.length, pixelsWithAlpha.length);
            if (w > 0) {
                int h = Math.min(pixels[0].length, pixelsWithAlpha[0].length);
                if (h > 0) {
                    for (int x = 0; x < w; ++x) {
                        for (int y = 0; y < h; ++y) {
                            int p2 = pixelsWithAlpha[x][y];
                            int a = p2 >>> 24;
                            if (a < 0xFF) {
                                if (a == 0) {
                                    pixels[x][y] = p2;
                                } else {
                                    float a2 = ((float) (0xFF - a)) / 0xFF;
                                    assert 0 < a2 && a2 < 1;
                                    float a1 = 1 - a2;
                                    int p1 = pixels[x][y];
                                    int r = (p1 & 0xFF0000) >> 16;
                                    int g = (p1 & 0xFF00) >> 8;
                                    int b = (p1 & 0xFF);
                                    r = Math.round(a1 * r + a2 * ((p2 & 0xFF0000) >> 16));
                                    assert r <= 0xFF;
                                    g = Math.round(a1 * g + a2 * ((p2 & 0xFF00) >> 8));
                                    assert g <= 0xFF;
                                    b = Math.round(a1 * b + a2 * ((p2 & 0xFF)));
                                    assert b <= 0xFF;
                                    pixels[x][y] = (r << 16) | (g << 8) | b;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Find the outlines of all areas where <code>pixels[x][y]</code> is <code>true</code>.
     */
    public static boolean[][] findOutlines(boolean[][] pixels) {
        int w = pixels.length;
        int h = pixels[0].length;
        int w1 = w - 1;
        int h1 = h - 1;
        boolean[][] outlines = new boolean[w][h];
        // Find starting point ...
        int x0 = 0;
        int y0 = 0;
        // Look for starting point on top border ...
        while (x0 < w && pixels[x0][y0]) {
            // ... and bottom border ...
            if (!pixels[x0][h1]) {
                y0 = h1;
                break;
            }
            ++x0;
        }
        if (x0 == w) {
            // Look for starting point on left border ...
            x0 = 1;
            // ... and right border ...
            while (y0 < h && pixels[x0][y0]) {
                if (!pixels[w1][y0]) {
                    x0 = w1;
                    break;
                }
                ++y0;
            }
        }
        if (y0 == h) {
            // No starting point found, therefore ...
            return outlines;
        }
        // Find outlines ...
        Queue<Point> todo = new LinkedList<Point>();
        todo.add(new Point(x0, y0));
        boolean[][] visited = new boolean[w][h];
        while (!todo.isEmpty()) {
            Point p = todo.poll();
            int x = p.x;
            int y = p.y;
            if (!visited[x][y]) {
                visited[x][y] = true;
                if (!pixels[x][y]) {
                    // Compare with pixel above ...
                    if (y > 0) {
                        int y1 = y - 1;
                        if (pixels[x][y1]) {
                            outlines[x][y] = true;
                        } else if (!visited[x][y1]){
                            todo.add(new Point(x, y1));
                        }
                    }
                    // Compare with pixel to the right ...
                    if (x < w1) {
                        int x1 = x + 1;
                        if (pixels[x1][y]) {
                            outlines[x][y] = true;
                        } else if (!visited[x1][y]){
                            todo.add(new Point(x1, y));
                        }
                    }
                    // Compare with pixel below ...
                    if (y < h1) {
                        int y1 = y + 1;
                        if (pixels[x][y1]) {
                            outlines[x][y] = true;
                        } else if (!visited[x][y1]){
                            todo.add(new Point(x, y1));
                        }
                    }
                    // Compare with pixel to the left ...
                    if (x > 0) {
                        int x1 = x - 1;
                        if (pixels[x1][y]) {
                            outlines[x][y] = true;
                        } else if (!visited[x1][y]){
                            todo.add(new Point(x1, y));
                        }
                    }
                }
            }
        }
        return outlines;
    }

    /**
     * Determines the contrast between the two given pixels based on the
     * <a href="http://www.w3.org/TR/WCAG20-TECHS/G17.html#G17-procedure">WCAG 2.0 formula</a>.
     */
    public static double getContrast(int rgb1, int rgb2) {
        double l1 = getLuminance(rgb1);
        double l2 = getLuminance(rgb2);
        return ((l1 >= l2) ? (l1 + 0.05) / (l2 + 0.05) : (l2 + 0.05) / (l1 + 0.05));
    }

    private static double[] PRE_CALCULATED_LUMINANCE_TABLE = new double[256];
    static {
        for (int i = 0; i < 256; ++i) {
            double x = i / 255.0;
            PRE_CALCULATED_LUMINANCE_TABLE[i] = (x <= 0.03928 ? x / 12.92 : Math.pow((x + 0.055) / 1.055, 2.4));
        }
    }

    private static double getLuminance(int rgb) {
        double r = PRE_CALCULATED_LUMINANCE_TABLE[(rgb & 0xFF0000) >> 16];
        double g = PRE_CALCULATED_LUMINANCE_TABLE[(rgb & 0xFF00) >> 8];
        double b = PRE_CALCULATED_LUMINANCE_TABLE[(rgb & 0xFF)];
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    protected ImageHelper() {}
}
