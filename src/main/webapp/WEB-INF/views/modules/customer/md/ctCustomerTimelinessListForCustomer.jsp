<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户时效等级列表</title>
    <meta name="decorator" content="default"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
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

            $(document).ready(function() {
                $("#treeTable").treeTable();

                <c:if test="${listmap !=null && listmap.size() > 0}">
                var customerId=${listmap.get(0).customerId};
                //设置默认打开三角图标的样式
                $("#"+customerId).find('span').removeClass('default_shut');
                $("#"+customerId).find('span').addClass('default_open');
                $("."+customerId).css('display','table-row');
                <c:if test="${listmap.size()==1}">
                //当只有一行数据 设置默认打开三角图标的样式
                $("#"+customerId).find('span').removeClass('default_last_shut');
                $("#"+customerId).find('span').addClass('default_last_open');
                </c:if>
                </c:if>

            });

            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
        });

    </script>
    <style type="text/css">
        .col_product {width: 250px;}
        .col_command {width: 78px;}
        .table tbody td.error {background-color: #f2dede!important;}
    </style>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">时效费用</a>
    <li><a href="${ctx}/customer/md/urgentcustomer/customerList">加急费用</a>
</ul>

<form:form id="searchForm" modelAttribute="customerTimeliness" action="${ctx}/customer/md/customertimeliness/customerList" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    &nbsp;
    <input id="btnSubmit" class="btn btn-primary"  type="submit" onclick="return setPage();" value="查询"/>
</form:form>
<sys:message content="${message}"/>
<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="50">序号</th>
        <th width="100">客户</th>
        <th width="100">省份</th>
        <c:forEach items="${timelineList}" var="timelineLevel">
            <th width="100">${timelineLevel.remarks}</th>
        </c:forEach>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${listmap}" var="entity">
        <c:set var="index" value="${index+1}" />
        <tr id="${entity.customerId}" pId="0">
            <td>${index}</td>
            <td>${entity.customerName}</td>
            <td></td>
            <c:forEach items="${timelineList}" var="timelineLevel">
                <td></td>
            </c:forEach>
        </tr>
        <c:forEach items="${entity.areaTimelinessModelList}" var="item" varStatus="areaStatus">
            <tr id="area_${item.area.id}" pId="${entity.customerId}">
                <td></td>
                <td>(${areaStatus.index+1})</td>
                <td>${item.area.name}</td>
                <c:forEach items="${item.list}" var="chargeModel">
                    <td style="${chargeModel.chargeIn>0?'color: blue;':''}">${chargeModel.chargeIn}</td>
                </c:forEach>
            </tr>
        </c:forEach>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>
