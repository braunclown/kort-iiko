package com.braunclown.kortiiko.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CookOrderRepository extends JpaRepository<CookOrder, Long>, JpaSpecificationExecutor<CookOrder> {

}
