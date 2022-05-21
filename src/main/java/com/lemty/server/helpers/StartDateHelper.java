package com.lemty.server.helpers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.lemty.server.jobPayload.CampaignPayload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StartDateHelper {
    Logger logger = LoggerFactory.getLogger(StartDateHelper.class);
    public ZonedDateTime dateParser(CampaignPayload campaignPayload, String timezone, Integer dayGap, Integer hourGap, Integer minuteGap){
        String date = campaignPayload.getStartAt();
        ZoneId id = ZoneId.of(timezone);
        ZonedDateTime d = ZonedDateTime.parse(date).withZoneSameInstant(id);

        String stringDateTime = String.format("%d-%02d-%02dT%02d:%02d:%02d", d.getYear(), d.getMonthValue(), (d.getDayOfMonth() + dayGap), (d.getHour() + hourGap), (d.getMinute() + minuteGap), d.getSecond());
        LocalDateTime localDateTime = LocalDateTime.from(LocalDateTime.parse(stringDateTime).atZone(ZoneId.of(timezone)));
        ZonedDateTime startDate = ZonedDateTime.of(localDateTime, ZoneId.of(timezone));

        return startDate;
    }
}
