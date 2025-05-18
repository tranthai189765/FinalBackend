package com.example.demo.service;

import com.example.demo.dto.ManualUserDTO;
import com.example.demo.dto.ResidentDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.Resident;
import com.example.demo.entity.User;
import com.example.demo.repository.ResidentRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private ApartmentService apartmentService;

    @Autowired
    private PasswordEncoder passwordEncoder;



    public void addUser(UserDTO request) {
        Resident resident = new Resident();
        resident.setFullName(request.getFullName());
        resident.setAge(request.getAge());
        resident.setPhone(request.getPhone());
        resident.setEmail(request.getEmail());
        resident.setApartmentNumbers(request.getApartmentNumbers());
        residentRepository.save(resident);

        User user = new User();
        user.setResidentId(resident.getId()); // Lấy ID sau khi lưu
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }
    public void addUser(ManualUserDTO request) {
        Resident resident = new Resident();
        resident.setFullName(request.getFullName());
        resident.setAge(request.getAge());
        resident.setPhone(request.getPhone());
        resident.setEmail(request.getEmail());
        resident.setApartmentNumbers(request.getApartmentNumbers());
        resident.setStatus(request.getStatus());
        residentRepository.save(resident);

        User user = new User();
        user.setResidentId(resident.getId()); // Lấy ID sau khi lưu
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        userRepository.save(user);

        apartmentService.updateResident(resident);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public boolean activateUser(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!user.isActivation()) {  // Chỉ kích hoạt nếu chưa kích hoạt
                user.setActivation(true);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    public boolean deactivateUser(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.isActivation()) {
                user.setActivation(false);
//                System.out.println(user);
                userRepository.save(user);
//                System.out.println(user);
                return true;
            }
        }
        return false;
    }

    public boolean updateUser(Long id, ResidentDTO user) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User userUpdate = userOptional.get();
            userUpdate.setRole(user.getRole());

            Resident resident = residentRepository.findById(userUpdate.getResidentId()).get();
            apartmentService.deleteResident(resident);
            resident.setApartmentNumbers(user.getApartmentNumbers());
            apartmentService.updateResident(resident);

            userRepository.save(userUpdate);
            residentRepository.save(resident);
            return true;
        }
        return false;
    }


    public boolean deleteUser(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Resident resident = residentRepository.findById(user.getResidentId()).orElse(null);
            if (resident == null) {
                return false;
            }
            apartmentService.deleteResident(resident);
            userRepository.delete(user);
            return true;
        }
        return false;
    }

    public String changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return "error_user_not_found";
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return "error_wrong_old_password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "success";
    }



    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    public List<User> findAllById(List<Long> ids) {
        return userRepository.findAllById(ids);
    }
    public User getUserByName(String username) {
        return userRepository.findByName(username);
    }

    public List<User> allUsers() {
        return userRepository.findAll();
    }

    public User findByName(String name) {
        return userRepository.findByName(name);
    }
}