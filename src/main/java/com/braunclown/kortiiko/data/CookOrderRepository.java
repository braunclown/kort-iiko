package com.braunclown.kortiiko.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CookOrderRepository extends JpaRepository<CookOrder, Long>, JpaSpecificationExecutor<CookOrder> {

    List<CookOrder> findByPeriodAndIsVisible(Period period, Boolean isVisible);
}
