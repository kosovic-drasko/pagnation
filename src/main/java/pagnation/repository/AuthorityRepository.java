package pagnation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pagnation.domain.Authority;

/**
 * Spring Data JPA repository for the {@link Authority} entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {}
