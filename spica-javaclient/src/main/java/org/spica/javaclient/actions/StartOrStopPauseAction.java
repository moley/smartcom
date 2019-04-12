package org.spica.javaclient.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spica.javaclient.timetracker.TimetrackerService;

public class StartOrStopPauseAction implements Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(StartOrStopPauseAction.class);

    private TimetrackerService timetrackerService = new TimetrackerService();

    @Override
    public boolean fromButton() {
        return true;
    }

    @Override
    public String getDisplayname() {
        return "Pause";
    }

    @Override
    public void execute(ActionContext actionContext, String parameterlist) {
        LOGGER.info("Start or stop pause called with parameter " + parameterlist);
        //TODO register callback btnPause.setText(timetrackerService.togglePause());
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
