package org.spica.server.software.api;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.spica.server.software.model.SoftwareInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins="*")
public class SoftwareController implements SoftwareApi {

  public ResponseEntity<List<SoftwareInfo>> getSoftware() {
    SoftwareInfo software = new SoftwareInfo();
    software.setName("Software");
    software.setDescription("Description");
    software.setId("id");
    software.setRequirement("requirement");

    return ResponseEntity.of(Optional.of(Arrays.asList(software)));

  }
}
