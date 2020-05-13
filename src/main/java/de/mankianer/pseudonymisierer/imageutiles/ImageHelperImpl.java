package de.mankianer.pseudonymisierer.imageutiles;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class ImageHelperImpl implements ImageHelper{

  public void drawRect(Path path, Rectangle rectangle, Color color) throws IOException {
    BufferedImage img = loadImageFromDisk(path);
    Graphics g = img.getGraphics();
    drawRect(g, rectangle, color);
    writeToFile(path, img);
  }

  @Override
  public BufferedImage drawRectAndText(BufferedImage img, String text, Rectangle rectangle, Color bgColor,
      Color fontColor) {
    Graphics g = img.getGraphics();
    drawRect(g, rectangle, bgColor);
    writeTextInRect(g, text, rectangle, fontColor);
    return img;
  }

  @Override
  public BufferedImage drawRectsAndTexts(BufferedImage img,
      List<Map.Entry<String, Rectangle>> changes) {
    Graphics g = img.getGraphics();
    changes.forEach(e -> {
      drawRect(g, e.getValue(), Color.WHITE);
      writeTextInRect(g, e.getKey(), e.getValue(), Color.BLACK);
    });
    return img;
  }


  @Override
  public void writeToFile(Path path, BufferedImage img) throws IOException {
    ImageIO.write(img, "jpg", path.toFile());
  }

  @Override
  public BufferedImage loadImageFromDisk(Path path) throws IOException {
    return ImageIO.read(path.toFile());
  }

  private void drawRect(Graphics g, Rectangle rectangle, Color bgColor) {
    g.setColor(bgColor);
    g.fillRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, 5, 5);
//    g.setColor(
//        new Color(255 - bgColor.getRed(), 255 - bgColor.getGreen(), 255 - bgColor.getBlue()));
//    g.drawRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, 5, 5);
  }

  private void writeTextInRect(Graphics g, String text, Rectangle rectangle, Color fontColor) {
    Font bestFond = findBestFond(g, text, rectangle);
    g.setFont(bestFond);
    g.setColor(fontColor);
    g.drawString(text, rectangle.x, (rectangle.height - g.getFontMetrics().getHeight())/2 + rectangle.y + g.getFontMetrics().getHeight());
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
