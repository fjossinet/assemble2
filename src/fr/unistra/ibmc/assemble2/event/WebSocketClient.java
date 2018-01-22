package fr.unistra.ibmc.assemble2.event;


import com.mongodb.util.JSON;
import fr.unistra.ibmc.assemble2.utils.AssembleConfig;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;


/**
 * A class to open a websocket with a website
 */
public class WebSocketClient extends org.java_websocket.client.WebSocketClient{

    public WebSocketClient(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        this.send(JSON.parse("{'header':'new external tool', 'id':'"+ AssembleConfig.getID()+"'}").toString());
    }

    @Override
    public void onMessage(String s) {
        System.out.println(s);
    }

    @Override
    public void onClose(int i, String s, boolean b) {

    }

    @Override
    public void onError(Exception e) {

    }
}
