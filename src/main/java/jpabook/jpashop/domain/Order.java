package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY) //XToOne 연관관계는 모두 FetchType.LAZY 로
    @JoinColumn(name = "member_id")
    private Member member;

    @BatchSize(size = 100) //이렇게 각각 페치 배치 사이즈를 정할수 있다
    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL) //영속성 전이
    private List<OrderItem> orderItems = new ArrayList<>();


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; //주문 시간 //이런 컬럼명도 카멜케이스에서
                                     // 언더스코어로 바꾼후 대문자를 소문자로 바꿔준다(order_date)

    @Enumerated(value = EnumType.STRING)
    private OrderStatus status; //주문상태 [ORDER, CANCEL]

    protected Order() {
    } //이런식으로 코드를 제약하는 형태로 짜야 좋은 유지보수가 될 수있다

    //==연관관계 편의 메서드==//
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);

    }

    public void addOrderItem(OrderItem orderItem) {

        this.orderItems.add(orderItem);
        orderItem.setOrders(this);

    }

    public void addDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrders(this);
    }
    //==생성 메서드==//
    public static Order createOrder(Member member, Delivery delivery,OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for(OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    } //이런 스타일로 작성하는 이유는 여기에 생성메서드가 다 있으므로 이것만 바꾸면 되기 때문.
      //이런 스타일로 코드를 작성하는것(엔티티가 비즈니스 로직을 가지고 객체지향의 특성을 적극 활용하는것
      //그리고 서비스 계층은 단순히 엔티티에 필요한 요청을 위임하는 역할을 하는것)을 도메인 모델
      //패턴 이라고한다. 반대로 엔티티에는 비즈니스 로직이 거의 없고 서비스 계층에서 대부분의 비즈니스 로직을 처리하는
      //것을 트랜잭션 스크립트 패턴 이라고 한다

    //==비즈니스 로직==//
    /*
     * 주문 취소
     */
    public void cancel() {
        if(delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException(("이미 배송 완료된 상품은 취소가 불가능합니다"));
        }

        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    //==조회 로직==//
    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice()
    {
        int totalPrice = 0;

        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }



}
