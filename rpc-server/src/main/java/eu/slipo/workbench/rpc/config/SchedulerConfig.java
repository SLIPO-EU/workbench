package eu.slipo.workbench.rpc.config;

import java.time.LocalDate;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.support.CronTrigger;

@Configuration
@EnableScheduling
public class SchedulerConfig
{
    @Autowired
    TaskScheduler scheduler;
    
    @Value("${slipo.rpc-server.scheduler.greeting.cron}")
    String greetingCronExpression;
    
    @PostConstruct
    public void initialize()
    {
        // Schedule several tasks based on configuration settings
        
        // Note: Another approach would be to decentralize definition of 
        // scheduled tasks (use EnableScheduling/Scheduled annotations).
        
        Trigger trigger1 = new CronTrigger(greetingCronExpression);
        scheduler.schedule(() -> {
            System.err.printf("%s [%s] Hello World (%s)\n", 
                LocalDate.now(), Thread.currentThread().getName(), greetingCronExpression);
        }, trigger1);
    }
}