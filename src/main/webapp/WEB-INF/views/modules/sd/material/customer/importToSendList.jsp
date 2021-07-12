<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
	<title>待发货配件列表(客户)</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <%@ include file="/WEB-INF/views/include/WdateLimitPicker.jsp" %>
	<meta name="decorator" content="default" />
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<script src="${ctxStatic}/sd/Material.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <c:set var="currentuser" value="${fns:getUser() }"/>
    <script type="text/javascript">
        top.layer.closeAll();
        Order.rootUrl = "${ctx}";
        Material.rootUrl = "${ctx}";
        //覆盖分页前方法
        function beforePage() {
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            layerLoading("查询中...", true);
        }

        var clicktag = 0;
        $(document).on("click", "#btnSubmit", function () {
            if (clicktag == 0) {
                clicktag = 1;
                beforePage();
                setPage();
                $("#searchForm").attr("action", "${ctx}/sd/material/customer/tosendlist");
                this.form.submit();
            }
        });
        $(document).on("click", "#btnExport", function () {
            $btnExport = $(this);
            var confirmClickTag = 0;
            layer.confirm('确定导出配件列表吗?', {icon: 3, title: '系统确认'}, function (index, layero) {
                if (confirmClickTag == 1) {
                    return false;
                }
                var btn0 = $(".layui-layer-btn0", layero);
                if (btn0.hasClass("layui-btn-disabled")) {
                    return false;
                }
                confirmClickTag = 1;

                var trs = $("#contentTable tbody").find("tr");
                if (trs.length == 0) {
                    layer.close(index);//关闭本身
                    layerInfo("查询无数据，无法导出！", "提示", true);
                    return false;
                }

                btn0.addClass("layui-btn-disabled").attr("disabled", "disabled");
                layer.close(index);//关闭本身
                $btnExport.attr('disabled', 'disabled');
                var loadingIndex = layer.msg('正在导出，请稍等...', {
                    icon: 16,
                    time: 0,
                    shade: 0.3
                });
                if ($("#exportForm").length > 0) {
                    $("#exportForm").remove();
                }
                var html = Utils.formToHiddenInputHtml("searchForm");
                jQuery('<form id="exportForm" action="${ctx}/sd/material/customer/tosendlist/export" method="post" style="display:none;">'+ html + '</form>')
                    .appendTo('body').submit().remove();
                setTimeout(function () {
                    $btnExport.removeAttr('disabled');
                    layer.close(loadingIndex);
                }, 2000);
            });
            return false;
        });

        // 同步申请单跟踪进度
        function updatePendingInfo(pending){
            Material.updatePendingInfo(pending);
        }

        $(document).ready(function(){
            var clickTag = 0;
            $("#btnImportSubmit").click(function () {
                var fileInput = $('#uploadFile').get(0).files[0];
                if(!fileInput){
                    layerError("请先导入文件", "提示", true);
                    return
                }
                if (clickTag == 1) {
                    return false;
                }
                clickTag = 1;
                $("#btnImportSubmit").attr('disabled', 'disabled');
                top.layer.msg('正在读取excel，请稍等...', {
                    icon: 16,
                    time: 0,
                    shade: 0.3
                });
                $("#searchForm").attr("action", "${ctx}/sd/material/import/read");
                $("#searchForm").attr("enctype", "multipart/form-data");
                $("#searchForm").submit();
            });

            /*$("#selectAll").change(function () {
                var $check = $(this);
                $("input[type=checkbox][name=masterId]").each(function () {
                    if ($check.attr("checked") == "checked") {
                        $(this).attr("checked", true);
                    }
                    else {
                        $(this).attr("checked", false);
                    }
                });
            });*/

            var saveTag = 0
            $("#btnSave").click(function(){
                var len=$("input[type=checkbox][name=masterId]:checked").length;
                if(len<=0){
                    layerError("至少选中一条数据", "提示", true);
                    return false
                }
                var materials=[];
                $("input[type=checkbox][name=masterId]:checked").each(function () {
                    var material={};
                    var expressCompany={};
                    var id = $(this).data("id");
                    var quarter = $(this).data("quarter");
                    var express = $(this).data("express");
                    var expressNo = $(this).data("expressno");
                    material['id'] = id;
                    material['quarter'] = quarter;
                    material['expressNo'] = expressNo;
                    expressCompany["value"] = express;
                    material['expressCompany'] = expressCompany;
                    materials.push(material);
                });
                if(saveTag==1){
                    return false;
                }
                $("#btnSave").attr("disabled","disabled")
                saveTag ==1
                var loadingIndex = layer.msg('正在提交，请稍等...', {
                    icon: 16,
                    time: 0,
                    shade: 0.3
                });
                $.ajax({
                    async: false,
                    cache : false,
                    type : "POST",
                    contentType: "application/json",
                    url : "${ctx}/sd/material/batchSend",
                    data : JSON.stringify(materials),
                    beforeSend: function () {
                    },
                    complete: function () {
                        if(loadingIndex) {
                            layer.close(loadingIndex);
                        }
                    },
                    success : function(data) {
                        if(loadingIndex) {
                            layer.close(loadingIndex);
                        }
                        $("#btnSave").removeAttr("disabled")
                        saveTag == 0
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if (data.success) {
                            layerMsg('保存成功!');
                            $("#searchForm").attr("action", "${ctx}/sd/material/customer/tosendlist");
                            $("#searchForm").removeAttr("enctype");
                            $("#searchForm").submit();
                        }else
                        {
                            layerError("保存失败:" + data.message, "错误提示");
                        }
                    },
                    error : function(e) {
                        if(loadingIndex) {
                            layer.close(loadingIndex);
                        }
                        $("#btnSave").removeAttr("disabled")
                        saveTag == 0
                        layerError("保存失败:" + e.responseText, "错误提示");
                    }
                });
            });
        });

        function selectAll(obj) {
            var $check = $(obj);
            $("input[type=checkbox][name=masterId]").each(function () {
                if ($check.attr("checked") == "checked") {
                    $(this).attr("checked", true);
                }
                else {
                    $(this).attr("checked", false);
                }
            });
        }

    </script>
    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
        .table tbody td.tdleft{text-align: left!important;padding-left: 15px;}
        .table tbody tr.danger > td{background-color: #ff00004d;}
    </style>
</head>
<body>
<ul id="navtabs" class="nav nav-tabs">
	<li ><a href="${ctx}/sd/material/customer/applylist" title="待审核配件单">待审核</a></li>
	<li class="active"><a href="javascript:void(0);" title="待发货配件单">待发货<span id="spn_count" class="badge badge-info">${page !=null ?page.count:''}</span></a></li>
	<li><a href="${ctx}/sd/material/customer/sendlist" title="已发货配件单">已发货</a></li>
	<li><a href="${ctx}/sd/material/customer/rejectlist" title="已驳回配件单">已驳回</a></li>
	<li><a href="${ctx}/sd/material/customer/closelist" title="已完成配件单">已完成</a></li>
    <li><a href="${ctx}/sd/material/customer/alllist" title="所有配件单">所有</a></li>
    <li><a href="${ctx}/sd/material/customer/findCustomerWaitMaterialReturnList" title="待签收(旧件)">待签收(旧件)</a></li>
</ul>
<form:form id="searchForm" modelAttribute="searchModel" method="post" action="${ctx}/sd/material/customer/tosendlist" class="breadcrumb form-search">
	<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
	<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
	<input id="repageFlag" name="repageFlag" type="hidden" value="false"/>
    <div class="alert alert-block" style="padding-top:6px;padding-bottom:6px;">
        注:当用工单号和用户电话进行搜索查询时，不受当前时间、快递单号等其他条件的限制。
    </div>
	<div>
        <c:set var="isCustomer" value="${currentuser.isCustomer()}" />
		<c:choose>
			<c:when test="${isCustomer}">
				<form:hidden path="customer.id"/>
				<form:hidden path="customer.name"/>
			</c:when>
			<c:otherwise>
				<label>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
				<form:select path="customer.id" style="width:280px;">
					<form:option value="0" label="所有"/>
					<form:options items="${fns:getMyCustomerList()}" itemLabel="name" itemValue="id" htmlEscape="false"/>
				</form:select>
			</c:otherwise>
		</c:choose>
		<label>工单单号：</label>
		<input type=text class="input-small" id="orderNo" name="orderNo" value="${searchModel.orderNo }" maxlength="20"/>
		<label>用户电话：</label>
		<input type=text class="input-small digits" id="userPhone" name="userPhone" value="${searchModel.userPhone}"
               maxlength="20"/>
        <c:if test="${isCustomer}">
            <label>申请时间：</label>
            <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:106px;"
                   maxlength="20" class="input-small Wdate" value="${fns:formatDate(searchModel.beginDate,'yyyy-MM-dd')}" />
            <label>~</label>&nbsp;&nbsp;&nbsp;
            <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:107px" maxlength="20"
                   class="input-small Wdate" value="${fns:formatDate(searchModel.endDate,'yyyy-MM-dd')}" />
        </c:if>
	</div>
	<div style="margin-top:5px;" id="div_search_customer">
		<div>
            <c:if test="${!isCustomer}">
			<label>申请时间：</label>
			<input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:106px;"
				   maxlength="20" class="input-small Wdate" value="${fns:formatDate(searchModel.beginDate,'yyyy-MM-dd')}" />
			<label>~</label>&nbsp;&nbsp;&nbsp;
			<input id="endDate" name="endDate" type="text" readonly="readonly" style="width:107px" maxlength="20"
				   class="input-small Wdate" value="${fns:formatDate(searchModel.endDate,'yyyy-MM-dd')}" />
            </c:if>
            <input type="hidden" id="materialType" name="materialType" value="1" />
			<label class="label-search">申请类型：</label>
			<form:select path="applyType" class="input-small" style="width:135px;">
				<form:option value="0" label="所有"/>
				<form:options items="${fns:getDictListFromMS('material_apply_type')}" itemLabel="label" itemValue="value"
							  htmlEscape="false"/>
			</form:select>
			<label class="label-search">跟踪状态：</label>
			<form:select path="pendingType" class="input-small" style="width:135px;">
                <form:option value="-1" label="所有"/>
                <form:option value="0" label=""/>
				<form:options items="${fns:getDictListFromMS('material_pending_type')}" itemLabel="label" itemValue="value"
							  htmlEscape="false"/>
			</form:select>
            &nbsp;
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="查询"/>&nbsp;&nbsp;
            <input id="btnExport" class="btn btn-primary" type="button" value="导出"/>&nbsp;&nbsp;
		</div>
	</div>
    <div style="margin-top:5px;">
        <label>文&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件：</label>
        <input id="uploadFile" name="file" type="file" style="width: 221px"/>
        <input id="btnImportSubmit" class="btn btn-primary" type="button" value="读取"/>&nbsp;&nbsp;
        <input id="btnSave" class="btn btn-primary" type="button" value="批量发货"/>
    </div>
</form:form>
<sys:message content="${message}"/>
<%--<div id="divGrid">--%>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="30"><input type="checkbox" id="selectAll" name="selectAll" onchange="selectAll(this)"></th>
        <th width="30">序号</th>
        <th width="120">工单单号</th>
        <th width="120">第三方单号</th>
        <th width="80">状态</th>
        <th width="80">用户姓名</th>
        <th width="80">用户电话</th>
        <th width="150">用户地址</th>
        <th width="80">产品</th>
        <th width="80">品牌</th>
        <th width="80">型号</th>
        <th width="120">配件单号</th>
        <th width="80">配件名称</th>
        <th width="50">配件数量</th>
        <th width="80">申请人</th>
        <th width="80">申请时间</th>
        <th width="150">申请备注</th>
        <th width="80">审核人</th>
        <th width="80">审核时间</th>
        <th width="150">审核备注</th>
        <th width="80">跟踪进度</th>
        <th width="80">跟踪时间</th>
        <th width="150">跟踪内容</th>
        <th width="60">返件</th>
        <th width="120">快递公司</th>
        <th width="120">快递单号</th>
        <th width="120">备注</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${materialMasters}" var="master" varStatus="i" begin="0">
        <c:set var="rowspan" value="${ master.items.size() eq 0?1: master.items.size()}"/>
        <tr id="tr_${master.id}" data-index="${i.index}" class="${master.successFlag==0?"":'danger'}">
            <td rowspan="${rowspan}">
                <c:if test="${master.successFlag==0}">
                    <input type="checkbox" data-id="${master.id}" data-quarter="${master.quarter}"
                           data-express="${master.expressCompany.value}" data-expressno="${master.expressNo}" name="masterId"/>
                </c:if>
            </td>
            <td rowspan="${rowspan}">${i.index+1}</td>
            <td rowspan="${rowspan}">${master.orderNo}</td>
            <td rowspan="${rowspan}">${master.thirdNo}</td>
            <td rowspan="${rowspan}">${master.statusLabel}</td>
            <td rowspan="${rowspan}">${master.userName}</td>
            <td rowspan="${rowspan}">${master.userPhone}</td>
            <td rowspan="${rowspan}">
                <a href="javascript:void(0);" data-toggle="tooltip"
                   data-tooltip="${master.userArea}${master.detailsArea}">${master.userArea}${master.detailsArea}</a>
            </td>
            <td>${master.items[0].productName}</td>
            <td>${master.items[0].brand}</td>
            <td>${master.items[0].productSpace}</td>
            <td rowspan="${rowspan}">${master.masterNo}</td>
            <td>${master.items[0].materialName}</td>
            <td>${master.items[0].qyt}</td>
            <td rowspan="${rowspan}">${master.applicant}</td>
            <td rowspan="${rowspan}">${master.applyDate}</td>
            <td rowspan="${rowspan}">
                <a href="javascript:void(0);" data-toggle="tooltip"
                   data-tooltip="${master.applyRemark}">${master.applyRemark}</a>
            </td>
            <td rowspan="${rowspan}">${master.reviewer}</td>
            <td rowspan="${rowspan}">${master.approveTime}</td>
            <td rowspan="${rowspan}">
                <a href="javascript:void(0);" data-toggle="tooltip"
                   data-tooltip="${master.approveRemark}">${master.approveRemark}</a>
            </td>
            <!-- 跟踪进度 -->
            <td rowspan="${rowspan}">${master.pendingLabel}</td>
            <td rowspan="${rowspan}">${master.pendingTime}</td>
            <td rowspan="${rowspan}">${master.pendingContent}</td>
            <td>
                ${master.items[0].returnFlagLabel}
            </td>

            <td rowspan="${rowspan}">${master.expressCompany.label}</td>
            <td rowspan="${rowspan}">${master.expressNo}</td>
            <td rowspan="${rowspan}">${master.checkMessage}</td>
        </tr>
        <c:forEach items="${master.items}" var="item" varStatus="i" begin="1">
            <tr class="${master.successFlag==0?"":'danger'}">
                <td>${item.productName}</td>
                <td>${item.brand}</td>
                <td>${item.productSpace}</td>
                <td>${item.materialName}</td>
                <td>${item.qyt}</td>
                <td>${item.returnFlagLabel}</td>
            </tr>
        </c:forEach>
    </c:forEach>
    </tbody>
</table>
<%--</div>--%>
<script type="text/javascript" language="javascript">
    $(document).ready(function () {
        oneYearDatePicker('beginDate','endDate',false);
        $('a[data-toggle=tooltip]').darkTooltip();
        $("#divGrid").height(screen.height-305);
        $("#contentTable").toSuperTable({
            width: screen.width-200,
            height: screen.height-500,
            fixedCols: 3,
            headerRows: 1,
            colWidths:
                [   30,30,130,120,80,80,80,150,
                    80, 80,80,120,80,50,80,80,
                    150,80,80,150,80,80,
                    150,60,120,120,120
                ],
            onStart: function () {},
            onFinish: function () {}
        });
    });
</script>
</body>
</html>