package aisafe.aircrafts.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JpaMappingAndHelperTest {

    @Test
    void ensureAircraftMapperNullInputs() {
        assertNull(AircraftMapper.toDomain(null, null));
        assertNull(AircraftMapper.toJpa(null, null));
    }

    @Test
    void ensureAircraftModelMapperNullInputs() {
        assertNull(AircraftModelMapper.toDomain(null));
        assertNull(AircraftModelMapper.toJpa(null));
    }

    @Test
    void ensureRegistrationNumberJpaEmbeddableGetterSetter() {
        RegistrationNumberJpaEmbeddable embeddable = new RegistrationNumberJpaEmbeddable();
        embeddable.setNumber("CS-TPA");
        assertEquals("CS-TPA", embeddable.getNumber());
    }

    @Test
    void ensureAircraftModelImageJpaEmbeddableGetterSetter() {
        AircraftModelImageJpaEmbeddable embeddable = new AircraftModelImageJpaEmbeddable();
        byte[] bytes = {1, 2, 3};
        embeddable.setBytes(bytes);
        embeddable.setContentType("image/png");
        assertArrayEquals(bytes, embeddable.getBytes());
        assertEquals("image/png", embeddable.getContentType());
    }

    @Test
    void ensureAircraftModelMapperWithImage() {
        AircraftModelImage domainImage = new AircraftModelImage(new byte[]{1, 2, 3}, "image/png");
        AircraftModel domainModel = new AircraftModel(
                "A320",
                Manufacturer.AIRBUS,
                26730.0,
                6150.0,
                833.0,
                domainImage,
                180
        );

        AircraftModelJpaEntity jpaEntity = AircraftModelMapper.toJpa(domainModel);
        assertNotNull(jpaEntity.getImage());
        assertArrayEquals(new byte[]{1, 2, 3}, jpaEntity.getImage().getBytes());
        assertEquals("image/png", jpaEntity.getImage().getContentType());

        AircraftModel domainModelMappedBack = AircraftModelMapper.toDomain(jpaEntity);
        assertNotNull(domainModelMappedBack.getImage());
        assertArrayEquals(new byte[]{1, 2, 3}, domainModelMappedBack.getImage().getBytes());
        assertEquals("image/png", domainModelMappedBack.getImage().getContentType());
    }

    @Test
    void ensureMappersCanBeInstantiated() {
        assertNotNull(new AircraftMapper());
        assertNotNull(new AircraftModelMapper());
    }

    @Test
    void ensureAircraftModelMapperWithNullImageBytes() {
        AircraftModelJpaEntity jpaEntity = new AircraftModelJpaEntity();
        jpaEntity.setModelName("A320");
        jpaEntity.setManufacturer(Manufacturer.AIRBUS);
        jpaEntity.setFuelCapacity(26730.0);
        jpaEntity.setMaxRange(6150.0);
        jpaEntity.setCruisingSpeed(833.0);
        jpaEntity.setMaximumSeatingCapacity(180);

        AircraftModelImageJpaEmbeddable embeddable = new AircraftModelImageJpaEmbeddable();
        embeddable.setBytes(null);
        embeddable.setContentType("image/png");
        jpaEntity.setImage(embeddable);

        AircraftModel domainModel = AircraftModelMapper.toDomain(jpaEntity);
        assertNull(domainModel.getImage());
    }
}
