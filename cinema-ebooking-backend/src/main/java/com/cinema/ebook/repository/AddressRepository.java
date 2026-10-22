package com.cinema.ebook.repository;

import com.cinema.ebook.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

    Optional<Address> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
