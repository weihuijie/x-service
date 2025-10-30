package com.x.common.base;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 分页工具
 * @author whj
 */
@Data
@Accessors(chain = true)
public class Query {

	/**
	 * 当前页
	 */
	private Integer pageNo = 1;

	/**
	 * 每页的数量
	 */
	private Integer pageSize = 10;

	/**
	 * 正排序规则
	 */
	private String asc;

	/**
	 * 倒排序规则
	 */
	private String desc;

}
