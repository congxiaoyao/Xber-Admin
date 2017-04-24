package com.congxiaoyao.xber_admin.publishedtask.bean;

import com.congxiaoyao.httplib.response.Page;
import com.congxiaoyao.httplib.response.Pageable;

import java.util.Date;
import java.util.List;

/**
 * Created by congxiaoyao on 2017/4/3.
 */

public class TaskAndDriverListRsp implements Pageable<TaskRspAndDriver> {

    private Date timestamp;
    private List<TaskRspAndDriver> list;
    private Page page;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<TaskRspAndDriver> getList() {
        return list;
    }

    public void setList(List<TaskRspAndDriver> list) {
        this.list = list;
    }

    @Override
    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    @Override
    public List<TaskRspAndDriver> getCurrentPageData() {
        return getList();
    }

    @Override
    public Date getTimeStamp() {
        return timestamp;
    }
}
