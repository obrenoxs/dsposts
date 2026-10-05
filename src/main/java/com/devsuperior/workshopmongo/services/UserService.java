package com.devsuperior.workshopmongo.services;

import com.devsuperior.workshopmongo.models.dto.UserDTO;
import com.devsuperior.workshopmongo.models.entities.User;
import com.devsuperior.workshopmongo.repositories.UserRepository;
import com.devsuperior.workshopmongo.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserDTO> findAll() {
        List<User> list = userRepository.findAll();
        return list.stream().map(x -> new UserDTO(x)).collect(Collectors.toList());
    }

    public UserDTO findById(String id) {
        User user = getEntityById(id);
        return new UserDTO(user);
    }

    public UserDTO insert(UserDTO userDTO) {
        User user = new User();
        copyDtoToEntity(userDTO, user);
        user = userRepository.insert(user);
        return new UserDTO(user);
    }

    public UserDTO update(String id, UserDTO userDTO) {
        User user = getEntityById(id);
        copyDtoToEntity(userDTO, user);
        user = userRepository.save(user);
        return new UserDTO(user);
    }
    private void copyDtoToEntity(UserDTO userDTO, User user) {
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
    }

    private User getEntityById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Objeto não encontrado"));
        return user;
    }
}
