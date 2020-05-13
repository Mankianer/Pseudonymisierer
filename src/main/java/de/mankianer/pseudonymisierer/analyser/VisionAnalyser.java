package de.mankianer.pseudonymisierer.analyser;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Vertex;
import com.google.gson.Gson;
import de.mankianer.pseudonymisierer.Utiles;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class VisionAnalyser implements ChangeAnalyser {

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private CloudVisionTemplate cloudVisionTemplate;

  @Value("${connectToVision: true}")
  private boolean connectToVision;

//  @PostConstruct
  public void init() {

//    List<Entry<String, Rectangle>> changes = null;
//    try {
//      changes = getChanges(getPath("S:/Projekte/Pseudonymisierer/Beispiele/PDFs/Serienbriefe-bilder/0001.jpg"),
//          Map.of("Balke", "XXXXXX", "Marvin", "No Name"));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    try {
//      imageHelperImpl.drawRectsAndTexts(
//          getPath("S:/Projekte/Pseudonymisierer/Beispiele/PDFs/Serienbriefe-bilder/0001.jpg"),
//          changes);
//    } catch (IOException e) {
//      e.printStackTrace();
//    }

//    createJsonFile("S:/Projekte/Pseudonymisierer/Beispiele/PDFs/Serienbriefe-bilder", "0001.jpg");
  }

  @Override
  public List<Entry<String, Rectangle>> getChanges(Path path,
      Map<String, String> replaceMap) {

    try {
      AnnotateImageResponse annotateImageResponse = loadAnylsation(path);
      List<Entry<String, Rectangle>> changes = annotateImageResponse.getTextAnnotationsList()
          .stream()
          .filter(e -> replaceMap.containsKey(e.getDescription())).map(e -> {
            Rectangle rectangle = boundingPolyToRect(e.getBoundingPoly());
            String newText = replaceMap.get(e.getDescription());
            log.info("Gefunden:{}, Ersatzt:{}, Rect:{}", e.getDescription(), newText, rectangle);
            return Map.entry(newText, rectangle);
          }).collect(Collectors.toList());
      log.info("Es wurden {} Änderungen gefunden.", changes.size());
      return changes;
    } catch (IOException e) {
      e.printStackTrace();
    }

    return List.of();
  }

  private AnnotateImageResponse loadAnylsation(Path path) throws IOException {
    Path jsonPath = getPath(Utiles.removeFileExtension(path.toString()) + ".json");
    AnnotateImageResponse annotateImageResponse = loadFromDisk(
        jsonPath);
    if(annotateImageResponse == null && connectToVision){
      annotateImageResponse = createJsonFile(jsonPath, path);
    }
    return annotateImageResponse;
  }

  //TODO return Poly!
  private Rectangle boundingPolyToRect(BoundingPoly boundingPoly) {
    Vertex v0 = boundingPoly.getVertices(0);
    Vertex v2 = boundingPoly.getVertices(2);
    Rectangle rectangle = new Rectangle();
    rectangle.setLocation(v0.getX(), v0.getY());
    rectangle.setSize(v2.getX() - v0.getX(), v2.getY() - v0.getY());
    return rectangle;
  }

  public Path getPath(String path) throws IOException {
    return resourceLoader.getResource("file:" + path).getFile().toPath();
  }

//  private void test() {
//    AnnotateImageResponse response = cloudVisionTemplate
//        .analyzeImage(resourceLoader.getResource(
//            "file:S:/Projekte/Pseudonymisierer/Beispiele/PDFs/Serienbriefe-bilder/0001.jpg"),
//            Type.DOCUMENT_TEXT_DETECTION);
//
//    System.out.println(response);
//  }

  private AnnotateImageResponse loadFromDisk(Path jsonPath) {
    if (Files.exists(jsonPath)) {

      try {
        log.info("Lade JsonDatei:{}", jsonPath);
        AnnotateImageResponse response = new Gson()
            .fromJson(Files.readString(jsonPath), AnnotateImageResponse.class);
        return response;
      } catch (IOException e) {
        log.error("Konnte " + jsonPath + " nicht gelesen werden!", e);
      }
    } else {
      log.info("Datei wurde nicht gefunden {}", jsonPath);
    }

    return null;
  }

  private AnnotateImageResponse createJsonFile(Path jsonPath, Path imgPath) {
    log.info("Load File:{}", imgPath);
    Type analyzeType = Type.DOCUMENT_TEXT_DETECTION;
    log.warn("Sende an Vision - Kontingentbeachten - : {}", analyzeType);
    AnnotateImageResponse response = cloudVisionTemplate
        .analyzeImage(resourceLoader.getResource("file:" + imgPath.toString()), analyzeType);

    String responeJson = new Gson().toJson(response);
    log.debug("Response:\n{}", responeJson);
    log.info("JsonfileName: {}", jsonPath);
    try {
      Files.writeString(jsonPath, responeJson);
    } catch (IOException e) {
      log.error("Konnte JsonFile(" + jsonPath + ") nicht anlegen!", e);
    }

    return response;
  }

}
