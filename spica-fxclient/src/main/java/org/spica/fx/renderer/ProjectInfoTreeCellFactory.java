package org.spica.fx.renderer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.spica.fx.Reload;
import org.spica.javaclient.actions.ActionContext;
import org.spica.javaclient.model.ProjectInfo;

public class ProjectInfoTreeCellFactory extends TreeCell<ProjectInfo> {

  private ActionContext actionContext;
  private Reload reload;

  public ProjectInfoTreeCellFactory(final ActionContext actionContext, Reload reload) {
    this.actionContext = actionContext;
    this.reload = reload;
  }

  @Override protected void updateItem(ProjectInfo projectInfo, boolean empty) {
    super.updateItem(projectInfo, empty);

    if (empty || projectInfo == null) {
      setText(null);
      setGraphic(null);
    }
    else {
      HBox hbox = new HBox();
      hbox.setAlignment(Pos.CENTER_LEFT);
      hbox.setSpacing(10);
      Pane panFiller = new Pane();
      HBox.setHgrow(panFiller, Priority.ALWAYS);

      Label lblName = new Label(projectInfo.getName());


      ListView<ProjectInfo> lviProjects = new ListView<ProjectInfo>();
      lviProjects.setCellFactory(cellfactory -> new ProjectInfoCellFactory());


      ColorPicker colorPicker = new ColorPicker();
      if (projectInfo.getColor() != null)
        colorPicker.setValue(Color.web(projectInfo.getColor()));

      colorPicker.setOnAction(new EventHandler<ActionEvent>() {
        @Override public void handle(ActionEvent event) {
          projectInfo.setColor(colorPicker.getValue().toString());
          actionContext.saveModel("Save color " + projectInfo.getColor() + " in project " + projectInfo.getId());
          reload.reload();

        }
      });


      hbox.getChildren().addAll(lblName, panFiller, colorPicker);
      setGraphic(hbox);
    }
  }
}