package com.gen.rally.repository;

import com.gen.rally.entity.MatchInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MatchInvitationRepository extends JpaRepository<MatchInvitation, Long> {

    // 받은 요청
    @Query("""
select mi from MatchInvitation mi
  join fetch mi.sender s
  join fetch mi.receiver r
  left join fetch mi.senderRequest sr
  left join fetch mi.receiverRequest rr
where r.userId = :userId
""")
    List<MatchInvitation> findReceivedByUserId(String userId);

    // 보낸 요청
    @Query("""
select mi from MatchInvitation mi
  join fetch mi.sender s
  join fetch mi.receiver r
  left join fetch mi.senderRequest sr
  left join fetch mi.receiverRequest rr
where s.userId = :userId
""")
    List<MatchInvitation> findSentByUserId(String userId);
}
