package de.mankianer.pseudonymisierer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import org.springframework.stereotype.Component;

@Component
public class ImageHelper {

  public void drawRect(Path path, Rectangle rectangle, Color color) throws IOException {
    BufferedImage img = getImg(path);
    Graphics g = img.getGraphics();
    drawRect(g, rectangle, color);
    writeToFile(path, img);
  }


  public void drawRectAndText(Path path, String text, Rectangle rectangle, Color bgColor,
      Color fontColor) throws IOException {
    BufferedImage img = getImg(path);
    Graphics g = img.getGraphics();
    drawRect(g, rectangle, bgColor);
    writeTextInRect(g, text, rectangle, fontColor);
    writeToFile(path, img);
  }

  public void drawRectsAndTexts(Path path, List<Map.Entry<String,Rectangle>> changes) throws IOException {
    BufferedImage img = getImg(path);
    Graphics g = img.getGraphics();
    changes.forEach(e -> {
      drawRect(g, e.getValue(), Color.CYAN);
      writeTextInRect(g, e.getKey(), e.getValue(), Color.BLACK);
    });
    writeToFile(path, img);
  }


  private void writeToFile(Path path, BufferedImage img) throws IOException {
    ImageIO.write(img, "jpg", path.toFile());
  }

  private BufferedImage getImg(Path path) throws IOException {
    return ImageIO.read(path.toFile());
  }

  private void drawRect(Graphics g, Rectangle rectangle, Color bgColor) {
    g.setColor(bgColor);
    g.fillRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, 5, 5);
    g.setColor(
        new Color(255 - bgColor.getRed(), 255 - bgColor.getGreen(), 255 - bgColor.getBlue()));
    g.drawRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, 5, 5);
  }

  private void writeTextInRect(Graphics g, String text, Rectangle rectangle, Color fontColor) {
    Font bestFond = findBestFond(g, text, rectangle);
    g.setFont(bestFond);
    g.setColor(fontColor);
    g.drawString(text, rectangle.x, rectangle.height + rectangle.y );
  }

  private Font findBestFond(Graphics g, String text, Rectangle rectangle) {
    Font f = g.getFont();
    while (g.getFontMetrics(f).stringWidth(text) < rectangle.getWidth()) {
      f = new Font(f.getName(), f.getStyle(), f.getSize() + 1);
    }
    while (g.getFontMetrics(f).stringWidth(text) >= rectangle.getWidth()) {
      f = new Font(f.getName(), f.getStyle(), f.getSize() - 1);
    }

    return f;
  }
}
