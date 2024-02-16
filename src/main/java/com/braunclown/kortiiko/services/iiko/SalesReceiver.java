package com.braunclown.kortiiko.services.iiko;

import com.braunclown.kortiiko.data.Period;
import com.braunclown.kortiiko.services.PeriodService;
import com.braunclown.kortiiko.services.SaleService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;

@Service
public class SalesReceiver {
    private SaleService saleService;
    private PeriodService periodService;

    public SalesReceiver(SaleService saleService, PeriodService periodService) {
        this.saleService = saleService;
        this.periodService = periodService;
    }

    public void planRequests(List<Period> todayPeriods) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(100);
        scheduler.initialize();

        for (Period period: todayPeriods) {
            scheduler.schedule(new SalesRequest(period),
                    period.getEndTime().atZone(ZoneId.of("Europe/Moscow")).toInstant());
        }
    }

    private class SalesRequest implements Runnable {

        private final Period period;

        public SalesRequest(Period period) {
            this.period = period;
        }

        @Override
        public void run() {
            // TODO: Добавить получение продаж
            System.out.println("Hello! This is test");
        }

    }
}
