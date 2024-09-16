package com.example.EmployeeManager.controller;


import com.example.EmployeeManager.dto.CommentDto;
import com.example.EmployeeManager.model.Comment;
import com.example.EmployeeManager.service.CommentService;
import com.example.EmployeeManager.util.entityAndDtoMappers.CommentMapper;
import jakarta.validation.Valid;
import jdk.jfr.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Validated
    @GetMapping("/{commentUuid}")
    public ResponseEntity<CommentDto> getCommentByUuid(@PathVariable UUID commentUuid ){
        /*
        * Expose endpoint "/api/task/{taskUuid}/comments/{commentUuid}"
            * It listens to Http requests using GET method.
        * You can read any comment using it's uuid.
        *
        * */
        return new ResponseEntity<>(
                CommentMapper
                        .CommentToCommentDto(
                                commentService.findByUuid(commentUuid)
                        ),
                HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Set<CommentDto>> getAllTasksComments(@PathVariable UUID taskUuid){
        /*
        * Expose endpoint "/api/task/{taskUuid}/comments"
            * It listens to Http requests using GET method.
        * It returns a list of all comments on a certain task.
        *
        * */

        return new ResponseEntity<>(
                                commentService
                                        .findAllByTaskUuid(taskUuid)
                                        .stream()
                                        .map(CommentMapper::CommentToCommentDto)
                                        .collect(Collectors.toSet()
                                        ),
                HttpStatus.OK);
    }

    @PutMapping("/{commentUuid}")
    public ResponseEntity<CommentDto> updateMyCommentByUuid(@PathVariable UUID commentUuid,
                                                            @RequestBody @Valid CommentDto commentDto ){
        /*
        * Expose endpoint "/api/task/{taskUuid}/comments/{commentUuid}"
        * Handles HTTP PUT requests to update a comment.
        * In case of successful update:
        *   - Returns the updated comment body and uuid.
        *   - Response status code 201 CREATED.
        *
        * In case of failed update due to:
        *   - Empty / null comment body
        *       - Response status code 400 BAD_REQUEST.
        *   - There is no comment with the specified uuid or the authenticated account is not the comment creator
        *       - Response status code 404 NOT FOUND.
        * */
        return new ResponseEntity<>(
                CommentMapper.CommentToCommentDto(
                commentService
                        .updateMyCommentByUuid(
                                commentUuid,
                                CommentMapper.CommentDtoToComment(commentDto)
                        )
                ),
                HttpStatus.CREATED
        );

    }
    @DeleteMapping("/{commentUuid}")
    public ResponseEntity deleteMyCommentByUuid(@PathVariable UUID commentUuid){
        /*
         * Expose endpoint "/api/task/{taskUuid}/comments/{commentUuid}"
         * Handles HTTP DELETE requests to delete a comment.
         * In case of successful deletion:
         *    - Response status code 201 CREATED.
         *
         * In case of failed deletion due to:
         *    - Deleting a comment with uuid that doesn't exist or comment that you didn't create.
         *          - Response status code 404 NOT_FOUND.
         *
         *    - Deleting without access token.
         *          - Response status code 401 UNAUTHORIZED.
         * */
        commentService.deleteMyCommentByUuid(commentUuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
