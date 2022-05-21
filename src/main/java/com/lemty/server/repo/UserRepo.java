package com.lemty.server.repo;
import com.lemty.server.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepo extends JpaRepository<AppUser, String> {
    AppUser findByUsername(String username);
//     @Modifying
//     @Query(value="update User u set u.deliveryStatus = :deliveryStatus where u.eventId = :eventId", nativeQuery = true)
//     void setUserInfoById(@Param("deliveryStatus")String deliveryStatus, @Param("userId")Integer eventId);
}
