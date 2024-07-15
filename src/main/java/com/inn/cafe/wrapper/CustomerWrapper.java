package com.inn.cafe.wrapper;


import com.inn.cafe.POJO.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerWrapper {

    private Integer id;

    private String name;

    private String email;

    private String contactNumber;

    public CustomerWrapper(User user) {
        this.id = user.getId();

        this.name = user.getName();

        this.email = user.getEmail();

        this.contactNumber = user.getContactNumber();

    }
}
