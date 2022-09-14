package com.hao.gulimall.member.dao;

import com.hao.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 12:29:41
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
