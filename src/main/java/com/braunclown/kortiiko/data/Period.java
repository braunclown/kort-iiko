package com.braunclown.kortiiko.data;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "period")
public class Period extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = "stable_period_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private StablePeriod stablePeriod;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public StablePeriod getStablePeriod() {
        return stablePeriod;
    }

    public void setStablePeriod(StablePeriod stablePeriod) {
        this.stablePeriod = stablePeriod;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
