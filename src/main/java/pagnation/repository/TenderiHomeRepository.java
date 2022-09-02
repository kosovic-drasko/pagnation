package pagnation.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import pagnation.domain.TenderiHome;

/**
 * Spring Data SQL repository for the TenderiHome entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TenderiHomeRepository extends JpaRepository<TenderiHome, Long> {}
