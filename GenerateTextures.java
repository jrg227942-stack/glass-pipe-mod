import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Standalone texture generator for Glass Pipe Transport mod.
 * Run with: javac GenerateTextures.java && java GenerateTextures
 */
public class GenerateTextures {

    public static void main(String[] args) throws IOException {
        String base = "src/main/resources/assets/glass_pipe_transport";

        save(makeGlassPipe(), base + "/textures/block/glass_pipe.png");
        save(makeFilterPipe(), base + "/textures/block/filter_pipe.png");
        save(makeSpeedUpgrade(), base + "/textures/item/speed_upgrade.png");
        save(makeSortingUpgrade(), base + "/textures/item/sorting_upgrade.png");
        save(makePipeWrench(), base + "/textures/item/pipe_wrench.png");
        save(makeGui(), base + "/textures/gui/filter_pipe.png");
        save(makeIcon(), base + "/icon.png");

        System.out.println("All textures generated!");
    }

    static void save(BufferedImage img, String path) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();
        ImageIO.write(img, "PNG", f);
        System.out.println("Created: " + path);
    }

    static BufferedImage makeGlassPipe() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int argb;
                if (x == 0 || x == 15 || y == 0 || y == 15) {
                    argb = color(180, 220, 255, 200);
                } else if (x == 1 || x == 14 || y == 1 || y == 14) {
                    argb = color(200, 235, 255, 160);
                } else if ((x == 2 || x == 13) && (y == 2 || y == 13)) {
                    argb = color(220, 245, 255, 120);
                } else {
                    argb = color(150, 200, 255, 40);
                }
                img.setRGB(x, y, argb);
            }
        }
        return img;
    }

    static BufferedImage makeFilterPipe() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int argb;
                if (x == 0 || x == 15 || y == 0 || y == 15) {
                    argb = color(255, 180, 80, 200);
                } else if (x == 1 || x == 14 || y == 1 || y == 14) {
                    argb = color(255, 200, 100, 160);
                } else if ((x == 2 || x == 13) && (y == 2 || y == 13)) {
                    argb = color(255, 220, 120, 120);
                } else {
                    argb = color(255, 160, 60, 40);
                }
                img.setRGB(x, y, argb);
            }
        }
        return img;
    }

    static BufferedImage makeSpeedUpgrade() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(30, 80, 180));
        g.fillRect(0, 0, 16, 16);
        g.setColor(Color.YELLOW);
        // Lightning bolt
        int[] bx = {9, 11, 8, 10, 5, 7};
        int[] by = {1,  4,  4,  8, 8, 14};
        g.fillPolygon(bx, by, 6);
        g.dispose();
        return img;
    }

    static BufferedImage makeSortingUpgrade() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(30, 150, 60));
        g.fillRect(0, 0, 16, 16);
        g.setColor(Color.WHITE);
        // Cross arrows
        g.fillRect(2, 7, 12, 2);   // Horizontal
        g.fillRect(7, 2, 2, 12);   // Vertical
        // Arrow heads
        int[] ax = {13, 15, 13};
        int[] ay = {6, 8, 10};
        g.fillPolygon(ax, ay, 3);  // Right arrow
        int[] ax2 = {6, 8, 10};
        int[] ay2 = {13, 15, 13};
        g.fillPolygon(ax2, ay2, 3); // Down arrow
        g.dispose();
        return img;
    }

    static BufferedImage makePipeWrench() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(40, 40, 40));
        g.fillRect(0, 0, 16, 16);
        g.setColor(new Color(180, 180, 180));
        // Wrench handle (diagonal)
        g.setStroke(new BasicStroke(2.5f));
        g.drawLine(3, 13, 13, 3);
        // Wrench head (circle)
        g.setStroke(new BasicStroke(2f));
        g.drawOval(1, 1, 6, 6);
        g.dispose();
        return img;
    }

    static BufferedImage makeGui() {
        BufferedImage img = new BufferedImage(176, 166, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        // Background
        g.setColor(new Color(198, 198, 198));
        g.fillRect(0, 0, 176, 166);
        // Border
        g.setColor(new Color(85, 85, 85));
        g.drawRect(0, 0, 175, 165);
        // Filter section
        g.setColor(new Color(130, 160, 190, 180));
        g.fillRect(2, 2, 172, 68);
        // Slot backgrounds (3x3 filter grid)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int sx = 44 + col * 18;
                int sy = 17 + row * 18;
                g.setColor(new Color(80, 80, 80));
                g.fillRect(sx - 1, sy - 1, 18, 18);
                g.setColor(new Color(140, 140, 140));
                g.fillRect(sx, sy, 16, 16);
            }
        }
        // Player inventory slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = 8 + col * 18;
                int sy = 84 + row * 18;
                g.setColor(new Color(80, 80, 80));
                g.fillRect(sx - 1, sy - 1, 18, 18);
                g.setColor(new Color(140, 140, 140));
                g.fillRect(sx, sy, 16, 16);
            }
        }
        // Hotbar slots
        for (int col = 0; col < 9; col++) {
            int sx = 8 + col * 18;
            int sy = 142;
            g.setColor(new Color(80, 80, 80));
            g.fillRect(sx - 1, sy - 1, 18, 18);
            g.setColor(new Color(140, 140, 140));
            g.fillRect(sx, sy, 16, 16);
        }
        g.dispose();
        return img;
    }

    static BufferedImage makeIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Background
        g.setColor(new Color(20, 40, 80));
        g.fillRect(0, 0, 64, 64);
        // Pipe cross
        g.setColor(new Color(150, 210, 255, 200));
        g.fillRect(24, 8, 16, 48);   // Vertical
        g.fillRect(8, 24, 48, 16);   // Horizontal
        // Center highlight
        g.setColor(new Color(200, 240, 255, 220));
        g.fillRect(28, 12, 8, 40);
        g.fillRect(12, 28, 40, 8);
        // Glow center
        g.setColor(new Color(255, 255, 255, 180));
        g.fillOval(26, 26, 12, 12);
        g.dispose();
        return img;
    }

    static int color(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
