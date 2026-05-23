package aisafe.maintenance.domain;


import org.springframework.util.Assert;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class MaintenancePart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column( nullable = false )
    private String partNumber;
    @Column ( nullable = false )
    private String name;
    private String description;
    @Column ( nullable = false )
    private Integer stockQuantity;
    @Column ( nullable = false )
    private Integer minimumThreshold;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MaintenanceComponent component;

    public MaintenancePart () {}

    public MaintenancePart (String partNumber, String name, String description, Integer stockQuantity, Integer minimumThreshold, MaintenanceComponent component) {
        Assert.hasText(name, "Part name must not be empty.");
        Assert.notNull(partNumber, "Part number must not be null.");
        Assert.notNull(stockQuantity, "Stock quantity must not be null.");
        Assert.notNull(minimumThreshold, "Minimum threshold must not be null.");
        Assert.notNull(component,"Component must not be null");
        Assert.isTrue(stockQuantity >= 0, "Stock quantity must be non-negative.");
        Assert.isTrue(minimumThreshold >= 0, "Minimum threshold must be non-negative.");
        this.partNumber = partNumber;
        this.name = name;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.minimumThreshold = minimumThreshold;
        this.component = component;
    }
}
