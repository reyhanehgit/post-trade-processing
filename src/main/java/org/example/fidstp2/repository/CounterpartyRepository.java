package org.example.fidstp2.repository;
import org.example.fidstp2.persistence.CounterpartyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CounterpartyRepository extends JpaRepository<CounterpartyEntity, String> {
}
