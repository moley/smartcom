package org.spica.fx.controllers;

import com.jfoenix.controls.JFXBadge;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.UUID;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.CustomTextField;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.spica.commons.UserPresence;
import org.spica.commons.filestore.FilestoreService;
import org.spica.commons.xmpp.XMPPAdapter;
import org.spica.fx.ApplicationContext;
import org.spica.fx.AutoImportClipboardThread;
import org.spica.fx.AutoImportMailsTask;
import org.spica.fx.Consts;
import org.spica.fx.MainMenuEntry;
import org.spica.fx.Mask;
import org.spica.fx.MaskLoader;
import org.spica.fx.ScreenManager;
import org.spica.fx.clipboard.ClipboardItem;
import org.spica.javaclient.Configuration;
import org.spica.javaclient.StandaloneActionContext;
import org.spica.javaclient.exceptions.NotFoundException;
import org.spica.javaclient.model.MessageInfo;
import org.spica.javaclient.model.MessageType;
import org.spica.javaclient.model.MessagecontainerInfo;
import org.spica.javaclient.model.Model;
import org.spica.javaclient.model.UserInfo;

@Slf4j
public class MainController extends AbstractController  {

  @FXML private Label lblAppname;
  @FXML private HBox panHeader;
  @FXML private TextField txtCurrentAction;
  @FXML private Button btnToggleVisibility;
  @FXML private Button btnState;
  @FXML private JFXBadge badClipboard;
  @FXML private Button btnClipboard;
  @FXML private Button btnCloseSearch;
  @FXML private CustomTextField txtFieldSearch;
  @FXML private Button btnSearchUp;
  @FXML private Button btnSearchDown;
  @FXML private BorderPane paRootPane;
  @FXML private ToolBar btnMainActions;

  private final HashMap<Pages, MainMenuEntry> menuEntries = new HashMap<>();

  private final ListView<ClipboardItem> lviClipboardItems = new ListView<>();

  private final StandaloneActionContext standaloneActionContext = new StandaloneActionContext();

  private ScreenManager screenManager = new ScreenManager();

  private boolean foldedOut = false;

  private String getVersion () {
    log.info("Determine version of fx app");
    String version = "not found";

    try {
      URL leguanVersion = getClass().getClassLoader().getResource("spica-fx.version");
      if (leguanVersion != null) {
        version = IOUtils.toString(leguanVersion, Charset.defaultCharset());
      }

      log.info("Determined version " + version);
      return version;


    } catch (Throwable e) {
      log.error("Error determining version: " + e.getLocalizedMessage(), e);
      return version;
    }
  }

  public void toggleVisibility (final boolean toFoldedOut) {
    log.info("toggle visibility to " + toFoldedOut + "(folded out == " + foldedOut + ")");

    Tooltip tooltipAppname = new Tooltip();
    Label lblTooltipAppname = new Label();
    lblTooltipAppname.setText("Version : " + getVersion() + "\nModel : " + getActionContext().getServices().getModelCacheService().getConfigFile().getAbsolutePath());
    tooltipAppname.setGraphic(lblTooltipAppname);
    lblAppname.setTooltip(tooltipAppname);

    paRootPane.getCenter().setVisible(toFoldedOut);
    panHeader.setVisible(toFoldedOut);

    screenManager.layoutEdged(getStage(), true);

    btnToggleVisibility.setGraphic(Consts.createIcon((toFoldedOut ?"fa-chevron-left" :"fa-chevron-right"), Consts.ICON_SIZE_TOOLBAR));

    double width = toFoldedOut ? screenManager.getFullWidth() : screenManager.getHalfWidth();

    getStage().setMaxWidth(width);
    getStage().setMinWidth(width);
    foldedOut = toFoldedOut;
  }

  @FXML
  public void initialize () {

    lblAppname.setMinWidth(screenManager.getHalfWidth());
    lblAppname.setGraphic(new ImageView(Consts.createImage("/spica.png", 40)));

    setPaRootPane(paRootPane);
    btnCloseSearch.setGraphic(Consts.createIcon("fa-close", 15));
    btnToggleVisibility.setGraphic(Consts.createIcon("fa-chevron-left", Consts.ICON_SIZE_TOOLBAR));

    btnToggleVisibility.setOnAction(event -> toggleVisibility(!foldedOut));

    btnState.setOnAction(event -> {

      UserPresence currentPresence = getApplicationContext().getPresence();
      if (currentPresence.equals(UserPresence.ONLINE)) {
        getApplicationContext().setPresence(UserPresence.OFFLINE);
      } else
        getApplicationContext().setPresence(UserPresence.ONLINE);

      XMPPAdapter xmppAdapter = getActionContext().getServices().getXmppAdapter();
      xmppAdapter.setPresence(getApplicationContext().getPresence(), txtCurrentAction.getText());

      getApplicationContext().presencePropertyProperty().setValue(getApplicationContext().getPresence().toString());
    });

    txtCurrentAction.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (oldValue && !newValue) {
        XMPPAdapter xmppAdapter = getActionContext().getServices().getXmppAdapter();
        xmppAdapter.setPresence(getApplicationContext().getPresence(), txtCurrentAction.getText());
      }
    });

    Label lbl = new Label();
    lbl.setGraphic(Consts.createIcon("fa-search", 15));
    txtFieldSearch.setLeft(lbl);


    PopOver popOver = new PopOver(lviClipboardItems);
    btnClipboard.setOnMouseEntered(mouseEvent -> {
      //Show PopOver when mouse enters label
      popOver.show(btnClipboard);
    });

    btnClipboard.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        getApplicationContext().getClipboard().clear();

      }
    });

    btnSearchUp.setGraphic(Consts.createIcon("fa-chevron-up", 15));
    btnSearchDown.setGraphic(Consts.createIcon("fa-chevron-down", 15));

    btnClipboard.setGraphic(Consts.createIcon("fa-clipboard", 15));

    setActionContext(standaloneActionContext);
    setApplicationContext(new ApplicationContext());

    for (Pages next : Pages.values()) {
      registerPane(next);

      if (next.isMainAction()) {

        JFXBadge jfxBadge = new JFXBadge();
        Button menuItem = new Button(next.getDisplayname(), Consts.createIcon(next.getIcon(), Consts.ICON_SIZE_TOOLBAR));
        menuItem.setMaxWidth(screenManager.getHalfWidth() - 20);
        menuItem.setContentDisplay(ContentDisplay.TOP);
        menuItem.setOnAction(event -> stepToPane(next));
        jfxBadge.setControl(menuItem);
        btnMainActions.getItems().add(jfxBadge);

        MainMenuEntry mainMenuEntry = new MainMenuEntry();
        mainMenuEntry.setButton(menuItem);
        mainMenuEntry.setJfxBadge(jfxBadge);

        menuEntries.put(next, mainMenuEntry);
      }
    }
  }

  public void registerPane (final Pages pages) {
    MaskLoader<?> maskLoader = new MaskLoader<>();
    try {
      Mask<?> mask = maskLoader.load(pages.getFilename());
      AbstractController controller = mask.getController();
      controller.setPaRootPane(getPaRootPane());
      controller.setStage(getStage());
      controller.setActionContext(getActionContext());
      controller.setApplicationContext(getApplicationContext());
      controller.setMainController(this);
      getRegisteredMasks().put(pages, mask);
    } catch (Exception e) {
      log.error("Error loading page " + pages.getFilename(), e);
    }

  }


  public void init() {

    AutoImportClipboardThread autoImportThread = new AutoImportClipboardThread(getApplicationContext());
    autoImportThread.start();

    Timer autoImportMailsTimer = new Timer();

    /**AutoImportMailsTask autoImportMailsTask = new AutoImportMailsTask(getActionContext(), () -> Platform.runLater(
        () -> {
          Mask<InfosController> mask = getMask(Pages.MESSAGES);
          InfosController messagesController = mask.getController();
          showMessageNotifications();
          messagesController.refreshData();
        }));
    autoImportMailsTimer.scheduleAtFixedRate(autoImportMailsTask, 0, 60000);**/


    log.info("Register clipboard");
    getApplicationContext().getClipboard().getItems().addListener((ListChangeListener<ClipboardItem>) c -> {
      log.info("Clipboard changed");
      badClipboard.setText(String.valueOf(getApplicationContext().getClipboard().getItems().size()));
      badClipboard.setEnabled(! getApplicationContext().getClipboard().getItems().isEmpty());
      badClipboard.refreshBadge();
      lviClipboardItems.setItems(getApplicationContext().getClipboard().getItems());

    });


    try {
      String serverUrl = standaloneActionContext.getProperties().getValueOrDefault("spica.cli.serverurl", "http://localhost:8765/api");
      Configuration.getDefaultApiClient().setBasePath(serverUrl);

      /*String username = standaloneActionContext.getProperties().getValueNotNull("spica.cli.username");
      String password = standaloneActionContext.getProperties().getValueNotNull("spica.cli.password");
      HttpBasicAuth httpBasicAuth = (HttpBasicAuth) Configuration.getDefaultApiClient().getAuthentication("basicAuth");
      httpBasicAuth.setUsername(username);
      httpBasicAuth.setPassword(password);*/

      standaloneActionContext.refreshServer();
    } catch (Exception e) {
      String message = "Error while synchronization with server: " + e.getLocalizedMessage();
      log.error(message, e);
      System.exit(1);
    }

    try {
      XMPPAdapter xmppAdapter = getActionContext().getServices().getXmppAdapter();
      xmppAdapter.login(getActionContext().getProperties(), (from, message, chat) -> {
        Model model = getModel();

        String username = from.toString().split("@")[0];
        UserInfo userInfo = null;
        try {
          userInfo = getModel().getUserNotNull("@" + username);
        } catch (NotFoundException e) {
          Notifications.create().text("User " + username + " not found").showError();
        }
        MessagecontainerInfo openMesssageContainer = getModel().findOpenMessageContainerByUser(MessageType.CHAT, userInfo);

        //if no open conversation found so far create new one
        MessagecontainerInfo newMessageContainer = openMesssageContainer != null ? openMesssageContainer : model.createNewMessageContainer();
        newMessageContainer.setId(UUID.randomUUID().toString());
        newMessageContainer.setTopic("Chat with " + from.toString());
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setType(MessageType.CHAT);
        messageInfo.setCreator(userInfo.getId());
        messageInfo.setMessage(message.getBody());
        messageInfo.setCreationtime(LocalDateTime.now());
        messageInfo.setRecieversTo(Arrays.asList(getModel().getMe().getId()));
        newMessageContainer.addMessageItem(messageInfo);
        if (openMesssageContainer == null)
          model.getMessagecontainerInfos().add(newMessageContainer);

        getModel().sortMessages();
        saveModel("Added new chatmessage to existing chat from " + from.toString());

        /**Platform.runLater(() -> {
          Mask<InfosController> mask = getMask(Pages.MESSAGES);
          InfosController messagesController = mask.getController();
          messagesController.refreshData();

          showMessageNotifications();

          Mask<MessageDetailController> detailMask = getMask(Pages.MESSAGEDIALOG);
          MessageDetailController controller = detailMask.getController();
          controller.refreshData();

        });**/

      }, request -> {
        System.out.println ("recieved a file transfer request " + request.getDescription() + "-" + request.getFileName() + " from " + request.getRequestor().toString());
        IncomingFileTransfer incomingFileTransfer = request.accept();

        String from = request.getRequestor().toString();

        Model model = getModel();

        String username = from.split("@")[0];
        UserInfo userInfo = null;
        try {
          userInfo = getModel().getUserNotNull(username);
        } catch (NotFoundException e) {
          Notifications.create().text("User " + username + " not found").showError();
        }
        MessagecontainerInfo openMesssageContainer = getModel().findOpenMessageContainerByUser(MessageType.CHAT, userInfo);

        //if no open conversation found so far create new one
        MessagecontainerInfo newMessageContainer = openMesssageContainer != null ? openMesssageContainer : model.createNewMessageContainer();
        newMessageContainer.setTopic("Chat with " + from);
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setType(MessageType.CHAT);
        messageInfo.setCreator(userInfo.getId());

        FilestoreService filestoreService = standaloneActionContext.getServices().getFilestoreService();

        String fileId = "filetransfer_" + (userInfo != null ? userInfo.getUsername() : "unknown") + "_" + request.getFileName();
        messageInfo.addDocumentsItem(fileId);

        File fileInFileStore = filestoreService.file(fileId).getFile();
        log.info("Recieve file " + fileInFileStore.getAbsolutePath());

        try {
          incomingFileTransfer.receiveFile(fileInFileStore);
        } catch (SmackException | IOException e) {
          throw new IllegalStateException(e);
        }

        messageInfo.setCreationtime(LocalDateTime.now());
        newMessageContainer.addMessageItem(messageInfo);
        if (openMesssageContainer == null)
          model.getMessagecontainerInfos().add(newMessageContainer);

        getModel().sortMessages();
        saveModel("Added new chatmessage to existing chat from " + from);

        /**Platform.runLater(() -> {
          Mask<InfosController> mask = getMask(Pages.MESSAGES);
          InfosController messagesController = mask.getController();
          messagesController.refreshData();

          showMessageNotifications();

          Mask<MessageDetailController> detailMask = getMask(Pages.MESSAGEDIALOG);
          MessageDetailController controller = detailMask.getController();
          controller.refreshData();

        });**/

    });
    } catch (Exception e) {
      log.error("Error login to xmpp", e.getLocalizedMessage(), e);
    }

    btnState.textProperty().bindBidirectional(getApplicationContext().presencePropertyProperty());
    btnState.textProperty().setValue(UserPresence.ONLINE.name());

    stepToPane(Pages.DIARY, false); //TODO step to dashboard, if exists

  }

  public void showMessageNotifications() {
    if (getModel() == null)
      return;

    int numberOfUnreadMessages = getModel().findUnreadMessages ().size();
    if (numberOfUnreadMessages > 0) {
      log.info("Show message recieved new messages (" + numberOfUnreadMessages + ")");
      Notifications.create().owner(btnState).title("Recieved new messages").text("You have " + numberOfUnreadMessages + " unread messages ").showInformation();
    }
  }

  @Override public void refreshData() {
    int numberOfUnreadMessages = getModel().findUnreadMessages ().size();

    /**MainMenuEntry mainMenuEntry = menuEntries.get(Pages.MESSAGES);
    JFXBadge jfxBadge = mainMenuEntry.getJfxBadge();
    jfxBadge.setEnabled(numberOfUnreadMessages > 0);
    jfxBadge.setText(String.valueOf(numberOfUnreadMessages));
    jfxBadge.refreshBadge();**/

  }
}
