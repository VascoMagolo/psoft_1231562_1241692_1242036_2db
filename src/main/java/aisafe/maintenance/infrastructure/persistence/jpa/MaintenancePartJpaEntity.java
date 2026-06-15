package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.maintenance.domain.MaintenanceComponent;
import jakarta.persistence.*;

@Entity
@Table(name = "maintenance_part")
public class MaintenancePartJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String partNumber;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private Integer minimumThreshold;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MaintenanceComponent component;

    protected MaintenancePartJpaEntity() {}

    public MaintenancePartJpaEntity(String partNumber, String name, String description,
                                    Integer stockQuantity, Integer minimumThreshold,
                                    MaintenanceComponent component) {
        this.partNumber = partNumber;
        this.name = name;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.minimumThreshold = minimumThreshold;
        this.component = component;
    }

    public Long getId() { return id; }
    public String getPartNumber() { return partNumber; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Integer getMinimumThreshold() { return minimumThreshold; }
    public MaintenanceComponent getComponent() { return component; }

    public void setId(Long id) { this.id = id; }
}
