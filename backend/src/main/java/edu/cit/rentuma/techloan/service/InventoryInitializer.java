package edu.cit.rentuma.techloan.service;

import edu.cit.rentuma.techloan.model.InventoryItem;
import edu.cit.rentuma.techloan.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to initialize inventory with sample data on application startup
 */
@Service
public class InventoryInitializer implements CommandLineRunner {

    private final InventoryRepository inventoryRepository;

    public InventoryInitializer(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only initialize if inventory is empty
        if (inventoryRepository.count() == 0) {
            initializeSampleInventory();
        }
    }

    private void initializeSampleInventory() {
        List<InventoryItem> items = List.of(
            createItem("LAPTOP-001", "MacBook Pro 14\"", "Apple MacBook Pro 14 inch with M3 Pro chip", "Laptop",
                      "Excellent", "Apple MacBook Pro M3 Pro, 14GB RAM, 512GB SSD", 3),
            createItem("LAPTOP-002", "Dell XPS 15", "Dell XPS 15 with RTX 4060", "Laptop",
                      "Good", "Dell XPS 15, Intel i7, 16GB RAM, 1TB SSD", 2),
            createItem("TABLET-001", "iPad Pro 12.9\"", "iPad Pro 12.9 inch 2023 model", "Tablet",
                      "Excellent", "iPad Pro 12.9 2023, 256GB, WiFi+Cellular", 2),
            createItem("CAMERA-001", "Canon EOS R5", "Canon EOS R5 Professional Camera", "Camera",
                      "Excellent", "Canon EOS R5, 45MP, RF 24-70mm f/2.8L lens included", 1),
            createItem("CAMERA-002", "Sony A6700", "Sony Alpha 6700 Mirrorless Camera", "Camera",
                      "Good", "Sony A6700, 26.1MP, 16-70mm kit lens", 1),
            createItem("DRONE-001", "DJI Air 3S", "DJI Air 3S Drone with 2 batteries", "Drone",
                      "Excellent", "DJI Air 3S, 4K video, 46-minute flight time", 2),
            createItem("MONITOR-001", "LG UltraWide 34\"", "LG 34\" Curved UltraWide Monitor", "Monitor",
                      "Excellent", "LG 34UC98 Curved UltraWide, 3440x1440, USB-C", 3),
            createItem("KEYBOARD-001", "Keychron K8 Pro", "Mechanical Keyboard with RGB", "Keyboard",
                      "Excellent", "Keychron K8 Pro, Gateron Brown switches, Aluminum frame", 5),
            createItem("MOUSE-001", "MX Master 3S", "Logitech MX Master 3S", "Mouse",
                      "Excellent", "Logitech MX Master 3S, Multi-device, USB-C charging", 4),
            createItem("PROJECTOR-001", "Epson LS500", "Epson LS500 Laser Projector", "Projector",
                      "Good", "Epson LS500, 3LCD laser, 5000 lumens, 4K ready", 1),
            createItem("MONITOR-002", "BenQ ProDesigner PA279DC", "27\" Professional Monitor", "Monitor",
                      "Excellent", "BenQ PA279DC, 27 inch, USB-C with 90W power delivery", 2),
            createItem("MICROPHONE-001", "Rode Podmic Pro", "Professional USB Microphone", "Microphone",
                      "Excellent", "Rode Podmic Pro, XLR, broadcast-quality sound", 2),
            createItem("HEADPHONES-001", "Sony WH-1000XM5", "Premium Noise Cancelling Headphones", "Headphones",
                      "Excellent", "Sony WH-1000XM5, ANC, 30-hour battery, wireless", 3),
            createItem("SPEAKER-001", "Sonos Arc", "Premium Soundbar", "Speaker",
                      "Good", "Sonos Arc, Dolby Atoms, WiFi enabled", 1),
            createItem("EXTERNAL-HDD-001", "Seagate 8TB", "External Hard Drive", "Storage",
                      "Excellent", "Seagate Barracuda Pro 8TB USB 3.0", 4)
        );

        inventoryRepository.saveAll(items);
        System.out.println("✓ Inventory initialized with " + items.size() + " items");
    }

    private InventoryItem createItem(String code, String name, String description, String category,
                                     String condition, String specs, int quantity) {
        InventoryItem item = new InventoryItem(code, name, description, category);
        item.setCondition(condition);
        item.setSpecifications(specs);
        item.setTotalQuantity(quantity);
        item.setAvailableQuantity(quantity);
        return item;
    }
}
