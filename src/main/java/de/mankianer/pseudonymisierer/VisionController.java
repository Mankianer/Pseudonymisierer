package de.mankianer.pseudonymisierer;

import de.mankianer.pseudonymisierer.analyser.VisionAnalyser;
import de.mankianer.pseudonymisierer.imageutiles.ImageHelper;
import de.mankianer.pseudonymisierer.imageutiles.ImageHelperImpl;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.stereotype.Component;

@Component
public class VisionController {

  @Autowired
  private ImageHelper imageHelperImpl;

  @Autowired
  private VisionAnalyser visionAnalyser;

  @Value("#{${PseudonymisierungsMap}}")
  private Map<String, String> replaceMap;

  @PostConstruct
  public void init() {
    System.out.println(replaceMap);
    try {
      pseudo("S:/Projekte/Pseudonymisierer/Beispiele/PDFs/Serienbriefe-bilder/0001.jpg",
          replaceMap);
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
  }

  public void pseudo(String pathImg, Map<String, String> replaceMap) throws IOException {
    List<Entry<String, Rectangle>> changes = visionAnalyser
        .getChanges(visionAnalyser.getPath(pathImg), replaceMap);

    BufferedImage img = imageHelperImpl.loadImageFromDisk(Path.of(pathImg));
    imageHelperImpl.writeToFile(Path.of(pathImg), imageHelperImpl.drawRectsAndTexts(img, changes));

  }

}
