package com.braunclown.kortiiko.services;

import com.braunclown.kortiiko.data.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CookOrderService {

    private final CookOrderRepository repository;
    private final DishService dishService;
    private final DishSettingService dishSettingService;

    public CookOrderService(CookOrderRepository repository,
                            DishService dishService,
                            DishSettingService dishSettingService) {
        this.repository = repository;
        this.dishService = dishService;
        this.dishSettingService = dishSettingService;
    }

    public Optional<CookOrder> get(Long id) {
        return repository.findById(id);
    }

    public CookOrder update(CookOrder entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<CookOrder> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<CookOrder> list(Pageable pageable, Specification<CookOrder> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    /**
     * Запустить калькулятор заказов
     * @param period Период, для которого рассчитываем заказ
     * @param sales Список продаж за предыдущий период
     */
    public void calculateOrders(Period period, List<Sale> sales) {
        List<Dish> dishes = dishService.findDishes();
        for (Dish dish: dishes) {
            if (dish.getMode() == Mode.MAX) {
                Optional<DishSetting> dishSetting =
                        dishSettingService.getByDishAndStablePeriod(dish, period.getStablePeriod());
                if (dishSetting.isPresent()) {
                    DishSetting ds = dishSetting.get();
                    if (dish.getAmount() < ds.getMinAmount()) {
                        calculateMaxOrder(ds, period);
                    }
                } else {
                    // TODO: Сообщить админу
                    System.out.println("АХТУНГ! Не настроил режим пополнения для блюда " + dish
                            + ", периода " + period.getStablePeriod() + "-" + period.getEndTime());
                }

            } else {
                double amountSold = sales.stream()
                        .filter(sale -> sale.getDish().equals(dish))
                        .mapToDouble(Sale::getAmount).sum();
                if (amountSold > 0)
                    calculateSalesOrder(dish, period, amountSold);
            }
        }
    }

    private void calculateSalesOrder(Dish dish, Period period, Double amountSold) {
        CookOrder order = new CookOrder();
        order.setDish(dish);
        order.setAmountOrdered(amountSold);
        order.setPeriod(period);
        order.setVisible(true);
        update(order);
    }

    private void calculateMaxOrder(DishSetting dishSetting, Period period) {
        CookOrder order = new CookOrder();
        order.setDish(dishSetting.getDish());
        order.setAmountOrdered(calculateMinMax(dishSetting));
        order.setPeriod(period);
        order.setVisible(true);
        update(order);
    }

    private Double calculateMinMax(DishSetting dishSetting) {
        if (dishSetting.getMaxAmount() - dishSetting.getMinAmount() < dishSetting.getDish().getMultiplicity()) {
            // TODO: Сообщить админу об ошибке
            System.out.println("АХТУНГ! Не могу готовить блюдо + " + dishSetting.getDish().getName() + " +: кратность меньше разности max и min");
            return 0d;
        } else {
            return Math.round((dishSetting.getMaxAmount() - dishSetting.getDish().getAmount())
                    / dishSetting.getDish().getMultiplicity())
                    * dishSetting.getDish().getMultiplicity();
        }
    }

    public List<CookOrder> getCurrentOrders(Period period) {
        return repository.findByPeriodAndIsVisible(period, true);
    }
}
