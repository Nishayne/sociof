package com.hashedin.huSpark.scheduler;

import com.hashedin.huSpark.entity.Post;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.repository.PostRepository;
import com.hashedin.huSpark.repository.UserRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for birthday post job
 */
@Configuration
public class BirthdayPostJobConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    /**
     * Job for creating birthday posts
     * @return Job
     */
    @Bean
    public Job birthdayPostJob() {
        return new JobBuilder("birthdayPostJob",jobRepository)
                .start(birthdayPostStep())
                .build();
    }

    /**
     * Step for creating birthday posts
     * @return Step
     */
    @Bean
    public Step birthdayPostStep() {
        return new StepBuilder("birthdayPostStep",jobRepository)
                .<User, Post>chunk(10)
                .reader(birthdayUserReader())
                .processor(birthdayPostProcessor())
                .writer(birthdayPostWriter())
                .build();
    }

    /**
     * Reader for users with birthdays today
     * @return ItemReader
     */
    @Bean
    public ItemReader<User> birthdayUserReader() {
        RepositoryItemReader<User> reader = new RepositoryItemReader<>();
        reader.setRepository(userRepository);
        reader.setMethodName("findUsersWithBirthdayToday");

        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("id", Sort.Direction.ASC);
        reader.setSort(sorts);

        return reader;
    }

    /**
     * Processor to create birthday posts
     * @return ItemProcessor
     */
    @Bean
    public ItemProcessor<User, Post> birthdayPostProcessor() {
        return user -> {
            Post post = Post.builder()
                    .content("Happy Birthday " + user.getEmail() + "! 🎂🎉")
                    .user(user)
                    .isShared(false)
                    .likes(0)
                    .build();
            return post;
        };
    }

    /**
     * Writer to save birthday posts
     * @return ItemWriter
     */
    @Bean
    public ItemWriter<Post> birthdayPostWriter() {
        return posts -> postRepository.saveAll(posts);
    }
}
