package com.inn.cafe.restImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.ContactMessage;
import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.dao.ContactMessageDao;
import com.inn.cafe.dto.Response;
import com.inn.cafe.exceptions.BadRequestException;
import com.inn.cafe.exceptions.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Pattern;

/** Landing-page contact form (public POST) and the admin inbox (GET). */
@RestController
@RequestMapping("/contact")
public class ContactRestImpl {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final ContactMessageDao dao;
    private final JwtFilter jwtFilter;

    public ContactRestImpl(ContactMessageDao dao, JwtFilter jwtFilter) {
        this.dao = dao;
        this.jwtFilter = jwtFilter;
    }

    @PostMapping
    public ResponseEntity<Response> submit(@RequestBody Map<String, String> body) {
        String name = trim(body.get("name"));
        String email = trim(body.get("email"));
        String message = trim(body.get("message"));
        if (name.isEmpty() || name.length() > 100) {
            throw new BadRequestException("Please tell us your name.");
        }
        if (!EMAIL.matcher(email).matches() || email.length() > 200) {
            throw new BadRequestException("That email address doesn't look right.");
        }
        if (message.isEmpty() || message.length() > 2000) {
            throw new BadRequestException("Your message should be between 1 and 2000 characters.");
        }
        ContactMessage saved = new ContactMessage();
        saved.setName(name);
        saved.setEmail(email);
        saved.setMessage(message);
        saved.setCreatedAt(LocalDateTime.now());
        dao.save(saved);
        return new ResponseEntity<>(new Response("Thanks, " + name + ". We'll reply within a day."), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Response> list() {
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORISED_ACCESS);
        }
        return new ResponseEntity<>(new Response(dao.findAllByOrderByCreatedAtDesc()), HttpStatus.OK);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
