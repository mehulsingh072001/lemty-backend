package com.lemty.server.service;

        import com.lemty.server.domain.Mail;
        import com.lemty.server.domain.Step;
        import com.lemty.server.repo.MailRepo;
        import com.lemty.server.repo.StepRepository;

        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.stereotype.Service;

        import java.util.List;
        import java.util.Set;

@Service
public class MailService {
    private final StepRepository stepRepository;
    private final MailRepo mailRepo;

    @Autowired
    public MailService(StepRepository stepRepository, MailRepo mailRepo){
        this.stepRepository = stepRepository;
        this.mailRepo = mailRepo;
    }

    //List all Mails for steps
    public Mail getFromStep(String stepId){
        // Step step = stepRepository.findById(stepId).get();
        return mailRepo.findByStepId(stepId);
    }

    //Add New Mail
    public void addNewMail(List<Mail> mail, String stepId){
        Step step = stepRepository.findById(stepId).get();
        for(int i=0; i < mail.size(); i++){
            mail.get(i).setStep(step);
        }
        mailRepo.saveAll(mail);
    }
    //Delete Step
    public void deleteMail(String mailId){
        stepRepository.deleteById(mailId);
    }
}
