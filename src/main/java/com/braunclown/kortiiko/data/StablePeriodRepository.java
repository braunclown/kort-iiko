package com.braunclown.kortiiko.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StablePeriodRepository extends JpaRepository<StablePeriod, Long>, JpaSpecificationExecutor<StablePeriod> {

}
