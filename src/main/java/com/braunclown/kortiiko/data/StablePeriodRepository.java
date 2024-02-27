package com.braunclown.kortiiko.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface StablePeriodRepository extends JpaRepository<StablePeriod, Long>, JpaSpecificationExecutor<StablePeriod> {

    List<StablePeriod> findByDayType(DayType dayType);

    long countByDayType(DayType dayType);
}
