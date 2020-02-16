package eu.kartoffelquadrat.asyncrestdemo.client;

/**
 * A bundled object. Useful as return type for the ClientUtils class.
 */
public class ServerUrls {

    private String serverTestOnlineUrl;
    private String serverGetUpdateUrl;

    public ServerUrls(String serverTestOnlineUrl, String serverGetUpdateUrl) {
        this.serverTestOnlineUrl = serverTestOnlineUrl;
        this.serverGetUpdateUrl = serverGetUpdateUrl;
    }

    public String getServerTestOnlineUrl() {
        return serverTestOnlineUrl;
    }

    public String getServerGetUpdateUrl() {
        return serverGetUpdateUrl;
    }
}
