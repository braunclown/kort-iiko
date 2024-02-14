package com.braunclown.kortiiko.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DishSettingRepository extends JpaRepository<DishSetting, Long>, JpaSpecificationExecutor<DishSetting> {

    Optional<DishSetting> findByDish_IdAndStablePeriod_Id(Long id, Long id1);
}
