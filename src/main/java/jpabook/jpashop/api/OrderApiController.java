package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByCriteria(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                orderItem.getItem().getName();
            }

        }
        return all;
    }
    @GetMapping("/api/v2/orders")
    public List<OrdersDto> ordersV2() {
        List<Order> all = orderRepository.findAllByCriteria(new OrderSearch());
        List<OrdersDto> result = all.stream()
                .map(o -> new OrdersDto(o))
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("api/v3/orders")
    public List<OrdersDto> ordersV3() {
        List<Order> all = orderRepository.findAllWithItem();
        List<OrdersDto> result = all.stream()
                .map(o -> new OrdersDto(o))
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("api/v3.1/orders")
    public List<OrdersDto> ordersV3_page(
            @RequestParam(value ="offset",defaultValue = "0") int offset,
            @RequestParam(value ="limit",defaultValue = "100") int limit) {
        List<Order> all = orderRepository.findAllWithMemberDelivery(offset,limit);
        List<OrdersDto> result = all.stream()
                .map(o -> new OrdersDto(o))
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();

    }

    @GetMapping("api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    @Data
    @Getter
    static class OrdersDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
//        private List<OrderItem> orderItems; //DTO 안에 엔티티가 있으면 안된다!! DTO 안에는 엔티티와의 의존을 완전히 끊어야한다!!
        private List<OrderItemDto> orderItems; //이렇게 컬렉션을 받을때도 엔티티가아닌 DTO 로 받아서 출력해야한다
        //정리하면 DTO 내에서 엔티티를 파라미터로 받는것은 괜찮지만 엔티티를 내보내면 안된다

        public OrdersDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
//            orderItems = order.getOrderItems();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());

        }
    }
    @Getter
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getItem().getPrice();
            count = orderItem.getCount();
        }
        //이렇게 orderitem에 대한 DTO도 따로 만들어 주어야 한다! 그래야 의존관계를 완전 끊을 수 있다
    }


}
