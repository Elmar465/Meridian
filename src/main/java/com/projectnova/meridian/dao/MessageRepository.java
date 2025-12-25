package com.projectnova.meridian.dao;

import com.projectnova.meridian.model.Message;
import com.projectnova.meridian.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends CrudRepository<Message,Long> {

     Page<Message> findBySenderAndReceiverOrReceiverAndSenderOrderByCreatedAtAsc
            (User sender1, User receiver1, User receiver2, User sender2, Pageable pageable);


     Page<Message> findBySenderOrReceiverOrderByCreatedAtDesc(User sender, User receiver, Pageable pageable);

     Long countByReceiverAndIsReadFalse(User receiver);

     Long countBySenderAndReceiverAndIsReadFalse(User sender, User receiver);


     @Modifying
     @Query("UPDATE Message  m SET  m.isRead = true where " +
             "m.sender.id = :senderId AND " +
             "m.receiver.id = :receiverId  AND " +
             "m.isRead = false")
     int markAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);


     @Query("SELECT DISTINCT m.receiver.id FROM Message m WHERE m.sender = :user")
     List<Long> findReceiverIdsBySender(@Param("user") User user);

     @Query("SELECT DISTINCT m.sender.id FROM Message m WHERE m.receiver = :user")
     List<Long> findSenderIdsByReceiver(@Param("user") User user);
}
