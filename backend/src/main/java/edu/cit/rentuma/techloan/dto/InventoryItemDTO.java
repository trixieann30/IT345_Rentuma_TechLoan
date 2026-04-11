package edu.cit.rentuma.techloan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Inventory Item (read-only from frontend)
 */
public class InventoryItemDTO {
    private Long id;
    private String itemCode;
    private String itemName;
    private String description;
    private String category;
    private String condition;
    private Boolean available;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private String specifications;
    private String imageUrl;

    // Constructors
    public InventoryItemDTO() {}

    public InventoryItemDTO(Long id, String itemCode, String itemName, String description, 
                            String category, String condition, Boolean available,
                            Integer totalQuantity, Integer availableQuantity) {
        this.id = id;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.description = description;
        this.category = category;
        this.condition = condition;
        this.available = available;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
