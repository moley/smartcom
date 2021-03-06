package org.spica.fx;

import com.sun.nio.file.SensitivityWatchEventModifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.spica.commons.SpicaProperties;
import org.spica.commons.filestore.FilestoreService;
import org.spica.fx.clipboard.ClipboardItem;

@Slf4j
public class AutoImportClipboardThread extends Thread {

  private ApplicationContext applicationContext;

  private FilestoreService filestoreService = new FilestoreService();


  public AutoImportClipboardThread(final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;

  }
  @Override public void run() {


    log.info("Start thread");

    File importDir = SpicaProperties.getImportFolder();

    try {
      WatchService watchService = FileSystems.getDefault().newWatchService();
      Path path = Paths.get(importDir.toURI());
      path.register(watchService, new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE}, SensitivityWatchEventModifier.HIGH);

      WatchKey watchKey;
      while ((watchKey = watchService.take()) != null) {
        final WatchKey finalWatchKey = watchKey;
        Platform.runLater(() -> {
          for (WatchEvent<?> event : finalWatchKey.pollEvents()) {
            log.info("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
            File affectedFile = new File (importDir, event.context().toString());
            File linkFile = filestoreService.addAutoImport(affectedFile);
            ClipboardItem clipboardItem = new ClipboardItem();
            clipboardItem.setFile(linkFile);
            applicationContext.getClipboard().getItems().add(clipboardItem);
          }

        });

        watchKey.reset();
      }
    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException(e);
    }

  }
}
