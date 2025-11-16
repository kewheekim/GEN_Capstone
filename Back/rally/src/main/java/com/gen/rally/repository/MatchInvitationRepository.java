package com.gen.rally.repository;

import com.gen.rally.entity.MatchInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // requestId로 관련 invitation 일괄 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
  delete from MatchInvitation mi
  where mi.senderRequest.requestId = :requestId
   or mi.receiverRequest.requestId = :requestId""")
    void deleteAllByRequestId(Long requestId);
}
