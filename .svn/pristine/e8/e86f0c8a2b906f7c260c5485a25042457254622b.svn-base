<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户加急等级列表</title>
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

            $("#treeTable").treeTable();

            <c:if test="${listmap !=null && listmap.size() > 0}">
                //设置默认打开三角图标的样式
                var customerId=${listmap.get(0).customerId};
                $("#"+customerId).find('span').removeClass('default_shut');
                $("#"+customerId).find('span').addClass('default_open');
                $("."+customerId).css('display','table-row');
                <c:if test="${listmap.size()==1}">
                //当只有一行数据 设置默认打开三角图标的样式
                $("#"+customerId).find('span').removeClass('default_last_shut');
                $("#"+customerId).find('span').addClass('default_last_open');
                </c:if>
            </c:if>

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
    <li class="active"><a href="javascript:void(0);">列表</a>
    <shiro:hasPermission name="md:urgentcustomer:edit">
        <li><a href="${ctx}/md/urgentcustomer/forms">添加</a>
        </li>
    </shiro:hasPermission>
</ul>

<form:form id="searchForm" modelAttribute="urgentCustomer" action="${ctx}/md/urgentcustomer/list" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <label>客户：</label>
    <select id="customerId" name="customer.id" class="input-small" style="width:250px;">
        <option value=""
                <c:out value="${(empty customerId)?'selected=selected':''}" />>所有</option>
        <c:forEach items="${fns:getMyCustomerList()}" var="dict">
            <option value="${dict.id}"
                    <c:out value="${(customerTimeliness.customer.id eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
        </c:forEach>
    </select>
    <input id="btnSubmit" class="btn btn-primary"  type="submit" onclick="return setPage();" value="查询"/>
</form:form>
<sys:message content="${message}"/>
<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="50" rowspan="2">序号</th>
        <th width="100" rowspan="2">客户</th>
        <th width="100" rowspan="2">省份</th>
        <c:forEach items="${urgentLevelList}" var="entity">
            <th width="200" colspan="2">${entity.remarks}</th>
        </c:forEach>
        <shiro:hasPermission name="md:urgentcustomer:edit">
        <th width="100" rowspan="2">操作</th>
        </shiro:hasPermission>
    </tr>
    <tr>
        <c:forEach items="${urgentLevelList}">
            <th width="100">收取</th>
            <th width="100">支付</th>
        </c:forEach>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${listmap}" var="entity">
        <c:set var="index" value="${index+1}" />
        <tr id="${entity.customerId}" pId="0">
            <td>${index}</td>
            <td>
                <shiro:hasPermission name="md:urgentcustomer:edit">
                <a href="${ctx}/md/urgentcustomer/forms?customer.id=${entity.customerId}&customer.name=${fns:urlEncode(entity.customerName)}">
                    ${entity.customerName}
                </a>
                </shiro:hasPermission>
                <shiro:lacksPermission name="md:urgentcustomer:edit">
                    ${entity.customerName}
                </shiro:lacksPermission>
            </td>
            <td></td>
            <c:forEach items="${urgentLevelList}">
                <td colspan="2"></td>
            </c:forEach>
            <shiro:hasPermission name="md:urgentcustomer:edit">
                <td>
                    <a href="${ctx}/md/urgentcustomer/forms?customer.id=${entity.customerId}&customer.name=${fns:urlEncode(entity.customerName)}">修改</a>
                    <a href="${ctx}/md/urgentcustomer/delete?customerId=${entity.customerId}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
                </td>
            </shiro:hasPermission>
        </tr>
        <c:forEach items="${entity.AreaUrgentModelList}" var="item" varStatus="areaStatus">
            <tr id="area_${item.area.id}" pId="${entity.customerId}">
                <td></td>
                <td>(${areaStatus.index+1})</td>
                <td>${item.area.name}</td>
                <c:forEach items="${item.list}" var="chargeModel">
                    <td style="${chargeModel.chargeIn>0?'color: blue;':''}">${chargeModel.chargeIn}</td>
                    <td style="${chargeModel.chargeOut>0?'color: blue;':''}">${chargeModel.chargeOut}</td>
                </c:forEach>
                <shiro:hasPermission name="md:urgentcustomer:edit">
                    <td>
                    </td>
                </shiro:hasPermission>
            </tr>
        </c:forEach>


    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>
