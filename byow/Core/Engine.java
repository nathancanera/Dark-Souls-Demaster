package byow.Core;
import javax.sound.sampled.*;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdDraw;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class Engine {
    TERenderer ter = new TERenderer();
    public static final int WIDTH = 80;
    public static final int HEIGHT = 33;
    private static final int MAX_HEALTH = 256;
    private static final int OPP_HEALTH = 50;
    private static final int OPP_DAMAGE = 64;
    private static final int OPP2_HEALTH = 80;
    private static final int OPP2_DAMAGE = 128;
    private static final int REQ_FIRE = 4;
    private static final int MID_LOW = 20;
    private static final int MID = 30;
    private static final int BIG = 60;
    private static final double HUD_HI = 0.97;
    private static final int SOUL_WIDTH = 13;
    private static final int SIXTEEN = 16;
    private static final int HALF_SEC = 1500;
    private static final int ONE_SEC = 1000;
    private static final int FIVE_SEC = 5000;
    private static final int MAX_RAND = 100;
    private static final double YORN = 2.5;
    private static final double SLAY = 1.5;
    private static final double DEM = 1.7;
    private static final double LOAD = 2.8;
    private static final double QUIT = 3.15;
    boolean gameStart = false;
    boolean mainMenu = true;
    boolean quit = false;
    String seed = "";
    String history = "";
    World world;
    ChosenUndead player;
    Random rand;
    Set<int[]> livingEnemies = new HashSet<>();


    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        menu();
        while (!gameStart) {
            if (StdDraw.hasNextKeyTyped()) {

                if (keyPress(StdDraw.nextKeyTyped())) {
                    return;
                }
            }
        }
        start();
    }

    public void start() {
        music("byow/Core/Audio/song.wav");
        while (gameStart) {
            if (StdDraw.hasNextKeyTyped()) {
                if (keyPress(StdDraw.nextKeyTyped())) {
                    return;
                }
            }
            hud();
            mouseHover();
        }
    }

    public void music(String path) {
        try {
            AudioInputStream aud = AudioSystem.getAudioInputStream(new File(path));
            Clip clip = AudioSystem.getClip();
            clip.open(aud);
            clip.start();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isDigits(String input) {
        if (input == null || input.length() == 0) {
            return false;
        }
        for (int i = 0; i < input.length(); i++) {
            if (!Character.isDigit(input.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean keyPress(char key) {
        this.history += Character.toLowerCase(key);
        if (gameStart) {
            if ("wasd".contains(Character.toLowerCase(key) + "")) {
                movement(Character.toLowerCase(key));
                quit = false;
            } else if (key == ':') {
                quit = true;
            } else if (Character.toLowerCase(key) == 'q' && quit) {
                gameStart = false;
                StdDraw.clear(Color.black);
                StdDraw.show();
                saveGame();
                return true;
            }

        } else {
            if (Character.toLowerCase(key) == 'q') {
                this.gameStart = false;
                StdDraw.clear(Color.black);
                StdDraw.show();
                return true;
            } else if (Character.toLowerCase(key) == 'n') {
                this.mainMenu = false;
                music("byow/Core/Audio/menu.wav");
                seedMenu();
            } else if (isDigits("" + key)) {
                this.seed += key;
                seedMenu();
            } else if (Character.toLowerCase(key) == 's' && !this.mainMenu) {
                music("byow/Core/Audio/menu.wav");
                this.gameStart = true;
                Long seed2 = Long.parseLong(seed);
                this.world = new World(seed2);
                this.rand = new Random(seed2);
                this.player = this.world.player();
                this.livingEnemies.addAll(world.enemies());
                ter.initialize(WIDTH, HEIGHT);
                ter.renderFrame(world.world());
            } else if (Character.toLowerCase(key) == 'l') {
                music("byow/Core/Audio/menu.wav");
                In readSave = new In("./byow/Core/save.txt");
                String saveDat = readSave.readLine();
                ter.initialize(WIDTH, HEIGHT);
                this.history += saveDat;
                interactWithInputString(saveDat);
                start();
            }

        }
        return false;
    }

    public void saveGame() {
        try {
            this.history = this.history.replace(":q", "");
            this.history = this.history.replace("l", "");
            Files.write(Paths.get("./byow/Core/save.txt"), this.history.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mouseHover() {
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();
        if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
            Font font = new Font("OptimusPrinceps", Font.PLAIN, MID_LOW);
            StdDraw.setFont(font);
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(MID, HEIGHT * HUD_HI, world.world()[x][y].description());
            StdDraw.show();
            StdDraw.setPenColor(Color.black);
            StdDraw.filledRectangle(MID, HEIGHT * HUD_HI, 7, 2);
        }
    }

    public void hud() {
        Font font = new Font("OptimusPrinceps", Font.PLAIN, MID_LOW);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(4, HEIGHT * HUD_HI, "HP: " + player.healthPoints() + "/" + MAX_HEALTH);
        StdDraw.text(SOUL_WIDTH, HEIGHT * HUD_HI, "Soul Level: " + player.level());
        StdDraw.show();
    }

    private void refreshMap() {
        for (int[] enemy : world.enemies()) {
            if (world.world()[enemy[0]][enemy[1]] == Tileset.FLOOR) {
                world.world()[enemy[0]][enemy[1]] = Tileset.OPP;
                livingEnemies.add(enemy);
            }
        }
        player.heal();
    }

    private void movement(char key) {
        int x = 0;
        int y = 0;
        switch (key) {
            case 'w':
                x = player.pos()[0];
                y = player.pos()[1] + 1;
                if (y < HEIGHT && world.world()[x][y] == Tileset.FLOOR) {
                    player.setPos(x, y);
                    world.world()[x][y] = Tileset.AVATAR;
                    world.world()[x][y - 1] = Tileset.FLOOR;
                } else if (y < HEIGHT && world.world()[x][y] == Tileset.BONFIRE) {
                    refreshMap();
                    player.setBonfire(x - 1, y);
                    music("byow/Core/Audio/fire.wav");
                }
                ter.renderFrame(world.world());
                break;
            case 's':
                x = player.pos()[0];
                y = player.pos()[1] - 1;
                if (y >= 0 && world.world()[x][y] == Tileset.FLOOR) {
                    player.setPos(x, y);
                    world.world()[x][y] = Tileset.AVATAR;
                    world.world()[x][y + 1] = Tileset.FLOOR;
                } else if (y >= 0 && world.world()[x][y] == Tileset.BONFIRE) {
                    refreshMap();
                    player.setBonfire(x - 1, y);
                    music("byow/Core/Audio/fire.wav");
                }
                ter.renderFrame(world.world());
                break;
            case 'a':
                x = player.pos()[0] - 1;
                y = player.pos()[1];
                if (x >= 0 && world.world()[x][y] == Tileset.FLOOR) {
                    player.setPos(x, y);
                    world.world()[x][y] = Tileset.AVATAR;
                    world.world()[x + 1][y] = Tileset.FLOOR;
                } else if (x >= 0 && world.world()[x][y] == Tileset.BONFIRE) {
                    refreshMap();
                    player.setBonfire(x - 1, y);
                    music("byow/Core/Audio/fire.wav");
                }
                ter.renderFrame(world.world());
                break;
            case 'd':
                x = player.pos()[0] + 1;
                y = player.pos()[1];
                if (x < WIDTH && world.world()[x][y] == Tileset.FLOOR) {
                    player.setPos(x, y);
                    world.world()[x][y] = Tileset.AVATAR;
                    world.world()[x - 1][y] = Tileset.FLOOR;
                } else if (x < WIDTH && world.world()[x][y] == Tileset.BONFIRE) {
                    refreshMap();
                    player.setBonfire(x - 1, y);
                    music("byow/Core/Audio/fire.wav");
                }
                ter.renderFrame(world.world());
                break;
            default:
                break;
        }
        movement2(x, y);

    }
    public void movement2(int x, int y) {
        if (world.world()[x][y] == Tileset.KILN) {
            if (player.bellsRung().size() >= REQ_FIRE) {
                music("byow/Core/Audio/fire.wav");
                gameWin();
            } else {
                Font font = new Font("OptimusPrinceps", Font.PLAIN, OPP_HEALTH);
                StdDraw.setFont(font);
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.clear(Color.black);
                StdDraw.text(WIDTH / 2, HEIGHT / 2, "Visit " + (REQ_FIRE - player.bellsRung().size())
                        + " More Bonfires");
                StdDraw.show();
                StdDraw.pause(HALF_SEC);
            }
        }
        if (world.surrounding(player.pos(), Tileset.OPP)) {
            encounter(-1, world.closest(livingEnemies, player.pos()));
        } else if (world.surrounding(player.pos(), Tileset.OPP2)) {
            encounter(1, world.closest(livingEnemies, player.pos()));
        }
    }



    public void gameWin() {
        Font font = new Font("OptimusPrinceps", Font.PLAIN, OPP_HEALTH);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.clear(Color.black);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Rekindle the First Flame?");
        StdDraw.text(WIDTH / 2, HEIGHT / YORN, "Y or N");
        StdDraw.show();
        while (!StdDraw.hasNextKeyTyped()) {
            String smh = "empty" + "whileloop";
        }
        if (StdDraw.hasNextKeyTyped()) {
            music("byow/Core/Audio/menu.wav");
            char key = Character.toLowerCase(StdDraw.nextKeyTyped());
            StdDraw.clear(Color.black);
            StdDraw.pause(ONE_SEC);
            switch (key) {
                case 'y':
                    for (int i = 0; i < WIDTH; i++) {
                        for (int j = 0; j < HEIGHT; j++) {
                            if (world.world()[i][j] != Tileset.AVATAR) {
                                world.world()[i][j] = Tileset.FLAME;
                            }
                        }
                    }
                    break;
                case 'n':
                    for (int i = 0; i < WIDTH; i++) {
                        for (int j = 0; j < HEIGHT; j++) {
                            if (world.world()[i][j] != Tileset.AVATAR) {
                                world.world()[i][j] = Tileset.NOTHING;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
            gameStart = false;
            ter.renderFrame(world.world());
            StdDraw.pause(FIVE_SEC);
            StdDraw.clear(Color.black);
        }
    }
    public void encounter(int ver, int[] enemy) {
        int health = 0;
        int damage = 0;
        int xp = 0;
        if (ver < 0) {
            health = OPP_HEALTH;
            damage = OPP_DAMAGE;
            xp = 2;
        } else {
            health = OPP2_HEALTH;
            damage = OPP2_DAMAGE;
            xp = 6;
        }
        int attack = rand.nextInt(MAX_RAND);
        Font font = new Font("OptimusPrinceps", Font.PLAIN, OPP_HEALTH);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.clear(Color.black);
        music("byow/Core/Audio/parry1.wav");
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "You Rolled: " + attack + " + " + player.level());
        StdDraw.show();
        StdDraw.pause(HALF_SEC);
        if (attack + player.level() > health) {
            music("byow/Core/Audio/parry2.wav");
            StdDraw.text(WIDTH / 2, HEIGHT / SLAY, "You Slayed the Beast!");
            StdDraw.show();
            StdDraw.pause(HALF_SEC);
            player.levelUp(xp);
            world.world()[enemy[0]][enemy[1]] = Tileset.FLOOR;
            this.livingEnemies.remove(enemy);
        } else {
            StdDraw.text(WIDTH / 2, HEIGHT / YORN, "The Beast with " + health + " Defense "
                    + "Blocked and Countered!");
            StdDraw.show();
            StdDraw.pause(ONE_SEC);
            if (player.loseHP(damage) <= 0) {
                StdDraw.setFont(font);
                StdDraw.setPenColor(Color.RED);
                StdDraw.clear(Color.black);
                StdDraw.text(WIDTH / 2, HEIGHT / 2, "Y O U  D I E D");
                music("byow/Core/Audio/death.wav");
                StdDraw.show();
                StdDraw.pause(FIVE_SEC);
                refreshMap();
                this.world.world()[player.pos()[0]][player.pos()[1]] = Tileset.FLOOR;
                world.world()[player.bonfire()[0]][player.bonfire()[1]] = Tileset.AVATAR;
                player.respawn();
            }
        }
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, running both of these:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        In readLine = new In("./byow/Core/save.txt");
        String saveDat = readLine.readLine();
        saveDat.replace(":q", "");
        this.seed = "";
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == 'l') {
                input = input.replace("l", saveDat);
            }
            keyPress(input.charAt(i));
        }
        return world.world();
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        Engine eng = new Engine();
        eng.interactWithKeyboard();
    }
    public void menu() {
        StdDraw.setCanvasSize(WIDTH * SIXTEEN, HEIGHT * SIXTEEN + MAX_RAND);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("OptimusPrincepsSemiBold", Font.BOLD, BIG);
        StdDraw.setFont(font);
        StdDraw.enableDoubleBuffering();
        StdDraw.text(WIDTH / 2, HEIGHT / SLAY, "CS61B SOULS");
        Font font2 = new Font("OptimusPrinceps", Font.PLAIN, MID);
        StdDraw.setFont(font2);
        StdDraw.text(WIDTH / 2, HEIGHT / DEM, "D E M A S T E R E D");
        Font font3 = new Font("OptimusPrinceps", Font.PLAIN, MID_LOW);
        StdDraw.setFont(font3);
        StdDraw.text(WIDTH / 2, HEIGHT / YORN, "New Game (N)");
        StdDraw.text(WIDTH / 2, HEIGHT / LOAD, "Load Game (L)");
        StdDraw.text(WIDTH / 2, HEIGHT / QUIT, "Quit (Q)");
        StdDraw.show();
        StdDraw.pause(1);
    }

    public void seedMenu() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("OptimusPrincepsSemiBold", Font.PLAIN, MID);
        StdDraw.setFont(font);
        StdDraw.text(WIDTH / 2, HEIGHT / DEM, "Enter Seed Ending with S:");
        StdDraw.text(WIDTH / 2, HEIGHT / 2, this.seed);
        StdDraw.show();
        StdDraw.pause(1);
    }

}
