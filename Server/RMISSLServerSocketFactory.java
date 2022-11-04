import java.io.*;
import java.net.*;
import java.rmi.server.*;
import javax.net.ssl.*;
import java.security.KeyStore;
import javax.net.ssl.*;

public class RMISSLServerSocketFactory implements RMIServerSocketFactory {

    /*
     * Create one SSLServerSocketFactory, so we can reuse sessions
     * created by previous sessions of this SSLContext.
     */
    public SSLServerSocketFactory ssf = null;

    public RMISSLServerSocketFactory() throws Exception {
        try {
            // set up key manager to do server authentication
            /*SSLContext ctx;
            KeyManagerFactory kmf;
            KeyStore ks;

            char[] passphrase = "passphrase".toCharArray();
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("testkeys"), passphrase);

            kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, passphrase);

            ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf.getKeyManagers(), null, null);*/
            //System.setProperty("javax.net.debug", "all");
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            String password = "abcdefg";
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("\\server\\server-certificate.p12");
            keyStore.load(inputStream, password.toCharArray());

            String password2 = "aabbcc";
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX", "SunJSSE");
            InputStream inputStream1 = ClassLoader.getSystemClassLoader().getResourceAsStream( "\\client\\client-certificate.p12");
            trustStore.load(inputStream1, password2.toCharArray());
            trustManagerFactory.init(trustStore);
            X509TrustManager x509TrustManager = null;
            for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
                if (trustManager instanceof X509TrustManager) {
                    x509TrustManager = (X509TrustManager) trustManager;
                    break;
                }
            }

            if (x509TrustManager == null) throw new NullPointerException();

            System.out.println(x509TrustManager);
            // KeyManagerFactory ()
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
            keyManagerFactory.init(keyStore, password.toCharArray());
            X509KeyManager x509KeyManager = null;
            for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
                if (keyManager instanceof X509KeyManager) {
                    x509KeyManager = (X509KeyManager) keyManager;
                    break;
                }
            }
            if (x509KeyManager == null) throw new NullPointerException();

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[]{x509KeyManager}, new TrustManager[]{x509TrustManager}, null);

            ssf = sslContext.getServerSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return ssf.createServerSocket(port);
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }
}