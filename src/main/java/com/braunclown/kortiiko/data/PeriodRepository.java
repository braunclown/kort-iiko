package com.braunclown.kortiiko.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PeriodRepository extends JpaRepository<Period, Long>, JpaSpecificationExecutor<Period> {

    List<Period> findByStartTimeBetween(LocalDateTime startTimeStart, LocalDateTime startTimeEnd);

    Optional<Period> findFirstByStartTimeGreaterThanEqualOrderByStartTimeAsc(LocalDateTime startTime);

    Optional<Period> findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(LocalDateTime startTime, LocalDateTime endTime);
}
