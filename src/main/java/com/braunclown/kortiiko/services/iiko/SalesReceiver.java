package com.braunclown.kortiiko.services.iiko;

import com.braunclown.kortiiko.data.Period;
import com.braunclown.kortiiko.data.Sale;
import com.braunclown.kortiiko.services.CookOrderService;
import com.braunclown.kortiiko.services.PeriodService;
import com.braunclown.kortiiko.services.SaleService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class SalesReceiver {
    private final SaleService saleService;
    private final PeriodService periodService;
    private final SalesImportService salesImportService;
    private final CookOrderService cookOrderService;

    public SalesReceiver(SaleService saleService,
                         PeriodService periodService,
                         SalesImportService salesImportService,
                         CookOrderService cookOrderService) {
        this.saleService = saleService;
        this.periodService = periodService;
        this.salesImportService = salesImportService;
        this.cookOrderService = cookOrderService;
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
            List<Sale> sales = salesImportService.importSales(period);
            Optional<Period> nextPeriod = periodService.getNext(period);
            nextPeriod.ifPresent(value -> cookOrderService.calculateOrders(value, sales));
            System.out.println("Hello! This is test");
        }

    }
}
