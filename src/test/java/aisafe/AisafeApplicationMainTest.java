package aisafe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AisafeApplicationMainTest {

    @Test
    void ensureMainMethodExecutes() {
        System.clearProperty("spring.main.web-application-type");
        System.clearProperty("spring.main.banner-mode");
        
        AisafeApplication app = new AisafeApplication();
        assertNotNull(app);
        
        AisafeApplication.main(new String[]{
            "--spring.main.web-application-type=none",
            "--spring.main.banner-mode=off"
        });
    }
}
