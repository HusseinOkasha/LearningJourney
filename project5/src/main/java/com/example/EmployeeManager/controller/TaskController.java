package com.example.EmployeeManager.controller;


import com.example.EmployeeManager.dto.TaskDto;
import com.example.EmployeeManager.dto.UpdateDescriptionRequest;
import com.example.EmployeeManager.dto.UpdateTaskStatusRequest;
import com.example.EmployeeManager.dto.UpdateTitleRequest;
import com.example.EmployeeManager.model.Task;
import com.example.EmployeeManager.service.AccountTasksService;
import com.example.EmployeeManager.service.TaskService;
import com.example.EmployeeManager.util.entityAndDtoMappers.TaskMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final AccountTasksService accountTasksService;
    private final TaskService taskService;

    @Autowired
    public TaskController(AccountTasksService accountTasksService, TaskService taskService) {
        this.accountTasksService= accountTasksService;
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@RequestBody @Valid TaskDto taskDto){

        // create task entity from the task dto.
        Task task = TaskMapper.taskDtoToTaskEntity(taskDto);

        // add the task to the authenticated users' account.
        Task createdTask = accountTasksService.addTaskToMyAccount(task);

        // convert the task entity to taskDto and return it in the response body.
        return new ResponseEntity<>(TaskMapper.taskEntityToTaskDto(createdTask), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Set<TaskDto>> getMyTasks(){
        return new ResponseEntity<>(
                accountTasksService
                        .getMyTasks()
                        .stream()
                        .map(TaskMapper::taskEntityToTaskDto).collect(Collectors.toSet()),
                HttpStatus.OK);
    }
    @GetMapping("/{uuid}")
    public ResponseEntity<TaskDto> getTaskByUuid(@PathVariable UUID uuid){
        return  new ResponseEntity<>(TaskMapper.taskEntityToTaskDto(taskService.findTaskByUuid(uuid)), HttpStatus.OK);
    }

    @PatchMapping("/{uuid}/title")
    public ResponseEntity<TaskDto> UpdateTaskTitleByUuid(@PathVariable UUID uuid, @RequestBody @Valid  UpdateTitleRequest request){
        return new ResponseEntity<>(
                TaskMapper.taskEntityToTaskDto(accountTasksService.updateMyTaskTitleByUuid(uuid, request.title())),
                HttpStatus.OK);
    }

    @PatchMapping("/{uuid}/description")
    public ResponseEntity<TaskDto> UpdateTaskDescriptionByUuid(@PathVariable UUID uuid,
                                                               @RequestBody @Valid UpdateDescriptionRequest request){
        // listens to requests on /api/task/uuid/description using HTTP patch method.
        // it updates tasks' description using the tasks' UUID passed as a path variable.
        return new ResponseEntity<>(
                TaskMapper.taskEntityToTaskDto(accountTasksService.updateMyTaskDescriptionByUuid(uuid, request.description())),
                HttpStatus.OK);
    }

    @PatchMapping("/{uuid}/status")
    public ResponseEntity<TaskDto> UpdateTaskStatusByUuid(@PathVariable UUID uuid,
                                                               @RequestBody @Valid UpdateTaskStatusRequest request){
        // listens to requests on /api/task/uuid/status using HTTP patch method.
        // it updates tasks' status using the tasks' UUID passed as a path variable.
        // it returns a response its body contains the update task along with a status code 200 (OK).
        return new ResponseEntity<>(
                TaskMapper.taskEntityToTaskDto(accountTasksService.updateMyTaskStatusByUuid(uuid, request.status())),
                HttpStatus.OK);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<TaskDto> UpdateTaskByUuid(@PathVariable UUID uuid,
                                                               @RequestBody @Valid TaskDto taskDto){
        // listens to requests on /api/task/uuid/using HTTP PUT method.
        // it updates tasks' status using the tasks' UUID passed as a path variable.
        // it returns a response its body contains the update task along with a status code 200 (OK).

        Task task = TaskMapper.taskDtoToTaskEntity(taskDto);
        return new ResponseEntity<>(
                TaskMapper.taskEntityToTaskDto(accountTasksService.updateMyTaskByUuid(uuid, task)),
                HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity deleteTaskByUuid(@PathVariable UUID uuid){
        /*
        * Handles HTTP DELETE Requests to "/api/task/uuid"
        * In case of a successful deletion
        *   - returns response with status code 200 OK.
        *
        * In case of a failed deletion due to
        *   - you aren't the task creator (there is no  entry in the account_tasks containing (account_id, task_id) ).
        *   - returns response with status code 404 NOT_FOUND
        *  - Deleting without access token.
        *       - Response status code 401 UNAUTHORIZED.
        * */

        accountTasksService.deleteTaskByUuid(uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
