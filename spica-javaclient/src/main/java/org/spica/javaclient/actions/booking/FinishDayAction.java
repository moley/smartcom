package org.spica.javaclient.actions.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spica.javaclient.actions.AbstractAction;
import org.spica.javaclient.actions.ActionContext;
import org.spica.javaclient.actions.ActionGroup;
import org.spica.javaclient.actions.Command;
import org.spica.javaclient.model.EventInfo;
import org.spica.javaclient.model.ModelCache;
import org.spica.javaclient.params.CommandLineArguments;
import org.spica.javaclient.params.ConfirmInputParam;
import org.spica.javaclient.params.InputParamGroup;
import org.spica.javaclient.params.InputParams;
import org.spica.javaclient.timetracker.TimetrackerService;
import org.spica.javaclient.utils.DateUtil;

public class FinishDayAction extends AbstractAction {

    private final static Logger LOGGER = LoggerFactory.getLogger(FinishDayAction.class);

    private DateUtil dateUtil = new DateUtil();

    public final static String DISPLAY_NAME = "Finish day";
    public final static String KEY_CONFIRM_LATER = "CONFIRM_LATER";

    @Override public String getDisplayname() {
        return "Finish day";
    }

    @Override
    public String getDescription() {
        return "Finished the timetracker and closes the day (parameter can be stop time)";
    }

    @Override
    public void execute(ActionContext actionContext, InputParams inputParams, CommandLineArguments commandLineArguments) {
        LOGGER.info("Finish day called with parameter " + commandLineArguments);



        LocalDate lastOpenEventDate = getDateLastOpenEvent(actionContext);
        if (lastOpenEventDate.equals(LocalDate.now()) || inputParams.getInputValueAsBoolean(KEY_CONFIRM_LATER, Boolean.FALSE)) {

            if (inputParams.getInputValueAsBoolean(KEY_CONFIRM_LATER, Boolean.FALSE) && commandLineArguments.isEmpty()) {
                outputError("If you want to finish a previous day you have to add the stop time as parameter");
            }
            else {
                LocalDateTime stopTime = LocalDateTime.now();
                if (! commandLineArguments.isEmpty()) {
                    LocalTime localTime = dateUtil.getTime(commandLineArguments.getSingleArgument());
                    stopTime = LocalDateTime.of(LocalDate.now(), localTime);
                }
                TimetrackerService timetrackerService = new TimetrackerService();
                timetrackerService.setModelCacheService(actionContext.getModelCacheService());
                timetrackerService.finishDay(stopTime);
                outputOk("Finished day at " + dateUtil.getTimeAsString(stopTime));
            }
        }
    }

    @Override
    public ActionGroup getGroup() {
        return ActionGroup.BOOKING;
    }

    @Override
    public Command getCommand() {
        return new Command ("day", "d");
    }

    private LocalDate getDateLastOpenEvent (ActionContext actionContext) {
        ModelCache modelCache = actionContext.getModelCache();
        EventInfo lastOpenEvent = modelCache.findLastOpenEvent();
        return lastOpenEvent.getStart().toLocalDate();
    }

    @Override
    public InputParams getInputParams(ActionContext actionContext, CommandLineArguments commandLineArguments) {


        InputParams inputParams = new InputParams();

        LocalDate lastOpenEventDate = getDateLastOpenEvent(actionContext);

        if (lastOpenEventDate.isBefore(LocalDate.now())) {
            InputParamGroup lastOpenBookingNotFromToday = new InputParamGroup();
            inputParams.getInputParamGroups().add(lastOpenBookingNotFromToday);
            String day = dateUtil.getDateAsString(lastOpenEventDate);
            ConfirmInputParam confirmInputParam = new ConfirmInputParam(KEY_CONFIRM_LATER, "An open event was found from " + day + ". Do you want to finish this day?", "Y");
            InputParamGroup inputParamGroupTopic = new InputParamGroup();
            inputParamGroupTopic.getInputParams().add(confirmInputParam);
            inputParams.getInputParamGroups().add(inputParamGroupTopic);

        }

        return inputParams;
    }
}
