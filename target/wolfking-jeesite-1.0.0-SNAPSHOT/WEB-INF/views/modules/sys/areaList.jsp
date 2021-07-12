<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>区域管理</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript">
        $(document).on("click", "#btnClearCache", function() {
            var lastTime = $("#lastTime").val();
            if(lastTime != '' && new Date().getTime() - parseInt(lastTime) <3600 * 5 ){
                top.$.jBox.closeTip();
                top.$.jBox.error("缓存在5分钟之内已清除过,请稍后再操作!");
                return;
            }
            var submit = function(v, h, f) {
                if (v == 'ok') {
                    $("#lastTime").val(new Date().getTime());//保存清除时间
                    top.$.jBox.tip('正在清除缓存...', 'loading');

                    $(this).attr("disabled", "disabled");
                    $.ajax({
                        cache : false,
                        type : "POST",
                        url : "${ctx}/sys/area/clearcache",
                        data : null,
                        success : function(data) {
                            if (data.success) {
                                top.$.jBox.tip('区域缓存已清除', 'success');
                                top.$.jBox.close();
                                $(this).removeAttr('disabled');
                            } else {
                                top.$.jBox.closeTip();
                                top.$.jBox.error(data.message);
                            }
                            $(this).removeAttr('disabled');
                        },
                        error : function(xhr, ajaxOptions, thrownError) {
                            top.$.jBox.closeTip();
                            top.$.jBox.error(thrownError.toString());
                            $(this).removeAttr('disabled');
                        }
                    });
                } else if (v == 'cancel') {
                    // 取消
                }
                top.$.jBox.closeTip();
                $(this).removeAttr('disabled');
                return true; //close
            };
            top.$.jBox.confirm('清除缓存后，系统将重新读取，确定要清除区域缓存吗？', '确认', submit);
        });
        <%--function search1(){--%>
            <%--$("#pageNo").val(1);--%>
            <%--$("#searchForm").attr("action","${ctx}/sys/area");--%>
            <%--$("#searchForm").submit();--%>
            <%--return false;--%>
        <%--}--%>
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="${ctx}/sys/area/">区域列表</a></li>
    <shiro:hasPermission name="sys:area:edit">
        <li><a href="${ctx}/sys/area/form">区域添加</a></li>
    </shiro:hasPermission>
</ul>
    <input id="lastTime" name="lastTime" type="hidden" />
<form:form id="searchForm"  modelAttribute="area"   action="${ctx}/sys/area" method="post"  class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <sys:tableSort id="orderBy" name="orderBy" value="${page.orderBy}" callback="page();"/>
    <label>区域名称：</label>
    <input type="text" id="name" name="name" maxlength="50" value="${name}" class="input-small" style="width:80px;"/>
    <label>上级区域：</label>
    <input type="text" id="parentName" name="parentName" maxlength="50" value="${parentName}" class="input-small" style="width:80px;"/>
    <label>区域类型：</label>
    <form:select id="type" path="type" class="input-small">
        <form:option value="0" label="请选择" />
        <c:forEach items="${fns:getDictListFromMS('sys_area_type')}" var="dict"><!-- 切换为微服务 -->
            <option value="${dict.value}" <c:out value="${(type eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
        </c:forEach>
    </form:select>
    <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>
    <%--
    <input id="btnClearCache" class="btn btn-primary" type="button" value="清除缓存"/>
    --%>
</form:form>
<sys:message content="${message}"/>
<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
    <tr>
        <th>序号</th>
        <th>区域名称</th>
        <th>全称</th>
        <th>区域编码</th>
        <th>区域类型</th>
        <th>排序</th>
        <th>上级区域</th>
        <th>备注</th>
        <shiro:hasPermission name="sys:area:edit"><th>操作</th></shiro:hasPermission>
    </tr>
    <c:forEach items="${page.list}" var="area">
        <c:set var="index" value="${index+1}" />
            <tr id="${area.id}">
                <td>${index+(page.pageNo-1)*page.pageSize}</td>
                <td><a href="${ctx}/sys/area/form?id=${area.id}">${area.name}</a></td>
                <td>${area.fullName}</td>
                <td>${area.code}</td>
                <%--<td>${fns:getDictLabel(area.type, 'sys_area_type', '无')}</td>--%>
                <td>${area.typeName}</td>
                <td>${area.sort}</td>
                <td>${area.parent.name}</td>
                <td>${area.remarks}</td>
                <shiro:hasPermission name="sys:area:edit">
                    <td>
                        <a href="${ctx}/sys/area/form?id=${area.id}">修改</a>
                        <a href="${ctx}/sys/area/delete?id=${area.id}" onclick="return confirmx('要删除该区域及所有子区域项吗？', this.href)">删除</a>
                        <a href="${ctx}/sys/area/form?parent.id=${area.id}">添加下级区域</a>
                        <c:choose>
                            <c:when test="${area.statusFlag eq 0}">
                                <a href="${ctx}/sys/area/disable?id=${area.id}" onclick="return layerConfirmx('确认要停用该区域吗？', this.href)">停用</a>
                            </c:when>
                            <c:otherwise>
                                <a href="${ctx}/sys/area/enable?id=${area.id}" onclick="return layerConfirmx('确认要启用该区域吗？', this.href)">启用</a>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </shiro:hasPermission>
        </tr>
    </c:forEach>
</table>
<div class="pagination">${page}</div>
</body>
</html>
