package com.example.EmployeeManager.service;

import com.example.EmployeeManager.dao.CommentRepository;
import com.example.EmployeeManager.exception.NotFoundException;
import com.example.EmployeeManager.model.Account;
import com.example.EmployeeManager.model.Comment;
import com.example.EmployeeManager.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final AuthenticationService authenticationService;
    private final TaskService taskService;

    @Autowired
    public CommentService(CommentRepository commentRepository, AuthenticationService authenticationService, TaskService taskService) {
        this.commentRepository = commentRepository;
        this.authenticationService = authenticationService;
        this.taskService = taskService;
    }
    public List<Comment> findAll(){
        return commentRepository.findAll();
    }
    public Comment save(Comment comment){
        return commentRepository.save(comment);
    }
    public void deleteAll(){
        commentRepository.deleteAll();
    }

    public Comment addCommentToTaskByUuid(Comment comment, UUID taskUuid){
        // get the currently authenticated account.
        Account account = authenticationService.getAuthenticatedAccount();

        // set the currently authenticated account as the creator of the comment.
        comment.setCreatedBy(account);

        // fetch the task from the database using its UUID.
        Task task = taskService.findTaskByUuid(taskUuid);

        // add the comment to the comments of the task.
        task.getComments().add(comment);
        taskService.save(task);

        // save the comment to the database.
        return commentRepository.save(comment);
    }

    public Comment findByUuidAndAccountAndTask(UUID commentUuid, Account account, Task task) {
         return this.commentRepository
                 .findByUuidAndAccountAndTask(commentUuid, account, task)
                 .orElseThrow(()->new NotFoundException("couldn't find comment with uuid: " + commentUuid));
    }

    public Comment findByUuid(UUID uuid) {
        return commentRepository
                .findByUuid(uuid)
                .orElseThrow(
                        ()->new NotFoundException("could not find comment with uuid: " + uuid)
                );

    }
}