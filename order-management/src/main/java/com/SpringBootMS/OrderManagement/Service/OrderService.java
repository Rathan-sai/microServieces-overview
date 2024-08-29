package com.SpringBootMS.OrderManagement.Service;

import com.SpringBootMS.OrderManagement.Dto.InventoryResponse;
import com.SpringBootMS.OrderManagement.Dto.OrderLineItemsDto;
import com.SpringBootMS.OrderManagement.Dto.OrderRequest;
import com.SpringBootMS.OrderManagement.Model.Order;
import com.SpringBootMS.OrderManagement.Model.OrderLineItems;
import com.SpringBootMS.OrderManagement.Repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient.Builder webClientBuilder;

    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItemLists = orderRequest.getOrderLineItemDtos()
                .stream().map(this::maptoDto).toList();
        order.setOrderLineItemList(orderLineItemLists);

        List<String> skuCodes = order.getOrderLineItemList().stream()
                .map(OrderLineItems::getSkuCode).toList();

        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                .uri("http://InventoryManagement/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();


        assert inventoryResponses != null;
        boolean result = Arrays.stream(inventoryResponses).allMatch(InventoryResponse::isInStock);
        if(result){
            orderRepository.save(order);
            return "order placed successfully";
        }else {
            throw new IllegalArgumentException("Product is not present. Please try again");
        }
    }

    private OrderLineItems maptoDto(OrderLineItemsDto orderLineItemDto){
        OrderLineItems OrderLineItem = new OrderLineItems();
        OrderLineItem.setPrice(orderLineItemDto.getPrice());
        OrderLineItem.setId(orderLineItemDto.getId());
        OrderLineItem.setQuantity(orderLineItemDto.getQuantity());
        OrderLineItem.setSkuCode(orderLineItemDto.getSkuCode());

        return OrderLineItem;
    }
}
