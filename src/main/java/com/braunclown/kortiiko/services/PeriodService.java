package com.braunclown.kortiiko.services;

import com.braunclown.kortiiko.data.Period;
import com.braunclown.kortiiko.data.PeriodRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public class PeriodService {

    private final PeriodRepository repository;

    public PeriodService(PeriodRepository repository) {
        this.repository = repository;
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

    public int count() {
        return (int) repository.count();
    }
}
