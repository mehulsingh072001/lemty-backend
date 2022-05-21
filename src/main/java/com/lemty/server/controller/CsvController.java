package com.lemty.server.controller;

import com.lemty.server.LemtyApplication;
import com.lemty.server.domain.Prospect;
import com.lemty.server.service.CsvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RestController
@RequestMapping(path = "api/csv")
public class CsvController {
    Logger logger = LoggerFactory.getLogger(LemtyApplication.class);

    @Autowired
    CsvService csvService;


    @PostMapping("/getCsvHeaders")
    public List<String> getCsvHeaders(@RequestParam("file") MultipartFile file){
        return csvService.getHeaders(file);
    }

    @PostMapping("/getColumnData/{listId}/{userId}")
    public List<String> getColumnData(@RequestParam("file") MultipartFile file, @RequestParam("first_name") String first_name, @PathVariable("listId") String listId, @PathVariable("userId") String userId){
        return csvService.getColumnData(file, first_name, listId, userId);
    }

    @PostMapping("/csvToProspects")
    public List<Prospect> csvToProspects(@RequestParam("file") MultipartFile file, 
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("prospectEmail") String prospectEmail,
            @RequestParam("prospectCompany") String prospectCompany,
            @RequestParam("prospectMobileNumber") String prospectMobileNumber,
            @RequestParam("prospectAccount") String prospectAccount,
            @RequestParam("prospectCompanyEmail") String prospectCompanyEmail,
            @RequestParam("prospectDepartment") String prospectDepartment,
            @RequestParam("prospectTitle") String prospectTitle,
            @RequestParam("prospectCompanyDomain") String prospectCompanyDomain,
            @RequestParam("prospectLinkedinUrl") String prospectLinkedinUrl,
            @RequestParam("prospectTwitterUrl") String prospectTwitterUrl,
            @RequestParam("prospectLocation") String prospectLocation,
            @RequestParam("prospectCountry") String prospectCountry,
            @RequestParam("listId") String listId,
            @RequestParam("userId") String userId)
    {
        return csvService.csvToProspects(file,
                firstName,
                lastName,
                prospectEmail,
                prospectCompany,
                prospectMobileNumber,
                prospectAccount,
                prospectCompanyEmail,
                prospectDepartment,
                prospectTitle,
                prospectCompanyDomain,
                prospectLinkedinUrl,
                prospectTwitterUrl,
                prospectLocation,
                prospectCountry,
                listId,
                userId
                );
    }
}
