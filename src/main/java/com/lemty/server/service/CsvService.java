package com.lemty.server.service;

import com.lemty.server.domain.Prospect;
import com.lemty.server.helpers.CsvHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class CsvService {
    @Autowired
    private final CsvHelper csvHelper;

    private final ProspectService prospectService;

    public CsvService(CsvHelper csvHelper, ProspectService prospectService) {
        this.csvHelper = csvHelper;
        this.prospectService = prospectService;
    }

    public List<String> getHeaders(MultipartFile file){
        try {
            return csvHelper.getHeaders(file.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getColumnData(MultipartFile file, String header, String listId, String userId){
        try {
            return csvHelper.getColumnData(file.getInputStream(), header);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Prospect> csvToProspects(MultipartFile file, 
            String firstNameHeader, 
            String lastNameHeader, 
            String prospectEmailHeader,
            String prospectCompanyHeader,
            String prospectMobileNumberHeader,
            String prospectAccountHeader,
            String prospectCompanyEmailHeader,
            String prospectDepartmentHeader,
            String prospectTitleHeader,
            String prospectCompanyDomainHeader,
            String prospectLinkedinurlHeader,
            String prospectTwitterurlHeader,
            String prospectLocationHeader,
            String prospectCountryHeader,
            String listId, 
            String userId){
        try {
            return csvHelper.csvToProspects(file.getInputStream(), 
                    firstNameHeader, 
                    lastNameHeader, 
                    prospectEmailHeader,
                    prospectCompanyHeader,
                    prospectMobileNumberHeader,
                    prospectAccountHeader,
                    prospectCompanyEmailHeader,
                    prospectDepartmentHeader,
                    prospectTitleHeader,
                    prospectCompanyDomainHeader,
                    prospectLinkedinurlHeader,
                    prospectTwitterurlHeader,
                    prospectLocationHeader,
                    prospectCountryHeader,
                    listId, 
                    userId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
