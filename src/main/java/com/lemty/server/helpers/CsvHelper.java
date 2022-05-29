package com.lemty.server.helpers;

import com.lemty.server.domain.Prospect;
import com.lemty.server.repo.ProspectRepository;
import com.lemty.server.service.ProspectService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

import com.lemty.server.LemtyApplication;


@Component
public class CsvHelper {
    Logger logger = LoggerFactory.getLogger(LemtyApplication.class);
    private final ProspectService prospectService;
    private final ProspectRepository prospectRepository;

    public CsvHelper(ProspectService prospectService, ProspectRepository prospectRepository) {
        this.prospectService = prospectService;
        this.prospectRepository = prospectRepository;
    }

    public List<String> getHeaders(InputStream is){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            CSVParser csvFileParser = new CSVParser(br, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
            List<String> headers = csvFileParser.getHeaderNames();
            return headers;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getColumnData(InputStream is, String header){
        List<String> datas = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            CSVParser csvFileParser = new CSVParser(br, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

            Iterable<CSVRecord> csvRecords = csvFileParser.getRecords();
            for(CSVRecord csvRecord : csvRecords){
                String data = new String(
                    csvRecord.get(header.toLowerCase())
                );
                logger.info(data);
                datas.add(data);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return datas;
    }

    public Map<String, Object> csvToProspects(
            InputStream is,
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
            String userId
    ){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            CSVParser csvFileParser = new CSVParser(br, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
            List<Prospect> prospects = new ArrayList<Prospect>();
            List<Prospect> failedProspects = new ArrayList<>();
            Map<String, Object> response = new HashMap<>();

            Iterable<CSVRecord> csvRecords = csvFileParser.getRecords();
            for(CSVRecord csvRecord : csvRecords){
                Prospect prospect = new Prospect(
                        (Objects.equals(firstNameHeader, "")) ? null : csvRecord.get(firstNameHeader),
                        (Objects.equals(lastNameHeader, "")) ? null : csvRecord.get(lastNameHeader),
                        (Objects.equals(prospectEmailHeader, "")) ? null : csvRecord.get(prospectEmailHeader),
                        (Objects.equals(prospectCompanyHeader, "")) ? null : csvRecord.get(prospectCompanyHeader),
                        (Objects.equals(prospectMobileNumberHeader, "")) ? null : csvRecord.get(prospectMobileNumberHeader),
                        (Objects.equals(prospectAccountHeader, "")) ? null : csvRecord.get(prospectAccountHeader),
                        (Objects.equals(prospectCompanyEmailHeader, "")) ? null : csvRecord.get(prospectCompanyEmailHeader),
                        (Objects.equals(prospectDepartmentHeader, "")) ? null : csvRecord.get(prospectDepartmentHeader),
                        (Objects.equals(prospectTitleHeader, "")) ? null : csvRecord.get(prospectTitleHeader),
                        (Objects.equals(prospectCompanyDomainHeader, "")) ? null : csvRecord.get(prospectCompanyDomainHeader),
                        (Objects.equals(prospectLinkedinurlHeader, "")) ? null : csvRecord.get(prospectCompanyDomainHeader),
                        (Objects.equals(prospectTwitterurlHeader, "")) ? null : csvRecord.get(prospectTwitterurlHeader),
                        (Objects.equals(prospectLocationHeader, "")) ? null : csvRecord.get(prospectLocationHeader),
                        (Objects.equals(prospectCountryHeader, "")) ? null : csvRecord.get(prospectCountryHeader)
                );
                if(
                        Objects.equals(csvRecord.get(firstNameHeader), "")
                                && Objects.equals(csvRecord.get(lastNameHeader), "")
                ){
                    failedProspects.add(prospect);
                }
                else{
                    if(prospectRepository.existsByProspectEmail(prospect.getProspectEmail())){
                        failedProspects.add(prospect);
                    }
                    else{
                        prospects.add(prospect);
                        prospectService.addNewProspect(prospect, listId, userId);
                    }
                }

            }
            response.put("prospects", prospects);
            response.put("failedProspects", failedProspects);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
