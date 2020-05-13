package de.mankianer.pseudonymisierer.analyser;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import java.awt.Rectangle;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public interface ChangeAnalyser {

  List<Entry<String, Rectangle>> getChanges(Path path,
      Map<String, String> replaceMap);
}
