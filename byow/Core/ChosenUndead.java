package byow.Core;

import java.util.HashSet;
import java.util.Set;

public class ChosenUndead {
    private static final int MAX_HEALTH = 256;
    private int x;
    private int y;
    private int hp;
    private int level;
    private int[] bonfire;
    private Set<int[]> visited;
    private String location;


    public ChosenUndead(int x, int y) {
        this.x = x;
        this.y = y;
        this.hp = MAX_HEALTH;
        this.level = 0;
        this.bonfire = new int[2];
        this.bonfire[0] = x;
        this.bonfire[1] = y;
        this.visited = new HashSet<>();
        this.location = "FireLink Shrine";
    }

    public void setPos(int i, int j) {
        this.x = i;
        this.y = j;
    }

    public int[] pos() {
        int[] pos = {this.x, this.y};
        return pos;
    }

    public String location() {
        return this.location;
    }

    public String locSim() {
        return null;
    }

    public Set<int[]> bellsRung() {
        return this.visited;
    }

    public int[] bonfire() {
        return this.bonfire;
    }

    public void respawn() {
        this.x = this.bonfire[0];
        this.y = this.bonfire[1];
    }

    public void setBonfire(int i, int j) {
        int[] fire = {i, j};
        this.visited.add(fire);
        this.bonfire[0] = x;
        this.bonfire[1] = y;
    }

    public int healthPoints() {
        return this.hp;
    }

    public int loseHP(int loss) {
        this.hp -= loss;
        return this.hp;
    }

    public int heal() {
        this.hp = MAX_HEALTH;
        return this.hp;
    }

    public int level() {
        return this.level;
    }
    public int levelUp(int xp) {
        this.level += xp;
        return this.level;
    }

}
