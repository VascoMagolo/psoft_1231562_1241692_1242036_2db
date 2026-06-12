package aisafe.maintenance.domain;

import org.springframework.util.Assert;

public class MaintenancePart {
    private String partNumber;
    private String name;
    private String description;
    private Integer stockQuantity;
    private Integer minimumThreshold;
    private MaintenanceComponent component;

    public MaintenancePart(String partNumber, String name, String description,
                           Integer stockQuantity, Integer minimumThreshold, MaintenanceComponent component) {
        Assert.hasText(name, "Part name must not be empty.");
        Assert.notNull(partNumber, "Part number must not be null.");
        Assert.notNull(stockQuantity, "Stock quantity must not be null.");
        Assert.notNull(minimumThreshold, "Minimum threshold must not be null.");
        Assert.notNull(component, "Component must not be null");
        Assert.isTrue(stockQuantity >= 0, "Stock quantity must be non-negative.");
        Assert.isTrue(minimumThreshold >= 0, "Minimum threshold must be non-negative.");
        this.partNumber = partNumber;
        this.name = name;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.minimumThreshold = minimumThreshold;
        this.component = component;
    }

    public String getPartNumber() { return partNumber; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Integer getMinimumThreshold() { return minimumThreshold; }
    public MaintenanceComponent getComponent() { return component; }

}
