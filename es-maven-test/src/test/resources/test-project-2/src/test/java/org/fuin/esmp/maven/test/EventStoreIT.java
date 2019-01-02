package org.fuin.esmp.maven.test;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.github.msemys.esjc.AllEventsSlice;
import com.github.msemys.esjc.EventStore;
import com.github.msemys.esjc.EventStoreBuilder;
import com.github.msemys.esjc.Position;

public class EventStoreIT {

    @Test
    public void testReadHttp() throws IOException {

        // PREPARE
        final URL url = new URL("http://127.0.0.1:2113/streams/$all");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        String userCredentials = "admin:changeit";
        String basicAuth = "Basic "
                + Base64.encodeBase64String(userCredentials.getBytes());
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
    
    @Test
    public void testReadTcp() {
        
        // PREPARE
        final EventStore es = EventStoreBuilder.newBuilder()
                .singleNodeAddress("127.0.0.1", 7773)
                .userCredentials("admin", "changeit")
                .maxClientReconnections(2)
                .build();
        
        // TEST
        final AllEventsSlice slice = es.readAllEventsForward(Position.START, 1, false).join();
        
        // VERIFY
        assertThat(slice.events.size()).isEqualTo(1);
        
    }

    @Test
    public void testReadTcpSsl() {

        // PREPARE
        final EventStore es = EventStoreBuilder.newBuilder()
                .singleNodeAddress("127.0.0.1", 7779)
                .useSslConnection() // Accept self signed certificates
                .userCredentials("admin", "changeit")
                .maxClientReconnections(2)
                .build();
        
        // TEST
        final AllEventsSlice slice = es.readAllEventsForward(Position.START, 1, false).join();
        
        // VERIFY
        assertThat(slice.events.size()).isEqualTo(1);
        
    }
    

}
