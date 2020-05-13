package de.mankianer.pseudonymisierer.imageutiles;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public interface ImageHelper {


  BufferedImage drawRectAndText(BufferedImage img, String text, Rectangle rectangle, Color bgColor,
      Color fontColor);

  BufferedImage drawRectsAndTexts(BufferedImage img,
      List<Entry<String, Rectangle>> changes);

  void writeToFile(Path path, BufferedImage img) throws IOException;

  BufferedImage loadImageFromDisk(Path path) throws IOException;
}
