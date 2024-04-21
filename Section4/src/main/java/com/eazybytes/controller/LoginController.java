package com.eazybytes.controller;

import com.eazybytes.model.Customer;
import com.eazybytes.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
public class LoginController {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Customer customer) {
        Customer savedCustomer = null;
        ResponseEntity response = null;

        try {
            String hashPwd = passwordEncoder.encode(customer.getPwd()); // 유저가 입력한 비밀번호 해싱
            System.out.println(hashPwd);
            customer.setPwd(hashPwd);   // 해싱한 비밀번호로 세팅

            savedCustomer = customerRepository.save(customer);  // 사용자 저장 -> 만약 실패했다면 catch 블럭으로 이동
            if(savedCustomer.getId() > 0) {     // 저장된 유저 아이디가 0보다 크다면 == 저장됐다면
                response = ResponseEntity       // 응답 메세지 생성
                        .status(CREATED)
                        .body("Given user details are successfully registered");
            }
        } catch (Exception e) {
            response = ResponseEntity       // 회원 가입에 실패한 응답 메세지 생성
                    .status(INTERNAL_SERVER_ERROR)
                    .body("An exception occur due to " + e.getMessage());
        }

        return response;
    }
}
