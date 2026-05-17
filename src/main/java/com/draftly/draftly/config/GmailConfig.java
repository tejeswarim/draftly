package com.draftly.draftly.config;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;

import java.io.InputStreamReader;
import java.util.Collections;

public class GmailConfig {

    private static final String APPLICATION_NAME = "Draftly";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static Gmail getGmailService() throws Exception {

        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        var in = GmailConfig.class.getResourceAsStream("/credentials.json");

        if (in == null) {
            throw new RuntimeException("credentials.json not found in resources folder");
        }

        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        httpTransport,
                        JSON_FACTORY,
                        clientSecrets,
                        Collections.singleton(GmailScopes.GMAIL_MODIFY)
                )
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                        .setAccessType("offline")
                        .build();

        var credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())
                .authorize("user");

        return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
