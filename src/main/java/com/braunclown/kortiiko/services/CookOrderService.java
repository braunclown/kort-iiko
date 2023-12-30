package com.braunclown.kortiiko.services;

import com.braunclown.kortiiko.data.CookOrder;
import com.braunclown.kortiiko.data.CookOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public class CookOrderService {

    private final CookOrderRepository repository;

    public CookOrderService(CookOrderRepository repository) {
        this.repository = repository;
    }

    public Optional<CookOrder> get(Long id) {
        return repository.findById(id);
    }

    public CookOrder update(CookOrder entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<CookOrder> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<CookOrder> list(Pageable pageable, Specification<CookOrder> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }
}
