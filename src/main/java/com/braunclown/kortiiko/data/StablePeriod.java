package com.braunclown.kortiiko.data;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalTime;

@Entity
@Table(name = "stable_period")
public class StablePeriod extends AbstractEntity {
    private LocalTime startTime;
    private LocalTime endTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "day_type_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DayType dayType;

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public DayType getDayType() {
        return dayType;
    }

    public void setDayType(DayType dayType) {
        this.dayType = dayType;
    }
}
