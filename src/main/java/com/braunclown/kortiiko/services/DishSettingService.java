package com.braunclown.kortiiko.services;

import com.braunclown.kortiiko.data.DishSetting;
import com.braunclown.kortiiko.data.DishSettingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public class DishSettingService {
    private final DishSettingRepository repository;

    public DishSettingService(DishSettingRepository repository) {
        this.repository = repository;
    }

    public Optional<DishSetting> get(Long id) {
        return repository.findById(id);
    }

    public DishSetting update(DishSetting entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<DishSetting> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<DishSetting> list(Pageable pageable, Specification<DishSetting> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }
}
