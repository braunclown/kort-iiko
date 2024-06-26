package com.braunclown.kortiiko.services;

import com.braunclown.kortiiko.data.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class PeriodService {

    private final StablePeriodService stablePeriodService;
    private final PeriodRepository repository;

    public PeriodService(PeriodRepository repository, StablePeriodService stablePeriodService) {
        this.repository = repository;
        this.stablePeriodService = stablePeriodService;
    }

    public Optional<Period> get(Long id) {
        return repository.findById(id);
    }

    public Period update(Period entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Period> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Period> list(Pageable pageable, Specification<Period> filter) {
        return repository.findAll(filter, pageable);
    }

    public Optional<Period> getNext(Period period) {
        return repository.findFirstByStartTimeGreaterThanEqualOrderByStartTimeAsc(period.getEndTime());
    }

    public List<Period> findTodayPeriods() {
        LocalDateTime todayMidnight = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime tomorrowMidnight = LocalDateTime.now().plusDays(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        return repository.findByStartTimeBetween(todayMidnight, tomorrowMidnight);
    }

    public void createTodayPeriods(DayType dayType) {
        LocalDateTime todayMidnight = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<StablePeriod> stablePeriods = stablePeriodService.findByDayType(dayType);
        for (StablePeriod stablePeriod: stablePeriods) {
            if (stablePeriod.getEndTime().isAfter(LocalTime.now())) {
                Period period = new Period();
                period.setStablePeriod(stablePeriod);
                period.setStartTime(todayMidnight
                        .withHour(stablePeriod.getStartTime().getHour())
                        .withMinute(stablePeriod.getStartTime().getMinute()));
                period.setEndTime(todayMidnight
                        .withHour(stablePeriod.getEndTime().getHour())
                        .withMinute(stablePeriod.getEndTime().getMinute()));
                this.update(period);
            }
        }
    }

    public Optional<Period> getCurrent() {
        LocalDateTime now = LocalDateTime.now();
        return repository.findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(now, now);
    }

    public Optional<Period> getNext() {
        return repository.findFirstByStartTimeGreaterThanEqualOrderByStartTimeAsc(LocalDateTime.now());
    }

    public int count() {
        return (int) repository.count();
    }
}
