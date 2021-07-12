<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
    <title>业务好评单-待处理</title>
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
        .fromInput {
            border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
        }
        .cance_btn{height: 30px;border-radius: 4px;color: white;border: 1px solid rgba(255, 255, 255, 0);background-color: rgba(255, 149, 2, 1)}
        #divNoRecord p {margin:10px 0 10px;}
    </style>
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

        function getAmount(id,amount) {
            if(amount>0){
                $.ajax({
                    url:"${ctx}/fi/customer/offline/recharge/getMoneyBack?amount="+amount,
                    type:"POST",
                    dataType:"json",
                    success: function(data){
                        if (data.success) {
                            if(data.data>0){
                                $("#finallyAmount_"+id).text(parseFloat(amount) + parseFloat(data.data))
                            }else{
                                $("#finallyAmount_"+id).text(amount)
                            }
                        }else{
                            $("#finallyAmount_"+id).text(amount)
                            layerError(data.message, "错误提示");
                        }
                        return false;
                    },
                    error: function (data)
                    {

                        $("#finallyAmount_"+id).text(amount)
                        ajaxLogout(data,null,"数据保存错误，请重试!");
                        //var msg = eval(data);
                    }
                });
            }else{
                $("#finallyAmount_"+id).text(0)
            }
        }

        //确认到账
        function approved(id,customerName,customerId,obj,phone,pendingAmount,createAt,payType,transferNo) {
            var actualAmount = $("#actualAmount_" + id).val();
            if(actualAmount<=0){
                layerError("实际到账金额不能小于0元", "错误提示");
                return false
            }
            layer.confirm("确认客户【"+customerName+"】充值金额<span style='color: #0096DA '>"+actualAmount+"元" + "</span>已到账吗？", {
                btn : [ '确定', '取消' ]//按钮
            }, function(index) {
                layer.close(index);
                $(obj).prop("disabled",true);
                var loadingIndex = layerLoading('正在提交，请稍等...');
                $.ajax({
                    url:"${ctx}/fi/customer/offline/recharge/approved",
                    type:"POST",
                    dataType:"json",
                    data:{id:id,actualAmount:actualAmount,customerId:customerId,phone:phone,pendingAmount:pendingAmount,createAt:createAt,payType:payType,transferNo:transferNo},
                    success: function(data){
                        if (data.success) {
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                           layerMsg("保存成功");
                           repage();
                        }else{
                            $(obj).removeAttr('disabled');
                            layerError(data.message, "错误提示");
                        }
                        return false;
                    },
                    error: function (data)
                    {
                        $(obj).removeAttr('disabled');
                        top.layer.close(loadingIndex);
                        ajaxLogout(data,null,"数据保存错误，请重试!");
                    }
                });
            });
        }
        
        //审核无效
        function invalid(id,phone,createAt,pendingAmount,payType) {
            var invalid = top.layer.open({
                type: 2,
                id:'layer_invalid',
                zIndex:19891015,
                title:'无效',
                content: "${ctx}/fi/customer/offline/recharge/invalidForm?id=" + id+"&phone="+phone +"&createAt="+createAt + "&pendingAmount=" +pendingAmount+"&payType="+payType,
                area: ['700px', '330px'],
                shade: 0.3,
                shadeClose:false,
                maxmin: false,
                success: function(layero,index){
                }
            });
        }
    </script>
</head>

<body>
<ul id="navtabs" class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);" title="待审核">待审核</a></li>
    <li><a href="${ctx}/fi/customer/offline/recharge/findHasReviewList" title="已审核">已审核</a></li>
</ul>
<form:form id="searchForm" modelAttribute="offlineRechargeSearch" action="${ctx}/fi/customer/offline/recharge/findPendingList" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div>
        <label>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
        <select name="customerId" class="input-large required" style="width:294px;">
            <option value="">请选择</option>
            <c:forEach items="${fns:getMyCustomerListFromMS()}" var="customer">
                <option value="${customer.id}"  <c:out value="${customer.id==offlineRechargeSearch.customerId ?'selected':''}"/>>${customer.name}</option>
            </c:forEach>
        </select>&nbsp;
        <%--<label>支付宝账号：</label>
        <input type=text class="input-small" name="alipayAccount" value="${offlineRechargeSearch.alipayAccount}" maxlength="50" style="width: 243px;"/>&nbsp;--%>
        <label>&nbsp;&nbsp;&nbsp; 充值时间：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:95px;"
               maxlength="20" class="input-small Wdate" value="${offlineRechargeSearch.beginDate}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>&nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:95px" maxlength="20"
               class="input-small Wdate" value="${offlineRechargeSearch.endDate}"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
    </div>
    <div style="margin-top: 5px">
        <label>交易单号：</label>
        <input type=text class="input-small" name="transferNo" value="${offlineRechargeSearch.transferNo}" maxlength="50" style="width: 280px;"/>
        &nbsp;&nbsp;&nbsp;
        <label>充值方式：</label>
        <form:select path="payType" cssClass="input-small" cssStyle="width:113px;">
            <form:option value="0" label="所有"/>
            <form:option value="10" label="支付宝"/>
            <form:option value="20" label="微信"/>
        </form:select>
        &nbsp;&nbsp;&nbsp;
        <input style="margin-left: 16px" id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询"/>
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
            <th width="200">客户</th>
            <th width="150">充值方式</th>
            <th width="200">交易单号</th>
            <th width="100">待审充值金额</th>
            <th width="100">实际到账金额</th>
            <th width="100">实际入账金额</th>
            <th width="200">充值时间</th>
            <th width="300">备注</th>
           <shiro:hasPermission name="fi:offlinerecharge:edit">
               <th width="200">操作</th>
           </shiro:hasPermission>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowcnt" value="${page.list.size()}"/>
        <c:forEach items="${page.list}" var="model">
            <c:set var="rowNumber" value="${rowNumber+1}"/>
            <tr>
                <td>${rowNumber}</td>
                <td>${model.customerName}</td>
                <c:choose>
                   <c:when test="${model.payType==10}">
                       <td>支付宝</td>
                   </c:when>
                   <c:when test="${model.payType==20}">
                       <td>微信</td>
                   </c:when>
                    <c:otherwise>
                        <td></td>
                    </c:otherwise>
                </c:choose>
                <td>${model.transferNo}</td>
                <td>${model.strPendingAmount}</td>
                <td>
                    <input id="actualAmount_${model.id}" class="input-small fromInput" value="${model.strPendingAmount}" maxlength="10" oninput="getAmount('${model.id}',this.value)">
                </td>
                <td id="finallyAmount_${model.id}">${model.strFinallyAmount}</td>
                <td>${model.rechargeTime}</td>
                <td>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.remarks}">
                            ${fns:abbr(model.remarks,30)}
                    </a>
                </td>
                <shiro:hasPermission name="fi:offlinerecharge:edit">
                    <td>
                        <input class="btn btn-primary" type="button" onclick="approved('${model.id}','${model.customerName}','${model.customerId}',this,'${model.phone}','${model.pendingAmount}','${model.createAt}','${model.payType}','${model.transferNo}')" value="确认到账"/>
                        <input class="cance_btn" type="button" style="width: 56px;margin-left: 10px" onclick="invalid('${model.id}','${model.phone}','${model.createAt}','${model.pendingAmount}','${model.payType}')" value="无效"/>
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
/*    $(document).ready(function() {
        if($("#contentTable tbody>tr").length>0) {
            $("#divNoRecord").hide();
            //无数据报错
        }
        else {
            var h = document.body.clientHeight;
            $("#divGrid").height(h-295);
            $("#divNoRecord").show();
        }
    });*/
    function showPraiseInfo(id,quarter) {
        top.layer.open({
            type: 2,
            id: 'layer_salesPending',
            zIndex: 19891015,
            title: '待审核',
            content: "${ctx}/sd/sales/praise/praiseInfoForSales?id="+id + "&quarter=" + quarter,
            area: ['900px','720px'],
            shade: 0.3,
            shadeClose: true,
            maxmin: true
        });
    }
</script>
</body>
</html>
