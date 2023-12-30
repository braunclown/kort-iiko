package com.braunclown.kortiiko.services;

import com.braunclown.kortiiko.data.StablePeriod;
import com.braunclown.kortiiko.data.StablePeriodRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

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

    public Page<StablePeriod> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<StablePeriod> list(Pageable pageable, Specification<StablePeriod> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }
}
