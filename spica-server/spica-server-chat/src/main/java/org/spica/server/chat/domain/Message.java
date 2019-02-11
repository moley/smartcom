
package org.spica.server.chat.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)	
  private Long id;

  private LocalDateTime creationDate;

  private LocalDateTime updateDate;

  @Column(length=10000)
  private String message;

  private Long creatorId;

  private String visibilityRules;

  @ElementCollection
  private List<Long> documentIds = new ArrayList<Long>();

  
  

  
}