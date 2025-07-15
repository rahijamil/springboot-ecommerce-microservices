package com.rahi.order_service.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.rahi.order_service.dto.InventoryResponse;
import com.rahi.order_service.dto.OrderLineItemsDto;
import com.rahi.order_service.dto.OrderRequest;
import com.rahi.order_service.event.OrderPlacedEvent;
import com.rahi.order_service.model.Order;
import com.rahi.order_service.model.OrderLineItems;
import com.rahi.order_service.repository.OrderRepository;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObservationRegistry observationRegistry;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        String orderNumber = UUID.randomUUID().toString();
        order.setOrderNumber(orderNumber);
        log.info("Placing order with number: {}", orderNumber);

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();

        // Call Inventory Service, and place order if product isin stock
        Observation inventoryServiceObservation = Observation.createNotStarted("inventory-service-lookup",
                this.observationRegistry);

        inventoryServiceObservation.lowCardinalityKeyValue("call", "inventory-service");

        return inventoryServiceObservation.observe(() -> {
            try {
                InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                        .uri("http://inventory-service/api/inventory",
                                uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                        .retrieve()
                        .bodyToMono(InventoryResponse[].class)
                        .block();

                boolean allProductsInStock = Arrays.stream(inventoryResponses).allMatch(InventoryResponse::isInStock);

                if (allProductsInStock) {
                    log.info("All products in stock - saving order: {}", order);
                    orderRepository.save(order);
                    log.info("Order saved");

                    kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
                    log.info("Sent Kafka event");

                    return "Order Placed Successfully";
                } else {
                    throw new IllegalArgumentException("Product is not in stock, please try again later");
                }
            } catch (Exception ex) {
                log.error("Error in placing order", ex);
                throw ex;
            }
        });
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemDto.getPrice());
        orderLineItems.setQuantity(orderLineItemDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemDto.getSkuCode());
        return orderLineItems;
    }
}
