<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>网点付款确认</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script type="text/javascript">
        function search1(){
            $("#pageNo").val(1);
            $("#searchForm").attr("action","${ctx}/fi/servicepointwithdraw/listNew");
            $("#searchForm").submit();
            return false;
        }

        $(document).ready(function() {
            $("#aSave").fancybox({
                fitToView : false,
                width  : 700,
                height  : 410,
                autoSize : false,
                closeClick : false,
                type  : 'iframe',
                openEffect : 'none',
                closeEffect : 'none'
            });
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});

            //全选绑定
            $("#selectAll").change(function() {
                var $check = $(this);
                $("input:checkbox").each(function(){
                    if ($(this).val() != "on"){
                        if ($check.attr("checked") == "checked") {
                            $(this).attr("checked", true);

                        }
                        else{
                            $(this).attr("checked", false);
                        }
                    }
                });
            });
        });
        //结算方式,银行联动
        $(document).on("change", "#withdrawStatus", function() {
            if ($("#withdrawStatus").val() == 20 || $("#withdrawStatus").val() == 30) {
                $("#lblDate").html("操作日期：");
            } else {
                $("#lblDate").html("付款日期：");
            }
        });

        function confirmEdit(withdrawId){
            $("#aSave").attr("href", "${ctx}/fi/servicepointinvoice/confirm/edit?withdrawId="+withdrawId);
            $("#aSave").click();
        }

    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">网点付款列表</a></li>
</ul>
<form:form id="searchForm" action="${ctx}/fi/servicepointwithdraw/listNew" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <a id="aSave" type="hidden" href="" class="fancybox"  data-fancybox-type="iframe"></a>
    <div>
        <c:set var="paymentTypeList" value="${fns:getDictExceptListFromMS('PaymentType', '30')}" /><%--切换为微服务--%>
        <label>结算方式：</label>
        <select id="payment" name="payment" style="width:140px;">
            <option value="" selected="selected">所有</option>
            <c:forEach items="${paymentTypeList}" var="paymentType">
                <option value="${paymentType.value}" <c:out value="${(payment eq paymentType.value)?'selected=selected':''}" />>${paymentType.label}</option>
            </c:forEach>
        </select>
        <c:set var="bankList" value="${fns:getDictListFromMS('banktype')}" /><%--切换为微服务--%>
        <label>银行：</label>
        <select id="bank" name="bank" style="width:140px;">
            <option value="" selected="selected">所有</option>
            <c:forEach items="${bankList}" var="b">
                <option value="${b.value}" <c:out value="${(bank eq b.value)?'selected=selected':''}" />>${b.label}</option>
            </c:forEach>
        </select>
        <label>服务网点：</label>
        <sd:servicePointSelect id="servicePoint" name="servicePointId" value="${servicePointId}" labelName="servicePointName" labelValue="${servicePointName}"
                             width="1200" height="780" title="选择服务网点" areaId="" cssClass="required"
                             showArea="false" allowClear="true" callbackmethod="" />
        <label>是否开票：</label>
        <select id="engineerInvoiceFlag" name="engineerInvoiceFlag" style="width:80px;">
            <option value="" selected="selected">所有</option>
            <option value="1" <c:out value="${(engineerInvoiceFlag eq '1')?'selected=selected':''}" />>是</option>
            <option value="0" <c:out value="${(engineerInvoiceFlag eq '0')?'selected=selected':''}" />>否</option>
        </select>
        &nbsp;&nbsp;<input id="btnSubmit" class="btn btn-primary" type="button" onclick="top.$.jBox.tip('请稍候...', 'loading');search1();" value="查  询" />
    </div>
    <div style="margin-top:8px">
        <label id="lblDate">付款日期：</label>
        <input id="payBeginDate" name="payBeginDate" type="text" readonly="readonly" style="width:125px; maxlength:20" class="input-small Wdate"
               value="${payBeginDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
        <label>　~　</label>&nbsp;&nbsp;&nbsp;<input id="payEndDate" name="payEndDate" type="text" readonly="readonly" style="width:125px" maxlength="20" class="input-small Wdate"
                                                   value="${payEndDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
        <label>付款状态：</label>
        <select id="withdrawStatus" name="withdrawStatus" class="input-small" style="width:254px;">
            <option value="" <c:out value="${(empty withdrawStatus)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${fns:getDictExceptListFromMS('ServicePointWithdrawStatus', '10')}" var="dict"><%--切换为微服务--%>
                <option value="${dict.value}" <c:out value="${(withdrawStatus eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
            </c:forEach>
        </select>
        &nbsp;<label>是否扣点：</label>
        <select id="engineerDiscountFlag" name="engineerDiscountFlag" style="width:80px;">
            <option value="" selected="selected">所有</option>
            <option value="1" <c:out value="${(engineerDiscountFlag eq '1')?'selected=selected':''}" />>是</option>
            <option value="0" <c:out value="${(engineerDiscountFlag eq '0')?'selected=selected':''}" />>否</option>
        </select>
    </div>
</form:form>
<sys:message content="${message}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th>序号</th>
        <th>结算方式</th>
        <th>网点编号</th>
        <th>网点名称</th>
        <th>负责人</th>
        <th>联系电话</th>
        <th>开票</th>
        <th>扣点</th>
        <th>开户银行</th>
        <th>开户人</th>
        <th>银行帐号</th>
        <th>操作日期</th>
        <th>描述</th>
        <th>付款日期</th>
        <th>状态</th>
        <th>金额</th>
        <th>操作</th>
    </tr>
    </thead>
    <tbody>
    <%int i=0; %>
    <c:set var="totalApplyAmount" value="0" />
    <c:forEach items="${page.list}" var="wd">
        <tr>
            <c:set var="index" value="${index+1}" />
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${wd.paymentTypeName}</td><%--切换为微服务--%>
            <td>${wd.servicePoint.servicePointNo}</td>
            <c:choose>
                <c:when test="${wd.servicePoint.finance.bankIssue.value eq '0'}">
                    <td>${wd.servicePoint.name}</td>
                </c:when>
                <c:otherwise>
                    <td style="background-color: #f2dede;">
                        <a href="javascript:" data-toggle="tooltip"
                            <%--data-tooltip="${fns:getDictLabelFromMS(wd.servicePoint.finance.bankIssue.value, 'BankIssueType', '')}">--%>
                           data-tooltip="${wd.servicePoint.finance.bankIssue.label}"><%--切换为微服务 --%>
                                ${wd.servicePoint.name}
                        </a>
                    </td><%-- 切换为微服务 --%>
                </c:otherwise>
            </c:choose>
            <td>${wd.servicePoint.primary.name}</td>
            <td>${wd.servicePoint.contactInfo1}</td>
            <td>
                <c:if test="${wd.servicePoint.finance.invoiceFlag == 1}"><span class="label status_Canceled">${fns:getDictLabelFromMS(servicePoint.finance.invoiceFlag, "yes_no","")}</span></c:if><%--切换为微服务--%>
                <c:if test="${wd.servicePoint.finance.invoiceFlag == 0}">${fns:getDictLabelFromMS(wd.servicePoint.finance.invoiceFlag, "yes_no","")}</c:if><%--切换为微服务--%>
            </td>
            <td>
                <c:if test="${wd.servicePoint.finance.discountFlag == 1}"><span class="label status_Canceled">${fns:getDictLabelFromMS(servicePoint.finance.discountFlag, "yes_no","")}</span></c:if><%--切换为微服务--%>
                <c:if test="${wd.servicePoint.finance.discountFlag == 0}">${fns:getDictLabelFromMS(wd.servicePoint.finance.discountFlag, "yes_no","")}</c:if><%--切换为微服务--%>
            </td>
            <td>${wd.bankName}</td><%--切换为微服务--%>
            <td>${wd.bankOwner}</td>
            <td>${wd.bankNo}</td>
            <td><fmt:formatDate value="${wd.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            <td>${wd.remarks}</td>
            <td><fmt:formatDate value="${wd.payDate}" pattern="yyyy-MM-dd"/></td>
            <td>
                <c:choose>
                    <c:when test="${wd.status eq 20}">
                        <span class="label status_Accepted">${wd.statusName}</span>
                    </c:when>
                    <c:when test="${wd.status eq 30}">
                        <span class="label status_Canceled">${wd.statusName}</span>
                    </c:when>
                    <c:when test="${wd.status eq 40}">
                        <span class="label status_Completed">${wd.statusName}</span>
                    </c:when>
                </c:choose>
            </td>
            <td>${fns:formatNum(wd.applyAmount)}</td>

            <c:set var="totalApplyAmount" value="${totalApplyAmount+wd.applyAmount}" />

            <td>
                <c:if test="${wd.status eq 20}">
                    <input type="Button" id="${wd.id}" class="btn btn-mini btn-danger" value="付款失败"
                           onclick="confirmFail('${wd.id}', '${wd.servicePoint.id}')"/>
                </c:if>
                <c:if test="${wd.status eq 40}">
                    <input type="Button" id="${wd.id}" class="btn btn-mini btn-primary" value="付款修改"
                           onclick="confirmEdit('${wd.id}')"/>
                </c:if>
            </td>
        </tr>
    </c:forEach>
    <tr>
        <td style="text-align:right;" colspan="15" ><B>合计</B></td>
        <td style="color:red;"><B>${fns:formatNum(totalApplyAmount)}</B>
        </td>
        <td></td>
    </tr>
    </tbody>
</table>
<div class="pagination">${page}</div>
</body>
</html>
