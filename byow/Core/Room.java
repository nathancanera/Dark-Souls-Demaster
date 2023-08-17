package byow.Core;
import java.util.Random;

public class Room {
    private int xLowLeft;
    private int yLowLeft;
    private int width;
    private int height;
    private Random rand;
    private int[] enter;
    private int[] exit;
    private String location;

    private int[] perimeterCalc(int dist) {
        int[] coord = new int[2];
        if (0 < dist && dist < this.width - 1) {
            coord[0] = xLowLeft + dist;
            coord[1] = yLowLeft + 1;
            return coord;
        } else if (this.width - 1 < dist && dist < this.width + this.height - 2) {
            coord[0] = xLowLeft + this.width - 2;
            dist = dist - (this.width - 1);
            coord[1] = yLowLeft + dist;
            return coord;
        } else if (this.width + this.height - 2 < dist && dist < this.width * 2 + this.height - 3) {
            coord[1] = yLowLeft + this.height - 2;
            dist = dist - (this.width + this.height - 2);
            coord[0] = xLowLeft + ((this.width - 1) - dist);
            return coord;
        } else if (this.width * 2 + this.height - 3 < dist && dist < (this.width * 2 + this.height * 2) - 4) {
            coord[0] = xLowLeft + 1;
            dist = dist - (this.width * 2 + this.height - 3);
            coord[1] = yLowLeft + ((this.height - 1) - dist);
            return coord;
        } else if (dist == (this.width * 2 + this.height * 2) - 4) {
            return perimeterCalc(dist - 1);
        } else {
            return perimeterCalc(dist + 1);
        }
    }

    public Room(int x, int y, int width, int height, Long seed, String area) {
        this.xLowLeft = x;
        this.yLowLeft = y;
        this.width = width;
        this.height = height;
        this.rand = new Random(seed);
        int entr = rand.nextInt((2 * width + 2 * height) - 5);
        int ext = entr;
        while (ext <= 1 + entr && ext >= entr - 1) {
            ext = rand.nextInt((2 * width + 2 * height) - 5);
        }
        this.enter = perimeterCalc(entr);
        this.exit = perimeterCalc(ext);


    }

    public int[] corners(int i) {
        int[] corner = new int[2];
        if (i == 1) {
            corner[0] = xLowLeft;
            corner[1] = yLowLeft;
        } else if (i == 2) {
            corner[0] = xLowLeft + this.width - 1;
            corner[1] = yLowLeft;
        } else if (i == 3) {
            corner[0] = xLowLeft + this.width - 1;
            corner[1] = yLowLeft + this.height - 1;
        } else if (i == 4) {
            corner[0] = xLowLeft;
            corner[1] = yLowLeft + this.height - 1;
        }
        return corner;
    }
    public int size() {
        return width * height;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public void areaSetter(String area) {
        this.location = area;
    }

    public int[] exit() {
        return this.exit.clone();
    }

    public int[] enter() {
        return this.enter.clone();
    }

    public int[] center() {
        int[] center = new int[2];
        center[0] = (this.width / 2) + xLowLeft;
        center[1] = (this.height / 2) + yLowLeft;
        return center;
    }

    public String location() {
        return this.location;
    }
    public int area() {
        return this.width * this.height;
    }
    public void spawnEnemies(double ratio) {
        int enemies = 0;
        while (enemies / area() < ratio) {
            int x = rand.nextInt(xLowLeft + 1, xLowLeft + width);
            int y = rand.nextInt(yLowLeft + 1, yLowLeft + height);

        }
    }

}
