package com.braunclown.kortiiko.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DishRepository extends JpaRepository<Dish, Long>, JpaSpecificationExecutor<Dish> {

    Optional<Dish> findByIikoId(String iikoId);

    List<Dish> findByParentGroup(Dish parentGroup);

    List<Dish> findByParentGroupIsNull();

    List<Dish> findByIsGroupTrue();

    List<Dish> findByIsGroup(Boolean isGroup);
}
