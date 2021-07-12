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
        function approved(id,customerName,customerId,obj) {
            var actualAmount = $("#actualAmount_" + id).val();
            if(actualAmount<=0){
                layerError("实际到账今晚不能小于0元", "错误提示");
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
                    data:{id:id,actualAmount:actualAmount,customerId:customerId},
                    success: function(data){
                        if (data.success) {
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                           /* if(ajaxLogout(data)){
                                setTimeout(function () {
                                    clickTag = 0;
                                    $("#btnSubmit").removeAttr('disabled');
                                }, 2000);
                                return false;
                            }*/
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
        function invalid(id) {
            var invalid = top.layer.open({
                type: 2,
                id:'layer_invalid',
                zIndex:19891015,
                title:'无效',
                content: "${ctx}/fi/customer/offline/recharge/invalidForm?id=" + id,
                area: ['700px', '330px'],
                shade: 0.3,
                shadeClose:true,
                maxmin: false,
                success: function(layero,index){
                }
            });
        }
    </script>
</head>

<body>
<ul id="navtabs" class="nav nav-tabs">
    <li><a href="${ctx}/fi/customer/offline/recharge/findPendingList" title="待审核">待审核</a></li>
    <li class="active"><a href="javascript:void(0);" title="已审核">已审核</a></li>
</ul>
<form:form id="searchForm" modelAttribute="offlineRechargeSearch" action="${ctx}/fi/customer/offline/recharge/findHasReviewList" method="post" class="breadcrumb form-search">
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
        <input type=text class="input-small" name="alipayAccount" value="${offlineRechargeSearch.alipayAccount}" maxlength="50" style="width: 240px;"/>&nbsp;--%>
        <label>充值时间：</label>
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
        <label>充值方式：</label>
        <form:select path="payType" cssClass="input-small" cssStyle="width:113px;">
            <form:option value="0" label="所有"/>
            <form:option value="10" label="支付宝"/>
            <form:option value="20" label="微信"/>
        </form:select>
        <label>状态：</label>
        <form:select path="status" cssClass="input-small" cssStyle="width:87px;">
            <form:option value="0" label="所有"/>
            <form:option value="20" label="已通过"/>
            <form:option value="30" label="无效"/>
        </form:select>
        &nbsp;
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
            <th width="300">客户</th>
            <th width="80">状态</th>
            <th width="150">充值方式</th>
            <th width="200">交易单号</th>
            <th width="100">待审充值金额</th>
            <th width="100">实际到账金额</th>
            <th width="100">实际入账金额</th>
            <th width="200">充值时间</th>
            <th width="200">无效原因</th>
            <th width="300">备注</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="rowcnt" value="${page.list.size()}"/>
        <c:forEach items="${page.list}" var="model">
            <c:set var="rowNumber" value="${rowNumber+1}"/>
            <tr>
                <td>${rowNumber}</td>
                <td>${model.customerName}</td>
                <td>
                    <c:choose>
                        <c:when test="${model.status==20}">
                            <span style="background-color:#34C758;color: white;padding: 2px 4px;border-radius: 3px">通过</span>
                        </c:when>
                        <c:otherwise>
                            <a href="javascript:void(0);" data-toggle="tooltip" style="text-decoration:none" data-tooltip="${fns:getDictLabelFromMS(model.invalidType,'recharge_invalid_type','')}">
                                <span style="background-color:#C5C8CE;color: white;padding: 2px 4px;border-radius: 3px">无效</span>
                            </a>
                        </c:otherwise>
                    </c:choose>
                </td>
              <%--  <td>${model.alipayAccount}</td>--%>
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
                <td>${model.pendingAmount}</td>
                <td>
                    <c:choose>
                        <c:when test="${model.status==20}">
                            ${model.actualAmount}
                        </c:when>
                        <c:otherwise>

                        </c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${model.status==20}">
                            ${model.finallyAmount}
                        </c:when>
                        <c:otherwise>

                        </c:otherwise>
                    </c:choose>
                </td>
                <td>${model.rechargeTime}</td>
                <td>${model.invalidReason}</td>
                <td>
                    <a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${model.remarks}">
                            ${fns:abbr(model.remarks,30)}
                    </a>
                </td>
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
</body>
</html>
