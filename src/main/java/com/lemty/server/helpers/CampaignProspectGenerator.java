package com.lemty.server.helpers;

import com.lemty.server.repo.ProspectMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CampaignProspectGenerator {
    @Autowired
    private ProspectMetadataRepository prospectMetadataRepository;

}
