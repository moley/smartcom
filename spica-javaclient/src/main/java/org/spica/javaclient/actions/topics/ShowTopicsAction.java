package org.spica.javaclient.actions.topics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spica.javaclient.actions.Action;
import org.spica.javaclient.actions.ActionContext;
import org.spica.javaclient.actions.ActionGroup;
import org.spica.javaclient.actions.Command;
import org.spica.javaclient.actions.params.InputParams;
import org.spica.javaclient.model.ModelCache;
import org.spica.javaclient.model.TopicInfo;

import java.util.List;

public class ShowTopicsAction implements Action {

    private final static Logger LOGGER = LoggerFactory.getLogger(ShowTopicsAction.class);

    @Override
    public boolean fromButton() {
        return false;
    }

    @Override
    public String getDisplayname() {
        return "Show topics";
    }

    @Override
    public String getDescription() {
        return "Show topics that matches a certain string (in key, description, name or id)";
    }

    @Override
    public void execute(ActionContext actionContext, InputParams inputParams, String parameterList) {
        ModelCache modelCache = actionContext.getModelCache();
        List<TopicInfo> infos = modelCache.findTopicInfosByQuery(parameterList);
        System.out.println ("Found " + infos.size() + " topics for query <" + parameterList + ">");

        for (TopicInfo next : modelCache.findTopicInfosByQuery(parameterList)) {

            System.out.println ("ID               : " + next.getId());
            System.out.println ("Name             : " + next.getName());
            System.out.println ("Description      : " + next.getDescription());
            System.out.println ("State            : " + next.getState());
            System.out.println ("Project          : " + (next.getProject() != null ? next.getProject().getName(): "none"));
            System.out.println("\n\n");
        }
    }


    @Override
    public ActionGroup getGroup() {
        return ActionGroup.TOPIC;
    }

    @Override
    public Command getCommand() {
        return new Command("show", "s");
    }
}