package com.braunclown.kortiiko.data;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "dish")
public class Dish extends AbstractEntity {

    private String name;
    private Double amount;
    @Column(unique = true)
    private String iikoId;
    private Double multiplicity;
    private Double initialAmount;
    private Mode mode;
    private String measure;
    private Boolean isGroup;
    @ManyToOne
    private Dish parentGroup;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "parentGroup")
    private Set<Dish> childDishes;

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

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public Boolean getGroup() {
        return isGroup;
    }

    public void setGroup(Boolean group) {
        isGroup = group;
    }

    public Dish getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(Dish parentGroup) {
        this.parentGroup = parentGroup;
    }

    public Set<Dish> getChildDishes() {
        return childDishes;
    }

    public void setChildDishes(Set<Dish> childDishes) {
        this.childDishes = childDishes;
    }
}
