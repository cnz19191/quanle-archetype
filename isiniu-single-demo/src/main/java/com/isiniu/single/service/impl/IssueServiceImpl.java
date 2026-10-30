package com.isiniu.single.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isiniu.common.api.PageResult;
import com.isiniu.common.enums.CommonErrorCode;
import com.isiniu.common.exception.CustomException;
import com.isiniu.common.util.DataParseUtil;
import com.isiniu.common.web.LoginUserHolder;
import com.isiniu.single.convert.IssueConvert;
import com.isiniu.single.dto.bo.IssueBO;
import com.isiniu.single.dto.bo.IssueCreateBO;
import com.isiniu.single.dto.bo.IssuePageQueryBO;
import com.isiniu.single.dto.request.IssueCreateRequest;
import com.isiniu.single.dto.request.IssuePageRequest;
import com.isiniu.single.dto.request.IssueStatusChangeRequest;
import com.isiniu.single.dto.response.IssueResponse;
import com.isiniu.single.enums.IssueStatusEnum;
import com.isiniu.single.exception.ErrorCodeEnum;
import com.isiniu.single.model.IssueModel;
import com.isiniu.single.repository.IssueRepository;
import com.isiniu.single.service.IssueService;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 问题领域服务实现。
 *
 * @author isiniu
 */
@Slf4j
@Service
public class IssueServiceImpl implements IssueService {

    @Resource
    private IssueRepository issueRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IssueResponse createIssue(IssueCreateRequest request) {
        Long reporterId = currentUserId();
        log.info("[创建问题] bizNo={}, reporterId={}", request.getBizNo(), reporterId);
        IssueModel existing = issueRepository.getByBizNo(request.getBizNo());
        if (existing != null) {
            return reuseOrReject(request, existing, reporterId);
        }
        IssueCreateBO bo = IssueConvert.INSTANCE.toCreateBO(request);
        IssueModel model = IssueConvert.INSTANCE.toModel(bo);
        model.setStatus(IssueStatusEnum.PENDING.getValue());
        model.setReporterId(reporterId);
        try {
            issueRepository.save(model);
        } catch (DuplicateKeyException ex) {
            log.warn("[创建问题唯一键冲突] bizNo={}, reporterId={}", request.getBizNo(), reporterId, ex);
            throw new CustomException(ErrorCodeEnum.ISSUE_DUPLICATE, "bizNo=" + request.getBizNo(), ex);
        }
        log.info("[创建问题成功] bizNo={}, issueId={}", request.getBizNo(), model.getId());
        return IssueConvert.INSTANCE.toResponse(model);
    }

    @Override
    public PageResult<IssueResponse> pageIssue(IssuePageRequest request) {
        Long reporterId = currentUserId();
        IssuePageQueryBO query = IssueConvert.INSTANCE.toPageQuery(request);
        query.setReporterId(reporterId);
        if (StringUtils.hasText(request.getStatus())) {
            IssueStatusEnum status = IssueStatusEnum.getByCode(request.getStatus());
            if (status == null) {
                throw new CustomException(ErrorCodeEnum.ISSUE_STATUS_ILLEGAL, "status=" + request.getStatus());
            }
            query.setStatus(status.getValue());
        }
        Page<IssueBO> page = issueRepository.pageIssue(query);
        List<IssueResponse> list = CollectionUtils.isEmpty(page.getRecords())
                ? List.of()
                : page.getRecords().stream()
                        .map(IssueConvert.INSTANCE::toResponse)
                        .toList();
        return PageResult.of(page.getTotal(), (int) page.getCurrent(), (int) page.getSize(), list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IssueResponse changeStatus(Long id, IssueStatusChangeRequest request) {
        Long reporterId = currentUserId();
        IssueModel issue = issueRepository.getById(id);
        if (issue == null) {
            log.warn("[问题状态变更失败] issueId={}, reason=notFound", id);
            throw new CustomException(ErrorCodeEnum.ISSUE_NOT_FOUND, "id=" + id);
        }
        if (!reporterId.equals(issue.getReporterId())) {
            log.warn("[问题越权访问] issueId={}, reporterId={}, operatorId={}", id, issue.getReporterId(), reporterId);
            throw new CustomException(ErrorCodeEnum.ISSUE_NOT_FOUND, "id=" + id);
        }
        IssueStatusEnum current = IssueStatusEnum.getByValue(issue.getStatus());
        IssueStatusEnum target = IssueStatusEnum.getByCode(request.getTargetStatus());
        if (current == null || !current.canTransitTo(target)) {
            log.warn(
                    "[问题状态变更非法] issueId={}, fromStatus={}, toStatus={}",
                    id,
                    current == null ? null : current.getCode(),
                    request.getTargetStatus());
            throw new CustomException(ErrorCodeEnum.ISSUE_STATUS_ILLEGAL, "id=" + id);
        }
        int rows = issueRepository.updateStatus(id, current.getValue(), target.getValue(), reporterId);
        if (rows == 0) {
            log.warn("[问题状态变更冲突] issueId={}, fromStatus={}, toStatus={}", id, current.getCode(), target.getCode());
            throw new CustomException(ErrorCodeEnum.ISSUE_STATUS_CONFLICT, "id=" + id);
        }
        log.info("[问题状态变更] issueId={}, fromStatus={}, toStatus={}", id, current.getCode(), target.getCode());
        issue.setStatus(target.getValue());
        return IssueConvert.INSTANCE.toResponse(issue);
    }

    @Override
    public IssueResponse getIssue(Long id) {
        Long reporterId = currentUserId();
        IssueModel issue = issueRepository.getById(id);
        if (issue == null || !reporterId.equals(issue.getReporterId())) {
            log.warn("[问题查询失败] issueId={}, operatorId={}", id, reporterId);
            throw new CustomException(ErrorCodeEnum.ISSUE_NOT_FOUND, "id=" + id);
        }
        return IssueConvert.INSTANCE.toResponse(issue);
    }

    private IssueResponse reuseOrReject(IssueCreateRequest request, IssueModel existing, Long reporterId) {
        String title = DataParseUtil.trimToNull(request.getTitle());
        if (!reporterId.equals(existing.getReporterId()) || !existing.getTitle().equals(title)) {
            log.warn(
                    "[创建问题幂等冲突] bizNo={}, issueId={}, operatorId={}", request.getBizNo(), existing.getId(), reporterId);
            throw new CustomException(ErrorCodeEnum.ISSUE_DUPLICATE, "bizNo=" + request.getBizNo());
        }
        log.info("[创建问题幂等命中] bizNo={}, issueId={}", request.getBizNo(), existing.getId());
        return IssueConvert.INSTANCE.toResponse(existing);
    }

    private Long currentUserId() {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            log.warn("[缺少登录用户]");
            throw new CustomException(CommonErrorCode.UNAUTHORIZED, "未登录");
        }
        return userId;
    }
}
