package com.inn.cafe.dao;

import com.inn.cafe.POJO.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactMessageDao extends JpaRepository<ContactMessage, Integer> {

    List<ContactMessage> findAllByOrderByCreatedAtDesc();
}
