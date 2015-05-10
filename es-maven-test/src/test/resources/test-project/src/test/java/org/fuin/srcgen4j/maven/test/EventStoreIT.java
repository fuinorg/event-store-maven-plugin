package org.fuin.srcgen4j.maven.test;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class EventStoreIT {

    @Test
    public void testRead() throws IOException {

        // PREPARE
        final URL url = new URL("http://127.0.0.1:2113/streams/$all");
        HttpURLConnection conn = (HttpURLConnection) url
                .openConnection();
        String userCredentials = "admin:changeit";
        String basicAuth = "Basic "
                + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
        conn.setRequestProperty("Authorization", basicAuth);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept",
                "application/vnd.eventstore.atom+json");

        // TEST
        final InputStream in = conn.getInputStream();
        final String response = IOUtils.toString(in);

        // VERIFY
        assertThat(response).contains("\"title\": \"All events\"");
        
    }
    
}
