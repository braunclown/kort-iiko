package com.braunclown.kortiiko.data;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "dish_setting")
public class DishSetting extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = "dish_id")
    private Dish dish;

    private Double minAmount;
    private Double maxAmount;

    @ManyToOne
    @JoinColumn(name = "stable_period_id")
    private StablePeriod stablePeriod;

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Double getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Double minAmount) {
        this.minAmount = minAmount;
    }

    public Double getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Double maxAmount) {
        this.maxAmount = maxAmount;
    }

    public StablePeriod getStablePeriod() {
        return stablePeriod;
    }

    public void setStablePeriod(StablePeriod stablePeriod) {
        this.stablePeriod = stablePeriod;
    }
}
