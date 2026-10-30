package com.isiniu.demo.comment.convert;

import com.isiniu.demo.comment.dto.request.CommentCreateRequest;
import com.isiniu.demo.comment.dto.response.CommentResponse;
import com.isiniu.demo.comment.model.CommentModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 评论领域对象转换。
 *
 * @author isiniu
 */
@Mapper
public interface CommentConvert {

    CommentConvert INSTANCE = Mappers.getMapper(CommentConvert.class);

    /**
     * 新建请求转实体。归属字段由 Service 赋值。
     *
     * @param request 新建请求
     * @return 实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "issueId", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    CommentModel toModel(CommentCreateRequest request);

    /**
     * 实体转返回对象。
     *
     * @param model 实体
     * @return 返回对象
     */
    @Mapping(target = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    CommentResponse toResponse(CommentModel model);
}
