package com.nearfuturelaboratory.humans.scheduler;

import com.nearfuturelaboratory.humans.instagram.entities.InstagramUser;
import com.nearfuturelaboratory.humans.service.InstagramAnalyticsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.quartz.*;

import java.io.IOException;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by julian on 1/12/16.
 */
@SuppressWarnings("WeakerAccess")
@DisallowConcurrentExecution
public class ScheduledFollowersFetcherJob implements Job{
}
