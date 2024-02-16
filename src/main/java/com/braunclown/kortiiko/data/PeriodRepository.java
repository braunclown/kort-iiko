package com.braunclown.kortiiko.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface PeriodRepository extends JpaRepository<Period, Long>, JpaSpecificationExecutor<Period> {

    List<Period> findByStartTimeBetween(LocalDateTime startTimeStart, LocalDateTime startTimeEnd);
}
