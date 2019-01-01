package fr.lrgn.yuzurss;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class YuzuRssApplicationTests
{
	private static final String RSS_PATH = "/rss.xml";
	private static final String RSS_RESPONSE = "<rss><channel><title>rss_feed</title><item><link>rss_link1</link><title>rss_title1</title><pubDate>Sun, 09 Dec 2018 09:22:00 +0000</pubDate></item><item><link>rss_link2</link><title>rss_title2</title><pubDate>Fri, 19 Oct 2018 21:49:54 +0000</pubDate></item></channel></rss>";
	private static final String ATOM_PATH = "/atom.xml";
	private static final String ATOM_RESPONSE = "<feed><entry><author><name>atom_feed</name></author><link><href>atom_link1</href></link><title>atom_title1</title><published>2018-11-03T18:12:15+00:00</published></entry><entry><author><name>atom_feed</name></author><link><href>atom_link2</href></link><title>atom_title2</title><published>2018-10-30T18:12:15+00:00</published></entry></feed>";

	private static final String ATOM_RESULT = "[{\"title\":\"atom_title1\",\"link\":\"atom_link1\",\"published\":\"2018-11-03T18:12:15.000+0000\",\"author\":\"atom_feed\"},{\"title\":\"atom_title2\",\"link\":\"atom_link2\",\"published\":\"2018-10-30T18:12:15.000+0000\",\"author\":\"atom_feed\"}]";
	private static final String RSS_RESULT = "[{\"title\":\"rss_title1\",\"link\":\"rss_link1\",\"published\":\"2018-12-09T09:22:00.000+0000\",\"author\":\"rss_feed\"},{\"title\":\"rss_title2\",\"link\":\"rss_link2\",\"published\":\"2018-10-19T21:49:54.000+0000\",\"author\":\"rss_feed\"}]";
	private static final String ATOM_RSS_RESULT = "[{\"title\":\"rss_title1\",\"link\":\"rss_link1\",\"published\":\"2018-12-09T09:22:00.000+0000\",\"author\":\"rss_feed\"},{\"title\":\"atom_title1\",\"link\":\"atom_link1\",\"published\":\"2018-11-03T18:12:15.000+0000\",\"author\":\"atom_feed\"},{\"title\":\"atom_title2\",\"link\":\"atom_link2\",\"published\":\"2018-10-30T18:12:15.000+0000\",\"author\":\"atom_feed\"},{\"title\":\"rss_title2\",\"link\":\"rss_link2\",\"published\":\"2018-10-19T21:49:54.000+0000\",\"author\":\"rss_feed\"}]";

	@Autowired
	private WebTestClient webClient;

	private MockWebServer server;

	@Before
	public void setup() throws IOException
	{
		server = new MockWebServer();
		server.setDispatcher(new Dispatcher()
		{
			@Override
			public MockResponse dispatch(RecordedRequest request) throws InterruptedException
			{
				switch (request.getPath())
				{
					case RSS_PATH:
						return new MockResponse().setBody(RSS_RESPONSE);
					case ATOM_PATH:
						return new MockResponse().setBody(ATOM_RESPONSE);
				}
				return null;
			}
		});
		server.start();
	}

	@After
	public void shutdown() throws IOException
	{
		server.shutdown();
	}

	@Test
	public void contextLoads()
	{
	}

	@Test
	public void testAtomFeed() throws UnsupportedEncodingException
	{
		final ArrayList<String> urls = new ArrayList<String>();
		urls.add("http://127.0.0.1:" + server.getPort() + "/atom.xml");
		final FeedRequestBody body = new FeedRequestBody(urls, 10);

		webClient.post().uri("/feed").body(BodyInserters.fromObject(body)).exchange().expectStatus().isOk().expectBody(String.class)
				.isEqualTo(ATOM_RESULT);
	}

	@Test
	public void testRssFeed() throws UnsupportedEncodingException
	{
		final ArrayList<String> urls = new ArrayList<String>();
		urls.add("http://127.0.0.1:" + server.getPort() + "/rss.xml");
		final FeedRequestBody body = new FeedRequestBody(urls, 10);

		webClient.post().uri("/feed").body(BodyInserters.fromObject(body)).exchange().expectStatus().isOk().expectBody(String.class)
				.isEqualTo(RSS_RESULT);
	}

	@Test
	public void testAtomRSSFeed() throws UnsupportedEncodingException
	{
		final ArrayList<String> urls = new ArrayList<String>();
		urls.add("http://127.0.0.1:" + server.getPort() + "/atom.xml");
		urls.add("http://127.0.0.1:" + server.getPort() + "/rss.xml");
		final FeedRequestBody body = new FeedRequestBody(urls, 10);

		webClient.post().uri("/feed").body(BodyInserters.fromObject(body)).exchange().expectStatus().isOk().expectBody(String.class)
				.isEqualTo(ATOM_RSS_RESULT);
	}
}
