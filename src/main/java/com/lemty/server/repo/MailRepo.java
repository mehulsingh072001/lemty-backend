package com.lemty.server.repo;

        import java.util.List;

        import com.lemty.server.domain.Mail;
        import com.lemty.server.domain.Step;

        import org.springframework.data.jpa.repository.JpaRepository;
        import org.springframework.stereotype.Repository;

@Repository
public interface MailRepo extends JpaRepository<Mail, String> {
    Mail findByStepId(String stepId);
}
