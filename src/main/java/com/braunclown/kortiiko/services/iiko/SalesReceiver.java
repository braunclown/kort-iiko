package com.braunclown.kortiiko.services.iiko;

import com.braunclown.kortiiko.data.Period;
import com.braunclown.kortiiko.data.Sale;
import com.braunclown.kortiiko.services.CookOrderService;
import com.braunclown.kortiiko.services.PeriodService;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class SalesReceiver {
    private final PeriodService periodService;
    private final SalesImportService salesImportService;
    private final CookOrderService cookOrderService;
    private final String timezone;

    public SalesReceiver(PeriodService periodService,
                         SalesImportService salesImportService,
                         CookOrderService cookOrderService,
                         Environment environment) {
        this.periodService = periodService;
        this.salesImportService = salesImportService;
        this.cookOrderService = cookOrderService;
        this.timezone = environment.getProperty("settings.timezone", "Europe/Moscow");
    }

    public void planRequests(List<Period> todayPeriods) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(100);
        scheduler.initialize();

        for (Period period: todayPeriods) {
            scheduler.schedule(new SalesRequest(period),
                    period.getEndTime().atZone(ZoneId.of(timezone)).toInstant());
        }
    }

    private class SalesRequest implements Runnable {

        private final Period period;

        public SalesRequest(Period period) {
            this.period = period;
        }

        @Override
        public void run() {
            List<Sale> sales = salesImportService.importSales(period);
            Optional<Period> nextPeriod = periodService.getNext(period);
            nextPeriod.ifPresent(value -> cookOrderService.calculateOrders(value, sales));
            System.out.println("Hello! This is test");
        }

    }
}
