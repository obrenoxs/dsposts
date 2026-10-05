package com.devsuperior.workshopmongo.services;

import com.devsuperior.workshopmongo.models.dto.PostDTO;
import com.devsuperior.workshopmongo.models.entities.Post;
import com.devsuperior.workshopmongo.repositories.PostRepository;
import com.devsuperior.workshopmongo.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public PostDTO findById(String id) {
        Post post = getEntityById(id);
        return new PostDTO(post);
    }

    public List<PostDTO> findByTitle(String text) {
        List<Post> list = postRepository.findByTitleContainingIgnoreCase(text);
        return list.stream().map(x -> new PostDTO(x)).collect(Collectors.toList());
    }

    private Post getEntityById(String id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Objeto não encontrado"));
        return post;
    }
}
