package de.mankianer.pseudonymisierer;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Vertex;
import com.google.gson.Gson;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class VisionController {

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private CloudVisionTemplate cloudVisionTemplate;

  @Autowired
  private ImageHelper imageHelper;

  @PostConstruct
  public void init() {
    AnnotateImageResponse annotateImageResponse = loadFromDisk(
        "S:/Projekte/Pseudonymisierer/Beispiele/PDFs/Serienbriefe-bilder", "0001.jpg");

    Vertex v0 = annotateImageResponse.getTextAnnotations(1).getBoundingPoly().getVertices(0);
    Vertex v2 = annotateImageResponse.getTextAnnotations(1).getBoundingPoly().getVertices(2);

    Rectangle rectangle = new Rectangle();
    rectangle.setLocation(v0.getX(), v0.getY());
    rectangle.setSize(v2.getX() - v0.getX(), v2.getY() - v0.getY());
    log.info("Rect{}", rectangle);
    try {
      imageHelper.drawRectAndText(
          getPath("S:/Projekte/Pseudonymisierer/Beispiele/PDFs/Serienbriefe-bilder/0001.jpg"), "Test213",
          rectangle, Color.CYAN, Color.BLACK);
    } catch (IOException e) {
      e.printStackTrace();
    }

//    log.info("Ok? {}", annotateImageResponse != null);

//    createJsonFile("S:/Projekte/Pseudonymisierer/Beispiele/PDFs/Serienbriefe-bilder", "0001.jpg");
  }

  private Path getPath(String path) throws IOException {
    return resourceLoader.getResource("file:" + path).getFile().toPath();
  }

  private void test() {
    AnnotateImageResponse response = cloudVisionTemplate
        .analyzeImage(resourceLoader.getResource(
            "file:S:/Projekte/Pseudonymisierer/Beispiele/PDFs/Serienbriefe-bilder/0001.jpg"),
            Type.DOCUMENT_TEXT_DETECTION);

    System.out.println(response);
  }

  private AnnotateImageResponse loadFromDisk(String path, String imgFileName) {
    String jsonFileName = Utiles.removeFileExtension(imgFileName) + ".json";
    try {
      log.info("Lade JsonDatei:{}", path + "/" + jsonFileName);
      Path path1 = resourceLoader.getResource("file:" + path + "/" + jsonFileName).getFile()
          .toPath();
      log.info(path1);
      AnnotateImageResponse response = new Gson()
          .fromJson(Files.readString(path1), AnnotateImageResponse.class);
//      AnnotateImageResponse response = AnnotateImageResponse
//          .parseFrom(Files.readAllBytes(
//              path1));
      return response;
    } catch (IOException e) {
      log.error("Konnte " + path + "/" + jsonFileName + " nicht gelesen werden!", e);
    }

    return null;
  }

  private void createJsonFile(String path, String imgFileName) {
    String imgPath = "file:" + path + "/" + imgFileName;
    log.info("Load File:{}", imgPath);
    Type analyzeType = Type.DOCUMENT_TEXT_DETECTION;
    log.warn("Sende an Vision - Kontingentbeachten - : {}", analyzeType);
    AnnotateImageResponse response = cloudVisionTemplate
        .analyzeImage(resourceLoader.getResource(imgPath), analyzeType);

    String responeJson = new Gson().toJson(response);
    log.info("Response:\n{}", responeJson);
    String jsonFileName = Utiles.removeFileExtension(imgFileName) + ".json";
    log.info("JsonfileName: {}", jsonFileName);
    try {
      Path file = Files.createFile(
          resourceLoader.getResource("file:" + path + "/" + jsonFileName).getFile().toPath());
      Files.writeString(file, responeJson);
    } catch (IOException e) {
      log.error("Konnte JsonFile(" + jsonFileName + ") nicht anlegen!", e);
    }

  }

}
