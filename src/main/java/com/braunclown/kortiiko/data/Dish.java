package com.braunclown.kortiiko.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "dish")
public class Dish extends AbstractEntity {

    private String name;
    private Double amount;
    private String iikoId;
    private Double multiplicity;
    private Double initialAmount;
    private Mode mode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getIikoId() {
        return iikoId;
    }

    public void setIikoId(String iikoId) {
        this.iikoId = iikoId;
    }

    public Double getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(Double multiplicity) {
        this.multiplicity = multiplicity;
    }

    public Double getInitialAmount() {
        return initialAmount;
    }

    public void setInitialAmount(Double initialAmount) {
        this.initialAmount = initialAmount;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }
}
