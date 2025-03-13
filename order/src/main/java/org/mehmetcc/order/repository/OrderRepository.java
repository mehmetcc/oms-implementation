package org.mehmetcc.order.repository;

import org.mehmetcc.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    // TODO: filtering logic will be here
}
