<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>选择服务网点</title>
    <meta name="decorator" content="default"/>
    <style type="text/css">
        .pagination {
            margin: 10px 0;
        }
    </style>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript">
        var data = null;
        var index = top.layer.index;

        function setData(i) {
            data = items[i];
            var layerIndex = $("#layerIndex").val() || index;
            top.$("#layui-layer" + layerIndex).find(".layui-layer-btn0").trigger("click");
        }

        var items = [
            <c:forEach items="${page.list}" var="point">
            {
                id: "${point.id}",
                servicePointNo: "${point.servicePointNo}",
                name: "${point.name}",
                primary: {
                    id: "${point.primary.id}",
                    name: "${point.primary.name}",
                    address: "${point.primary.address}",
                    area: "${point.primary.area.id}"
                },
                area: {
                    id: "${point.area.id}",
                    fullName: "${point.area.fullName}"
                },
                address: "${point.address}",
                subAddress: "${point.subAddress}",
                phone: "${point.contactInfo1 }",
                grade:${point.grade},
                appFlag:${point.appFlag},
                paymentType: {label: "${point.finance.paymentType.label}", value:${point.finance.paymentType.value}}
            },
            </c:forEach>
        ];
    </script>

</head>

<body>
<c:set var="currentuser" value="${fns:getUser() }"/>
<sys:message content="${message}" type="loading"/>
<div style="margin-left:3px;margin-right:3px;">
    <form:form id="searchForm" modelAttribute="servicePoint" action="${ctx}/md/servicepoint/mdservicePointSelector"
               method="post" class="breadcrumb form-search">
        <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
        <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
        <input id="layerIndex" name="layerIndex" type="hidden" value="${servicePoint.layerIndex}"/>
        <div style="margin-bottom:5px;">
            <label>网点编号：</label>
            <form:input path="servicePointNo" maxlength="20" class="input-mini digits" cssStyle="width: 150px;"/>
            &nbsp;<label>网点名称：</label>
            <form:input path="name" maxlength="20" class="input-mini digits" cssStyle="width: 250px;"/>
            &nbsp;<label>网点电话：</label>
            <form:input path="contactInfo1" maxlength="20" class="input-mini digits" cssStyle="width: 150px;"/>
            &nbsp;
            <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>
        </div>
    </form:form>

    <c:set var="isEngineer" value="${currentuser.isEngineer()}"/>
    <table id="contentTable" class="datatable table table-bordered table-condensed table-hover">
        <thead>
        <tr>
            <th width="45">序号</th>
            <th width="180">网点</th>
            <th width="100">主帐号</th>
            <th width="250">网点地址</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="index" value="0"/>
        <c:forEach items="${page.list}" var="servicepoint">
        <c:set var="i" value="${i+1}"/>
        <tr>
            <td>${i+(page.pageNo-1)*page.pageSize}</td>
            <td>
                <a href="javascript:void(0);" onclick="javascript:setData(${index});">
                        ${servicepoint.servicePointNo}<br/>${servicepoint.name}
                    <c:if test="${servicepoint.primary.appLoged eq 1}">&nbsp;<i class="icon-mobile-phone" style="font-size: 17px;" title="该用户有手机登陆过APP" ></i></c:if>
                </a>
            </td>
            <td>${servicepoint.primary.name}</td>
            <td>${servicepoint.address}</td>
                <c:set var="index" value="${index+1}"/>
            </c:forEach>
        </tbody>
    </table>
    <div class="pagination">${page}</div>
</div>
<script type="text/javascript" language="javascript" class="init">
    $(document).ready(function () {
        $("td,th").css({"text-align": "center", "vertical-align": "middle"});
        if (Utils.isEmpty($("#layerIndex").val())) {
            $("#layerIndex").val(index);
        }
    });
</script>
</body>
</html>
