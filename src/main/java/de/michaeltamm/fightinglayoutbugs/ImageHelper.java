/*
 * Copyright 2009 Michael Tamm
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

package de.michaeltamm.fightinglayoutbugs;

import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Michael Tamm
 */
public class ImageHelper {

    public static int[][] fileToPixels(File imageFile) throws IOException {
        final BufferedImage image = ImageIO.read(imageFile);
        return imageToPixels(image);
    }

    public static int[][] bytesToPixels(byte[] bytes) throws IOException {
        InputStream in = new ByteArrayInputStream(bytes);
        try {
            final BufferedImage image = ImageIO.read(in);
            return imageToPixels(image);
        } finally {
            in.close();
        }
    }

    public static void pixelsToFile(int[][] pixels, File pngFile) throws IOException {
        final BufferedImage image = pixelsToImage(pixels);
        final File dir = pngFile.getParentFile();
        if (dir != null && !dir.exists()) {
            FileUtils.forceMkdir(dir);
        }
        ImageIO.write(image, "png", pngFile);
    }

    public static void pixelsToFile(boolean[][] pixels, File pngFile) throws IOException {
        final BufferedImage image = pixelsToImage(pixels);
        final File dir = pngFile.getParentFile();
        if (dir != null && !dir.exists()) {
            FileUtils.forceMkdir(dir);
        }
        ImageIO.write(image, "png", pngFile);
    }

    public static int[][] imageToPixels(BufferedImage image) {
        if (image == null) {
            return null;
        }
        final int w = image.getWidth();
        final int h = image.getHeight();
        final int[][] pixels = new int[w][h];
        final Raster raster = image.getRaster();
        if (raster.getTransferType() == DataBuffer.TYPE_BYTE) {
            final byte[] bytes = (byte[]) raster.getDataElements(0, 0, w, h, null);
            final int bytesPerPixel = (bytes.length / (w*h));
            final ColorModel colorModel = image.getColorModel();
            final byte[] buf = new byte[bytesPerPixel];
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
        final int w = pixels.length;
        if (w > 0) {
            final int h = pixels[0].length;
            if (h > 0) {
                final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < w; ++x) {
                    final int[] column = pixels[x];
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
        final int w = pixels.length;
        if (w > 0) {
            final int h = pixels[0].length;
            if (h > 0) {
                final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < w; ++x) {
                    final boolean[] column = pixels[x];
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
        final int[][] copy;
        if (pixels == null) {
            copy = null;
        } else {
            final int w = pixels.length;
            if (w == 0) {
                copy = new int[0][];
            } else {
                final int h = pixels[0].length;
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
     * Returns <code>true</code> if the given image contains the
     * given subimage at an arbitrary location. (Pixels of subimage
     * with an alpha value lower than 255 are ignored.)
     */
    public static boolean contains(BufferedImage image, BufferedImage subimage) {
        final Map<Integer, List<Point>> rgb2offsets = new HashMap<Integer, List<Point>>();
        for (int x = 0; x < subimage.getWidth(); ++x) {
            for (int y = 0; y < subimage.getHeight(); ++y) {
                final int argb = subimage.getRGB(x, y);
                final int a = argb >>> 24;
                if (a == 255) {
                    final Integer rgb = argb & 0xFFFFFF;
                    List<Point> offsets = rgb2offsets.get(rgb);
                    if (offsets == null) {
                        offsets = new ArrayList<Point>();
                        rgb2offsets.put(rgb, offsets);
                    }
                    offsets.add(new Point(x, y));
                }
            }
        }
        final int w = image.getWidth();
        final int h = image.getHeight();
        final int[][] p = new int[w][h];
        final Raster raster = image.getRaster();
        if (raster.getTransferType() == DataBuffer.TYPE_BYTE) {
            final byte[] bytes = (byte[]) raster.getDataElements(0, 0, w, h, null);
            final int bytesPerPixel = (bytes.length / (w*h));
            final ColorModel colorModel = image.getColorModel();
            final byte[] buf = new byte[bytesPerPixel];
            for (int x = 0; x < w; ++x) {
                for (int y = 0; y < h; ++y) {
                    System.arraycopy(bytes, (x + y * w) * bytesPerPixel, buf, 0, bytesPerPixel);
                    p[x][y] = colorModel.getRGB(buf) & 0xFFFFFF;
                }
            }
            for (int x = 0; x < w; ++x) {
                for (int y = 0; y < h; ++y) {
                    final Iterator<Map.Entry<Integer,List<Point>>> i = rgb2offsets.entrySet().iterator();
                    compareWithSubimageLoop:
                    do {
                        final Map.Entry<Integer,List<Point>> mapEntry = i.next();
                        final int expectedRgb = mapEntry.getKey();
                        for (Point offset : mapEntry.getValue()) {
                            final int xx = x + offset.x;
                            final int yy = y + offset.y;
                            if (xx >= w || yy >= h || expectedRgb != p[xx][yy]) {
                                break compareWithSubimageLoop;
                            }
                        }
                        if (!i.hasNext()) {
                            return true;
                        }
                    } while(true);
                }
            }
            return false;
        } else {
            throw new RuntimeException("transfer type " + raster.getTransferType() + " not implemented yet");
        }
    }

    public static void applyConvolutionFilter(int[][] pixels, float[][] kernel) {
        if (kernel == null) {
            throw new IllegalArgumentException("Method parameter kernel must not be null.");
        }
        final int kernelSize = kernel.length;
        if (kernelSize % 2 == 0) {
            throw new IllegalArgumentException("Method parameter kernel must have odd size. (e.g. 3, 5, 7, ...)");
        }
        if (kernel[0].length != kernelSize) {
            throw new IllegalArgumentException("Method parameter kernel must have square dimensions. (e.g. 3x3, 5x5, 7x7, ...)");
        }
        if (pixels != null) {
            final int w = pixels.length;
            if (w > 0) {
                final int h = pixels[0].length;
                if (h > 0) {
                    final int[][] r = new int[w][h];
                    final int[][] g = new int[w][h];
                    final int[][] b = new int[w][h];
                    final int[][] a = splitIntoChannels(pixels, r, g, b);
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

    private static void combineChannels(int[][] a, int[][] r, int[][] g, int[][] b, int[][] pixels) {
        final int w = pixels.length;
        final int h = pixels[0].length;
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                pixels[x][y] = (a[x][y] << 24) | (r[x][y] << 16) | (g[x][y] << 8) | b[x][y];
            }
        }
    }

    private static void combineChannels(int[][] r, int[][] g, int[][] b, int[][] pixels) {
        final int w = pixels.length;
        final int h = pixels[0].length;
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
    private static int[][] splitIntoChannels(int[][] pixels, int[][] r, int[][] g, int[][] b) {
        int[][] a = null;
        final int w = pixels.length;
        final int h = pixels[0].length;
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                final int p = pixels[x][y];
                final int alpha = p >>> 24;
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

    private static void applyConvolutionFilterToChannel(int[][] channel, float[][] kernel) {
        final int w = channel.length;
        final int h = channel[0].length;
        final int kernelSize = kernel.length;
        final int r = (kernelSize / 2);
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
                                final float k = kernel[i][j];
                                final int oldValue = channel[xx][yy];
                                assert 0 <= oldValue && oldValue <= 255;
                                n += k * oldValue;
                                d += k;
                            }
                        }
                    }
                }
                final int newValue = Math.round(n / d);
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
        final float[][] kernel = new float[kernelSize][kernelSize];
        final int m = kernelSize / 2;
        final double q = 2 * sigma * sigma;
        final double f = 1 / (Math.PI * q);
        for (int x = 0; x < kernelSize; ++x) {
            final int dx = x - m;
            for (int y = 0; y < kernelSize; ++y) {
                final int dy = y - m;
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
            final int w = pixels.length;
            if (w > 0) {
                final int h = pixels[0].length;
                if (h > 0) {
                    assert pixelsWithAlpha.length == w;
                    assert pixelsWithAlpha[0].length == h;
                    for (int x = 0; x < w; ++x) {
                        for (int y = 0; y < h; ++y) {
                            final int p2 = pixelsWithAlpha[x][y];
                            final int a = p2 >>> 24;
                            if (a < 0xFF) {
                                if (a == 0) {
                                    pixels[x][y] = p2;
                                } else {
                                    final float a2 = ((float) (0xFF - a)) / 0xFF;
                                    assert 0 < a2 && a2 < 1;
                                    final float a1 = 1 - a2;
                                    final int p1 = pixels[x][y];
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
        final int w = pixels.length;
        final int h = pixels[0].length;
        final int w1 = w - 1;
        final int h1 = h - 1;
        final boolean[][] outlines = new boolean[w][h];
        // Find starting point ...
        int x0 = 0;
        int y0 = 0;
        // Look for starting point on top border ...
        while (pixels[x0][y0] && x0 < w) {
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
            while (pixels[x0][y0] && y0 < h) {
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
        final Queue<Point> todo = new LinkedList<Point>();
        todo.add(new Point(x0, y0));
        final boolean[][] visited = new boolean[w][h];
        while (!todo.isEmpty()) {
            final Point p = todo.poll();
            final int x = p.x;
            final int y = p.y;
            if (!visited[x][y]) {
                visited[x][y] = true;
                if (!pixels[x][y]) {
                    // Compare with pixel above ...
                    if (y > 0) {
                        final int y1 = y - 1;
                        if (pixels[x][y1]) {
                            outlines[x][y] = true;
                        } else if (!visited[x][y1]){
                            todo.add(new Point(x, y1));
                        }
                    }
                    // Compare with pixel to the right ...
                    if (x < w1) {
                        final int x1 = x + 1;
                        if (pixels[x1][y]) {
                            outlines[x][y] = true;
                        } else if (!visited[x1][y]){
                            todo.add(new Point(x1, y));
                        }
                    }
                    // Compare with pixel below ...
                    if (y < h1) {
                        final int y1 = y + 1;
                        if (pixels[x][y1]) {
                            outlines[x][y] = true;
                        } else if (!visited[x][y1]){
                            todo.add(new Point(x, y1));
                        }
                    }
                    // Compare with pixel to the left ...
                    if (x > 0) {
                        final int x1 = x - 1;
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

    private ImageHelper() {}
}
