package org.spica.javaclient.actions.workingsets;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.spica.javaclient.actions.ActionContext;
import org.spica.javaclient.actions.ActionResult;
import org.spica.javaclient.actions.Command;
import org.spica.javaclient.model.WorkingSetInfo;
import org.spica.javaclient.model.WorkingSetSourcePartInfo;
import org.spica.javaclient.params.CommandLineArguments;
import org.spica.javaclient.params.InputParamGroup;
import org.spica.javaclient.params.InputParams;
import org.spica.javaclient.params.TextInputParam;

@Slf4j
public class CheckoutBranchProjectAction extends AbstractWorkingSetAction {

  private static String KEY_BRANCH = "branch";


  @Override public String getDisplayname() {
    return "Checkout branch on workingset";
  }

  @Override public String getDescription() {
    return "Checks out a specific branch on all source parts of a working set";
  }

  @Override public ActionResult execute(ActionContext actionContext, InputParams inputParams,
      CommandLineArguments commandLineArguments) {

    final String branch = inputParams.getInputValueAsString(KEY_BRANCH);

    WorkingSetInfo workingSetInfo = getWorkingSet(actionContext.getModel(), commandLineArguments);

    for (WorkingSetSourcePartInfo nextModule : workingSetInfo.getSourceparts()) {
      if (nextModule.getEnabled()) {
        File toDir = new File(nextModule.getId()).getAbsoluteFile();
        try {
          Git git = Git.open(toDir);
          outputDefault("Switch to branch " + branch + " in " + toDir.getAbsolutePath());
          git.checkout().setCreateBranch(false).setName(branch).call();
        } catch (GitAPIException | IOException e) {
          throw new IllegalStateException("Error resetting " + toDir.getAbsolutePath(), e);
        }
      }
    }

    return null;

  }

  @Override public Command getCommand() {
    return new Command("checkout", "r");
  }

  @Override public InputParams getInputParams(ActionContext actionContext, CommandLineArguments commandLineArguments) {

    InputParamGroup inputParamGroup = new InputParamGroup();
    InputParams inputParams = new InputParams(Arrays.asList(inputParamGroup));

    TextInputParam branch = new TextInputParam(1, KEY_BRANCH, "Branch");
    inputParamGroup.getInputParams().add(branch);

    return inputParams;
  }

}
