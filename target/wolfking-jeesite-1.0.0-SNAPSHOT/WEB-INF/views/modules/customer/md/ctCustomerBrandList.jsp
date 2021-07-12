<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户品牌</title>
    <meta about="客户品牌(微服务md)" />
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>

    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
            height: 30px;
        }
    </style>
    <script type="text/javascript">
        //覆盖分页前方法
        function beforePage() {
            var val = $("#customerId").val();
            if (val == undefined || val.length == 0) {
                layerInfo("请选择客户!", "信息提示");
                return false;
            }
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            $("#btnClearSearch").attr('disabled', 'disabled');
            layerLoading("查询中...", true);
            return true;
        }
        var clicktag = 0;
        $(document).on("click", "#btnSubmit", function () {
            if (clicktag == 0) {
                clicktag = 1;
                var result = beforePage();
                if(!result){
                    clicktag = 0;
                    return false;
                }
                setPage();
                this.form.submit();
            }
        });

        function editCustomerBrand(type,id,customerName,brandName) {
            var text = "添加品牌";
            var url = "${ctx}/customer/md/customerBrand/form";
            var area = ['727px', '414px'];
            if(type == 20){
                text = "修改品牌";
                url = "${ctx}/customer/md/customerBrand/form?id= "+ id +"&customerName=${fns:urlEncode(customerName)}&brandName=${fns:urlEncode(brandName)}";
            }
            top.layer.open({
                type: 2,
                id:"customerBrand",
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
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">列表</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<form:form id="searchForm" modelAttribute="customerBrand" action="${ctx}/customer/md/customerBrand/getList" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden"
           value="${page.pageSize}" />

    <label style="margin-left: 0px"><span class="red">*</span>客&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp户：</label>
    <c:choose>
        <c:when test="${currentuser.isCustomer()==true}">
            <li>
                <form:hidden path="customerId"/>
                <form:input path="customerName" readonly="true" style="width:236px;"/>
            </li>
        </c:when>
        <c:otherwise>
            <li>
                <form:select path="customerId" class="input-large" style="width:250px;">
                    <form:option value="" label="所有"/>
                    <form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id" htmlEscape="false" />
                </form:select>
            </li>
        </c:otherwise>
    </c:choose>
    &nbsp;
    <label>品&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp牌:</label>
    <form:input path="brandName" htmlEscape="false" maxlength="50" class="input-small" style="width:186px"/>
    &nbsp;&nbsp;
    <input id="btnSubmit" class="btn btn-primary" type="submit"  value="查询" />
</form:form>
<shiro:hasPermission name="customer:md:customerbrand:edit">
    <button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 128px;height: 32px" onclick="editCustomerBrand()">
        <i class="icon-plus-sign"></i>&nbsp;添加品牌
    </button>
</shiro:hasPermission>
<sys:message content="${message}" />
<table id="contentTable"
       class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="30">序号</th>
        <th width="280">品牌</th>
        <th>备注</th>
        <th width="100">排序</th>
        <shiro:hasPermission name="customer:md:customerbrand:edit">
            <th width="200">操作</th>
        </shiro:hasPermission>
    </tr>
    </thead>
    <tbody>
        <c:set var="index" value="0"></c:set>
        <c:forEach items="${page.list}" var="entity">
            <tr>
                <c:set var="index" value="${index+1}"></c:set>
                <td>${index+(page.pageNo-1)*page.pageSize}</td>
                <td>${entity.brandName}</td>
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
                <td>${entity.sort}</td>
                <shiro:hasPermission name="customer:md:customerbrand:edit">
                    <td><a href="javascript:editCustomerBrand(20,'${entity.id}','${entity.customerName}','${entity.brandName}')">修改</a>
                        <a style="margin-left: 6px;" href="${ctx}/customer/md/customerBrand/delete?id=${entity.id}&customerId=${entity.customerId}" onclick="return confirmx('确认要删除吗？', this.href)">删除</a>
                    </td>
                </shiro:hasPermission>
            </tr>
        </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>
