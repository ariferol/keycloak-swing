package com.sample.keycloak;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import javax.swing.SwingUtilities;

/**
 * Keycloak Masaüstü Login Uygulaması - PKCE with Authorization Code Flow
 */
public class MainScreen extends javax.swing.JFrame {

    public MainScreen() {
        initComponents();
        jTextArea1.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 11));
        jTextArea1.setText("OAuth2/PKCE Login Uygulaması\n" +
                "==========================\n" +
                "Login butonuna tıklayarak başlayın...\n");
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int width = getWidth();
        int height = screenSize.height - 100;
        setSize(width, height);
        setLocation(0, 0);
        setTitle("Keycloak Masaüstü Login Uygulaması");
    }

    private void initComponents() {
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jButton1.setText("Login");
        jButton1.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 17));
        jButton1.addActionListener(evt -> jButton1ActionPerformed());
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 672, Short.MAX_VALUE)
                                .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                                .addGap(203, 203, 203)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
                                .addContainerGap())
        );
        pack();
    }

    private void jButton1ActionPerformed() {
        new Thread(this::loginWithKeycloak).start();
    }

    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) { }
        java.awt.EventQueue.invokeLater(() -> new MainScreen().setVisible(true));
    }

    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;

    private Properties config;

    private void ensureConfigLoaded() {
        if (config != null) return;
        config = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) throw new IllegalStateException("config.properties bulunamadı (resources klasöründe olmalı)");
            config.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Konfigürasyon yüklenemedi: " + e.getMessage(), e);
        }
    }

    private String cfg(String key) {
        ensureConfigLoaded();
        String val = config.getProperty(key);
        if (val == null) throw new IllegalStateException("Zorunlu parametre eksik: " + key);
        return val.trim();
    }

    private int cfgInt(String key, int def) {
        ensureConfigLoaded();
        String val = config.getProperty(key);
        if (val == null) return def;
        try { return Integer.parseInt(val.trim()); } catch (NumberFormatException e) { return def; }
    }

    private void loginWithKeycloak() {
        SwingUtilities.invokeLater(() -> jButton1.setEnabled(false));
        try {
            setTextArea("");
            logStep("BAŞLANGIC", "OAuth2/PKCE login işlemi başlatılıyor...");
            logStep("PKCE", "PKCE icin Code verifier oluşturuluyor...");
            String codeVerifier = PKCEUtil.generateCodeVerifier();
            String codeChallenge = PKCEUtil.generateCodeChallenge(codeVerifier);
            logStep("PKCE", "PKCE icin, Code challenge oluşturuldu: " + codeChallenge.substring(0, 10) + "...");
            logStep("CSRF Koruma", "State ve nonce oluşturuluyor...");
            String state = generateRandomString(16);
            String nonce = generateRandomString(16);
            logStep("CSRF Koruma", "State: " + state.substring(0, 8) + "..., Nonce: " + nonce.substring(0, 8) + "...");
            logStep("URL", "Authorize URL oluşturuluyor...");
            String clientId = cfg("keycloak.clientId");
            String redirectUri = cfg("keycloak.redirectUri");
            String scope = cfg("keycloak.scope");
            String authUrl = buildAuthUrl()
                    + "?response_type=code"
                    + "&client_id=" + URLEncoder.encode(clientId, "UTF-8")
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8")
                    + "&scope=" + URLEncoder.encode(scope, "UTF-8")
                    + "&code_challenge=" + codeChallenge
                    + "&code_challenge_method=S256"
                    + "&state=" + URLEncoder.encode(state, "UTF-8")
                    + "&nonce=" + URLEncoder.encode(nonce, "UTF-8");
            logStep("URL", "Authorize URL hazırlandı");
            logStep("TARAYICI", "Keycloak login sayfası açılıyor...");
            Desktop.getDesktop().browse(new URI(authUrl));
            logStep("TARAYICI", "Login sayfası açıldı");
            int port = cfgInt("local.server.port", 8080);
            String[] codeAndState = waitForCodeWithState(port);
            String code = codeAndState[0];
            String returnedState = codeAndState[1];
            if (code == null) { logStep("HATA", "Authorization code alınamadı!"); return; }
            logStep("CALLBACK", "Authorization code alındı: " + code.substring(0, Math.min(20, code.length())) + "...");
            if (!state.equals(returnedState)) { logStep("HATA", "State doğrulaması başarısız"); return; }
            logStep("GÜVENLİK", "State doğrulaması başarılı");
            logStep("TOKEN", "Token exchange başlatılıyor...");
            String token = exchangeToken(code, codeVerifier, clientId, redirectUri);
            logStep("BAŞARILI", "Token exchange tamamlandı");
            formatAndDisplayToken(token);
        } catch (Exception ex) {
            logStep("HATA", "Beklenmeyen hata: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            SwingUtilities.invokeLater(() -> jButton1.setEnabled(true));
        }
    }

    private void setTextArea(String text) { SwingUtilities.invokeLater(() -> jTextArea1.setText(text)); }
    private void appendTextArea(String text) { SwingUtilities.invokeLater(() -> { jTextArea1.append(text + "\n"); jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength()); }); }
    private void logStep(String step, String detail) { String timestamp = java.time.LocalTime.now().toString().substring(0,8); appendTextArea(String.format("[%s] %s: %s", timestamp, step, detail)); }
    // PKCE util methods replaced by PKCEUtil class
    private String generateRandomString(int length) { byte[] bytes = new byte[length]; new SecureRandom().nextBytes(bytes); return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }

    private String[] waitForCodeWithState(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            logStep("SERVER", "HTTP server port " + port + "'de başlatıldı, callback bekleniyor...");
            String code = null; String state = null;
            try {
                Socket client = serverSocket.accept();
                logStep("CALLBACK", "Tarayıcıdan geri dönüş alındı, veriler işleniyor...");
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line; String query = null;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    if (line.startsWith("GET ")) {
                        String[] parts = line.split(" ");
                        if (parts.length >= 2) {
                            String path = parts[1];
                            if (path.startsWith("/callback")) {
                                int qIdx = path.indexOf("?");
                                if (qIdx != -1 && qIdx + 1 < path.length()) query = path.substring(qIdx + 1);
                            }
                        }
                    }
                    if (line.trim().isEmpty()) break;
                }
                if (query != null) {
                    logStep("CALLBACK", "Query parametreleri ayrıştırılıyor...");
                    for (String param : query.split("&")) {
                        if (param.startsWith("code=")) code = URLDecoder.decode(param.substring(5), "UTF-8");
                        else if (param.startsWith("state=")) state = URLDecoder.decode(param.substring(6), "UTF-8");
                    }
                }
                PrintWriter out = new PrintWriter(client.getOutputStream());
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println();
                out.println("<html><body><h1>Login islemi BASARILI!</h1><p>Bu sekmeyi kapatabilirsiniz.</p></body></html>");
                out.flush();
                client.close();
                logStep("CALLBACK", "Tarayıcıya başarı mesajı gönderildi");
            } finally {
                serverSocket.close();
                logStep("SERVER", "HTTP server kapatıldı");
            }
            return new String[]{code, state};
        } catch (Exception ex) {
            logStep("HATA", "HTTP server hatası: " + ex.getMessage());
            ex.printStackTrace();
            return new String[]{null, null};
        }
    }

    private String exchangeToken(String code, String codeVerifier, String clientId, String redirectUri) throws Exception {
        logStep("TOKEN", "Token endpoint'ine POST isteği hazırlanıyor...");
        String clientSecret = config.getProperty("keycloak.clientSecret", "");
        String tokenUrl = buildTokenUrl();
        String params = "grant_type=authorization_code" +
                "&code=" + URLEncoder.encode(code, "UTF-8") +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8") +
                "&client_id=" + URLEncoder.encode(clientId, "UTF-8") +
                "&code_verifier=" + URLEncoder.encode(codeVerifier, "UTF-8");
        if (!clientSecret.isEmpty()) {
            params += "&client_secret=" + URLEncoder.encode(clientSecret, "UTF-8");
            logStep("TOKEN", "Client secret ile gönderiliyor...");
        }
        HttpURLConnection conn = (HttpURLConnection) new URL(tokenUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        try (OutputStream os = conn.getOutputStream()) { os.write(params.getBytes(StandardCharsets.UTF_8)); }
        int status = conn.getResponseCode();
        logStep("TOKEN", "HTTP yanıt kodu: " + status);
        InputStream is = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder(); String line;
        while ((line = in.readLine()) != null) sb.append(line).append("\n");
        if (status >= 400) logStep("HATA", "Token exchange başarısız oldu!"); else logStep("TOKEN", "Access token alındı!");
        return sb.toString();
    }

    private void formatAndDisplayToken(String tokenResponse) {
        String line = createLine("=", 50);
        appendTextArea("\n" + line);
        appendTextArea("            TOKEN EXCHANGE SONUCU");
        appendTextArea(line);
        if (tokenResponse.trim().startsWith("{")) {
            Map<String,String> map = JsonTokenFormatter.parse(tokenResponse);
            for (Map.Entry<String,String> e : map.entrySet()) {
                String k = e.getKey(); String v = e.getValue();
                switch (k) {
                    case "access_token": appendTextArea("✓ ACCESS TOKEN:"); appendTextArea("  " + formatToken(v)); appendTextArea(""); break;
                    case "refresh_token": appendTextArea("✓ REFRESH TOKEN:"); appendTextArea("  " + formatToken(v)); appendTextArea(""); break;
                    case "id_token": appendTextArea("✓ ID TOKEN:"); appendTextArea("  " + formatToken(v)); appendTextArea(""); break;
                    case "expires_in": appendTextArea("✓ EXPIRES IN: " + v + " saniye"); break;
                    case "token_type": appendTextArea("✓ TOKEN TYPE: " + v); break;
                    case "scope": appendTextArea("✓ SCOPE: " + v); break;
                    default: appendTextArea("✓ " + k.toUpperCase() + ": " + v); break;
                }
            }
        } else {
            appendTextArea("RAW RESPONSE:");
            appendTextArea(tokenResponse);
        }
        String endLine = createLine("=", 50);
        appendTextArea(endLine);
        appendTextArea("Login işlemi başarıyla tamamlandı!");
        appendTextArea(endLine);
    }

    private String formatToken(String token) {
        if (token == null || token.length() < 20) return token;
        StringBuilder formatted = new StringBuilder(); int lineLength = 80;
        for (int i = 0; i < token.length(); i += lineLength) {
            if (i + lineLength < token.length()) formatted.append(token, i, i + lineLength).append("\n  "); else formatted.append(token.substring(i));
        }
        return formatted.toString();
    }

    private String normalizeServerUrl(String serverUrl) { return serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length()-1) : serverUrl; }
    private String buildRealmBase() { String serverUrl = normalizeServerUrl(cfg("keycloak.serverUrl")); String realmId = cfg("keycloak.realmId"); return serverUrl + "/realms/" + realmId + "/protocol/openid-connect"; }
    private String buildAuthUrl() { return buildRealmBase() + "/auth"; }
    private String buildTokenUrl() { return buildRealmBase() + "/token"; }
    private String createLine(String character, int count) { StringBuilder sb = new StringBuilder(); for (int i=0;i<count;i++) sb.append(character); return sb.toString(); }
}
