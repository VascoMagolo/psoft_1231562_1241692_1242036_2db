package aisafe.aircrafts.infrastructure.persistence;

import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class AircraftModelRepositoryImpl implements AircraftModelRepository {

    private final SpringDataAircraftModelRepository springRepo;

    @Override
    public long count() {
        return springRepo.count();
    }

    public AircraftModelRepositoryImpl(SpringDataAircraftModelRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public Optional<AircraftModel> findByModelName(String modelName) {
        return springRepo.findByModelName(modelName)
                .map(AircraftModelMapper::toDomain);
    }

    @Override
    public boolean existsByModelName(String modelName) {
        return springRepo.existsByModelName(modelName);
    }

    @Override
    public List<AircraftModel> findAll(int pageNumber, int pageSize) {
        var springPageable = PageRequest.of(pageNumber, pageSize);

        return springRepo.findAll(springPageable)
                .stream()
                .map(AircraftModelMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(AircraftModel domainModel) {
        AircraftModelJpaEntity jpaEntity = springRepo.findByModelName(domainModel.getModelName())
                .orElse(new AircraftModelJpaEntity());

        AircraftModelJpaEntity newData = AircraftModelMapper.toJpa(domainModel);

        if (jpaEntity.getId() != null) {
            newData.setId(jpaEntity.getId());
        }

        springRepo.save(newData);
    }

    @Override
    public void delete(AircraftModel domainModel) {
        AircraftModelJpaEntity jpaEntity = springRepo.findByModelName(domainModel.getModelName())
                .orElseThrow(() -> new IllegalArgumentException("Aircraft model not found: " + domainModel.getModelName()));
        springRepo.delete(jpaEntity);
    }
}