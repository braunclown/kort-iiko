package com.braunclown.kortiiko.services;

import com.braunclown.kortiiko.data.Sale;
import com.braunclown.kortiiko.data.SaleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SaleService {

    private final SaleRepository repository;

    public SaleService(SaleRepository repository) {
        this.repository = repository;
    }

    public Optional<Sale> get(Long id) {
        return repository.findById(id);
    }

    public Sale update(Sale entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Sale> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Sale> list(Pageable pageable, Specification<Sale> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }
}
