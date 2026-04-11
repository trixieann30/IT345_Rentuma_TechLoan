package edu.cit.rentuma.techloan.repository;

import edu.cit.rentuma.techloan.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByAvailableTrue();
    List<InventoryItem> findByCategory(String category);
    Optional<InventoryItem> findByItemCode(String itemCode);
    List<InventoryItem> findByItemNameContainingIgnoreCase(String itemName);
}
