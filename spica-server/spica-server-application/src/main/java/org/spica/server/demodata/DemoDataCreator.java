package org.spica.server.demodata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spica.server.chat.domain.Meep;
import org.spica.server.chat.domain.MeepContainer;
import org.spica.server.chat.domain.MeepContainerRepository;
import org.spica.server.chat.domain.MeepRepository;
import org.spica.server.commons.Idable;
import org.spica.server.commons.MemberShipType;
import org.spica.server.commons.ReferenceType;
import org.spica.server.commons.Role;
import org.spica.server.project.domain.*;
import org.spica.server.user.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;

@Component
public class DemoDataCreator {


  private final static Logger LOGGER = LoggerFactory.getLogger(DemoDataCreator.class);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserGroupRepository userGroupRepository;

  @Autowired
  private UserGroupMemberRepository userGroupMemberRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private TopicRepository topicRepository;

  @Autowired
  private ProjectMemberRepository projectMemberRepository;

  @Autowired
  private MeepRepository meepRepository;

  @Autowired
  private MeepContainerRepository meepContainerRepository;

  protected User user (final String name, final String firstname, final Role role) {
    LOGGER.info("Create new user " + name + "," + firstname + " with role " + role.name());
    String username = name.substring(0, 1) + firstname.substring(0,1);
    String mail = firstname.toLowerCase() + "." + name.toLowerCase() + "@somedomain.org";
    User user = User.builder().name(name).firstname(firstname).username(username).createdAt(LocalDateTime.now()).email(mail).role(role).password(username).build();
    userRepository.save(user);
    return user;
  }

  protected UserGroup userGroup(final String name, User user, final boolean groupLead) {
    LOGGER.info("User " + user.getUsername() + " added to usergroup " + name + " (groupLead " + groupLead + ")");
    UserGroup userGroup = userGroupRepository.findByName(name);
    if (userGroup == null) {
      LOGGER.info("Create new usergroup " + name);
      userGroup = new UserGroup();
      userGroup.setName(name);
      userGroupRepository.save(userGroup);
    }

    UserGroupMember userGroupMember = new UserGroupMember();
    userGroupMember.setUser(user);
    userGroupMember.setUserGroup(userGroup);
    userGroupMember.setGroupLead(groupLead);
    userGroupMemberRepository.save(userGroupMember);
    return userGroup;
  }

  protected Project project (final String name, final User creator, final Project parentProject, final Idable... members) {
    LOGGER.info("User " + creator.getUsername() + " creates new project " + name + " with parent " + parentProject + " and " + members.length + " members");
    Project project = new Project();
    project.setParentProject(parentProject);
    project.setName(name);
    project.setCreatorID(creator.getId());
    projectRepository.save(project);

    for (Idable nextMember: members) {
      ProjectMember projectMember = new ProjectMember();
      projectMember.setProject(project);
      if (nextMember.equals(members[0]))
        projectMember.setProjectLead(true);

      if (nextMember instanceof User) {
        projectMember.setMemberShipType(MemberShipType.USER);
      }
      else if (nextMember instanceof UserGroup) {
        projectMember.setMemberShipType(MemberShipType.USERGROUP);
      }
      else
        throw new IllegalStateException("Not supported membership type " + nextMember.getClass().getSimpleName());

      projectMember.setMemberID(nextMember.getId());

      projectMemberRepository.save(projectMember);
    }

    return project;
  }

  protected Topic topic (final String name, final User creator, Project project) {
    LOGGER.info("User " + creator.getUsername() + " creates new topic " + name + " in project " + project.getName());

    Topic topic = new Topic();
    topic.setName(name);
    topic.setProjectID(project.getId());
    topic.setCreatorID(creator.getId());
    topicRepository.save(topic);
    return topic;
  }

  protected Meep meep (final User creator, Idable reference, final String message) {
    Meep meep = new Meep();
    meep.setCreationDate(LocalDateTime.now());
    meep.setCreatorId(creator.getId());
    meep.setMessage(message);

    Long referenceID = reference.getId();
    ReferenceType referenceType = ReferenceType.valueOf(reference.getClass().getSimpleName().toUpperCase());

    MeepContainer container = meepContainerRepository.findByReferenceTypeAndReferenceID(referenceType, referenceID);
    if (container == null) {
      container = new MeepContainer();
      container.setReferenceType(referenceType);
      container.setReferenceID(referenceID);
    }

    container.getMeeps().add(meep);
    meepContainerRepository.save(container);

    return meep;



  }





}
