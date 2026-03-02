package com.digitalwallet.natwest.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NatWestTokenRepository extends JpaRepository<NatWestTokenEntity, String> {}
