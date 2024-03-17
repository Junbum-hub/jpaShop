package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class,id);
    }


//    public List<Order> fidnAll(OrderSearch orderSearch) {
//        return em.createQuery("select o from Order o join o.member m" +
//                        " where o.status =: status " +
//                        " and m.name like :name",Order.class)
//                .setParameter("status",orderSearch.getOrderStatus())
//                .setParameter("name",orderSearch.getMemberName())
//                .setMaxResults(1000)
//                .getResultList();
//    }

    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName()
                            + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();
    }


    public List<Order> findAllWithMemberDelivery() {
       return  em.createQuery(
                     "select o from Order o " +
                        "join fetch o.member m " +
                        "join fetch o.delivery d", Order.class

        ).getResultList();

    }
    public List<Order> findAllWithItem() {
        return  em.createQuery(
                "select distinct o from Order o " +
                        "join fetch o.member m " +   //toOne 관계
                        "join fetch o.delivery d " + //toOne 관계는 페치조인을 계속 걸어도 페이징에 상관없다! 데이터 뻥튀기 X
                        "join fetch o.orderItems oi " + //toMany 관계(컬렉션) 은 지연로딩으로 조회한다
                        "join fetch oi.item" , Order.class

        ).getResultList();

    }


    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return  em.createQuery(
                "select o from Order o " +
                        "join fetch o.member m " +
                        "join fetch o.delivery d", Order.class

        )       .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList(); //이렇게하면 v3방식보다 원하는 만큼의 데이터와 가까운 양의 데이터만 전송할 수 있고, 이 방식이 쿼리는 더
                                  //나갈 수 있지만 이 방법이 더 효율적일수 있다 (1+N -> 1+1) 그리고 페이징도 가능하다(batch_fetch 사이즈로 IN 쿼리 날림
    }
}
