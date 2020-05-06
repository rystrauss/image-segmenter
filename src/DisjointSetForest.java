import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class for representing pixel objects as a disjoint set forest.
 *
 * @author Ryan Strauss
 */
public class DisjointSetForest {

    private final Node[][] pixelMap;

    /**
     * Class for representing a single node within the disjoint set forest.
     */
    private static class Node {

        Node parent;
        Pixel pixel;
        int size, rank;
        double internalDistance;

        /**
         * Constructs a single Node object containing the given Pixel.
         *
         * @param pixel The Pixel object to put inside this node.
         */
        Node(Pixel pixel) {
            this.pixel = pixel;
            this.parent = null;
            this.size = 1;
            this.rank = 0;
            this.internalDistance = 0;
        }

    }

    public DisjointSetForest(Pixel[][] pixelArray) {
        int height = pixelArray.length;
        int width = pixelArray[0].length;

        pixelMap = new Node[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                pixelMap[i][j] = new Node(pixelArray[i][j]);
            }
        }
    }

    /**
     * Finds the representative pixel for the given pixel's segment.
     *
     * @param pixel the pixel for which to find the representative pixel
     * @return the pixel's segment's representative pixel
     */
    public Pixel find(Pixel pixel) {
        List<Node> path = new LinkedList<>();
        Node current = pixelMap[pixel.getRow()][pixel.getCol()];

        if (current == null)
            throw new IllegalStateException("Pixel's node cannot be null.");

        while (current.parent != null) {
            path.add(current);
            current = current.parent;
        }

        for (Node n : path) {
            n.parent = current;
        }

        return current.pixel;
    }

    /**
     * Merges the segments of the supplied pixels.
     *
     * @param p1               the first pixel to be merged
     * @param p2               the second pixel to be merged
     * @param internalDistance the internal distance of the new segment
     */
    public void union(Pixel p1, Pixel p2, double internalDistance) {
        Node n1 = pixelMap[p1.getRow()][p1.getCol()];
        Node n2 = pixelMap[p2.getRow()][p2.getCol()];

        if (n1.parent != null || n2.parent != null)
            throw new IllegalStateException("Both pixels should be the representative pixels for their segments.");

        if (n1.rank >= n2.rank) {
            n2.parent = n1;
            n1.size += n2.size;
            n1.internalDistance = internalDistance;
            if (n1.rank == n2.rank)
                n1.rank++;
        } else {
            n1.parent = n2;
            n2.size += n1.size;
            n2.internalDistance = internalDistance;
        }

    }

    /**
     * Returns the segments in this disjoint set forest.
     *
     * @return a Map from the representative pixels to a List of the pixels in the segment
     */
    public Map<Pixel, List<Pixel>> getSegments() {
        Map<Pixel, List<Pixel>> segments = new HashMap<>();

        for (int i = 0; i < pixelMap.length; i++) {
            for (int j = 0; j < pixelMap[0].length; j++) {
                Pixel rep = find(pixelMap[i][j].pixel);
                if (!segments.containsKey(rep))
                    segments.put(rep, new LinkedList<>());
                segments.get(rep).add(pixelMap[i][j].pixel);
            }
        }

        return segments;
    }

    /**
     * Returns the internal distance of the specified representative pixel.
     *
     * @param pixel the pixel for which to get the internal distance; should be a representative pixel
     * @return the internal distance of pixel
     */
    public double getInternalDistance(Pixel pixel) {
        Node node = pixelMap[pixel.getRow()][pixel.getCol()];

        if (node.parent != null)
            throw new IllegalArgumentException("Given pixel should be a representative of a segment.");

        return node.internalDistance;
    }

    /**
     * Returns the size of the specified representative pixel.
     *
     * @param pixel the pixel for which to get the size; should be a representative pixel
     * @return the size of pixel
     */
    public int getSize(Pixel pixel) {
        Node node = pixelMap[pixel.getRow()][pixel.getCol()];

        if (node.parent != null)
            throw new IllegalArgumentException("Given pixel should be a representative of a segment.");

        return node.size;
    }

}
