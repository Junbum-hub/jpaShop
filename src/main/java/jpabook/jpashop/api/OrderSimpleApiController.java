package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  xToOne(ManyToOne, OneToOne)
 *  Order
 *  Order -> Member
 *  Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByCriteria(new OrderSearch());
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {

        List<Order> orders = orderRepository.findAllByCriteria(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;

    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();

        //v4로 하게되면 select 에서 원하는 필드만 (DTO에 있는 필드) select 하게된다
        //즉 select 절이 줄게 된다
        //하지만 단점은 재사용성이 줄어든다(리포지토리 재사용성이 떨어짐, api 스펙에 맞춘 코드가 리포지토리에 들어가는 단점)
        //왜냐하면 특정 DTO의 값만 가져올 수 있기 때문이다(fit 하게 만듬)
        //성능이 중요할때는 이렇게 사용하는게 좋지만 만약 재사용성을 생각하면 v3처럼 사용해야한다
        //하지만 대부분의 경우에는 v3이나 v4나 성능차이는 많이나지 않는다
        //이런식으로 레포지토리 패키지에 따로 order.simplequery 라는 패키지를 따로파서 화면용 조회 레포를 따로파는것이 좋다
        //이래야 유지보수성이 좋아진다
        //쿼리방식 선택 권장순서
        //1. 엔티티를 DTO로 변환하는 방법을 선택한다.
        //2. 필요하면 페치 조인으로 성능을 최적화한다. -> 대부분의 성능 이슈 해결.
        //3. 그래도 안되면 DTO로 직접조회하는 방법을 사용한다.
        //4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC 템플릿을 사용해서 SQL을 직접 사용한다.

    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }

}
