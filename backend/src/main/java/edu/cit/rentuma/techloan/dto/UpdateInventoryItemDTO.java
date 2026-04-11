package edu.cit.rentuma.techloan.dto;

import jakarta.validation.constraints.Min;

/**
 * DTO for updating an existing inventory item (custodian only).
 * All fields are optional — only non-null values are applied (PATCH semantics).
 * Maps to SDD: PUT /inventory/{id} body { name?, description?, category?, quantity?, condition? }
 */
public class UpdateInventoryItemDTO {

    private String name;
    private String description;
    private String category;

    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    private String condition;
    private String specifications;
    private Boolean available;

    public UpdateInventoryItemDTO() {}

    public String getName()                       { return name; }
    public void setName(String name)              { this.name = name; }

    public String getDescription()                { return description; }
    public void setDescription(String desc)       { this.description = desc; }

    public String getCategory()                   { return category; }
    public void setCategory(String category)      { this.category = category; }

    public Integer getQuantity()                  { return quantity; }
    public void setQuantity(Integer quantity)     { this.quantity = quantity; }

    public String getCondition()                  { return condition; }
    public void setCondition(String condition)    { this.condition = condition; }

    public String getSpecifications()             { return specifications; }
    public void setSpecifications(String s)       { this.specifications = s; }

    public Boolean getAvailable()                 { return available; }
    public void setAvailable(Boolean available)   { this.available = available; }
}