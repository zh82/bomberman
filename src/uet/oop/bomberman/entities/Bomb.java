package uet.oop.bomberman.entities;

import javafx.scene.media.MediaPlayer;
import uet.oop.bomberman.InputManager;
import uet.oop.bomberman.Map;
import uet.oop.bomberman.graphics.Sprite;
import uet.oop.bomberman.graphics.SpritePlayer;
import uet.oop.bomberman.sound.SoundEffect;

import java.util.Arrays;

public class Bomb extends Entity {
    private SpritePlayer bombSprite;
    private SpritePlayer bombExplodedSprite;
    private MediaPlayer bombExplodeSoundPlayer;
    private Bomber spawner;

    private double timeStart = 0;
    private int explodeLength;
    private boolean waitExplode = true;
    private boolean isDead = false;

    private static final double DURATION = 0.2000;
    private static final double BOMB_EXPLODE_TIME = 1.5000;
    private static final double BOMB_FLAME_TIME = 0.4000;

    public Bomb(Map map, Bomber spawner, int xUnit, int yUnit, int explodeLength) {
        super(map, xUnit, yUnit, FLAG_ENEMY_HARDBLOCK | FLAG_FLAME_EATABLE, null);

        this.spawner = spawner;

        this.bombSprite = new SpritePlayer(Arrays.asList(Sprite.bomb, Sprite.bomb_1, Sprite.bomb_2), DURATION);
        this.bombExplodedSprite = new SpritePlayer(Arrays.asList(Sprite.bomb_exploded, Sprite.bomb_exploded1, Sprite.bomb_exploded2),
                DURATION);
        this.explodeLength = explodeLength;
        this.bombExplodeSoundPlayer = new MediaPlayer(SoundEffect.BOMB_EXPLODE_SOUND);

        map.registerForUpdating(this);
    }

    @Override
    public void update(InputManager input, double time) {
        if (isDead) {
            int xUnit = (int)(x / Entity.SIZE);
            int yUnit = (int)(y / Entity.SIZE);

            map.spawnEntity(new Explosion(map, xUnit - 1, yUnit, 0, explodeLength));
            map.spawnEntity(new Explosion(map, xUnit + 1, yUnit, 1, explodeLength));
            map.spawnEntity(new Explosion(map, xUnit, yUnit - 1, 2, explodeLength));
            map.spawnEntity(new Explosion(map, xUnit, yUnit + 1, 3, explodeLength));

            spawner.bombExploded();
            map.despawnEntity(this);

            return;
        }

        if (timeStart == 0) {
            timeStart = time;
        }

        if (waitExplode) {
            img = bombSprite.playFrame(time);

            // First time when the bomb is spawned, the player is intersects with the bomb
            // If the player moves away from the bomb, the bomb will become solid and player can't move on it no more.
            if (spawner.getIntersectSize(this) == null) {
                flags |= FLAG_PLAYER_HARDBLOCK;
            }
        } else {
            img = bombExplodedSprite.playFrame(time);
        }

        if (waitExplode && (time - timeStart >= BOMB_EXPLODE_TIME)) {
            timeStart = time;
            waitExplode = false;

            flags &= ~(FLAG_PLAYER_HARDBLOCK | FLAG_ENEMY_HARDBLOCK);

            int xUnit = (int)(x / Entity.SIZE);
            int yUnit = (int)(y / Entity.SIZE);

            bombExplodeSoundPlayer.play();

            map.spawnEntity(new Explosion(map, xUnit - 1, yUnit, 0, explodeLength));
            map.spawnEntity(new Explosion(map, xUnit + 1, yUnit, 1, explodeLength));
            map.spawnEntity(new Explosion(map, xUnit, yUnit - 1, 2, explodeLength));
            map.spawnEntity(new Explosion(map, xUnit, yUnit + 1, 3, explodeLength));
        } else if (!waitExplode && (time - timeStart >= BOMB_FLAME_TIME)) {
            spawner.bombExploded();
            map.despawnEntity(this);
        }
    }

    public void dead() {
        isDead = true;
    }
}
