package aisafe;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * Controller for serving the documentation tree structure.
 */
@Hidden
@RestController
public class DocsTreeController {

    @Value("${aisafe.docs.path:docs}")
    private String docsPath;

    /**
     * Endpoint to retrieve the list of documentation files in a tree structure.
     * @return a ResponseEntity containing a list of file paths relative to the docs directory
     * @throws IOException if an I/O error occurs while reading the documentation directory
     */
    @GetMapping("/docs/tree")
    public ResponseEntity<List<String>> getTree() throws IOException {
        Path root = Paths.get(docsPath);
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
