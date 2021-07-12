<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>菜单管理</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <script src="${ctxStatic}/common/Utils.js" type="text/javascript"></script>
    <script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/modules/sys/tpl/menuTreeTable.html" %>
    <script type="text/javascript">

        function updateSort() {
            loading('正在提交，请稍等...');
            $("#listForm").attr("action", "${ctx}/sys/menu/updateSort");
            $("#listForm").submit();
        }
    </script>
    <script type="text/javascript">
        $(document).ready(function () {
            $.ajax(
                {
                    url : "${ctx}/sys/menu/treeList",
                    type : "GET",
                    data : null,
                    contentType : "application/json",
                    success : function(data)
                    {
                        if(!data || data.menus.length ==0){
                            top.$.jBox.error("装载菜单错误","错误提示");
                            return;
                        }
                        var tmpl = document.getElementById('tpl-menu-tree').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var html = doTtmpl(data);
                        $("#treeTable > tbody").append(html);
                        $("#treeTable").treeTable({expandLevel: 3}).show();
                        $("#divLoading").remove();
                        return;
                    },
                    error : function()
                    {
                        top.$.jBox.error("装载菜单错误","错误提示");
                    }
                });
        });

    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="${ctx}/sys/menu/">菜单列表</a></li>
    <shiro:hasPermission name="sys:menu:edit">
        <li><a href="${ctx}/sys/menu/form">菜单添加</a></li>
    </shiro:hasPermission>
</ul>
<sys:message content="${message}"/>
<form id="listForm" method="post">
    <table id="treeTable" class="table table-striped table-bordered table-condensed hide table-hover">
        <thead>
        <tr>
            <th>名称</th>
            <th>链接</th>
            <th style="text-align:center;">排序</th>
            <th>可见</th>
            <th>权限标识</th>
            <shiro:hasPermission name="sys:menu:edit">
                <th>操作</th>
            </shiro:hasPermission></tr>
        </thead>
        <tbody></tbody>
    </table>
    <div id="divLoading">正在加载中...</div>
    <shiro:hasPermission name="sys:menu:edit">
        <div class="form-actions pagination-left">
            <input id="btnSubmit" class="btn btn-primary" type="button" value="保存排序" onclick="updateSort();"/>
        </div>
    </shiro:hasPermission>
</form>
</body>
</html>