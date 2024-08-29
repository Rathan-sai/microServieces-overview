package com.SpringBootMS.InventoryManagement.Repository;

import com.SpringBootMS.InventoryManagement.Model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findBySkuCodeIn(List<String> skuCode);
}
