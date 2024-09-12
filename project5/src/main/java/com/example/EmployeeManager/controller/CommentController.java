package com.example.EmployeeManager.controller;


import com.example.EmployeeManager.dto.CommentDto;
import com.example.EmployeeManager.model.Comment;
import com.example.EmployeeManager.service.CommentService;
import com.example.EmployeeManager.util.entityAndDtoMappers.CommentMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/task/{taskUuid}/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Validated
    @PostMapping
    public ResponseEntity<CommentDto> addComment(@PathVariable UUID taskUuid, @RequestBody @Valid CommentDto commentDto ){
        // convert commentDto to comment entity.
        Comment comment = this.commentService.addCommentToTaskByUuid(
                        CommentMapper.CommentDtoToComment(commentDto),
                        taskUuid
                );
        return new ResponseEntity<>(CommentMapper.CommentToCommentDto(comment), HttpStatus.CREATED);
    }
}
