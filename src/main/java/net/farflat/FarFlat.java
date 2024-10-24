package net.farflat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.farflat.Mathematical.MAX_NORMAL_WORLD_POS;
import static net.farflat.Mathematical.squishOutsideZC;

public class FarFlat extends JPanel {
    private static final float divisor = 1.1f;
    public static FarFlat instance;
    private int scale = 5;
    private final World world;
    private final Player player;
    private int screenWidth;
    private int screenHeight;
    private double screenSize;

    public FarFlat() {
        instance = this;
        world = new World(1);
        player = new Player(world, new Random().nextLong(0L, Mathematical.MAX_SAFE_WORLD_POS), world.getHeight() / 2);
        screenWidth = 800;
        screenHeight = 600;

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                screenWidth = (int) (getWidth() / divisor);
                screenHeight = (int) (getHeight() / divisor);
                screenSize = (Math.max(screenWidth, screenHeight) / 800.);
                scale = (int) (7 * screenSize);
            }
        });

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                player.keyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player.keyReleased(e);
            }
        });
    }

    public void run(double delta) {
        player.update(delta);
        if (player.x.compareTo(new BigDecimal(MAX_NORMAL_WORLD_POS)) > 0 && player.x.compareTo(new BigDecimal("1000000000000")) < 0 && StoryEffects.enable_out_of_world_warning) world.loadedChunks.clear();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        BigDecimal playerX = player.getX();
        double playerY = player.getY();
        g.setColor(Color.RED);
        g.fillRect((int) ((screenWidth * divisor - screenWidth) / 2), (int) ((screenHeight * divisor - screenHeight) / 2), screenWidth, screenHeight);

        // Draw the visible portion of the world around the player
        AABB aabb = new AABB((playerX.subtract(BigDecimal.valueOf(screenWidth / 2.))), (playerY - screenHeight / 2.), (playerX.add(BigDecimal.valueOf(screenWidth / 2.))), (playerY + screenHeight / 2.)).factor(1. / scale).inflate(world.getChunkWidth(), 10).add(Math.min(0, player.velocityX / world.getChunkWidth()), 0, Math.max(0, player.velocityX / world.getChunkWidth()), 0);
        BlockPos[] positions = world.getAllPositionsInAABB(aabb);
        final BigDecimal trillion = new BigDecimal("1000000000000");
        final BigDecimal quadrillion = new BigDecimal("1000000000000000");
        for (BlockPos position : positions) {
            g.setColor(determineColor(world.getBlock(position.x(), position.y()), position.x(), (int) position.y()));
            double x = (new BigDecimal(position.x().multiply(BigInteger.valueOf(scale))).subtract(playerX.multiply(BigDecimal.valueOf(scale))).add(BigDecimal.valueOf(screenWidth / 2.)).subtract(BigDecimal.valueOf(scale / 2.))).doubleValue();
            if (StoryEffects.enable_trillion_glitch && position.x().compareTo(trillion.toBigInteger()) >= 0 && (position.x().compareTo(quadrillion.toBigInteger()) < 0 || !StoryEffects.enable_infinite_quadrillion_paradise)) {
                x += new Random().nextDouble(playerX.divide(trillion, RoundingMode.FLOOR).subtract(BigDecimal.ONE).divide(new BigDecimal("1000"), RoundingMode.HALF_UP).doubleValue())*2-1;
            }
            double y = (((float) position.y()) * scale - ((float) playerY) * scale + screenHeight / 2. - scale / 2.);
            g.fillRect((int) ((squishOutsideZC(x, screenWidth)) + (screenWidth * divisor - screenWidth) / 2), (int) ((squishOutsideZC(y, screenHeight)) + (screenHeight * divisor - screenHeight) / 2), scale*2, scale);
        }
        int maxChunks = (int) (100 + (aabb.x2().divide(BigDecimal.valueOf(world.getChunkWidth()), new MathContext(34, RoundingMode.CEILING)).subtract(aabb.x1().divide(BigDecimal.valueOf(world.getChunkWidth()), new MathContext(34, RoundingMode.FLOOR)))).doubleValue());
        int lim = 10;
        while (world.loadedChunks.size() > maxChunks) {
            if (lim <= 0) break;
            Object[] arr = world.loadedChunks.keySet().stream().filter(i -> !world.loadedChunks.get(i).hasBeenAccessed).toArray();
            if (arr.length > 0) {
                world.unloadChunk((BigInteger) arr[0]);
            }
            lim--;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Draw the player at the center of the screen
        int playerScreenX = (int) (screenWidth / 2 - scale / 2 + (screenWidth * divisor - screenWidth) / 2);
        int playerScreenY = (int) (screenHeight / 2 - scale / 2 + (screenHeight * divisor - screenHeight) / 2);
        g.setColor(Color.GREEN);
        if (StoryEffects.enable_infinite_quadrillion_paradise && playerX.compareTo(quadrillion) >= 0) {
            g.setColor(Color.PINK);
        }
        g.fillRect(playerScreenX, playerScreenY, scale, scale);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        g.drawString("X: %s".formatted(new DecimalFormat("#.###################").format(playerX)), 0, 20);
        g.drawString("Y: %s".formatted(world.getHeight() - playerY), 0, 40);
        g.drawString("C: %s/%s".formatted(world.loadedChunks.size(), maxChunks), 0, 60);

        g.setColor(Color.BLACK);
        g.drawRect((int) ((screenWidth * divisor - screenWidth) / 2), (int) ((screenHeight * divisor - screenHeight) / 2), screenWidth, screenHeight);

        world.resetAccessedCheck();
    }

    private Color determineColor(int blockType, BigInteger x, int y) {
        float sky = Math.max(0, Math.min(1, (float) y / world.getHeight()));
        float spaceFalloff = (float) Math.pow(sky, 3);
        return switch (blockType) {
            case 0 ->
                    new Color((int) (((1.1f - sky) * spaceFalloff) * 255), (int) (((1.1f - sky) * spaceFalloff) * 255), (int) ((sky + (1 - sky) * spaceFalloff) * 255));
            case 1 -> new Color(0x7E7E7E);
            case 2 -> new Color(0x8D4A11);
            case 3 -> new Color(0x149B10);
            case 4 -> new Color(0x313131);
            case 5 -> new Color(0x1B52FA);
            default -> Color.WHITE;
        };
    }
}

class Player {
    private final World world;
    public boolean hasCollision = true;
    BigDecimal x;
    double y;
    double velocityX;
    double velocityY;
    double accelerationX;
    double accelerationY;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean upPressed;
    private boolean downPressed;
    private double speed = 10;

    public Player(World world, double x, double y) {
        this.x = BigDecimal.valueOf(x);
        this.y = y;
        this.world = world;
    }

    public BigDecimal getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_LEFT -> leftPressed = true;
            case KeyEvent.VK_RIGHT -> rightPressed = true;
            case KeyEvent.VK_UP -> upPressed = true;
            case KeyEvent.VK_DOWN -> downPressed = true;
            case KeyEvent.VK_T -> {
                JPanel panel = new JPanel();
                panel.add(new Label("Type location to teleport"));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                String s = JOptionPane.showInputDialog(FarFlat.instance, panel, "teleport", JOptionPane.PLAIN_MESSAGE);
                if (s == null) break;
                x = new BigDecimal(s.split(" ")[0]);
                y = world.getHeight() - Double.parseDouble(s.split(" ")[1]);
            }
            case KeyEvent.VK_S -> {
                JPanel panel = new JPanel();
                panel.add(new Label("Type speed to set"));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                String s = JOptionPane.showInputDialog(FarFlat.instance, panel, "speed", JOptionPane.PLAIN_MESSAGE);
                if (s == null) break;
                speed = Double.parseDouble(s);
            }
            case KeyEvent.VK_R -> world.loadedChunks.clear();
            case KeyEvent.VK_SPACE -> {
                velocityX = 0;
                velocityY = 0;
            }
            case KeyEvent.VK_C -> hasCollision ^= true;
        }
    }

    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_LEFT -> leftPressed = false;
            case KeyEvent.VK_RIGHT -> rightPressed = false;
            case KeyEvent.VK_UP -> upPressed = false;
            case KeyEvent.VK_DOWN -> downPressed = false;
        }
    }

    private List<BlockPos> getCollidingBlocks() {
        List<BlockPos> neighborBlocks = getSolidTouchingBlocks();
        List<BlockPos> collidingBlocks = new ArrayList<>();
        for (BlockPos neighborBlock : neighborBlocks) {
            boolean xLeft = x.add(BigDecimal.ONE).compareTo(new BigDecimal(neighborBlock.x())) >= 0;
            boolean xRight = x.compareTo(new BigDecimal(neighborBlock.x().add(BigInteger.ONE))) < 0;
            boolean yDown = y + 1 >= neighborBlock.y();
            boolean yUp = y < neighborBlock.y() + 1;
            if (xLeft && xRight && yDown && yUp) {
                collidingBlocks.add(neighborBlock);
                continue;
            }
            break;
        }
        return collidingBlocks;
    }

    public List<BlockPos> getTouchingBlocks() {
        return List.of(world.getAllPositionsInAABB(new AABB(x, y, x.add(BigDecimal.ONE), y + 1).deflate(0.1)));
    }

    public List<BlockPos> getSolidTouchingBlocks() {
        return getTouchingBlocks().stream().filter(x -> BlockInfo.getBlockInfo(world.getBlock(x.x(), x.y())).solid()).toList();
    }

    public List<BlockPos> getLiquidTouchingBlocks() {
        return getTouchingBlocks().stream().filter(x -> BlockInfo.getBlockInfo(world.getBlock(x.x(), x.y())).liquid()).toList();
    }

    public void update(double delta) {
        // Update acceleration based on key presses
        accelerationX = ((rightPressed ? speed * 500 : 0) + (leftPressed ? -speed * 500 : 0)) * delta;
        accelerationY = ((downPressed ? speed * 500 : 0) + (upPressed ? -speed * 500 : 0)) * delta;

        velocityY += 30 * delta;
        // Update velocity based on acceleration
        velocityX += accelerationX * delta;
        velocityY += accelerationY * delta;
        velocityX *= Math.pow(0.6, delta);
        velocityY *= Math.pow(0.6, delta);

        // Update position based on velocity
        if (true) {
            x = x.add(BigDecimal.valueOf(velocityX * delta));
            y += velocityY * delta;
        } else {
            velocityY = 0;
            y -= 0.1;
        }

        if (y > world.getHeight() + 10) {
            velocityY *= -0.7;
            velocityX += -Math.signum(velocityX) * velocityY * 0.3;
            y = world.getHeight() + 10;
        }
        if (y < -10) {
            velocityY *= -0.7;
            velocityX += Math.signum(velocityX) * velocityY * 0.3;
            y = -10;
        }

        if (hasCollision) {
            if (getLiquidTouchingBlocks().size() > 0) {
                this.velocityY -= 3;
            }

            List<BlockPos> pos = getCollidingBlocks();
            final double radiusStep = 0.1;
            final double maxRadius = 100;
            double radius = radiusStep;
            BigDecimal ox = x;
            double oy = y;
            while (pos.size() > 0 && radius < maxRadius) {
                x = ox;
                y = oy - radius;
                pos = getCollidingBlocks();
                if (pos.size() == 0) break;
                x = ox.add(BigDecimal.valueOf(radius));
                y = oy;
                pos = getCollidingBlocks();
                if (pos.size() == 0) break;
                x = ox;
                y = oy + radius;
                pos = getCollidingBlocks();
                if (pos.size() == 0) break;
                x = ox.subtract(BigDecimal.valueOf(radius));
                y = oy;
                pos = getCollidingBlocks();
                if (pos.size() == 0) break;
                x = ox;
                y = oy;
                radius += radiusStep;
            }
            if (radius != maxRadius) {
                if (x.compareTo(ox) != 0) {
                    velocityX = 0;
                    x = x.round(new MathContext(34, RoundingMode.HALF_UP));
                }
                if (y != oy) velocityY = 0;
            }
        }

//        y = world.getHeight() / 2;

//        if (Math.random() < 0.01) world.reloadAllChunks();
    }
}