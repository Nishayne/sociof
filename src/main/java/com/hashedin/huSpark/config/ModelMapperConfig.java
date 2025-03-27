package com.hashedin.huSpark.config;

import java.util.Set;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hashedin.huSpark.dto.PostDto;
import com.hashedin.huSpark.entity.Comment;
import com.hashedin.huSpark.entity.Like;
import com.hashedin.huSpark.entity.Post;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Convert PersistentSet<Comment> to Integer (Count)
        Converter<Set<Comment>, Integer> commentCountConverter = ctx -> 
            (ctx.getSource() == null) ? 0 : ctx.getSource().size();

        // Convert PersistentSet<Like> to Integer (Count)
        Converter<Set<Like>, Integer> likeCountConverter = ctx -> 
            (ctx.getSource() == null) ? 0 : ctx.getSource().size();

        // Apply Converters
        TypeMap<Post, PostDto> typeMap = modelMapper.createTypeMap(Post.class, PostDto.class);
        typeMap.addMappings(mapper -> {
            mapper.using(commentCountConverter).map(Post::getComments, PostDto::setComments);
            mapper.using(likeCountConverter).map(Post::getPostLikes, PostDto::setLikes);
        });

        return modelMapper;
    }
}