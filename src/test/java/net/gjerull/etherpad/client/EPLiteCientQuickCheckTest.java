package net.gjerull.etherpad.client;

import static org.junit.Assert.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;

import net.gjerull.etherpad.client.generators.StringGenerator;

@RunWith(JUnitQuickcheck.class)
public class EPLiteCientQuickCheckTest {
	private EPLiteClient client;
	private ClientAndServer mockServer;

	@Before
	public void setUp() throws Exception {
		this.client = new EPLiteClient("http://localhost:9001",
				"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");
	}

	@Before
	public void startMockServer() {
		((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("org.mockserver.mock"))
				.setLevel(ch.qos.logback.classic.Level.OFF);
		mockServer = startClientAndServer(9001);
	}

	@After
	public void stopMockServer() {
		mockServer.stop();
	}

	@Property
	public void create_pad_set_and_get_content(@From(StringGenerator.class) String plaintext) {
		// assumeThat(plaintext.matches("[a-zA-Z0-9*"));
		String padID = "integration-test-pad";
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		client.createPad(padID);
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setText")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		client.setText(padID, plaintext);
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"" + plaintext + "\"}}"));
		String text = (String) client.getText(padID).get("text");
		assertEquals(plaintext, text);
	}

}
