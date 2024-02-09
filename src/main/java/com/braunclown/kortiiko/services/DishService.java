package com.braunclown.kortiiko.services;

import com.braunclown.kortiiko.data.Dish;
import com.braunclown.kortiiko.data.DishRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DishService {

    private final DishRepository repository;

    public DishService(DishRepository repository) {
        this.repository = repository;
    }

    public Optional<Dish> get(Long id) {
        return repository.findById(id);
    }

    public Dish getByIikoId(String iikoId) {
        return repository.findByIikoId(iikoId);
    }

    public Dish update(Dish entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public Page<Dish> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Dish> list(Pageable pageable, Specification<Dish> filter) {
        return repository.findAll(filter, pageable);
    }

    public List<Dish> findAll() {
        return repository.findAll();
    }

    public List<Dish> findByParentGroup(Dish parentGroup) {
        return repository.findByParentGroup(parentGroup);
    }

    public List<Dish> findRoots() {
        return repository.findByParentGroupIsNull();
    }

    public int count() {
        return (int) repository.count();
    }
}
