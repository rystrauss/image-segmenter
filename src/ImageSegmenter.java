import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An implementation of the Felzenswalb-Huttenlocher image segmentation
 * algorithm.
 *
 * @author Ryan Strauss
 */
public class ImageSegmenter {

    /**
     * Builds a Pixel array from the supplied Color array.
     *
     * @param rgbArray the Color array to make Pixels from
     * @return a Pixel array containing the Colors from the supplied Color array
     */
    private static Pixel[][] buildPixelArray(Color[][] rgbArray) {
        int height = rgbArray.length;
        int width = rgbArray[0].length;

        Pixel[][] pixelArray = new Pixel[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                pixelArray[i][j] = new Pixel(i, j, rgbArray[i][j]);
            }
        }

        return pixelArray;
    }

    /**
     * Constructs and a sorted list of edges corresponding to the grid
     * graph on the given pixel array.
     *
     * @param pixelArray the pixels representing the grid graph
     * @return a list of sorted edges in the grid graph
     */
    private static SortedSet<Edge> buildEdgeSet(Pixel[][] pixelArray) {
        final int[] XDELTA = {0, 1, 1, 1};
        final int[] YDELTA = {1, 1, 0, -1};

        SortedSet<Edge> edges = new TreeSet<>();

        int height = pixelArray.length;
        int width = pixelArray[0].length;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Pixel p1 = pixelArray[i][j];
                for (int k = 0; k < XDELTA.length; k++) {
                    int x = j + XDELTA[k];
                    int y = i + YDELTA[k];
                    if (y >= 0 && y < height && x < width) {
                        Pixel p2 = pixelArray[y][x];
                        edges.add(new Edge(p1, p2));
                    }
                }
            }
        }

        return edges;
    }

    /**
     * Returns an RGB array colored by the provided segments.
     *
     * @param segments a collection of Pixel segments
     * @param height   the height of the image
     * @param width    the width of the image
     * @return the new Color array
     */
    private static Color[][] recolor(Collection<List<Pixel>> segments, int height, int width) {
        Color[][] newRaster = new Color[height][width];
        ColorPicker generator = new ColorPicker();

        segments.stream().parallel().forEach(pixels -> {
            Color newColor = generator.nextColor();
            for (Pixel pixel : pixels)
                newRaster[pixel.getRow()][pixel.getCol()] = newColor;
        });

        return newRaster;
    }

    /**
     * Returns the segmented version of the input image.
     *
     * @param rgbArray    the input image
     * @param granularity the granularity parameter to use for segmentation
     * @return the segmented image
     */
    public static Color[][] segment(Color[][] rgbArray, double granularity) {
        System.out.println("Segmenting image...");

        Pixel[][] pixelArray = buildPixelArray(rgbArray);
        SortedSet<Edge> edges = buildEdgeSet(pixelArray);
        System.out.println("Created " + edges.size() + " edges.");

        DisjointSetForest forest = new DisjointSetForest(pixelArray);

        for (Edge e : edges) {
            Pixel rep1 = forest.find(e.getFirstPixel());
            Pixel rep2 = forest.find(e.getSecondPixel());
            if (rep1 != rep2) {
                double bound1 = forest.getInternalDistance(rep1) + (granularity / forest.getSize(rep1));
                double bound2 = forest.getInternalDistance(rep2) + (granularity / forest.getSize(rep2));
                double threshold = Math.min(bound1, bound2);

                if (e.getWeight() < threshold)
                    forest.union(rep1, rep2, e.getWeight());
            }
        }

        Collection<List<Pixel>> segments = forest.getSegments();
        System.out.println("Created " + segments.size() + " segments.");

        return recolor(segments, rgbArray.length, rgbArray[0].length);
    }

}

