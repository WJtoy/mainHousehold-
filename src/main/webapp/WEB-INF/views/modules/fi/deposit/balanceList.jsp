<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<head>
    <title>网点质保金账号清单</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
        .label {padding: 4px 4px !important;text-shadow:none !important;font-weight:400;}
        .label-fullPay{background-color: #0096DA;}
        .label-notFullPay{background-color: #FF9500;}
        .label-notPay{background-color: #F64344;}
        .btn-new {margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 105px;height: 30px;}
    </style>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#btnSubmit").on("click", function(){
                $("#pageNo").val(1);
                layerLoading('正在查询，请稍等...',true);
                $("#searchForm").submit();
                return false;
            });
        });

        function depositCharge(sid,sname){
            top.layer.open({
                type: 2,
                id:'layer_deposit_charge',
                zIndex:19891015,
                title:'充值',
                content: "${ctx}/fi/servicepoint/deposit/chargeForm?servicePoint.id=" + sid + "&servicePoint.name=" + encodeURIComponent(sname),
                shade: 0.3,
                area: ['800px', '410px'],
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

        function showMyCurrencyList(sid,sname){
            var date = new Date();
            var dateStr = DateFormat.format(date, 'yyyy-MM-01');
            $("#beginDate").val(dateStr);
            var dateEndStr = DateFormat.format(DateFormat.addDay(DateFormat.addMonthStr(dateStr, 1),-1), 'yyyy-MM-dd');
            $("#endDate").val(dateEndStr);
            top.layer.open({
                type: 2,
                id:'layer_deposit_mycurrency',
                zIndex:19891015,
                title:'质保金明细',
                content: "${ctx}/fi/servicepoint/deposit/myCurrencyList?servicePointId=" + sid + "&servicePointName=" + encodeURIComponent(sname) + "&startDate=" + dateStr +"&endDate=" + dateEndStr,
                shade: 0.3,
                area: ['1400px', '800px'],
                resize: false,
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
<ul id="navtabs" class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">网点质保金</a></li>
</ul>
<sys:message content="${message}"/>
<form:form id="searchForm" modelAttribute="depositEntity" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div>
        <label>网点名称：</label>
        <md:selectServicePointForDeposit id="servicePoint" name="servicePointId" value="${depositEntity.servicePointId}"
                                         labelName="servicePointName" labelValue="${depositEntity.servicePointName}"
                                         width="1400" height="760"
                                         title="网点" allowClear="true" disabled="false" cssStyle="width: 245px;" />
        <label>网点编号：</label>
        <form:input path="servicePointNo" maxlength="50" class="input-mini" cssStyle="width: 200px;"/>
        <label>网点电话：</label>
        <form:input path="contactInfo" maxlength="20" class="input-mini digits" cssStyle="width: 100px;"/>

        <label>质保等级：</label>
        <select id="depositLevel" name="depositLevel" style="width:200px;">
            <option value="-1" <c:out value="${(depositLevel == -1)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${levels}" var="dict">
                <option value="${dict.id}" <c:out value="${(depositLevel eq dict.id)?'selected=selected':''}" />>${dict.name}</option>
            </c:forEach>
        </select>
        <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>
    </div>
</form:form>
    <button class="btn-new" onclick="depositCharge(0,'')"><i class="icon-plus-sign"></i>&nbsp;充值质保金</button>
<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="50">序号</th>
        <th>网点编号</th>
        <th>网点名称</th>
        <th>主账号</th>
        <th>质保等级</th>
        <th>应缴金额(元)</th>
        <th>已缴金额(元)</th>
        <th>每单扣除(元)</th>
        <th>操作</th>
    <tbody>
    <c:forEach items="${page.list}" var="entity">
        <c:set var="index" value="${index+1}" />
        <tr>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${entity.servicePointNo}</td>
            <td>${entity.name}</td>
            <td>${entity.primary.name}</td>
            <td>${entity.mdDepositLevel.name}</td>
            <td><fmt:formatNumber pattern="0.0">${entity.deposit}</fmt:formatNumber> </td>
            <td>
                <fmt:formatNumber pattern="0.0">${entity.finance.deposit}</fmt:formatNumber><br/>
                <c:if test="${entity.mdDepositLevel.id != null}">
                <c:choose>
                    <c:when test="${entity.finance.deposit>=entity.deposit}"><span class="label label-fullPay">已缴满</span></c:when>
                    <c:when test="${entity.finance.deposit > 0}"><span class="label label-notFullPay">未缴满</span></c:when>
                    <c:otherwise><span class="label label-notPay">未缴费</span></c:otherwise>
                </c:choose>
                </c:if>
            </td>
            <td><fmt:formatNumber pattern="0.0">${entity.mdDepositLevel.deductPerOrder}</fmt:formatNumber></td>
            <td>
                <a href="javascript:;" onclick="depositCharge(${entity.id},'${entity.name}');">充值</a>
                <a href="javascript:;" style="margin-left:15px;" onclick="showMyCurrencyList(${entity.id},'${entity.name}');">质保金明细</a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>

