import java.io.*;
import java.net.*;
import java.rmi.server.*;
import java.security.*;
import java.security.cert.CertificateException;
import javax.net.ssl.*;

public class RMISSLClientSocketFactory
        implements RMIClientSocketFactory, Serializable {

    public Socket createSocket(String host, int port) throws IOException {
        try{
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            String password = "aabbcc";
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream( "\\client\\client-certificate.p12");
            keyStore.load(inputStream, password.toCharArray());

            // TrustManagerFactory ()
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            String password2 = "abcdefg";
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX", "SunJSSE");
            InputStream inputStream1 = ClassLoader.getSystemClassLoader().getResourceAsStream("\\server\\server-certificate.p12");
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

            // set up the SSL Context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[]{x509KeyManager}, new TrustManager[]{x509TrustManager}, null);

            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            SSLSocket kkSocket = (SSLSocket) socketFactory.createSocket("127.0.0.1", 5099);
            kkSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
            return kkSocket;

        }catch (IOException e){
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return null;
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