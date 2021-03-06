package uet.oop.bomberman;

import javafx.scene.canvas.GraphicsContext;
import uet.oop.bomberman.entities.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Map {
    public static final int GAME_STATE_PENDING = 0;
    public static final int GAME_STATE_LOSE = 1;
    public static final int GAME_STATE_VICTORY = 2;

    private List<Entity> layer0Entities = new ArrayList<>();
    private List<Entity> layer1Entities = new ArrayList<>();
    private List<Entity> layer2Entities = new ArrayList<>();
    private List<Entity> registeredUpdateEntities = new ArrayList<>();
    private List<Entity> pendingUpdateRegisters = new ArrayList<>();
    private List<Entity> pendingUpdateRemovals = new ArrayList<>();
    private Bomber bomberEntity;

    private static int rows;
    private static int columns;

    private boolean isInUpdate;
    private int gameState;

    public Map(int level) throws FileNotFoundException, URISyntaxException {
        this.isInUpdate = false;
        this.gameState = GAME_STATE_PENDING;

        String path = "res/levels/Level" + level + ".txt";
        Scanner scanner = new Scanner(new File(path));
        scanner.nextInt();
        rows = scanner.nextInt();
        columns = scanner.nextInt();
        scanner.nextLine();

        for (int i = 0; i < rows; i++) {
            String row = scanner.nextLine();
            for (int j = 0; j < columns; j++) {
                switch (row.charAt(j)) {
                    case '#':
                        layer2Entities.add(new Wall(this, j, i));
                        break;
                    case '*':
                        layer2Entities.add(new Brick(this, j, i));
                        break;
                    case 'x':
                        // Portals are hidden behind brick
                        layer2Entities.add(new Brick(this, j, i));
                        layer0Entities.add(new Portal(this, j, i));
                        break;
                    case 'p':
                        if (bomberEntity != null) {
                            throw new RuntimeException("Bomber da ton tai!");
                        }

                        bomberEntity = new Bomber(this, j, i);
                        layer1Entities.add(bomberEntity);
                        break;
                    case '1':
                        layer1Entities.add(new Balloom(this, j, i));
                        break;
                    case '2':
                        layer1Entities.add(new Oneal(this, j, i));
                        break;
                    case 'b':
                        layer2Entities.add(new Brick(this, j, i));
                        layer0Entities.add(new BombItem(this, j, i));
                        break;
                    case 'f':
                        layer2Entities.add(new Brick(this, j, i));
                        layer0Entities.add(new FlameItem(this, j, i));
                        break;
                    case 's':
                        layer2Entities.add(new Brick(this, j, i));
                        layer0Entities.add(new SpeedItem(this, j, i));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public void registerForUpdating(Entity entity) {
        if (!registeredUpdateEntities.contains(entity)) {
            if (isInUpdate && !pendingUpdateRegisters.contains(entity)) {
                pendingUpdateRegisters.add(entity);
            } else {
                registeredUpdateEntities.add(entity);
            }
        }
    }

    public void spawnEntity(Entity entity) {
        if (entity instanceof Wall || entity instanceof Brick) {
            layer2Entities.add(entity);
        } else if (entity instanceof Bomber || entity instanceof Balloom || entity instanceof Oneal
                || entity instanceof Bomb) {
            layer1Entities.add(entity);
        } else {
            layer0Entities.add(entity);
        }
    }

    public void despawnEntity(Entity entity) {
        if (entity instanceof Wall || entity instanceof Brick) {
            layer2Entities.remove(entity);
        } else if (entity instanceof Bomber || entity instanceof Balloom || entity instanceof Oneal
                || entity instanceof Bomb) {
            layer1Entities.remove(entity);
        } else {
            layer0Entities.remove(entity);
        }
        if (pendingUpdateRegisters.contains(entity)) {
            pendingUpdateRegisters.remove(entity);
        } else if (isInUpdate && registeredUpdateEntities.contains(entity)) {
            pendingUpdateRemovals.add(entity);
        }
    }

    public void update(InputManager manager, double time) {
        isInUpdate = true;

        for (Entity entity: registeredUpdateEntities) {
            entity.update(manager, time);
        }

        isInUpdate = false;

        for (Entity entity: pendingUpdateRegisters) {
            registeredUpdateEntities.add(entity);
        }

        for (Entity entity: pendingUpdateRemovals) {
            registeredUpdateEntities.remove(entity);
        }

        pendingUpdateRegisters.clear();
        pendingUpdateRemovals.clear();
    }

    public List<Entity> getEntitiesWithFlags(int flagMask) {
        List<Entity> result = new ArrayList<>();

        for (Entity entity: layer2Entities) {
            if ((entity.getFlags() & flagMask) != 0) {
                result.add(entity);
            }
        }

        for (Entity entity: layer1Entities) {
            if ((entity.getFlags() & flagMask) != 0) {
                result.add(entity);
            }
        }

        for (Entity entity: layer0Entities) {
            if ((entity.getFlags() & flagMask) != 0) {
                result.add(entity);
            }
        }

        return result;
    }

    public void render(GraphicsContext context) {
        layer0Entities.forEach(e -> e.render(context));
        layer1Entities.forEach(e -> e.render(context));
        layer2Entities.forEach(e -> e.render(context));
    }

    public Bomber getBomberEntity() {
        return bomberEntity;
    }

    public void setGameStateLose() {
        gameState = GAME_STATE_LOSE;
    }

    public void setGameStateVictory() {
        gameState = GAME_STATE_VICTORY;
    }

    int getGameState() {
        return gameState;
    }
}
