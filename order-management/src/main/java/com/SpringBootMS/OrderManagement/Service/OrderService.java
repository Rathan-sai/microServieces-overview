package com.SpringBootMS.OrderManagement.Service;

import com.SpringBootMS.OrderManagement.Dto.InventoryResponse;
import com.SpringBootMS.OrderManagement.Dto.OrderLineItemsDto;
import com.SpringBootMS.OrderManagement.Dto.OrderRequest;
import com.SpringBootMS.OrderManagement.Event.OrderPlacedEvent;
import com.SpringBootMS.OrderManagement.Model.Order;
import com.SpringBootMS.OrderManagement.Model.OrderLineItems;
import com.SpringBootMS.OrderManagement.Repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    @Autowired
    private final OrderRepository orderRepository;

    @Autowired
    private final WebClient.Builder webClientBuilder;

    private KafkaTemplate<String , OrderPlacedEvent> kafkaTemplate;

    public void orderService(KafkaTemplate<String , OrderPlacedEvent> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

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
            sendMessage(new OrderPlacedEvent(order.getOrderNumber()));
            return "order placed successfully";
        }else {
            throw new IllegalArgumentException("Product is not present. Please try again");
        }
    }

    public void sendMessage(OrderPlacedEvent event) {
        CompletableFuture<SendResult<String, OrderPlacedEvent>> future = kafkaTemplate.send("notificationTopic", event);

        try {
            SendResult<String, OrderPlacedEvent> result = future.get();
            System.out.println("Sent message=[" + event +
                    "] with offset=[" + result.getRecordMetadata().offset() + "]");
        } catch (Exception e) {
            System.out.println("Unable to send message=[" + event + "] due to: " + e.getMessage());
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
