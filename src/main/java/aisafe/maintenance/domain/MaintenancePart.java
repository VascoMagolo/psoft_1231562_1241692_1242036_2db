package aisafe.maintenance.domain;

import java.util.Objects;

public class MaintenancePart {
    private final String partNumber;
    private final String name;
    private String description;
    private Integer stockQuantity;
    private Integer minimumThreshold;
    private final MaintenanceComponent component;

    public MaintenancePart(String partNumber, String name, String description,
                           Integer stockQuantity, Integer minimumThreshold, MaintenanceComponent component) {
        if (name == null || name.trim().isEmpty()) throw new MaintenanceInvalidFieldException("Part name must not be empty.");
        if (partNumber == null) throw new MaintenanceInvalidFieldException("Part number must not be null.");
        if (stockQuantity == null) throw new MaintenanceInvalidFieldException("Stock quantity must not be null.");
        if (minimumThreshold == null) throw new MaintenanceInvalidFieldException("Minimum threshold must not be null.");
        if (component == null) throw new MaintenanceInvalidFieldException("Component must not be null");
        if (stockQuantity < 0) throw new MaintenanceInvalidFieldException("Stock quantity must be non-negative.");
        if (minimumThreshold < 0) throw new MaintenanceInvalidFieldException("Minimum threshold must be non-negative.");
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

    public void updateDetails(String description, Integer stockQuantity, Integer minimumThreshold) {
        if (stockQuantity != null) {
            if (stockQuantity < 0) throw new MaintenanceInvalidFieldException("Stock quantity must be non-negative.");
            this.stockQuantity = stockQuantity;
        }
        if (minimumThreshold != null) {
            if (minimumThreshold < 0) throw new MaintenanceInvalidFieldException("Minimum threshold must be non-negative.");
            this.minimumThreshold = minimumThreshold;
        }
        if (description != null) {
            this.description = description;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaintenancePart that = (MaintenancePart) o;
        return Objects.equals(partNumber, that.partNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partNumber);
    }
}
