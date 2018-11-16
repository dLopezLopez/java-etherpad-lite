package net.gjerull.etherpad.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

/**
 * Integration test for simple App.
 */
public class EPLiteClientIntegrationTest {
    private EPLiteClient client;
	private ClientAndServer mockServer;

    /**
     * Useless testing as it depends on a specific API key
     *
     * TODO: Find a way to make it configurable
     */
	@Before
	public void setUp() throws Exception {
		this.client = new EPLiteClient("http://localhost:9001",
				"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");
	}

	@Before
	public void startMockServer() {
		mockServer = startClientAndServer(9001);
	}

	@After
	public void stopMockServer() {
		mockServer.stop();
	}

	@Test
	public void validate_token() throws Exception {

		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/checkToken")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		client.checkToken();
	}

	@Test
	public void create_and_delete_group() throws Exception {

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroup"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.\"}}"));

		Map response = client.createGroup();

		assertTrue(response.containsKey("groupID"));
		String groupId = (String) response.get("groupID");
		assertTrue("Unexpected groupID " + groupId, groupId != null && groupId.startsWith("g."));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		client.deleteGroup(groupId);
	}

	@Test
	public void create_group_if_not_exists_for_and_list_all_groups() throws Exception {
		String groupMapper = "groupname";

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.01\"}}"));

		Map response = client.createGroupIfNotExistsFor(groupMapper);

		assertTrue(response.containsKey("groupID"));
		String groupId = (String) response.get("groupID");

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAllGroups"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupIDs\":[\"g.01\"]}}"));

		try {
			Map listResponse = client.listAllGroups();
			assertTrue(listResponse.containsKey("groupIDs"));
			int firstNumGroups = ((List) listResponse.get("groupIDs")).size();

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupIfNotExistsFor"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.01\"}}"));

			client.createGroupIfNotExistsFor(groupMapper);

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAllGroups"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupIDs\":[\"g.01\"]}}"));

			listResponse = client.listAllGroups();
			int secondNumGroups = ((List) listResponse.get("groupIDs")).size();

			assertEquals(firstNumGroups, secondNumGroups);
		} finally {
			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			client.deleteGroup(groupId);
		}
	}

	@Test
	 public void create_group_pads_and_list_them() throws Exception {
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroup"))
	 .respond(HttpResponse.response().withStatusCode(200)
	 .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.01\"}}"));
	 Map response = client.createGroup();
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupPad"))
	 .respond(HttpResponse.response().withStatusCode(200)
	 .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"g.01$integration-test-1\"}}"));
	 String groupId = (String) response.get("groupID");
	 String padName1 = "integration-test-1";
	 String padName2 = "integration-test-2";
	 try {
	 Map padResponse = client.createGroupPad(groupId, padName1);
	 assertTrue(padResponse.containsKey("padID"));
	 String padId1 = (String) padResponse.get("padID");
	
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setPublicStatus"))
	 .respond(HttpResponse.response().withStatusCode(200)
	 .withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	 mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getPublicStatus"))
	 .respond(HttpResponse.response().withStatusCode(200)
	 .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"publicStatus\":true}}"));
	 client.setPublicStatus(padId1, true);
	 boolean publicStatus = (boolean)
	 client.getPublicStatus(padId1).get("publicStatus");
	 assertTrue(publicStatus);
	
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setPassword"))
	 .respond(HttpResponse.response().withStatusCode(200)
	 .withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	 mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/isPasswordProtected"))
	 .respond(HttpResponse.response().withStatusCode(200)
	 .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"isPasswordProtected\":true}}"));
	 client.setPassword(padId1, "integration");
	 boolean passwordProtected = (boolean)
	 client.isPasswordProtected(padId1).get("isPasswordProtected");
	 assertTrue(passwordProtected);
	
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupPad"))
	 .respond(HttpResponse.response().withStatusCode(200).withBody(
	 "{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"g.01$integration-test-2\"}}"));
	
	 padResponse = client.createGroupPad(groupId, padName2, "Initial text");
	 assertTrue(padResponse.containsKey("padID"));
	
	 mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"))
	 .respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"Initial text\\n\"}}"));
	
	 String padId = (String) padResponse.get("padID");
	 String initialText = (String) client.getText(padId).get("text");
	 assertEquals("Initial text\n", initialText);
	
	 mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listPads"))
	 .respond(HttpResponse.response().withStatusCode(200).withBody(
	 "{\"code\":0,\"message\":\"ok\",\"data\":{\"padIDs\":[\"g.01$integration-test-1\",\"g.01$integration-test-2\"]}}"));
	
	 Map padListResponse = client.listPads(groupId);
	
	 assertTrue(padListResponse.containsKey("padIDs"));
	 List padIds = (List) padListResponse.get("padIDs");
	
	 assertEquals(2, padIds.size());
	 } finally {
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup"))
	 .respond(HttpResponse.response().withStatusCode(200)
	 .withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	 client.deleteGroup(groupId);
	 }
	 }

	@Test
	public void create_author() throws Exception {
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/createAuthor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.01\"}}"));
		Map authorResponse = client.createAuthor();
		String authorId = (String) authorResponse.get("authorID");
		assertTrue(authorId != null && !authorId.isEmpty());
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.02\"}}"));

		authorResponse = client.createAuthor("integration-author");
		authorId = (String) authorResponse.get("authorID");

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author\"}"));

		String authorName = client.getAuthorName(authorId);
		assertEquals("integration-author", authorName);
	}

	@Test
	public void create_author_with_author_mapper() throws Exception {
		String authorMapper = "username";
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.01\"}}"));

		Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
		String firstAuthorId = (String) authorResponse.get("authorID");
		assertTrue(firstAuthorId != null && !firstAuthorId.isEmpty());
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-1\"}"));
		String firstAuthorName = client.getAuthorName(firstAuthorId);

		mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.01\"}}"));

		authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-2");
		String secondAuthorId = (String) authorResponse.get("authorID");
		assertEquals(firstAuthorId, secondAuthorId);

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-2\"}"));
		String secondAuthorName = client.getAuthorName(secondAuthorId);

		assertNotEquals(firstAuthorName, secondAuthorName);
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.01\"}}"));
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-2\"}"));
		authorResponse = client.createAuthorIfNotExistsFor(authorMapper);
		String thirdAuthorId = (String) authorResponse.get("authorID");
		assertEquals(secondAuthorId, thirdAuthorId);
		String thirdAuthorName = client.getAuthorName(thirdAuthorId);

		assertEquals(secondAuthorName, thirdAuthorName);
	}

	@Test
	public void create_and_delete_session() throws Exception {
		String authorMapper = "username";
		String groupMapper = "groupname";

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.01\"}}"));
		Map groupResponse = client.createGroupIfNotExistsFor(groupMapper);
		String groupId = (String) groupResponse.get("groupID");
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.01\"}}"));
		Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
		String authorId = (String) authorResponse.get("authorID");
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createSession"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"sessionID\":\"s.01\"}}"));
		int sessionDuration = 8;
		Map sessionResponse = client.createSession(groupId, authorId, sessionDuration);
		String firstSessionId = (String) sessionResponse.get("sessionID");
		mockServer.clear(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createSession"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createSession"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"sessionID\":\"s.02\"}}"));
		Calendar oneYearFromNow = Calendar.getInstance();
		oneYearFromNow.add(Calendar.YEAR, 1);
		Date sessionValidUntil = oneYearFromNow.getTime();
		sessionResponse = client.createSession(groupId, authorId, sessionValidUntil);
		String secondSessionId = (String) sessionResponse.get("sessionID");
		try {
			assertNotEquals(firstSessionId, secondSessionId);
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getSessionInfo"))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.01\",\"authorID\":\"a.01\",\"validUntil\":"
									+ Long.toString(sessionValidUntil.getTime() / 1000L) + "}}"));
			Map sessionInfo = client.getSessionInfo(secondSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));
			assertEquals(authorId, sessionInfo.get("authorID"));
			assertEquals(sessionValidUntil.getTime() / 1000L, (long) sessionInfo.get("validUntil"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listSessionsOfGroup"))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"s.01\":{\"groupID\":\"g.01\",\"authorID\":\"a.01\",\"validUntil\":1542420364},\"s.02\":{\"groupID\":\"g.01\",\"authorID\":\"a.01\",\"validUntil\":1573927564}}}"));
			Map sessionsOfGroup = client.listSessionsOfGroup(groupId);
			sessionInfo = (Map) sessionsOfGroup.get(firstSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));
			sessionInfo = (Map) sessionsOfGroup.get(secondSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listSessionsOfAuthor"))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"s.01\":{\"groupID\":\"g.01\",\"authorID\":\"a.01\",\"validUntil\":1542420364},\"s.02\":{\"groupID\":\"g.01\",\"authorID\":\"a.01\",\"validUntil\":1573927564}}}"));
			Map sessionsOfAuthor = client.listSessionsOfAuthor(authorId);
			sessionInfo = (Map) sessionsOfAuthor.get(firstSessionId);
			assertEquals(authorId, sessionInfo.get("authorID"));
			sessionInfo = (Map) sessionsOfAuthor.get(secondSessionId);
			assertEquals(authorId, sessionInfo.get("authorID"));
		} finally {
			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteSession"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
			client.deleteSession(firstSessionId);
			client.deleteSession(secondSessionId);
		}

	}

	@Test
	public void create_pad_set_and_get_content() {
		String padID = "integration-test-pad";
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		client.createPad(padID);
		try {
			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setText"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
			client.setText(padID, "gå å gjør et ærend");
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"gå å gjør et ærend\"}}"));
			String text = (String) client.getText(padID).get("text");
			// assertEquals("gå å gjør et ærend\n", text);

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setHTML"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
			client.setHTML(padID, "<!DOCTYPE HTML><html><body><p>gå og gjøre et ærend igjen</p></body></html>");
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getHTML"))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"html\":\"<!DOCTYPE HTML><html><body><p>g&#229; og gj&#248;re et &#230;rend igjen<br><br></p></body></html>\"}}"));
			String html = (String) client.getHTML(padID).get("html");
			assertTrue(html, html.contains("g&#229; og gj&#248;re et &#230;rend igjen<br><br>"));
			mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getHTML"));
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getHTML"))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"html\":\"<!DOCTYPE HTML><html><body><br></body></html>\"}}"));
			html = (String) client.getHTML(padID, 2).get("html");
			assertEquals("<!DOCTYPE HTML><html><body><br></body></html>", html);
			mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"));
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"\\n\"}}"));
			text = (String) client.getText(padID, 2).get("text");
			assertEquals("\n", text);

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getRevisionsCount"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"revisions\":3}}"));
			long revisionCount = (long) client.getRevisionsCount(padID).get("revisions");
			assertEquals(3L, revisionCount);

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getRevisionChangeset"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"Z:1>r|1+r$ gå og gjøre et ærend igjen\\n\"}"));
			String revisionChangeset = client.getRevisionChangeset(padID);
			 assertTrue(revisionChangeset, revisionChangeset.contains("gå og gjøre et ærend igjen"));
			mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getRevisionChangeset"));
			 mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getRevisionChangeset"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"Z:j<i|1-j|1+1$\\n\"}"));
			revisionChangeset = client.getRevisionChangeset(padID, 2);
			assertTrue(revisionChangeset, revisionChangeset.contains("|1-j|1+1$\n"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/createDiffHTML"))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"html\":\"<style>\\n.removed {text-decoration: line-through; -ms-filter:'progid:DXImageTransform.Microsoft.Alpha(Opacity=80)'; filter: alpha(opacity=80); opacity: 0.8; }\\n</style><span class=\\\"removed\\\">g&#229; &#229; gj&#248;r et &#230;rend</span><br><br>\",\"authors\":[\"\"]}}"));
			String diffHTML = (String) client.createDiffHTML(padID, 1, 2).get("html");
			assertTrue(diffHTML,
					diffHTML.contains("<span class=\"removed\">g&#229; &#229; gj&#248;r et &#230;rend</span>"));
			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendText"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
			client.appendText(padID, "lagt til nå");
			text = (String) client.getText(padID).get("text");
			// assertEquals("gå og gjøre et ærend igjen\nlagt til nå\n", text);

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAttributePool"))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"pool\":{\"numToAttrib\":{\"0\":[\"author\",\"\"],\"1\":[\"removed\",\"true\"]},\"attribToNum\":{\"author,\":0,\"removed,true\":1},\"nextNum\":2}}}"));
			Map attributePool = (Map) client.getAttributePool(padID).get("pool");
			assertTrue(attributePool.containsKey("attribToNum"));
			assertTrue(attributePool.containsKey("nextNum"));
			assertTrue(attributePool.containsKey("numToAttrib"));

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/saveRevision"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
			client.saveRevision(padID);
			client.saveRevision(padID, 2);
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getSavedRevisionsCount"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"savedRevisions\":2}}"));
			long savedRevisionCount = (long) client.getSavedRevisionsCount(padID).get("savedRevisions");
			assertEquals(2L, savedRevisionCount);

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listSavedRevisions"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"savedRevisions\":[2,4]}}"));
			List savedRevisions = (List) client.listSavedRevisions(padID).get("savedRevisions");
			assertEquals(2, savedRevisions.size());
			assertEquals(2L, savedRevisions.get(0));
			assertEquals(4L, savedRevisions.get(1));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/padUsersCount"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padUsersCount\":0}}"));
			long padUsersCount = (long) client.padUsersCount(padID).get("padUsersCount");
			assertEquals(0, padUsersCount);
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/padUsers"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padUsers\":[]}}"));
			List padUsers = (List) client.padUsers(padID).get("padUsers");
			assertEquals(0, padUsers.size());

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getReadOnlyID"))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"readOnlyID\":\"r.efb9156f0b6d471f721a3a6a7439cfd9\"}}"));
			String readOnlyId = (String) client.getReadOnlyID(padID).get("readOnlyID");
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getPadID"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad\"}}"));
			String padIdFromROId = (String) client.getPadID(readOnlyId).get("padID");
			assertEquals(padID, padIdFromROId);

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAuthorsOfPad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorIDs\":[]}}"));
			List authorsOfPad = (List) client.listAuthorsOfPad(padID).get("authorIDs");
			assertEquals(0, authorsOfPad.size());

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getLastEdited"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"lastEdited\":1542395476864}}"));
			long lastEditedTimeStamp = (long) client.getLastEdited(padID).get("lastEdited");
			Calendar lastEdited = Calendar.getInstance();
			lastEdited.setTimeInMillis(lastEditedTimeStamp);
			Calendar now = Calendar.getInstance();
			assertTrue(lastEdited.before(now));
			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/sendClientsMessage"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{}}"));
			client.sendClientsMessage(padID, "test message");
		} finally {
			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
			client.deletePad(padID);
		}
	}

	@Test
	 public void create_pad_move_and_copy() throws Exception {
	 String padID = "integration-test-pad";
	 String copyPadId = "integration-test-pad-copy";
	 String movePadId = "integration-move-pad-move";
	 String keep = "should be kept";
	 String change = "should be changed";
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")).respond(
	 HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	 client.createPad(padID, keep);
	
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad"))
	 .respond(HttpResponse.response().withStatusCode(200).withBody(
	 "{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad-copy\"}}"));
	 client.copyPad(padID, copyPadId);
	 mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"))
	 .respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be kept\\n\"}}"));
	 String copyPadText = (String) client.getText(copyPadId).get("text");
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/movePad")).respond(
	 HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	 client.movePad(padID, movePadId);
	 String movePadText = (String) client.getText(movePadId).get("text");
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setText")).respond(
	 HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	 client.setText(movePadId, change);
	 client.copyPad(movePadId, copyPadId, true);
	 mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"));
	 mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"))
	 .respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be changed\\n\"}}"));
	 String copyPadTextForce = (String) client.getText(copyPadId).get("text");
	 client.movePad(movePadId, copyPadId, true);
	 String movePadTextForce = (String) client.getText(copyPadId).get("text");
	
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad")).respond(
	 HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	 client.deletePad(copyPadId);
	 client.deletePad(padID);
	
	 assertEquals(keep + "\n", copyPadText);
	 assertEquals(keep + "\n", movePadText);
	
	 assertEquals(change + "\n", copyPadTextForce);
	 assertEquals(change + "\n", movePadTextForce);
	 }

	@Test
	public void create_pads_and_list_them() throws InterruptedException {
		String pad1 = "integration-test-pad-1";
		String pad2 = "integration-test-pad-2";
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		client.createPad(pad1);
		client.createPad(pad2);
		Thread.sleep(100);
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAllPads"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padIDs\":[\"integration-test-pad-1\",\"integration-test-pad-2\"]}}"));
		List padIDs = (List) client.listAllPads().get("padIDs");
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		client.deletePad(pad1);
		client.deletePad(pad2);

		assertTrue(String.format("Size was %d", padIDs.size()), padIDs.size() >= 2);
		assertTrue(padIDs.contains(pad1));
		assertTrue(padIDs.contains(pad2));
	}

	@Test
	 public void create_pad_and_chat_about_it() {
	 String padID = "integration-test-pad-1";
	 String user1 = "user1";
	 String user2 = "user2";
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
	 .respond(HttpResponse.response().withStatusCode(200)
	 .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.01\"}}"));
	 Map response = client.createAuthorIfNotExistsFor(user1,
	 "integration-author-1");
	 String author1Id = (String) response.get("authorID");
	 mockServer.clear(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"));
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
	 .respond(HttpResponse.response().withStatusCode(200)
	 .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.02\"}}"));
	 response = client.createAuthorIfNotExistsFor(user2, "integration-author-2");
	 String author2Id = (String) response.get("authorID");
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")).respond(
	 HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	 client.createPad(padID);
	 try {
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendChatMessage"))
	 .respond(HttpResponse.response().withStatusCode(200)
	 .withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	 client.appendChatMessage(padID, "hi from user1", author1Id);
	 client.appendChatMessage(padID, "hi from user2", author2Id,
	 System.currentTimeMillis() / 1000L);
	 client.appendChatMessage(padID, "gå å gjør et ærend", author1Id,
	 System.currentTimeMillis() / 1000L);
	 mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHead"))
	 .respond(HttpResponse.response().withStatusCode(200)
	 .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"chatHead\":2}}"));
	 response = client.getChatHead(padID);
	 long chatHead = (long) response.get("chatHead");
	 assertEquals(2, chatHead);
	
	 mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory"))
	 .respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"messages\": [ \"hi from user1\",\"hi from user2\",\"gå å gjør et ærend\"]}}"));
	 response = client.getChatHistory(padID);
	 List chatHistory = (List) response.get("messages");
	 assertEquals(3, chatHistory.size());
	 // assertEquals("gå å gjør et ærend", ((Map)chatHistory.get(2)).get("text"));
	 mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory"));
	 mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory"))
	 .respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"messages\": [ {\"text\":\"hi from user1\"},{\"text\":\"hi from user2\"}]}}"));
	 response = client.getChatHistory(padID, 0, 1);
	 chatHistory = (List) response.get("messages");
	 assertEquals(2, chatHistory.size());
	 assertEquals("hi from user2", ((Map) chatHistory.get(1)).get("text"));
	 } finally {
	 mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad"))
	 .respond(HttpResponse.response().withStatusCode(200)
	 .withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	 client.deletePad(padID);
	 }
	
	 }
}
