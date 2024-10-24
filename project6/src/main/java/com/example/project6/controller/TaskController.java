package com.example.project6.controller;


import com.example.project6.Service.AccountTasksService;
import com.example.project6.Service.AuthenticationService;
import com.example.project6.Service.TaskService;
import com.example.project6.dto.*;
import com.example.project6.entity.Task;
import com.example.project6.util.entityAndDtoMappers.TaskMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    final private TaskService taskService;
    final private AccountTasksService accountTasksService;
    final private AuthenticationService authenticationService;


    public TaskController(TaskService taskService, AccountTasksService accountTasksService, AuthenticationService authenticationService) {
        this.taskService = taskService;
        this.accountTasksService = accountTasksService;
        this.authenticationService = authenticationService;
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<TaskDto> createNewTask(@RequestBody @Valid CreateTaskRequest request){
        Task task = TaskMapper.createTaskRequestToTaskEntity(request);
        Task dbTask = taskService.createNewTask(task);
        return new ResponseEntity<>(TaskMapper.TaskEntityToTaskDto(dbTask) ,HttpStatus.CREATED);
    }

    @GetMapping("/{taskUuid}")
    public ResponseEntity<TaskDto> getTaskByUuid(@PathVariable UUID taskUuid){

        Task task = taskService.getTaskByUuid(taskUuid);

        // convert task to taskDto
        TaskDto taskDto = TaskMapper.TaskEntityToTaskDto(taskService.getTaskByUuid(taskUuid));

        return new ResponseEntity<>(taskDto, HttpStatus.OK);
    }

    @PutMapping("/{taskUuid}")
    public ResponseEntity updateTaskByUuid(@RequestBody UpdateTaskRequest request, @PathVariable UUID taskUuid){
        /*
        * Handles HTTP PUT requests to "/api/task/{taskUuid}".
        * It updates the all task attributes.
        * It returns HTTP STATUS CODE 200 OK in case of success.
        * */
        taskService.updateTaskByTaskUuid(TaskMapper.updateTaskRequestToTaskEntity(request), taskUuid);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PatchMapping("/{taskUuid}/title")
    public ResponseEntity updateTaskTitleByUuid(@RequestBody UpdateTaskTitleRequest request, @PathVariable UUID taskUuid){
        taskService.updateTaskTitleByUuid(request.title(), taskUuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/{taskUuid}/description")
    public ResponseEntity updateTaskDescriptionByUuid(@RequestBody UpdateTaskDescriptionRequest request,
                                                @PathVariable UUID taskUuid){
        /*
        * Handles HTTP PATCH requests to "/api/task/{taskUuid}"
        * Updates the task description.
        * Returns HTTP RESPONSE STATUS CODE 200 OK.
        * */
        taskService.updateTaskDescriptionByUuid(request.description(), taskUuid);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PatchMapping("/{taskUuid}/status")
    public ResponseEntity updateTaskStatusByUuid(@RequestBody UpdateTaskStatusRequest request,
                                                 @PathVariable UUID taskUuid){
        /*
        * Handles HTTP PATCH requests to "/api/task/{taskUuid}/status"
        * Updates the task status.
        * Returns HTTP response STATUS CODE 200 OK.
        * */

        taskService.updateTaskStatusByUuid(request.status(), taskUuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{taskUuid}")
    public ResponseEntity deleteByTaskUuid(@PathVariable UUID taskUuid){
        taskService.deleteTaskByUuid(taskUuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
