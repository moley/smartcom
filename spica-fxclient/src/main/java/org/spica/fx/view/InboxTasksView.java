package org.spica.fx.view;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.spica.javaclient.model.TaskInfo;

@Slf4j
public class InboxTasksView extends AbstractTaskView {


  @Override public String getName() {
    return "Inbox";
  }

  public void renderTasks (final List<TaskInfo> taskInfos) {
    log.info("renderTasks " + taskInfos.size());
    getTaskInfos().clear();
    for (TaskInfo next: taskInfos) {
      if (next.getPlannedDate() != null)
        continue;

      //TODO If no project or no plan date available
      if (next.getTaskState() == null || ! next.getTaskState().equals(TaskInfo.TaskStateEnum.FINISHED))
        getTaskInfos().add(next);
    }
  }
}
