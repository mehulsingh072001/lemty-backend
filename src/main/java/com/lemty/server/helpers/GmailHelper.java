//TODO: Get Access Token From refresh Token
package com.lemty.server.helpers;

import com.lemty.server.LemtyApplication;
import com.lemty.server.domain.GmailCreds;
import com.lemty.server.jobPayload.MailRequest;
import com.lemty.server.service.GmailCredsService;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Component
public class GmailHelper {
    Logger logger = LoggerFactory.getLogger(LemtyApplication.class);
    private final GmailCredsService gmailCredsService;

    public GmailHelper(GmailCredsService gmailCredsService){
        this.gmailCredsService = gmailCredsService;
    }

    public String getAccessToken(String emailId){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        GmailCreds creds = gmailCredsService.getCredsByEmail(emailId);

        String refresh_token = "refresh_token=" + creds.getRefreshToken() + "&";
        String client_id = "client_id=1087727582839-72qgrk3g3ea46kq65coo7lbgg3f5cteg.apps.googleusercontent.com&";
        String client_secret = "client_secret=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&";
        String grant_type = "grant_type=refresh_token&";

        String baseUrl = "https://oauth2.googleapis.com/token?"+client_id+client_secret+refresh_token+grant_type;
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject request = new JSONObject();
        HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);
		Map<Object, Object> response = restTemplate.postForObject(baseUrl, entity, Map.class);

        String id_token = String.valueOf(response.get("id_token"));

        // byte[] id = Base64.getDecoder().decode(id_token);

        String access_token = String.valueOf(response.get("access_token"));
        return access_token;
    }

     public Map<Object, Object> sendMessage(MailRequest mailRequest){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        GmailCreds creds = gmailCredsService.getCredsByEmail(mailRequest.getFrom());

        String email = (
                "From: " + creds.getDisplayName() + " <"+ mailRequest.getFrom() +">\n"+
                "Subject: " + mailRequest.getSubject() + " \n"+
                "To: " + mailRequest.getTo() + "\n"+
                "MIME-Version 1.0\n"+
                "Content-Type: text/html; charset=utf-8\n\n"+
                mailRequest.getBody()
                );
        String baseUrl = "https://gmail.googleapis.com/gmail/v1/users/singhrathoremehul@gmail.com/messages/send";

        JSONObject request = new JSONObject();

        String access_token = getAccessToken(mailRequest.getFrom());
        headers.setBearerAuth(access_token);
        String encodedString = Base64.getEncoder().encodeToString(email.getBytes());

        request.put("raw", encodedString);

        HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);
		Map<Object, Object> response = restTemplate.postForObject(baseUrl, entity, Map.class);
        return response;
    }

     public Map<Object, Object> sendMessageInThread(MailRequest mailRequest, String threadId, String msgId){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        GmailCreds creds = gmailCredsService.getCredsByEmail(mailRequest.getFrom());

        String email = (
                "From: " + creds.getDisplayName() + " <"+ mailRequest.getFrom() +">\n"+
                "Subject: " + mailRequest.getSubject() + " 2\n"+
                "To: " + mailRequest.getTo() + "\n"+
                "MIME-Version 1.0\n"+
                "Content-Type: text/html; charset=utf-8\n\n"+
                mailRequest.getBody()
                );
        // https://gmail.googleapis.com/gmail/v1/users/[USERID]/messages/send?key=[YOUR_API_KEY] HTTP/1.1
        String baseUrl = "https://gmail.googleapis.com/gmail/v1/users/singhrathoremehul@gmail.com/messages/send";

        JSONObject request = new JSONObject();

        String access_token = getAccessToken(mailRequest.getFrom());
        headers.setBearerAuth(access_token);
        headers.add("Reference", msgId);
        String encodedString = Base64.getEncoder().encodeToString(email.getBytes());

        request.put("raw", encodedString);
        request.put("threadId", threadId);

        HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);
		Map<Object, Object> response = restTemplate.postForObject(baseUrl, entity, Map.class);
        return response;
    }
}
