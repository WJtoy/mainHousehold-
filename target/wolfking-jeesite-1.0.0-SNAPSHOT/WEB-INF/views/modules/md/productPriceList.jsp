<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>产品价格</title>
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
            FixTable("treeTable", 2, w, "100%");

            var pagestyle = function() {
                var width = $(window).width() -0;
                FixTable("treeTable", 2, width, "100%");
                $("#treeTable_tableLayout").css("width",width);
            }

            //注册窗体改变大小事件
            $(window).resize(pagestyle);
            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
        });

        function go2Delete(productPriceId, priceType){
            var submit = function (v, h, f) {
                if (v == 'ok') {
                    top.$.jBox.tip('正在停用参考价格...', 'loading');
                    window.location="${ctx}/md/productprice/delete?id="+productPriceId+"&priceType="+priceType;
                }
                return true; //close
            };
            top.$.jBox.confirm('确认要停用该参考价格吗？', '参考价格', submit);
        }
        function go2Active(productPriceId, priceType){
            var submit = function (v, h, f) {
                if (v == 'ok') {
                    top.$.jBox.tip('正在启用参考价格...', 'loading');
                    window.location="${ctx}/md/productprice/active?id="+productPriceId+"&priceType="+priceType;
                }
                return true; //close
            };
            top.$.jBox.confirm('确认要启用该参考价格吗？', '参考价格', submit);
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
    <c:set var="priceTypes" value="${fns:getDictListFromMS('PriceType')}" /><%--切换为微服务--%>
    <c:forEach items="${priceTypes}" var="dict">
        <c:choose>
            <c:when test="${dict.value eq priceType}">
                <li class="active"><a href="javascript:void(0);">${dict.label}参考价格</a>
            </c:when>
            <c:otherwise>
                <li><a href="${ctx}/md/productprice/list?type=${dict.value}">${dict.label}参考价格</a>
            </c:otherwise>
        </c:choose>
        </li>
    </c:forEach>
    <shiro:hasPermission name="md:customerprice:edit">
        <li><a href="${ctx}/md/productprice/forms">添加参考价格</a>
        </li>
    </shiro:hasPermission>
</ul>

<form:form id="searchForm" modelAttribute="productPrice" action="${ctx}/md/productprice/list?type=${priceType}" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <input id="priceType" name="priceType" type="hidden" value="${priceType}"/>
    <label>产品：</label>
    <form:select path="product.id" class="required" style="width:300px;">
        <form:option value="" label="所有"/>
        <form:options items="${fns:getProducts()}" itemLabel="name" itemValue="id" htmlEscape="false" />
    </form:select>
    &nbsp;
    <input id="btnSubmit" class="btn btn-primary"  type="submit" onclick="return setPage();" value="查询"/>
</form:form>
<sys:message content="${message}"/>
<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th rowspan="2" colspan="2"><label class="col_product">产品名称</label></th>
        <c:forEach items="${serviceTypes}" var="serviceType">
            <th colspan="5" width="400">${serviceType.name}</th>
        </c:forEach>
    </tr>
    <tr>
        <c:set var="serviceTypeCount" value="1"/>
        <c:forEach items="${serviceTypes}" var="serviceType">
            <th>厂商标准价</th>
            <th>厂商优惠价</th>
            <th>网点标准价</th>
            <th>网点优惠价</th>
            <th><label class="col_command">操作</label></th>
            <c:set var="serviceTypeCount" value="${serviceTypeCount+1}"/>
        </c:forEach>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${page.list}" var="productItem">
        <c:set var="index" value="${index+1}" />
        <tr>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${productItem.productName}</td>
            <c:forEach items="${productItem.itemlist}" var="serviceItem">
                <td style="color: blue;">${serviceItem.customerStandardPrice}</td>
                <td style="color: blue;">${serviceItem.customerDiscountPrice}</td>
                <td style="color: red;">${serviceItem.engineerStandardPrice}</td>
                <td style="color: red;">${serviceItem.engineerDiscountPrice}</td>

                <td>
                    <c:if test="${not empty serviceItem.productPriceId }">
                        <c:choose>
                            <c:when test="${serviceItem.delFlag==1}">
                                    <span class="label status_Canceled"> 已停用 </span>
                                    <a href="javascript:void(0);" onclick="go2Active('${serviceItem.productPriceId}','${priceType}');"
                                       title="启用 ${serviceItem.serviceTypeName} 价格">启用</a>
                            </c:when>
                            <c:otherwise>
                                    <c:if test="${serviceItem.productPriceId ne null && serviceItem.productPriceId ne 0}">
                                        <a href="javascript:void(0);" onclick="go2Delete('${serviceItem.productPriceId}','${priceType}');"
                                                title="停用 ${serviceItem.serviceTypeName} 价格">停用</a>
                                        <a href="${ctx}/md/productprice/form?id=${serviceItem.productPriceId}&qPriceType=${priceType}"
                                           title="修改 ${serviceItem.serviceTypeName} 价格">修改</a>
                                    </c:if>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                    <c:if test="${empty serviceItem.productPriceId }">
                        <a href="${ctx}/md/productprice/form?qProductId=${productItem.productId}&qServiceTypeId=${serviceItem.serviceTypeId}&qPriceType=${priceType}"
                           title="添加 ${serviceItem.serviceTypeName} 价格">添加</a>
                    </c:if>
                </td>

            </c:forEach>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>
