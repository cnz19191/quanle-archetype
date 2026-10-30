package com.isiniu.demo.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isiniu.demo.comment.model.CommentModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论表 Mapper。
 *
 * @author isiniu
 */
@Mapper
public interface CommentMapper extends BaseMapper<CommentModel> {}
