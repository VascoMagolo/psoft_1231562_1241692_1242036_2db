package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.ModelName;
import aisafe.maintenance.domain.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceMappingAndHelperTest {

    @Test
    void ensureMaintenancePartMapperNullAndRoundtrip() {
        assertNull(MaintenancePartMapper.toDomain(null));

        MaintenancePart domainPart = new MaintenancePart(
                "P123", "Bolt", "Description", 10, 5, MaintenanceComponent.ENGINE
        );

        MaintenancePartJpaEntity jpaEntity = MaintenancePartMapper.toJpa(domainPart);
        assertNotNull(jpaEntity);
        assertEquals("P123", jpaEntity.getPartNumber());
        assertEquals("Bolt", jpaEntity.getName());
        assertEquals("Description", jpaEntity.getDescription());
        assertEquals(10, jpaEntity.getStockQuantity());
        assertEquals(5, jpaEntity.getMinimumThreshold());
        assertEquals(MaintenanceComponent.ENGINE, jpaEntity.getComponent());

        // Setter coverage
        jpaEntity.setId(10L);
        assertEquals(10L, jpaEntity.getId());

        // Cover default constructor of MaintenancePartJpaEntity
        MaintenancePartJpaEntity defaultEntity = new MaintenancePartJpaEntity();
        assertNull(defaultEntity.getId());

        MaintenancePart domainMapped = MaintenancePartMapper.toDomain(jpaEntity);
        assertNotNull(domainMapped);
        assertEquals("P123", domainMapped.getPartNumber());
    }

    @Test
    void ensureMaintenanceTemplateMapperRoundtrip() {
        MaintenanceTemplate domainTemplate = new MaintenanceTemplate(
                "Line Check", MaintenanceType.INSPECTION, List.of(new ModelName("A320")),
                List.of("Verify oil"), 500, 30
        );

        MaintenanceTemplateJpaEntity jpaEntity = MaintenanceTemplateMapper.toJpa(domainTemplate);
        assertNotNull(jpaEntity);
        assertEquals("Line Check", jpaEntity.getName());
        assertEquals(MaintenanceType.INSPECTION, jpaEntity.getTemplateType());
        assertEquals(List.of("A320"), jpaEntity.getApplicableModelNames());
        assertEquals(List.of("Verify oil"), jpaEntity.getChecklist());
        assertEquals(500, jpaEntity.getIntervalFlightHours());
        assertEquals(30, jpaEntity.getIntervalDays());

        // Test Setters
        jpaEntity.setId(20L);
        jpaEntity.setName("New Name");
        jpaEntity.setTemplateType(MaintenanceType.OVERHAUL);
        jpaEntity.setApplicableModelNames(List.of("B737"));
        jpaEntity.setChecklist(List.of("Check landing gear"));
        jpaEntity.setIntervalFlightHours(1000);
        jpaEntity.setIntervalDays(60);

        assertEquals(20L, jpaEntity.getId());
        assertEquals("New Name", jpaEntity.getName());
        assertEquals(MaintenanceType.OVERHAUL, jpaEntity.getTemplateType());
        assertEquals(List.of("B737"), jpaEntity.getApplicableModelNames());
        assertEquals(List.of("Check landing gear"), jpaEntity.getChecklist());
        assertEquals(1000, jpaEntity.getIntervalFlightHours());
        assertEquals(60, jpaEntity.getIntervalDays());

        // Cover default constructor
        MaintenanceTemplateJpaEntity defaultEntity = new MaintenanceTemplateJpaEntity();
        assertNull(defaultEntity.getId());

        MaintenanceTemplate domainMapped = MaintenanceTemplateMapper.toDomain(jpaEntity);
        assertNotNull(domainMapped);
        assertEquals("New Name", domainMapped.getName());
    }

    @Test
    void ensureMaintenanceRecordMapperNull() {
        assertNull(MaintenanceRecordMapper.toDomain(null));
    }

    @Test
    void ensureRegistrationNumberJpaEmbeddable() {
        RegistrationNumberJpaEmbeddable embeddable = new RegistrationNumberJpaEmbeddable();
        embeddable.setNumber("CS-TPA");
        assertEquals("CS-TPA", embeddable.getNumber());

        RegistrationNumberJpaEmbeddable embeddable2 = new RegistrationNumberJpaEmbeddable("CS-TPB");
        assertEquals("CS-TPB", embeddable2.getNumber());
    }

    @Test
    void ensureMappersPrivateConstructors() throws Exception {
        // MaintenancePartMapper
        Constructor<MaintenancePartMapper> partConstructor = MaintenancePartMapper.class.getDeclaredConstructor();
        partConstructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, partConstructor::newInstance);

        // MaintenanceRecordMapper
        Constructor<MaintenanceRecordMapper> recordConstructor = MaintenanceRecordMapper.class.getDeclaredConstructor();
        recordConstructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, recordConstructor::newInstance);

        // MaintenanceTemplateMapper
        Constructor<MaintenanceTemplateMapper> templateConstructor = MaintenanceTemplateMapper.class.getDeclaredConstructor();
        templateConstructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, templateConstructor::newInstance);
    }
}
