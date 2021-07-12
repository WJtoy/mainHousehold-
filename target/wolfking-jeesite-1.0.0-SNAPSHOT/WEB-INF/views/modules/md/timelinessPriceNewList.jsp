<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品分类时效奖励列表</title>
    <meta name="decorator" content="default"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script src="${ctxStatic}/js/fixtable.js" type="text/javascript"></script>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript">
        $(document).ready(function () {
            var w = $(window).width();

            var pagestyle = function() {
                var width = $(window).width() -0;
                $("#treeTable_tableLayout").css("width",width);
            }

            //注册窗体改变大小事件
            $(window).resize(pagestyle);
            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
        });
        function editTimeliness(type,id) {
            var text = "添加时效";
            var url = "${ctx}/md/timelinessPriceNew/forms";
            var area = ['640px', '440px'];
            if(type == 2){
                text = "修改时效";
                url = "${ctx}/md/timelinessPriceNew/forms?category.id=" + id;
                area = ['640px', '440px'];
            }
            top.layer.open({
                type: 2,
                id:"engineer",
                zIndex:19,
                title:text,
                content: url,
                area: area,
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }
    </script>
    <style type="text/css">
        .col_product {width: 250px;}
        .col_command {width: 78px;}
        .table tbody td.error {background-color: #f2dede!important;}
    </style>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">补贴金额</a></li>
    <li>
        <a href="${ctx}/md/areaTimelinessNew/list">区域设置</a>
    </li>
    <li><a href="${ctx}/md/servicepoint/findServicePointTimelinessList">网点设置</a></li>
    <li><a href="${ctx}/md/servicepoint/servicePointAreaTimelinessList">网点批量设置</a></li>
</ul>

<form:form id="searchForm" modelAttribute="TimeLinessPrices" action="${ctx}/md/timelinessPriceNew/list" method="post" class="breadcrumb form-search">
    <div>

        <label>产品品类：</label>
        <select id="category.id" name="category.id" class="input-small" style="width:198px;">
            <option value="0" <c:out value="${(empty timeLinessPrices.category.id)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${fns:getProductCategories()}" var="dict">
                <option value="${dict.id}" <c:out value="${(timeLinessPrices.category.id eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
            </c:forEach>
        </select>

        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary"  type="submit" value="查询"/>

    </div>

</form:form>

<button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 90px;height: 30px" onclick="editTimeliness(1,null)">
    <i class="icon-plus-sign"></i>&nbsp;添加时效
</button>
<sys:message content="${message}"/>
<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th rowspan="2" width="10">序号</th>
        <th rowspan="2" width="200">产品类别</th>
        <th colspan="4" width="600">时效奖励</th>
        <shiro:hasPermission name="md:timelinessprice:edit">
            <th rowspan="2" width="100">操作</th>
        </shiro:hasPermission>
    </tr>
    <tr>
        <c:forEach items="${levelList}" var="dict">
            <th width="100">${dict.description}</th>
        </c:forEach>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${listmap}" var="entity">
        <c:set var="index" value="${index+1}" />
        <tr>
            <td>${index}</td>
            <td>
                <shiro:hasPermission name="md:timelinessprice:edit">

                            ${entity.categoryName}

                </shiro:hasPermission>
                <shiro:lacksPermission name="md:timelinessprice:edit">
                    ${entity.categoryName}
                </shiro:lacksPermission>
            </td>
            <c:forEach items="${entity.timeLinessPriceList}" var="timelevelPrice">
                <td>${timelevelPrice.amount}</td>
            </c:forEach>
            <shiro:hasPermission name="md:timelinessprice:edit">
                <td>
                    <a href="javascript:editTimeliness(2,'${entity.categoryId}')">修改</a>
                    &nbsp;&nbsp;&nbsp;
                    <a href="${ctx}/md/timelinessPriceNew/delete?categoryId=${entity.categoryId}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
                </td>
            </shiro:hasPermission>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>
