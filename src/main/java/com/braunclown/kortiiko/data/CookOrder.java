package com.braunclown.kortiiko.data;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cook_order")
public class CookOrder extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "cook_id")
    private User cook;

    @ManyToOne
    @JoinColumn(name = "dish_id")
    private Dish dish;

    @ManyToOne
    @JoinColumn(name = "period_id")
    private Period period;

    private Double amountOrdered;
    private Double amountCooked;
    private Boolean isVisible;

    public User getCook() {
        return cook;
    }

    public void setCook(User cook) {
        this.cook = cook;
    }

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

    public Double getAmountOrdered() {
        return amountOrdered;
    }

    public void setAmountOrdered(Double amountOrdered) {
        this.amountOrdered = amountOrdered;
    }

    public Double getAmountCooked() {
        return amountCooked;
    }

    public void setAmountCooked(Double amountCooked) {
        this.amountCooked = amountCooked;
    }

    public Boolean getVisible() {
        return isVisible;
    }

    public void setVisible(Boolean visible) {
        isVisible = visible;
    }
}
