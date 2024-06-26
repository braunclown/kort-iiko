package com.braunclown.kortiiko.services.iiko;

import com.braunclown.kortiiko.data.Period;
import com.braunclown.kortiiko.data.Sale;
import com.braunclown.kortiiko.services.CookOrderService;
import com.braunclown.kortiiko.services.PeriodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

@Service
public class SalesReceiver {
    private final PeriodService periodService;
    private final SalesImportService salesImportService;
    private final CookOrderService cookOrderService;
    private final String timezone;
    private final static List<ScheduledFuture<?>> tasks = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(SalesReceiver.class);

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
        scheduler.setRemoveOnCancelPolicy(true);
        tasks.clear();
        for (Period period: todayPeriods) {
            tasks.add(scheduler.schedule(new SalesRequest(period),
                    period.getEndTime().atZone(ZoneId.of(timezone)).toInstant()));
        }
    }

    public void cancelTasks() {
        for (ScheduledFuture<?> task: tasks) {
            task.cancel(false);
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
            logger.info("Orders have been calculated");
        }

    }
}
