<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户产品</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>

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

        function editCustomerProduct(type,id) {
            var text = "安装规范-添加安装规范";
            var url = "${ctx}/customer/md/customerProduct/form";
            if(type == 2){
                text = "安装规范-修改"
                url = "${ctx}/customer/md/customerProduct/form?id=" + id;
            }
            top.layer.open({
                type: 2,
                id:"custoemrProduct",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['840px', '350px'],
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
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
            height: 30px;
        }
    </style>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">列表</a></li>
</ul>
<c:set var="canDdit" value="0"/>
<shiro:hasPermission name="customer:md:customerproduct:edit">
    <c:set var="canDdit" value="1"/>
</shiro:hasPermission>
<c:set var="currentuser" value="${fns:getUser()}"/>
<sys:message content="${message}"/>
<form:form id="searchForm" modelAttribute="customerProduct" action="${ctx}/customer/md/customerProduct/findList" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <label>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
    <c:choose>
        <c:when test="${currentuser.isCustomer()==true}">
            <li>
                <form:hidden path="customer.id"/>
                <form:input path="customer.name" readonly="true" style="width:186px;"/>
            </li>
        </c:when>
        <c:otherwise>
            <li>
                <form:select path="customer.id" class="input-large" style="width:200px;">
                    <form:option value="" label="请选择"/>
                    <form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id" htmlEscape="false" />
                </form:select>
            </li>
        </c:otherwise>
    </c:choose>
<%--    <form:select path="customer.id" cssStyle="width: 250px;">--%>
<%--        <form:option value="" label="请选择"></form:option>--%>
<%--        <form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id"></form:options>--%>
<%--    </form:select>--%>
    &nbsp;
    <label>产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
    <form:select path="product.id" cssClass="input-small" cssStyle="width:200px;">
        <form:option value="" label="所有"/>
        <form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
    </form:select>
    &nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
</form:form>
<shiro:hasPermission name="customer:md:customerproduct:edit">
    <button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 120px;height: 30px" onclick="editCustomerProduct(1,null)">
        <i class="icon-plus-sign"></i>&nbsp;添加安装规范
    </button>
</shiro:hasPermission>

<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="50">序号</th>
        <th width="350">客户名称</th>
        <th width="250">产品名称</th>
        <th width="500">安装规范</th>
        <th>视频链接</th>
    </tr>
    </thead>
    <tbody>
    <c:set var="index" value="0"></c:set>
    <c:forEach items="${page.list}" var="entity">
    <c>
            <c:set var="index" value="${index+1}"></c:set>
        <td>${index+(page.pageNo-1)*page.pageSize}</td>
        <td>${entity.customerDto.name}</td>
        <c:choose>
        <c:when test="${canDdit==1}">
        <td><a href="javascript:editCustomerProduct(2,'${entity.id}')">${entity.productDto.name}</a></td>
        </c:when>
        <c:otherwise>
        <td>${entity.productDto.name}</td>
        </c:otherwise>
        </c:choose>
        <td><a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${entity.fixSpec}">${fns:abbr(entity.fixSpec,250)}</a></td>
        <td><a href="${entity.videoUrl}" target="_blank" data-toggle="tooltip" data-tooltip="${entity.videoUrl}">${fns:abbr(entity.videoUrl,250)}</a></td>
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
    });
</script>
</body>
</html>

