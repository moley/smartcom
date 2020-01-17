package org.spica.javaclient.actions.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spica.javaclient.actions.AbstractAction;
import org.spica.javaclient.actions.ActionContext;
import org.spica.javaclient.actions.ActionGroup;
import org.spica.javaclient.actions.Command;
import org.spica.javaclient.params.CommandLineArguments;
import org.spica.javaclient.params.InputParams;
import org.spica.javaclient.timetracker.TimetrackerService;

public class StartOrStopPauseAction extends AbstractAction {

    private final static Logger LOGGER = LoggerFactory.getLogger(StartOrStopPauseAction.class);

    @Override public String getDisplayname() {
        return "Start or stop pause";
    }

    @Override
    public String getDescription() {
        return "Starts a pause / Stops a pause and starts working on the previous topic again";
    }

    @Override
    public void execute(ActionContext actionContext, InputParams inputParams, CommandLineArguments commandLineArguments) {
        TimetrackerService timetrackerService = new TimetrackerService();
        timetrackerService.setModelCacheService(actionContext.getModelCacheService());

        outputOk(timetrackerService.togglePause());

        actionContext.saveModelCache(getClass().getName());
    }

    @Override
    public ActionGroup getGroup() {
        return ActionGroup.BOOKING;
    }

    @Override
    public Command getCommand() {
        return new Command ("pause", "p");
    }

}
