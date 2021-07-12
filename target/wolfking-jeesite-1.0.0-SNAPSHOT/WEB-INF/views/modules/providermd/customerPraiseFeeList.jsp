<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>好评费</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript">
        function editCustomerPraiseFee(type,id) {
            var text = "添加客户好评";
            var url = "${ctx}/provider/md/customerPraiseFee/form";
            if(type == 2){
                text = "修改客户好评"
                url = "${ctx}/provider/md/customerPraiseFee/form?id=" + id;
            }
            top.layer.open({
                type: 2,
                id:"customerPraiseFee",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['900px', '850px'],
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
         th {text-align: center !important;}
         td {text-align: center !important;}
    </style>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">客户好评</a></li>
</ul>
<form:form id="searchForm" action="${ctx}/provider/md/customerPraiseFee/" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <label>客户：</label>
    </label>
    <select name="customerId" class="input-large">
        <option value="">所有</option>
        <c:forEach items="${fns:getMyCustomerListFromMS()}" var="customer">
            <option value="${customer.id}"  <c:out value="${customer.id == customerPraiseFee.customerId ?'selected':''}"/>>${customer.name}</option>
        </c:forEach>
    </select>
    &nbsp;
    <input id="btnSubmit" class="btn btn-primary" value="查询"  type="submit" onclick="return setPage();" />
</form:form>
<shiro:hasPermission name="md:customerpraisefee:edit">
    <button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 130px;height: 30px" onclick="editCustomerPraiseFee(1,null)"><i class="icon-plus-sign"></i>&nbsp;添加客户好评</button>
</shiro:hasPermission>
<sys:message content="${message}" />
<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="50px">序号</th>
        <th>客户</th>
        <th width="78px">好评费用</th>
        <th width="78px">线上</th>
        <th width="78px">起始费用</th>
        <th width="78px">上限费用</th>
        <th width="180px">平台提成</th>
        <th width="250px">好评要求</th>
        <th width="300px">好评标准</th>
        <th width="250px">审核标准</th>
        <shiro:hasPermission name="md:customerpraisefee:edit">
            <th width="65px">操作</th>
        </shiro:hasPermission>
    </tr>
    </thead>
    <tbody>
    <c:set var="index" value="0"></c:set>
    <c:forEach items="${page.list}" var="customerPraiseFee">
        <tr>
            <c:set var="index" value="${index+1}"></c:set>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${customerPraiseFee.customerName}</td>
            <td>
                <c:choose>
                    <c:when test="${customerPraiseFee.praiseFeeFlag==0}">
<%--                        <span class="label label-important">否</span>--%>
                        <span><font color="red">无</font></span>
                    </c:when>
                    <c:otherwise>
<%--                        <span class="label label-success">是</span>--%>
                        <span><font color="#2fa4e7">有</font></span>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:choose>
                    <c:when test="${customerPraiseFee.onlineFlag==0}">
                        <span><font color="red">否</font></span>
                    </c:when>
                    <c:otherwise>
                        <span><font color="#2fa4e7">是</font></span>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>${customerPraiseFee.praiseFee}</td>
            <td>${customerPraiseFee.maxPraiseFee}</td>
            <td><fmt:formatNumber type="number" value="${customerPraiseFee.discount}" maxFractionDigits="0" pattern="#"/>%(平台:<span style="color: red">${customerPraiseFee.maxPraiseFee * customerPraiseFee.discount / 100}</span> 师傅:<span style="color: blue">${customerPraiseFee.maxPraiseFee * (100 - customerPraiseFee.discount) / 100}</span>)</td>
            <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${customerPraiseFee.praiseRequirement}">${fns:abbr(customerPraiseFee.praiseRequirement,50)}</a></td>
            <td>
                <c:set var="count" value="0" />
                <c:set var="itemSize" value="${fn:length(customerPraiseFee.praiseStandardItems)}" />
                <c:forEach items="${customerPraiseFee.praiseStandardItems}" var="item" varStatus="i" begin="0">
                    <c:set var="count" value="${count+1}" />
                    <c:set var="hintInfo" value=""/>
                    <c:set var="splitStr" value=""/>
                    <c:if test="${item.mustFlag eq 1}">
                        <c:set var="hintInfo" value="(必选)"/>
                    </c:if>
                    <c:if test="${count ne itemSize}">
                        <c:set var="splitStr" value=","/>
                    </c:if>
                    ${item.name}${hintInfo}${splitStr}
                </c:forEach>
            </td>
            <td>
                <c:set var="checkCount" value="0" />
                <c:set var="checkItemSize" value="${fn:length(customerPraiseFee.checkStandardItems)}" />
                <c:forEach items="${customerPraiseFee.checkStandardItems}" var="item" varStatus="i" begin="0">
                    <c:set var="checkCount" value="${checkCount+1}" />
                    <c:set var="split" value=""/>
                    <c:if test="${checkCount ne checkItemSize}">
                        <c:set var="split" value=","/>
                    </c:if>
                    ${item.description}${split}
                </c:forEach></td>
            <shiro:hasPermission name="md:customerpraisefee:edit">
                <td><a href="javascript:editCustomerPraiseFee(2,'${customerPraiseFee.id}')">修改</a>
                    <a href="${ctx}/provider/md/customerPraiseFee/delete?id=${customerPraiseFee.id}&customerId=${customerPraiseFee.customerId}"
                       onclick="return confirmx('确认要删除该好评费吗？', this.href)">删除</a></td>
            </shiro:hasPermission>
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
