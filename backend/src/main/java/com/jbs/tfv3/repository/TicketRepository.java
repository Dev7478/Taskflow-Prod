package com.jbs.tfv3.repository;

import com.jbs.tfv3.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
	List<Ticket> findByUserDtlsId(Long userId);
}
