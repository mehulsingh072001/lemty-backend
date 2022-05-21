package com.lemty.server.repo;
        import com.lemty.server.domain.Step;
        import org.springframework.data.jpa.repository.JpaRepository;
        import org.springframework.stereotype.Repository;

@Repository
public interface StepRepository extends JpaRepository<Step, String>{
}
