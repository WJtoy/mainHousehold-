<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>配件分类</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>

    <script type="text/javascript">
        //覆盖分页前方法
        function beforePage() {
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            $("#btnClearSearch").attr('disabled', 'disabled');
            layerLoading("查询中...", true);
        }

        var clicktag = 0;
        $(document).on("click", "#btnSubmit", function () {
            if (clicktag == 0) {
                clicktag = 1;
                beforePage();
                setPage();
                this.form.submit();
            }
        });

        function editMaterialCategory(type,id) {
            var text = "添加配件分类";
            var url = "${ctx}/md/materialCategory/newForm";
            if(type == 2){
                text = "修改配件分类"
                url = "${ctx}/md/materialCategory/newForm?id=" + id;
            }
            //var url = "${ctx}/md/materialCategory/newForm?parentIndex=" + (parent_index || '');
            top.layer.open({
                type: 2,
                id:"materialCategory",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['700px', '340px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }
    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li><a href="${ctx}/md/material/list">&nbsp;&nbsp;&nbsp;&nbsp;配件&nbsp;&nbsp;&nbsp;&nbsp;</a></li>
    <li class="active"><a href="javascript:void(0);">配件分类</a></li>
    <li><a href="${ctx}/md/material/requirement">照片要求</a></li>
</ul>
<form:form id="searchForm" modelAttribute="materialCategory" action="${ctx}/md/materialCategory/list" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <label>分类名称:</label>
    <form:select path="name" cssStyle="width: 200px;">
        <form:option value="" label="所有"></form:option>
        <form:options items="${materialCategoryList}" itemLabel="name" itemValue="name"></form:options>
    </form:select>
    &nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
</form:form>
<shiro:hasPermission name="md:material:edit">
    <button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 130px;height: 30px" onclick="editMaterialCategory(1,null)"><i class="icon-plus-sign"></i>&nbsp;添加配件分类</button>
</shiro:hasPermission>
<sys:message content="${message}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="50">序号</th>
        <th width="200">分类名称</th>
        <th>描述</th>
        <shiro:hasPermission name="md:materialcategory:edit"><th width="100">操作</th></shiro:hasPermission>
    </tr>
    </thead>
    <tbody>
    <c:set var="index" value="0"></c:set>
    <c:forEach items="${page.list}" var="entity">
        <tr>
            <c:set var="index" value="${index+1}"></c:set>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>
                ${entity.name}
            </td>
            <td>
                <c:choose>
                    <c:when test="${fn:length(entity.remarks)>40}">
                        <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${entity.remarks}">${fns:abbr(entity.remarks,80)}</a>
                    </c:when>
                    <c:otherwise>
                        ${entity.remarks}
                    </c:otherwise>
                </c:choose>
            </td>
            <shiro:hasPermission name="md:materialcategory:edit"><td>
                <a href="javascript:void(0)" onclick="editMaterialCategory(2, ${entity.id})">修改</a>
                <a href="${ctx}/md/materialCategory/delete?id=${entity.id}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
            </td></shiro:hasPermission>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
<script type="text/javascript" language="javascript">
    $(document).ready(function () {
        $('a[data-toggle=tooltip]').darkTooltip();
        $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
        $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});

        $("th").css({"text-align":"center","vertical-align":"middle"});
        $("td").css({"text-align":"center","vertical-align":"middle"});
        $("td").css({"vertical-align":"middle"});
    });
</script>
</body>
</html>
