
//---------- Scheduled Tasks ----------

//src/main/java/com/library/management/scheduled/ScheduledTasks.java
package com.codehawk.library.librarymanagement.scheduled;

import com.codehawk.library.librarymanagement.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {
 
 private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
 
 private final TransactionService transactionService;
 
 @Autowired
 public ScheduledTasks(TransactionService transactionService) {
     this.transactionService = transactionService;
 }
 
 // Run every day at midnight to check for overdue books
 @Scheduled(cron = "0 0 0 * * ?")
 public void checkOverdueBooks() {
     logger.info("Running scheduled task to check for overdue books");
     transactionService.updateOverdueStatus();
 }
 
 // Run weekly to generate reports (example)
 @Scheduled(cron = "0 0 0 * * SUN")
 public void generateWeeklyReports() {
     logger.info("Generating weekly library reports");
     // Code to generate reports
 }
}
