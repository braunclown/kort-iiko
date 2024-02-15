package com.braunclown.kortiiko.data;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "sale")
public class Sale extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "dish_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Dish dish;

    @ManyToOne
    @JoinColumn(name = "period_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Period period;

    private LocalDateTime time;

    private Double amount;

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
