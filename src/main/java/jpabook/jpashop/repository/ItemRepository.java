package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if(item.getId() ==null) {
            em.persist(item);
        } else {
            Item merge = em.merge(item);//item 단지 파라미터로 넘어온거고 merge를 통해 반환되는
                                        //객체(merge)가 영속상태이다.
                                        //merge를 사용하면 데이터베이스의 모든 속성을 업데이트하므로 만약
                                        //어떤 필드에 값이 없으면 null로 업데이트 되므로 실무에서 사용X
        }

    }

    public Item findOne(Long id) {
        return em.find(Item.class,id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i",Item.class)
                .getResultList();
    }
}
