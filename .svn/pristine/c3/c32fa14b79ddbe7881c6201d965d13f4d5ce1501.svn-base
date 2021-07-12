<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>异常单列表-好评驳回</title>
    <meta name="description" content="待回复">
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <meta name="decorator" content="default"/>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
        }
        #divNoRecord p {margin:10px 0 10px;}
    </style>
    <script type="text/javascript">
        //覆盖分页前方法
        var clicktag = 0
        function beforePage() {
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            $("#btnClearSearch").attr('disabled', 'disabled');
            layerLoading("查询中...", true);
        }

        $(document).on("click", "#btnSubmit", function () {
            if (clicktag == 0) {
                clicktag = 1;
                beforePage();
                setPage();
                this.form.submit();
            }
        });

        top.layer.closeAll();
        Order.rootUrl = "${ctx}";

    </script>

</head>

<body>
<ul id="navtabs" class="nav nav-tabs">
    <shiro:hasPermission name="sd:servicepointabnormal:view">
       <%-- <li class="active"><a href="javascript:void(0);" title="异常单待处理">待处理</a></li>--%>
        <li class="active"><a href="javascript:void(0);" title="好评驳回">好评驳回</a></li>
    </shiro:hasPermission>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="abnormalForm" action="${ctx}/servicePoint/receipt/abnormalForm/praiseAbnormalList" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div>
        <label>工单单号：</label>
        <input type=text class="input-small" id="orderNo" name="orderNo" value="${abnormalForm.orderNo}" maxlength="14" />&nbsp;
        <label>用户电话：</label>
        <input type=text class="input-small" id="userPhone" name="userPhone" value="${abnormalForm.userPhone}" maxlength="14" />&nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>
    </div>
</form:form>
<sys:message content="${message}"/>
<c:set var="rowNumber" value="0"/>
<div id="divNoRecord" style="display: none">
    <div class="alert alert-info">
        <h4>提示!</h4>
        <p>
            查询无符合的数据，请调整查询条件重新查询。
        </p>
    </div>
</div>
    <table id="contentTable" class="table table-bordered table-condensed table-hover">
        <thead>
        <tr>
            <th width="40">序号</th>
            <th width="130">工单单号</th>
            <th width="130">反馈时效</th>
            <th width="200">异常分类</th>
            <th width="200">异常原因</th>
            <th width="150">反馈时间</th>
            <th width="80">用户姓名</th>
            <th width="100">用户电话</th>
            <th width="250">用户地址</th>
            <shiro:hasPermission name="sd:servicepointpraise:edit">
                <th width="100">操作</th>
            </shiro:hasPermission>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowcnt" value="${page.list.size()}"/>
        <c:forEach items="${page.list}" var="model">
            <c:set var="rowNumber" value="${rowNumber+1}"/>
            <tr>
                <td>${rowNumber}</td>
                <td>
                    <a href="javascript:void(0);" style="">
                       ${model.orderNo}
                    </a>
                </td>
                <c:choose>
                    <c:when test="${model.cutOffTimeliness>=0}">
                        <c:choose>
                            <c:when test="${model.cutOffTimeliness>0.50}">
                                <td>${model.feedBackTimeliness}</td>
                            </c:when>
                            <c:otherwise>
                                <td><p style="color: #D46B08;margin: auto">${model.feedBackTimeliness}</p></td>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <td><p style="color: red;margin: auto">${model.feedBackTimeliness}</p></td>
                    </c:otherwise>
                </c:choose>
                <td>${model.subTypeName}</td>
                <td>${model.reason}</td>
                <td>${model.strCreateDate}</td>
                <td>${model.userName}</td>
                <td>${model.userPhone}</td>
                <td>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.userAddress}">
                            ${fns:abbr(model.userAddress,30)}
                    </a>
                </td>
                <shiro:hasPermission name="sd:servicepointpraise:edit">
                    <td>
                        <input id="abnormalClose" class="btn btn-warning" type="button" onclick="showPraiseInfo('${model.orderId}','${model.quarter}','${model.servicePoint.id}')" value="处理"/>
                    </td>
                </shiro:hasPermission>
            </tr>
        </c:forEach>
        </tbody>
    </table>
<c:if test="${rowcnt > 0}">
    <div id="pagination" class="pagination">${page}</div>
</c:if>
<script type="text/javascript" language="javascript">
    $(document).ready(function () {
        $('a[data-toggle=tooltip]').darkTooltip();
        $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
        $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
    });
</script>
<style type="text/css">
    .dropdown-menu {min-width: 80px;}
    .dropdown-menu > li > a {text-align: left;padding: 3px 10px;}
    .pagination {margin: 4px 0 0 4px;}
    .label-search {width: 70px;  text-align: right;}
    form {margin: 0 0 5px;}
</style>
<script type="text/javascript">
    function showPraiseInfo(id,quarter,servicePointId) {
        top.layer.open({
            type: 2,
            id: 'layer_praiseServiceAbnormal',
            zIndex: 19891015,
            title: '好评驳回',
            content: "${ctx}/servicePoint/receipt/abnormalForm/praiseHandleForm?orderId="+id + "&quarter=" + quarter + "&servicePointId=" + servicePointId,
            area: ['900px','720px'],
            shade: 0.3,
            shadeClose: true,
            maxmin: true
        });
    }
</script>
</body>
</html>
