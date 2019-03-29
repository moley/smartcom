package org.spica.server.project.api;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.spica.server.project.domain.ProjectRepository;
import org.spica.server.project.domain.Topic;
import org.spica.server.project.domain.TopicRepository;
import org.spica.server.project.service.JiraTopicImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBootConfiguration()
@ContextConfiguration(classes = {TopicsController.class, TopicsControllerTest.class})
public class TopicsControllerTest {

    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext context;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Bean
    public JiraTopicImporter getJiraTopicImporter () throws InterruptedException {

        Topic topic = Topic.builder().name("TOPIC").build();
        JiraTopicImporter mockedImporter = Mockito.mock(JiraTopicImporter.class);
        Mockito.when(mockedImporter.importTopicsOfUser(Mockito.eq("Zeuchs"), Mockito.eq("spica"), Mockito.eq("spicaPwd"))).thenReturn(Arrays.asList(topic));

        return mockedImporter;
    }

    @Bean
    public TopicRepository getTopicRepository () {
        return Mockito.mock(TopicRepository.class);
    }

    @BeforeClass
    public static void beforeClass () throws Exception {
        //TestUtil.configureTestKeyspace();
        //DemoDataCreator demoDataCreator = new DemoDataCreator();
        //demoDataCreator.createGlobalConfigurations();
        //demoDataCreator.createActionsConfigurations();
        //SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(TestUtil.DEFAULT_USERNAME, TestUtil.DEFAULT_PASSWORD));

    }

    @AfterClass
    public static void reset () {
        //SecurityContextHolder.getContext().setAuthentication(null);
        //TestUtil.resetDefaultConfiguration();
    }


    @Test
    public void importFromJira () throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/topics/import/{userId}?usernameExternalSystem=spica&passwordExternalSystem=spicaPwd", "Zeuchs")).andExpect(MockMvcResultMatchers.status().isOk());


    }

}
