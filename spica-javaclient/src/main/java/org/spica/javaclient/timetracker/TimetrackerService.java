package org.spica.javaclient.timetracker;

import org.spica.javaclient.model.*;
import org.spica.javaclient.utils.DateUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TimetrackerService {

    private DateUtil dateUtil = new DateUtil();

    private ModelCacheService modelCacheService;

    private ModelCache getModelCache () {
        return modelCacheService.get();
    }

    public boolean isPause () {
        ModelCache modelCache = getModelCache();
        EventInfo lastEventInfo = modelCache.findLastOpenEventFromToday();
        return lastEventInfo != null && lastEventInfo.getEventType().equals(EventType.PAUSE);
    }

    public void startPause () {
        stopLastOpenEvent();
        EventInfo pauseEvent = new EventInfo();
        pauseEvent.setId(UUID.randomUUID().toString());
        pauseEvent.setStart(LocalDateTime.now());
        pauseEvent.setEventType(EventType.PAUSE);
        getModelCache().getEventInfosReal().add(pauseEvent);
    }

    public String togglePause () {
        if (isPause()) {
            stopPause();
            return "Pause is stopped at " + dateUtil.getDateAndTimeAsString(LocalDateTime.now());
        } else {
            startPause();
            return "Pause is started at " + dateUtil.getDateAndTimeAsString(LocalDateTime.now());
        }
    }

    private void stopLastOpenEvent() {
        EventInfo eventInfo = getModelCache().findLastOpenEventFromToday();
        if (eventInfo != null) {
            eventInfo.setStop(LocalDateTime.now());
        }
    }

    public void stopPause () {
        stopLastOpenEvent();

        ModelCache modelCache = getModelCache();

        List<EventInfo> eventInfoListToday = modelCache.getEventInfosRealToday();
        if (eventInfoListToday.size() < 2 )
            throw new IllegalStateException("You cannot stop a pause with less than two events. Seems your day started with a pause, which is invalid");

        restartLastRealEvent(modelCache, EventType.PAUSE);
    }

    private void restartLastRealEvent (final ModelCache modelCache, final EventType eventType) {
        EventInfo pauseInfo = modelCache.getEventInfosRealToday().get(modelCache.getEventInfosRealToday().size() - 1);
        if (! pauseInfo.getEventType().equals(eventType))
            throw new IllegalStateException("Your last event is not a started " + eventType.toString() + ". You cannot stop a " + pauseInfo.getEventType().toString() + " at this point");

        EventInfo lastEventInfo = modelCache.getEventInfosRealToday().get(modelCache.getEventInfosRealToday().size() - 2);
        EventInfo newStartedEvent = new EventInfo();
        newStartedEvent.setId(UUID.randomUUID().toString());
        newStartedEvent.setStart(LocalDateTime.now());
        newStartedEvent.setEventType(lastEventInfo.getEventType());
        newStartedEvent.setName(lastEventInfo.getName());
        newStartedEvent.setReferenceId(lastEventInfo.getReferenceId());
        modelCache.getEventInfosReal().add(newStartedEvent);
        modelCacheService.set(modelCache);

    }

    public void startWorkOnTopic (final TopicInfo topicInfo) {
        stopLastOpenEvent();
        ModelCache modelCache = getModelCache();
        EventInfo newStartedEvent = new EventInfo();
        newStartedEvent.setId(UUID.randomUUID().toString());
        newStartedEvent.setStart(LocalDateTime.now());
        newStartedEvent.setEventType(EventType.TOPIC);
        newStartedEvent.setName(topicInfo.getName());
        newStartedEvent.setReferenceId(topicInfo.getId());
        modelCache.getEventInfosReal().add(newStartedEvent);
        modelCacheService.set(modelCache);
    }

    public void startTelephoneCall () {
        stopLastOpenEvent();
        ModelCache modelCache = getModelCache();
        EventInfo eventInfo = new EventInfo();
        eventInfo.setId(UUID.randomUUID().toString());
        eventInfo.setStart(LocalDateTime.now());
        eventInfo.setEventType(EventType.MESSAGE);
        eventInfo.setName("Telephone call");

        modelCache.getEventInfosReal().add(eventInfo);
        modelCacheService.set(modelCache);
    }

    public void finishTelephoneCall (final MessageInfo messageInfo, UserInfo userInfo) {
        EventInfo eventInfo = getModelCache().findLastOpenEventFromToday();
        if (eventInfo == null)
            throw new IllegalStateException("Message not found, but " + eventInfo + "-" + getModelCache().getEventInfosRealToday());

        if (! eventInfo.getEventType().equals(EventType.MESSAGE))
            throw new IllegalStateException("Last event is expected to be a phone call, but is " + eventInfo);
        else {
            eventInfo.setStop(LocalDateTime.now());
            eventInfo.setName("Telephone call with " + userInfo.getName() + ", " + userInfo.getFirstname());
        }

        eventInfo.setReferenceId(messageInfo.getId());

        ModelCache modelCache = getModelCache();

        restartLastRealEvent(modelCache, EventType.MESSAGE);


    }

    public void finishDay() {
        stopLastOpenEvent();
        modelCacheService.set(getModelCache());
    }

    public ModelCacheService getModelCacheService() {
        return modelCacheService;
    }

    public void setModelCacheService(ModelCacheService modelCacheService) {
        this.modelCacheService = modelCacheService;
    }

    public void finishEvent(EventInfo eventInfo) {
        eventInfo.setStop(LocalDateTime.now());
    }
}
