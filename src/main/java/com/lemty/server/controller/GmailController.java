package com.lemty.server.controller;

import java.util.Map;

import com.lemty.server.LemtyApplication;
import com.lemty.server.domain.GmailCreds;
import com.lemty.server.helpers.GmailHelper;
import com.lemty.server.jobPayload.MailRequest;
import com.lemty.server.service.GmailCredsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(path = "api/gmail")
public class GmailController{
    Logger logger = LoggerFactory.getLogger(LemtyApplication.class);
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    final GmailCredsService gmailCredsService;
    private final GmailHelper gmailHelper;

    public GmailController(GmailCredsService gmailCredsService, GmailHelper gmailHelper) {
        this.gmailCredsService = gmailCredsService;
        this.gmailHelper = gmailHelper;
    }

    @GetMapping(path = "/get-token/{userId}", produces = "application/json")
    public String getToken(@RequestParam Map<String, String> input, @PathVariable("userId") String userId){
        String fcode = input.get("code");

        String code = "code="+fcode+"&";
        String client_id = "client_id=1087727582839-72qgrk3g3ea46kq65coo7lbgg3f5cteg.apps.googleusercontent.com&";
        String client_secret = "client_secret=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&";
        //String redirect_uri = "redirect_uri=http://localhost:8080/api/gmail/redirect&";
        String redirect_uri = "redirect_uri=http://localhost:3000/redirect&";
        String grant_type = "grant_type=authorization_code&";

        headers.setContentType(MediaType.APPLICATION_JSON);

        String baseUrl = "https://oauth2.googleapis.com/token?"+code+client_id+client_secret+redirect_uri+grant_type;
        HttpEntity<String> entity = new HttpEntity<String>(headers);
		Map<String, String> response = restTemplate.postForObject(baseUrl, entity, Map.class);
        //String infoUrl = "https://www.googleapis.com/oauth2/v1/userinfo?access_token="+response.get("access_token");
        String infoUrl = "https://www.googleapis.com/gmail/v1/users/me/profile?access_token="+response.get("access_token");
        logger.info(infoUrl);
        Map<String, String> ud = restTemplate.getForObject(infoUrl,Map.class);
        GmailCreds creds = new GmailCreds();
        creds.setEmail(ud.get("emailAddress"));
        creds.setRefreshToken(response.get("refresh_token"));
         gmailCredsService.addNewCreds(creds, userId);
         return "Success";
    }

    @GetMapping(path = "/access_token")
    public String getAccessToken(@RequestParam("emailId") String emailId){
        return gmailHelper.getAccessToken(emailId);
    }

/*
    @PostMapping(path = "/create")
    public Map<Object, Object> sendMessage(@RequestBody MailRequest mailRequest){
        return gmailHelper.sendMessage(mailRequest);
    }
*/
}
