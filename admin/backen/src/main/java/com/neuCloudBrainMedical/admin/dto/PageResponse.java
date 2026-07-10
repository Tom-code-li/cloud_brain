package com.neuCloudBrainMedical.admin.dto;

import java.util.List;

/**
 * 通用分页响应。
 * 与前端 Element Plus 分页组件对齐。
 */
public class PageResponse<T> {

	private long total;
	private int pageNum;
	private int pageSize;
	private List<T> list;

	public PageResponse() {}

	public PageResponse(long total, int pageNum, int pageSize, List<T> list) {
		this.total = total;
		this.pageNum = pageNum;
		this.pageSize = pageSize;
		this.list = list;
	}

	public static <T> PageResponse<T> of(long total, int pageNum, int pageSize, List<T> list) {
		return new PageResponse<>(total, pageNum, pageSize, list);
	}

	public long getTotal() { return total; }
	public void setTotal(long total) { this.total = total; }

	public int getPageNum() { return pageNum; }
	public void setPageNum(int pageNum) { this.pageNum = pageNum; }

	public int getPageSize() { return pageSize; }
	public void setPageSize(int pageSize) { this.pageSize = pageSize; }

	public List<T> getList() { return list; }
	public void setList(List<T> list) { this.list = list; }
}