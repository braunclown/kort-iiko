package com.braunclown.kortiiko.services;

import com.braunclown.kortiiko.data.DayType;
import com.braunclown.kortiiko.data.DayTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DayTypeService {
    private final DayTypeRepository repository;

    public DayTypeService(DayTypeRepository repository) {
        this.repository = repository;
    }

    public Optional<DayType> get(Long id) {
        return repository.findById(id);
    }

    public DayType update(DayType entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<DayType> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<DayType> list(Pageable pageable, Specification<DayType> filter) {
        return repository.findAll(filter, pageable);
    }

    public List<DayType> findAll() {
        return repository.findAll();
    }

    public int count() {
        return (int) repository.count();
    }
}
