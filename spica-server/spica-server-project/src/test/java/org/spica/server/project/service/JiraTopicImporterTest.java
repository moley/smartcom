package org.spica.server.project.service;

import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import io.atlassian.util.concurrent.Promise;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.spica.commons.SpicaProperties;
import org.spica.server.project.domain.Topic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

@ExtendWith(value = {SpringExtension.class})
@DataJpaTest
@AutoConfigurationPackage
public class JiraTopicImporterTest {

    @Autowired
    JiraTopicImporter jiraTopicImporter;


    @AfterEach
    public void after () {
        SpicaProperties.close();
    }

    @Test
    public void testImportWrapper () throws InterruptedException, ExecutionException {

        SpicaProperties properties = new SpicaProperties();
        properties.setProperty(JiraConfiguration.PROPERTY_SPICA_JIRA_URL, "https://my.jira.com");

        SearchResult mockedSearchResult = Mockito.mock(SearchResult.class);

        Promise<SearchResult> mockedPromise = Mockito.mock(Promise.class);
        Mockito.when(mockedPromise.claim()).thenReturn(mockedSearchResult);

        SearchRestClient mockedSearchRestClient = Mockito.mock(SearchRestClient.class);
        Mockito.when(mockedSearchRestClient.searchJql(Mockito.any())).thenReturn(mockedPromise);
        JiraRestClient mockedJiraRestClient = Mockito.mock(JiraRestClient.class);
        AsynchronousJiraRestClientFactory mockedJiraRestClientFactory = Mockito.mock(AsynchronousJiraRestClientFactory.class);
        Mockito.when(mockedJiraRestClientFactory.create((URI)Mockito.any(), (AuthenticationHandler) Mockito.any())).thenReturn(mockedJiraRestClient);
        Mockito.when(mockedJiraRestClient.getSearchClient()).thenReturn(mockedSearchRestClient);

        jiraTopicImporter.setJiraRestClientFactory(mockedJiraRestClientFactory);
        jiraTopicImporter.importTopicsOfUser(1L);

    }

    @Test
    public void importLogic () {

        final HashMap<String, Topic> keyAndTopic = new HashMap<String, Topic>();
        final String KEY1 = "KEY1";
        final String KEY2 = "KEY2";

        final String SUMMARY1 = "SUMMARY1";
        final String SUMMARY2 = "SUMMARY2";

        final String DESC1 = "DESC1";
        final String DESC2 = "DESC2";

        Status status = Mockito.mock(Status.class);
        Mockito.when(status.getName()).thenReturn("OPEN");
        Issue issue1 = Mockito.mock(Issue.class);
        Mockito.when(issue1.getKey()).thenReturn(KEY1);
        Mockito.when(issue1.getSummary()).thenReturn(SUMMARY1);
        Mockito.when(issue1.getDescription()).thenReturn(DESC1);
        Mockito.when(issue1.getStatus()).thenReturn(status);

        Issue issue2 = Mockito.mock(Issue.class);
        Mockito.when(issue2.getKey()).thenReturn(KEY2);
        Mockito.when(issue2.getSummary()).thenReturn(SUMMARY2);
        Mockito.when(issue2.getDescription()).thenReturn(DESC2);
        Mockito.when(issue2.getStatus()).thenReturn(status);

        //initial
        SearchResult mockedSearchResult = Mockito.mock(SearchResult.class);
        Mockito.when(mockedSearchResult.getIssues()).thenReturn(Arrays.asList(issue1, issue2));
        Collection<Topic> topicsFirst = jiraTopicImporter.importSearchResult(keyAndTopic, mockedSearchResult);

        Assert.assertEquals (2, keyAndTopic.keySet().size());
        Assert.assertTrue (keyAndTopic.keySet().contains(KEY1));
        Assert.assertTrue (keyAndTopic.keySet().contains(KEY2));

        //and overwrite
        Collection<Topic> topicsOverwritten = jiraTopicImporter.importSearchResult(keyAndTopic, mockedSearchResult);

        Assert.assertEquals (2, keyAndTopic.keySet().size());
        Assert.assertTrue (keyAndTopic.keySet().contains(KEY1));
        Assert.assertTrue (keyAndTopic.keySet().contains(KEY2));

    }
}
