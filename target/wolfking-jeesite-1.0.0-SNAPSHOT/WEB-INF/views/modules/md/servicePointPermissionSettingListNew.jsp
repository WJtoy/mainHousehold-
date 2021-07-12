<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>网点权限设定</title>
    <meta name="decorator" content="default" />
    <%@include file="/WEB-INF/views/include/dialog.jsp"%>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <%@include file="/WEB-INF/views/include/treeview.jsp"%>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <style type="text/css">
        .sort {color: #0663A2;cursor: pointer;}
        .form-horizontal .control-label {width: 70px;}
        .form-horizontal .controls { margin-left: 80px;}
        .form-search .ul-form li label {width: auto;}
        .flex-container {display: -webkit-flex;display: flex;width: 100%;margin-bottom: 15px;}
        .flex-item {}
        .flex-item-line {margin-right: 15px;}
        /*.flex-container .flex-item:nth-child(2){*/
            /*margin-left: 10px;*/
        /*}*/
    </style>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript">
        top.layer.closeAll();
        $(document).ready(function() {

            $("#contentTable").treeTable({expandLevel : 2});
            // 表格排序
            var orderBy = $("#orderBy").val().split(" ");
            $("#contentTable th.sort").each(function(){
                if ($(this).hasClass(orderBy[0])){
                    orderBy[1] = orderBy[1]&&orderBy[1].toUpperCase()=="DESC"?"down":"up";
                    $(this).html($(this).html()+" <i class=\"icon icon-arrow-"+orderBy[1]+"\"></i>");
                }
            });
            $("#contentTable th.sort").click(function(){
                var order = $(this).attr("class").split(" ");
                var sort = $("#orderBy").val().split(" ");
                for(var i=0; i<order.length; i++){
                    if (order[i] == "sort"){order = order[i+1]; break;}
                }
                if (order == sort[0]){
                    sort = (sort[1]&&sort[1].toUpperCase()=="DESC"?"ASC":"DESC");
                    $("#orderBy").val(order+" DESC"!=order+" "+sort?"":order+" "+sort);
                }else{
                    $("#orderBy").val(order+" ASC");
                }
                page();
            });

            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});

        });

        //查看网店备注历史列表
        function viewRemarkList(servicePointId,servicePointNo,servicePointName) {
            var planIndex = top.layer.open({
                type: 2,
                id:'layer_planRemarkList_view',
                zIndex:19891016,
                title:'网点备注',
                content: "${ctx}/provider/md/servicePointNew/viewRemarkList?servicePointId=" + (servicePointId || '')+"&servicePointNo="+ (servicePointNo || '')+"&servicePointName="+ (servicePointName || ''),
                // area: ['980px', '640px'],
                area: ['936px', (screen.height/2)+'px'],
                shade: 0.3,
                shadeClose: true,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

        // 修改
        function editJurisdiction(id){
            var text = "修改";
            var url = "${ctx}/provider/md/servicePointNew/psForm?id=" + id;
            var area = ['1000px', '825px'];
            top.layer.open({
                type: 2,
                id:"jurisdiction",
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
    <li class="active"><a href="javascript:;">服务网点</a></li>
</ul>
<sys:message content="${message}" type="loading"/>
<form:form id="searchForm" modelAttribute="servicePoint" action="${ctx}/provider/md/servicePointNew/psList" method="POST" class="breadcrumb form-search"
           cssStyle="border-bottom: 1px solid #EEEEEE;padding-bottom: 5px;margin-top: 15px;">
    <form:hidden path="firstSearch" />
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <input id="orderBy" name="orderBy" type="hidden" value="${servicePoint.orderBy}" />

    <div class="flex-container" style="margin-left: -10px;margin-top: 7px;margin-bottom: 7px;">

        <div class="flex-item-line">

            <label>网点编号：</label>
            <form:input path="servicePointNo" htmlEscape="false" maxlength="20" class="input-small"
                        cssStyle="width: 166px"/>
        </div>

        <div class="flex-item-line">
            <label>网点名称：</label>
            <form:input path="name" htmlEscape="false" maxlength="30" class="input-small" cssStyle="width: 266px"/>
        </div>



        <div class="flex-item-line">
            <label>网点电话：</label>
            <form:input path="contactInfo1" htmlEscape="false" maxlength="20" class="input-small"
                        cssStyle="width: 166px"/>
        </div>

        <div class="flex-item-line">
            <label>账&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;号：</label>
            <form:input path="finance.bankNo" htmlEscape="false" maxlength="20" class="input-small" cssStyle="width: 166px"/>
        </div>

        <div class="flex-item-line">
            <label>支付异常：</label>
            <form:select path="finance.bankIssue.value" class="input-small" cssStyle="width: 250px;height: 30px">
                <form:option value="" label="所有"/>
                <form:options items="${fns:getDictListFromMS('BankIssueType')}" itemLabel="label" itemValue="value"
                              htmlEscape="false"/><%-- 切换为微服务 --%>
            </form:select>
        </div>

        <div class="flex-item">
            <input id="btnSubmit" class="btn btn-primary" type="submit" onclick="top.$.jBox.tip('正在查询,请稍候...', 'loading');return setPage();" value="查询" />
        </div>
    </div>
</form:form>

<table id="contentTable"
       class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="50">序号</th>
        <th width="120">网点编号</th>
        <th width="120">网点名称</th>
        <th width="92">网点电话</th>
        <th width="150">网点地址</th>
        <th width="60">接单量</th>
        <th width="92">网点状态</th>
        <th width="60">网点等级</th>
        <th width="80">手机接单</th>
        <shiro:hasPermission name="md:servicepoint:autocomplete">
            <th width="80">自动完工</th>
        </shiro:hasPermission>
        <shiro:hasPermission name="md:servicepoint:bank">
            <th width="60">扣点</th>
            <th width="60">开票</th>
            <th width="170">账户信息</th>
            <th width="80">网点开发</th>
        </shiro:hasPermission>
        <th width="92">覆盖区域</th>
        <shiro:hasPermission name="md:servicepoint:insurance">
            <th width="80">互助基金</th>
        </shiro:hasPermission>
        <shiro:hasPermission name="md:servicepoint:timeliness">
            <th width="91">快可立时效</th>
        </shiro:hasPermission>
        <th width="91">使用价格</th>
        <shiro:hasPermission name="md:servicepoint:defaultpriceedit">
            <th width="130">结算标准</th>
        </shiro:hasPermission>
        <%--<th width="100">分级</th>--%>
        <th width="80">备注</th>
        <%--<th width="50" style="border-left-style: none">&nbsp;&nbsp;&nbsp;&nbsp;</th>--%>
        <shiro:hasPermission name="md:servicepoint:edit">
            <th width="80">操作</th>
        </shiro:hasPermission>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${page.list}" var="point">
        <c:set var="index" value="${index+1}" />
        <tr id="${point.id}">
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${point.servicePointNo}</td>
            <td>${point.name}</td>
            <td>${point.contactInfo1}</td>
            <td><a href="javascript:" data-toggle="tooltip"
                   data-tooltip="${point.address}">${point.address}</a></td>
            <td>${point.orderCount}</td>
            <td>${fns:getDictLabelFromMS(point.status.value, 'service_point_status', '')}</td>
            <td>${point.level.label}</td>
            <td style="color: <c:out value="${point.appFlag == 1 ?'':'#F54142'}"/>">${point.appFlag == 1 ? '是' : '否'}</td>
            <shiro:hasPermission name="md:servicepoint:autocomplete">
                <td style="color: <c:out value="${point.autoCompleteOrder == 1 ?'':'#F54142'}"/>">${point.autoCompleteOrder == 1 ? '是' : '否'}</td>
            </shiro:hasPermission>
            <shiro:hasPermission name="md:servicepoint:bank">
                <td style="color: <c:out value="${point.finance.discountFlag == 1 ?'':'#F54142'}"/>">
                        ${point.finance.discountFlag == 1 ? '是' : '否'}
                </td>
                <td style="color: <c:out value="${point.finance.invoiceFlag == 1 ?'':'#F54142'}"/>">
                        ${point.finance.invoiceFlag == 1 ? '是' : '否'}
                </td>
                <td>
                        ${point.finance.bankOwner}<br>
                        ${point.finance.bank.label}<br>
                        ${point.finance.bankNo}
                </td>
                <td>${point.developer}</td>
            </shiro:hasPermission>
            <td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${point.serviceAreas}">${fns:abbr(point.serviceAreas,30)}</a></td>
            <shiro:hasPermission name="md:servicepoint:insurance">
                <td style="color: <c:out value="${point.insuranceFlag == 1 ?'':'#F54142'}"/>">${point.insuranceFlag == 1?'开启':'关闭'}</td>
            </shiro:hasPermission>
            <shiro:hasPermission name="md:servicepoint:timeliness">
                <td style="color: <c:out value="${point.timeLinessFlag == 1 ?'':'#F54142'}"/>">${point.timeLinessFlag ==1?"开启":"关闭"}</th>
            </shiro:hasPermission>
            <td>${point.customizePriceFlag == 1?"自定义":"标准价"}</th>
            <shiro:hasPermission name="md:servicepoint:defaultpriceedit">
                <td>
                ${fns:getDictLabelFromMS(point.useDefaultPrice.toString(),'PriceType','')}
                </th>
            </shiro:hasPermission>
            <td>
                <a href="javascript:" data-toggle="tooltip"  data-tooltip="${point.remarks}" style="cursor: pointer;display: block;width: 100px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;">${fns:abbr(point.remarks,30)}</a>
                <c:if test="${point.remarks ne ''}">
                    <button id="btnShowPlanRemarkList" class="btn btn-small" type="button"
                            onclick="viewRemarkList('${point.id}','${point.servicePointNo}','${point.name}');">
                        历史备注
                    </button>
                </c:if>
            </td>
            <shiro:hasAnyPermissions name="md:servicepoint:edit">
                <td>
                    <a href="#" onclick="editJurisdiction('${point.id}')">修改</a><br>
                </td>
            </shiro:hasAnyPermissions>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
<script type="text/javascript">
    $(document).ready(function () {
        $("th").css({"text-align": "center", "vertical-align": "middle"});
        $("td").css({"text-align": "center", "vertical-align": "middle"});
    });
</script>
</html>
