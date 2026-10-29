package com.quanle.single.dto.bo;

import lombok.Data;

/**
 * 新建问题的内部对象。提交人由 Service 写入 Model，不放在本对象里。
 *
 * @author quanle
 */
@Data
public class IssueCreateBO {

    private String title;

    private String content;

    private String bizNo;
}
