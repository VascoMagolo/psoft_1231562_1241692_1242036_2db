package aisafe;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@RestController
public class DocsTreeController {

    @GetMapping("/docs/tree")
    public ResponseEntity<List<String>> getTree() throws IOException {
        Path root = Paths.get("docs");
        if (!Files.exists(root)) return ResponseEntity.ok(List.of());
        try (Stream<Path> stream = Files.walk(root)) {
            return ResponseEntity.ok(
                stream.filter(Files::isRegularFile)
                      .filter(p -> {
                          String s = p.toString().replace('\\', '/');
                          return s.endsWith(".md") || s.endsWith(".pdf") || s.endsWith(".json")
                              || (s.endsWith(".svg") && s.contains("/svg/"));
                      })
                      .map(p -> "/docs/" + root.relativize(p).toString().replace('\\', '/'))
                      .sorted()
                      .toList()
            );
        }
    }
}
