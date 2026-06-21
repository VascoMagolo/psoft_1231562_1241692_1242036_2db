package aisafe.shared.infrastructure;

import aisafe.security.application.JwtService;
import aisafe.security.domain.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocsTreeController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocsTreeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocsTreeController docsTreeController;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private final Path testDocsDir = Paths.get("target/test-docs");

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(testDocsDir);
        ReflectionTestUtils.setField(docsTreeController, "docsPath", "target/test-docs");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(testDocsDir)) {
            try (var stream = Files.walk(testDocsDir)) {
                stream.sorted(Comparator.reverseOrder())
                      .forEach(p -> {
                          try {
                              Files.delete(p);
                          } catch (IOException ignored) {}
                      });
            }
        }
    }

    @Test
    void ensureGetTreeReturnsExpectedFiles() throws Exception {
        // Create files that should be included
        Files.createFile(testDocsDir.resolve("doc1.md"));
        Files.createFile(testDocsDir.resolve("doc2.pdf"));
        Files.createFile(testDocsDir.resolve("doc3.json"));
        
        Path svgDir = testDocsDir.resolve("svg");
        Files.createDirectories(svgDir);
        Files.createFile(svgDir.resolve("diagram.svg"));

        // Create files that should be excluded
        Files.createFile(testDocsDir.resolve("exclude.txt"));
        
        Path notSvgDir = testDocsDir.resolve("notsvg");
        Files.createDirectories(notSvgDir);
        Files.createFile(notSvgDir.resolve("diagram.svg"));

        mockMvc.perform(get("/docs/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0]").value("/docs/doc1.md"))
                .andExpect(jsonPath("$[1]").value("/docs/doc2.pdf"))
                .andExpect(jsonPath("$[2]").value("/docs/doc3.json"))
                .andExpect(jsonPath("$[3]").value("/docs/svg/diagram.svg"));
    }

    @Test
    void ensureGetTreeReturnsEmptyWhenDirectoryDoesNotExist() throws Exception {
        ReflectionTestUtils.setField(docsTreeController, "docsPath", "target/non-existent-docs-dir");

        mockMvc.perform(get("/docs/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
