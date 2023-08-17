package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.In;

import java.util.*;

public class World {
    private static final String TXT = "./byow/Core/areas.txt";
    private static final double FLOOR_RATIO = 0.25;
    private static final double ENEMY_RATIO = 0.03;
    private static final int MIN_ROOM = 25;
    //private static final int seed = 832342;
    private static final int MAX_BONFIRE = 4;
    private static final int FRAME_WIDTH = 80;
    private static final int FRAME_HEIGHT = 33;
    private static final int MIN_DIAM = 5;
    private static final int MAX_DIAM = 12;
    private static final int FIFTEEN = 12;
    private TETile[][] world;
    private ArrayList<Room> rooms;
    private Random rand;
    private Long seed;
    private ArrayList<String> locations;
    private Set<int[]> bonfires;
    private Set<int[]> enemies;
    private ChosenUndead player;

    public World(Long seed) {
        this.seed = seed;
        this.rand = new Random(this.seed);
        this.world = new TETile[FRAME_WIDTH][FRAME_HEIGHT];
        this.rooms = new ArrayList<>();
        this.locations = new ArrayList<>();
        this.bonfires = new HashSet<>();
        this.enemies = new HashSet<>();
        fillAreas();
        for (int x = 0; x < FRAME_WIDTH; x++) {
            for (int y = 0; y < FRAME_HEIGHT; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        while (tileRatio() < FLOOR_RATIO) {
            Room newRoom = roomGenerator();
            if (!collides(newRoom)) {
                locationSetter(newRoom);
                roomAdder(newRoom);
                rooms.add(newRoom);
            }
        }
        for (int i = 0; i < rooms.size() - 1; i++) {
            connect(rooms.get(i), rooms.get(i + 1));
        }
        connect(rooms.get(rooms.size() - 1), rooms.get(0));
        outline();
        int[] pos = rooms.get(0).center();
        this.player = new ChosenUndead(pos[0] - 1, pos[1]);
        darkSign();
        fireKeeper();
        lordNito();
    }

    private void fillAreas() {
        In locs = new In(TXT);
        int i = 0;
        while (locs.hasNextLine()) {
            locations.add(locs.readLine());
            i += 1;
        }
    }

    public ChosenUndead darkSign() {
        int[] pos = rooms.get(0).center();
        world[pos[0]][pos[1]] = Tileset.BONFIRE;
        world[pos[0] - 1][pos[1]] = Tileset.AVATAR;
        double max = Double.MIN_VALUE;
        Room farthest = null;
        for (Room room : rooms) {
            if (distance(room.center(), pos) > max) {
                max = distance(room.center(), pos);
                farthest = room;
            }
        }
        farthest.areaSetter("Kiln of the first flame");
        world[farthest.center()[0]][farthest.center()[1]] = Tileset.KILN;
        return player;
    }

    public void lordNito() {
        for (Room room : rooms) {
            if (room.location().equals("Kiln of the first flame")) {
                int[] pos2 = {room.center()[0], room.center()[1] + 1};
                world[room.center()[0]][room.center()[1] + 1] = Tileset.OPP2;
                this.enemies.add(pos2);
                pos2[1] = pos2[1] - 2;
                world[room.center()[0]][room.center()[1] - 1] = Tileset.OPP2;
            }
            if (!room.location().equals("FireLink Shrine")  && !room.location().equals("Kiln of the first flame")) {
                double enemy = 0.0;
                while (enemy / room.area() < ENEMY_RATIO) {
                    int x = rand.nextInt(room.corners(1)[0] + 1, room.corners(1)[0] + room.width());
                    int y = rand.nextInt(room.corners(1)[1] + 1, room.corners(1)[1] + room.height());
                    if (world[x][y] == Tileset.FLOOR && world[x + 1][y] != Tileset.BONFIRE) {
                        world[x][y] = Tileset.OPP;
                        int[] pos = {x, y};
                        this.enemies.add(pos);
                        enemy += 1;
                    }
                }
            }
        }
    }

    public void fireKeeper() {
        int numFires = 0;
        int i = 0;
        while (numFires < MAX_BONFIRE && i < rooms.size()) {
            int[] pos = rooms.get(i).center();
            if (world[pos[0]][pos[1]] == Tileset.FLOOR) {
                if (this.bonfires.isEmpty()) {
                    world[pos[0]][pos[1]] = Tileset.BONFIRE;
                    bonfires.add(pos);
                    numFires += 1;
                } else {
                    if (distance(closest(bonfires, pos), pos) >= FIFTEEN) {
                        world[pos[0]][pos[1]] = Tileset.BONFIRE;
                        bonfires.add(pos);
                        numFires += 1;
                    }
                }
            }
            i += 1;
        }
    }

    public Room roomGenerator() {
        int width = rand.nextInt(MIN_DIAM, MAX_DIAM);
        int height = rand.nextInt(MIN_DIAM, MAX_DIAM);
        int x = rand.nextInt(0, FRAME_WIDTH - 1);
        int y = rand.nextInt(0, FRAME_HEIGHT - 4);
        return new Room(x, y, width, height, seed, "");
    }

    private void locationSetter(Room room) {
        String area = "";
        if (tileRatio() == 0) {
            area = "FireLink Shrine";
            this.locations.remove(area);
        } else {
            int i = rand.nextInt(locations.size());
            area = locations.get(i);
            locations.remove(area);
        }
        room.areaSetter(area);
    }
    public void roomAdder(Room newRoom) {
        int xll = newRoom.corners(1)[0];
        int yll = newRoom.corners(1)[1];
        for (int i = xll; i < newRoom.width() + xll; i++) {
            for (int j = yll; j < newRoom.height() + yll; j++) {
                if (i == xll || j == yll || i == newRoom.corners(3)[0] || j == newRoom.corners(3)[1]) {
                    world[i][j] = Tileset.WALL;
                } else {
                    world[i][j] = Tileset.FLOOR;
                }
            }
        }
    }
    public void connect(Room origin, Room target) {
        int longOrLat = rand.nextInt(2);
        if (longOrLat == 0) {
            connectVertical(target, origin);
        } else {
            connectHorizontal(target, origin);
        }
    }
    private void connectVertical(Room target, Room origin) {
        int departX = origin.exit()[0];
        int departY = origin.exit()[1];
        int landX = target.enter()[0];
        int landY = target.enter()[1];
        if (departY > landY) {
            while (departY > landY) {
                departY -= 1;
                world[departX][departY] = Tileset.FLOOR;
            }
        } else if (departY < landY) {
            while (departY < landY) {
                departY += 1;
                world[departX][departY] = Tileset.FLOOR;
            }
        }
        if (departX > landX) {
            while (departX > landX) {
                departX -= 1;
                world[departX][departY] = Tileset.FLOOR;
            }
        } else if (departX < landX) {
            while (departX < landX) {
                departX += 1;
                world[departX][departY] = Tileset.FLOOR;
            }
        }
    }

    private void connectHorizontal(Room target, Room origin) {
        int departX = origin.exit()[0];
        int departY = origin.exit()[1];
        int landX = target.enter()[0];
        int landY = target.enter()[1];
        if (departX > landX) {
            while (departX > landX) {
                departX -= 1;
                world[departX][departY] = Tileset.FLOOR;
            }
        } else if (departX < landX) {
            while (departX < landX) {
                departX += 1;
                world[departX][departY] = Tileset.FLOOR;
            }
        }
        if (departY > landY) {
            while (departY > landY) {
                departY -= 1;
                world[departX][departY] = Tileset.FLOOR;
            }
        } else if (departY < landY) {
            while (departY < landY) {
                departY += 1;
                world[departX][departY] = Tileset.FLOOR;
            }

        }
    }

    private void outline() {
        for (int x = 0; x < FRAME_WIDTH; x++) {
            for (int y = 0; y < FRAME_HEIGHT - 3; y++) {
                if (world[x][y] == Tileset.FLOOR) {
                    outlineHelper(x, y);
                }
            }
        }
    }

    private void outlineHelper(int x, int y) {
        for (int i = x - 1; i < x + 2; i++) {
            for (int j = y - 1; j < y + 2; j++) {
                if (world[i][j] == Tileset.NOTHING) {
                    world[i][j] = Tileset.WALL;
                }
            }
        }
    }

    public double tileRatio() {
        double sizes = 0.0;
        for (Room room : rooms) {
            sizes += room.size();
        }
        double worldArea = FRAME_WIDTH * (FRAME_HEIGHT - 3);
        return sizes / worldArea;
    }

    public double distance(int[] start, int[] end) {
        return Math.sqrt(Math.pow(start[0] - end[0], 2) + Math.pow(start[1] - end[1], 2));
    }

    public int[] closest(Set<int[]> points, int[] start) {
        Double min = Double.MAX_VALUE;
        int[] closest = new int[2];
        for (int[] pts : points) {
            if (distance(pts, start) < min) {
                min = distance(pts, start);
                closest = pts;
            }
        }
        return closest;
    }
    public boolean collides(Room newRoom) {
        int xll = newRoom.corners(1)[0];
        int yll = newRoom.corners(1)[1];
        for (int e = 1; e <= 4; e++) {
            if (newRoom.corners(e)[0] >= FRAME_WIDTH || newRoom.corners(e)[1] >= FRAME_HEIGHT - 3) {
                return true;
            }
        }
        for (int i = xll; i < newRoom.width() + xll; i++) {
            for (int j = yll; j < newRoom.height() + yll; j++) {

                if (world[i][j] != Tileset.NOTHING) {
                    return true;
                }
            }
        }
        return false;
    }
    public ChosenUndead player() {
        return this.player;
    }

    public Set<int[]> enemies() {
        return this.enemies;
    }

    public boolean surrounding(int[] pos, TETile tile) {
        for (int i = pos[0] - 1; i < pos[0] + 2; i++) {
            for (int j = pos[1] - 1; j < pos[1] + 2; j++) {
                if (world[i][j] == tile) {
                    return true;
                }
            }
        }
        return false;
    }
    public TETile[][] world() {
        return this.world;
    }
}
