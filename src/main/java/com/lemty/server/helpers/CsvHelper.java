package com.lemty.server.helpers;

import com.lemty.server.domain.Prospect;
import com.lemty.server.service.ProspectService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.lemty.server.LemtyApplication;


@Component
public class CsvHelper {
    Logger logger = LoggerFactory.getLogger(LemtyApplication.class);
    private final ProspectService prospectService;

    public CsvHelper(ProspectService prospectService) {
        this.prospectService = prospectService;
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

    public List<Prospect> csvToProspects(
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

            Iterable<CSVRecord> csvRecords = csvFileParser.getRecords();
            for(CSVRecord csvRecord : csvRecords){
                if(!Objects.equals(csvRecord.get(firstNameHeader), "") && !Objects.equals(csvRecord.get(lastNameHeader), "") && !Objects.equals(csvRecord.get(prospectEmailHeader), "")){
                    Prospect prospect = new Prospect(
                            (Objects.equals(firstNameHeader, "empty")) ? null : csvRecord.get(firstNameHeader),
                            (Objects.equals(lastNameHeader, "empty")) ? null : csvRecord.get(lastNameHeader),
                            (Objects.equals(prospectEmailHeader, "empty")) ? null : csvRecord.get(prospectEmailHeader),
                            (Objects.equals(prospectCompanyHeader, "empty")) ? null : csvRecord.get(prospectCompanyHeader),
                            (Objects.equals(prospectMobileNumberHeader, "empty")) ? null : csvRecord.get(prospectMobileNumberHeader),
                            (Objects.equals(prospectAccountHeader, "empty")) ? null : csvRecord.get(prospectAccountHeader),
                            (Objects.equals(prospectCompanyEmailHeader, "empty")) ? null : csvRecord.get(prospectCompanyEmailHeader),
                            (Objects.equals(prospectDepartmentHeader, "empty")) ? null : csvRecord.get(prospectDepartmentHeader),
                            (Objects.equals(prospectTitleHeader, "empty")) ? null : csvRecord.get(prospectTitleHeader),
                            (Objects.equals(prospectCompanyDomainHeader, "empty")) ? null : csvRecord.get(prospectCompanyDomainHeader),
                            (Objects.equals(prospectLinkedinurlHeader, "empty")) ? null : csvRecord.get(prospectCompanyDomainHeader),
                            (Objects.equals(prospectTwitterurlHeader, "empty")) ? null : csvRecord.get(prospectTwitterurlHeader),
                            (Objects.equals(prospectLocationHeader, "empty")) ? null : csvRecord.get(prospectLocationHeader),
                            (Objects.equals(prospectCountryHeader, "empty")) ? null : csvRecord.get(prospectCountryHeader)
                            );
                    prospects.add(prospect);
                }
            }
            prospectService.addMultipleProspect(prospects, listId, userId);
            return prospects;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
