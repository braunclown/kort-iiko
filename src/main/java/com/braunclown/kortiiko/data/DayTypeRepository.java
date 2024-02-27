package com.braunclown.kortiiko.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DayTypeRepository extends JpaRepository<DayType, Long>, JpaSpecificationExecutor<DayType> {

}
