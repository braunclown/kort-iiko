package com.braunclown.kortiiko.services;

import com.braunclown.kortiiko.data.User;
import com.braunclown.kortiiko.data.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<User> get(Long id) {
        return repository.findById(id);
    }

    public User update(User entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<User> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<User> list(Pageable pageable, Specification<User> filter) {
        return repository.findAll(filter, pageable);
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public int count() {
        return (int) repository.count();
    }

    public boolean usernameIsTaken(String username) {
        return repository.findByUsername(username) != null;
    }

    public boolean usernameIsTaken(String username, Long id) {
        User user = repository.findByUsername(username);
        return user != null && !user.getId().equals(id);
    }
}
