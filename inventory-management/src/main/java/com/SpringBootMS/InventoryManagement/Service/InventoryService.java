package com.SpringBootMS.InventoryManagement.Service;

import com.SpringBootMS.InventoryManagement.Dto.InventoryResponse;
import com.SpringBootMS.InventoryManagement.Model.Inventory;
import com.SpringBootMS.InventoryManagement.Repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    @SneakyThrows
    public List<InventoryResponse> isInStock(List<String> skuCode){
        log.info("Wait Started");
        Thread.sleep(10000);
        log.info("Wait Ended");
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(Inventory -> InventoryResponse
                        .builder()
                        .skuCode(Inventory.getSkuCode())
                        .isInStock(Inventory.getQuantity() > 0)
                        .build()).toList();
    }
}
