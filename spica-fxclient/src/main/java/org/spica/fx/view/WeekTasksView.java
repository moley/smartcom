package org.spica.fx.view;

import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.spica.javaclient.model.TaskInfo;
import org.spica.javaclient.utils.DateUtil;

@Slf4j
public class WeekTasksView extends AbstractTaskView {

  private DateUtil dateUtil = new DateUtil();


  @Override public String getName() {
    return "Planned this week";
  }

  public void renderTasks (final List<TaskInfo> taskInfos) {
    log.info("renderTasks " + taskInfos.size());
    getTaskInfos().clear();
    for (TaskInfo next: taskInfos) {
      //TODO If no project or no plan date available

      if (next.getTaskState() == null || ! next.getTaskState().equals(TaskInfo.TaskStateEnum.FINISHED)) {
        if (next.getPlannedDate() != null) {
          int weekOfYearNext = dateUtil.getDayOfWeek(next.getPlannedDate());
          int weekOfToday = dateUtil.getDayOfWeek(LocalDate.now());
          if (weekOfYearNext == weekOfToday)
            getTaskInfos().add(next);
        }

      }
    }
  }
}