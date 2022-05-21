package com.lemty.server.service;

import java.util.List;

import com.lemty.server.domain.AppUser;
import com.lemty.server.domain.EmailSignature;
import com.lemty.server.repo.EmailSignatureRepository;
import com.lemty.server.repo.UserRepo;

import org.springframework.stereotype.Service;

@Service
public class EmailSignatureService {
    private final EmailSignatureRepository emailSignatureRepository;
    private final UserRepo userRepo;

    public EmailSignatureService(EmailSignatureRepository emailSignatureRepository, UserRepo userRepo){
        this.emailSignatureRepository = emailSignatureRepository;
        this.userRepo = userRepo;
    }

    //List all signatures
    public List<EmailSignature> getSignatures(String userId){
        return emailSignatureRepository.findByAppUserId(userId);
    }

    // Add new signature
    public void addNewSignature(EmailSignature  newEmailSignature, String userId){
        AppUser appUser = userRepo.findById(userId).get();
        newEmailSignature.setAppUser(appUser);
        emailSignatureRepository.saveAndFlush(newEmailSignature);
    }

    //Select Signature
    public void updateSignature(EmailSignature newSignature, String signatureId){
        EmailSignature signature = emailSignatureRepository.findById(signatureId).get();
        signature.setSignature(newSignature.getSignature());
        emailSignatureRepository.save(signature);
    }

    //Delete signature
    public void deleteSignature(String signatureId){
        boolean exists = emailSignatureRepository.existsById(signatureId);
        if(!exists){
            throw new IllegalStateException(
                    "signature with id " + signatureId + " does not exists"
            );
        }
        emailSignatureRepository.deleteById(signatureId);
    }
}
