package com.lemty.server.helpers;

import com.lemty.server.LemtyApplication;
import com.lemty.server.domain.EmailSignature;
import com.lemty.server.domain.Prospect;
import com.lemty.server.domain.Unsubscribe;
import com.lemty.server.service.EmailSignatureService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlaceholderHelper {
    Logger logger = LoggerFactory.getLogger(PlaceholderHelper.class);
    @Autowired
    private Environment env;

    public String fieldsReplacer(String body, Prospect prospect) {
        if(prospect.getLastName() != null || prospect.getFirstName() != null){
            body = body.replace("{{fullName}}", prospect.getFirstName() + " " + prospect.getLastName());
        }
        if(prospect.getFirstName() != null){
            body = body.replace("{{firstName}}", prospect.getFirstName());
        }
        if(prospect.getLastName() != null){
            body = body.replace("{{lastName}}", prospect.getLastName());
        }
        if(prospect.getProspectEmail() != null){
            body = body.replace("{{email}}", prospect.getProspectEmail());
        }
        if(prospect.getProspectCompany() != null){
            body = body.replace("{{company}}", prospect.getProspectCompany());
        }
        if(prospect.getProspectAccount() != null){
            body = body.replace("{{account}}", prospect.getProspectAccount());
        }
        if(prospect.getProspectTitle() != null){
            body = body.replace("{{title}}", prospect.getProspectTitle());
        }
        if(prospect.getProspectDepartment() != null){
            body = body.replace("{{department}}", prospect.getProspectDepartment());
        }
        if(prospect.getProspectMobileNumber() != null){
            body = body.replace("{{phone}}", prospect.getProspectMobileNumber());
        }
        if(prospect.getProspectCompanyEmail() != null){
            body = body.replace("{{companyEmail}}", prospect.getProspectCompanyEmail());
        }
        if(prospect.getProspectCompanyPhone() != null){
            body = body.replace("{{companyPhone}}", prospect.getProspectCompanyPhone());
        }
        if(prospect.getProspectCompanyDomain() != null){
            body = body.replace("{{companyDomain}}", prospect.getProspectCompanyDomain());
        }
        if(prospect.getProspectLinkedinUrl() != null){
            body = body.replace("{{linkedinUrl}}", prospect.getProspectLinkedinUrl());
        }
        if(prospect.getProspectTwitterUrl() != null){
            body = body.replace("{{twitterId}}", prospect.getProspectTwitterUrl());
        }
        if(prospect.getProspectCity() != null){
            body = body.replace("{{city}}", prospect.getProspectCity());
        }

        if(prospect.getProspectLocation() != null){
            body = body.replace("{{location}}", prospect.getProspectLocation());
        }
        if(prospect.getProspectCountry() != null){
            body = body.replace("{{country}}", prospect.getProspectCountry());
        }
        return body;
    }

    // public String unsubLinkReplacer(String body){
    //     body = body.replace("{{unsubscribe_link}}", "fd");
    // }

    public String signatureReplacer(String body, EmailSignature signature){
        body = body.replace("{{signature}}", signature.getSignature());
        return body;
    }

    public String unsubLinkReplacer(String body, String prospectId, Unsubscribe unsubscribe){
        String link = unsubscribe.getBody();
        link = link.replace("{{prospectId}}", prospectId);
        body = body.replace("{{unsubscribe}}", link);
        return body;
    }

    public String bodyLinkReplacer(String body, String prospectId, String campaignId, Integer stepNumber, Integer mailNumber){
        String trackLink = env.getProperty("track.url") + "/clicked/" + prospectId + "/" + campaignId + "/" + stepNumber+ "/" + mailNumber;
        // String oldLinks = StringUtils.substringBetween(body, "href=\"", "\"");
        String str = "href=\"";
        if(StringUtils.contains(body, str)){
            String newString = trackLink + "?url=";
            int pos_str = body.indexOf(str);
            StringBuilder sb = new StringBuilder(body);
            String blah = sb.insert((pos_str + 6), newString).toString();
            body = blah;
        }
        return body;

    }
}
