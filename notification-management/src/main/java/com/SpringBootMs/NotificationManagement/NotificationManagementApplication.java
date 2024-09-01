package com.SpringBootMs.NotificationManagement;

import com.SpringBootMs.NotificationManagement.Event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.KafkaListener;

@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
public class NotificationManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationManagementApplication.class, args);
    }

    @KafkaListener(topics = "notificationTopic", groupId = "notificationId")
    public void handleNotification(OrderPlacedEvent orderPlacedEvent) {
        log.info("Received Notification for order - {}", orderPlacedEvent.getOrderNumber());
    }
}