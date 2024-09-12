package com.example.EmployeeManager.util.entityAndDtoMappers;

import com.example.EmployeeManager.dto.CommentDto;
import com.example.EmployeeManager.model.Comment;

public class CommentMapper {

  public static Comment CommentDtoToComment(CommentDto commentDto){
      /*
      * Converts CommentDto to comment entity.
      * */
      return Comment.builder().withBody(commentDto.body()).build();
  }

    public static CommentDto CommentToCommentDto(Comment comment){
        /*
         * Converts Comment entity to CommentDto.
         * */
        return new CommentDto(comment.getBody(), comment.getUuid());
    }

}
