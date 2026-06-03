package aisafe.aircrafts.infrastructure.persistence;

import aisafe.aircrafts.domain.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class AircraftRepositoryImpl implements AircraftRepository {

    private final SpringDataAircraftRepository springRepo;
    private final SpringDataAircraftModelRepository modelSpringRepo;
    public AircraftRepositoryImpl(SpringDataAircraftRepository springRepo, SpringDataAircraftModelRepository modelSpringRepo) {
        this.springRepo = springRepo;
        this.modelSpringRepo = modelSpringRepo;
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public List<Aircraft> searchAircrafts(String modelName, AircraftStatus status, Integer year, int pageNumber, int pageSize) {
        String statusStr = status != null ? status.name() : null;

        var springPageable = PageRequest.of(pageNumber, pageSize);

        var jpaPage = springRepo.searchAircrafts(modelName, statusStr, year, springPageable);

        return jpaPage.stream()
                .map(jpaEntity -> {
                    var pureModel = AircraftModelMapper.toDomain(jpaEntity.getModel());
                    return AircraftMapper.toDomain(jpaEntity, pureModel);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Aircraft> findAll(int pageNumber, int pageSize) {
        var springPageable = PageRequest.of(pageNumber, pageSize);

        var jpaPage = springRepo.findAll(springPageable);

        return jpaPage.stream()
                .map(jpaEntity -> AircraftMapper.toDomain(
                        jpaEntity,
                        AircraftModelMapper.toDomain(jpaEntity.getModel())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Aircraft> findByRegistrationNumber(RegistrationNumber number) {
        return springRepo.findByRegistrationNumber(number.getNumber())
                .map(jpa -> AircraftMapper.toDomain(jpa, AircraftModelMapper.toDomain(jpa.getModel())));
    }

    @Override
    public boolean existsByRegistrationNumber(RegistrationNumber number) {
        return springRepo.existsByRegistrationNumber(number.getNumber());
    }
    @Override
    public void save(Aircraft aircraft, Long clientVersion) {
        AircraftJpaEntity existingEntity = springRepo.findByRegistrationNumber(aircraft.getRegistrationNumber().getNumber())
                .orElse(null);

        AircraftModelJpaEntity managedModel = modelSpringRepo.findByModelName(aircraft.getModel().getModelName())
                .orElseThrow(() -> new IllegalStateException("Model not found in DB: " + aircraft.getModel().getModelName()));

        AircraftJpaEntity newJpaData = AircraftMapper.toJpa(aircraft, managedModel);

        if (existingEntity != null) {
            newJpaData.setId(existingEntity.getId());
            newJpaData.setVersion(existingEntity.getVersion());

            if (clientVersion != null && !clientVersion.equals(existingEntity.getVersion())) {
                throw new IllegalStateException("Version conflict detected for aircraft with registration number: " + aircraft.getRegistrationNumber().getNumber());
            }
        }

        springRepo.save(newJpaData);
    }
    @Override
    public void delete(Aircraft aircraft){
        AircraftJpaEntity jpaEntity = springRepo.findByRegistrationNumber(aircraft.getRegistrationNumber().getNumber())
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + aircraft.getRegistrationNumber().getNumber()));
        springRepo.delete(jpaEntity);
    }
}