package com.isiniu.demo.issue.convert;

import com.isiniu.common.util.DataParseUtil;
import com.isiniu.demo.issue.dto.bo.IssueBO;
import com.isiniu.demo.issue.dto.bo.IssueCreateBO;
import com.isiniu.demo.issue.dto.bo.IssuePageQueryBO;
import com.isiniu.demo.issue.dto.request.IssueCreateRequest;
import com.isiniu.demo.issue.dto.request.IssuePageRequest;
import com.isiniu.demo.issue.dto.response.IssueBriefResponse;
import com.isiniu.demo.issue.dto.response.IssueResponse;
import com.isiniu.demo.issue.enums.IssueStatusEnum;
import com.isiniu.demo.issue.model.IssueModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 问题对象转换。状态、提交人、审计字段由 Service 赋值，这里忽略。
 *
 * @author isiniu
 */
@Mapper(imports = {DataParseUtil.class, IssueStatusEnum.class})
public interface IssueConvert {

    IssueConvert INSTANCE = Mappers.getMapper(IssueConvert.class);

    @Mapping(target = "title", expression = "java(DataParseUtil.trimToNull(request.getTitle()))")
    @Mapping(target = "content", expression = "java(DataParseUtil.trimToNull(request.getContent()))")
    IssueCreateBO toCreateBO(IssueCreateRequest request);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reporterId", ignore = true)
    IssuePageQueryBO toPageQuery(IssuePageRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reporterId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    IssueModel toModel(IssueCreateBO bo);

    @Mapping(target = "id", expression = "java(DataParseUtil.toIdString(model.getId()))")
    @Mapping(target = "reporterId", expression = "java(DataParseUtil.toIdString(model.getReporterId()))")
    @Mapping(target = "status", expression = "java(IssueStatusEnum.codeOf(model.getStatus()))")
    @Mapping(target = "statusDesc", expression = "java(IssueStatusEnum.descOf(model.getStatus()))")
    @Mapping(target = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "updateTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    IssueResponse toResponse(IssueModel model);

    @Mapping(target = "id", expression = "java(DataParseUtil.toIdString(bo.getId()))")
    @Mapping(target = "reporterId", expression = "java(DataParseUtil.toIdString(bo.getReporterId()))")
    @Mapping(target = "status", expression = "java(IssueStatusEnum.codeOf(bo.getStatus()))")
    @Mapping(target = "statusDesc", expression = "java(IssueStatusEnum.descOf(bo.getStatus()))")
    @Mapping(target = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "updateTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    IssueResponse toResponse(IssueBO bo);

    IssueBriefResponse toBriefResponse(IssueResponse response);
}
