package com.braunclown.kortiiko.services;

import com.braunclown.kortiiko.data.DayType;
import com.braunclown.kortiiko.data.StablePeriod;
import com.braunclown.kortiiko.data.StablePeriodRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class StablePeriodService {

    private final StablePeriodRepository repository;

    public StablePeriodService(StablePeriodRepository repository) {
        this.repository = repository;
    }

    public Optional<StablePeriod> get(Long id) {
        return repository.findById(id);
    }

    public StablePeriod update(StablePeriod entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<StablePeriod> findAll() {
        return repository.findAll();
    }

    public List<StablePeriod> findByDayType(DayType dayType) {
        return repository.findByDayType(dayType);
    }

    public List<StablePeriod> findByDayTypeIn(Collection<DayType> dayTypes) {
        return repository.findByDayTypeIn(dayTypes);
    }

    public Page<StablePeriod> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<StablePeriod> list(Pageable pageable, Specification<StablePeriod> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public boolean isOverlapping(StablePeriod period1, StablePeriod period2) {
        return period1.getStartTime().isBefore(period2.getEndTime())
                && period1.getEndTime().isAfter(period2.getStartTime());
    }

    public long countByDayType(DayType dayType) {
        return repository.countByDayType(dayType);
    }
}
