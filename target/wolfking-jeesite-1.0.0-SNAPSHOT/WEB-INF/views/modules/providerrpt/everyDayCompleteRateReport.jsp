<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML>
<html>
<head>
    <title>每日完工时效报表</title>
    <meta name="decorator" content="default" />
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@ include file="/WEB-INF/views/include/treetable.jsp" %>
    <link href="${ctxStatic}/jquery-fixedheadertable/defaultTheme.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery-fixedheadertable/jquery.fixedheadertable.min.js" type="text/javascript"></script>
    <script src="${ctxStatic}/area/Area-1.2.js" type="text/javascript"></script>

    <style type="text/css">
        .table thead th,.table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
        .parent:after{
            content:"";
            height:0;
            line-height:0;
            display:block;
            visibility:hidden;
            clear:both;
        }

        .target{
            display:none;
            z-index: 4;
        }

        .triggle:hover + .target {
            display: block;
        }

        .border{
            display: none;
            opacity: 0.8;
            width: 0 !important;
            border-bottom:solid 12px #1B1E24;
            border-left:12px solid transparent;
            border-right: 6px solid transparent;
            boder-top: 0px solid transparent;
        }
    </style>
    <script type="text/javascript" language="javascript">
        $(document).ready(function () {
            $(".triggle").on('hover', function(){
                $(".border").css({
                    display:"block"
                })
            })
            $(".triggle").on('mouseleave', function(){
                $(".border").css({
                    display:"none"
                })
            })
            $("#contentTable").treeTable({expandLevel : 1});
            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action", "${ctx}/rpt/provider/everyDayComplete/areaOrderCompleteRateReport");
                $("#searchForm").submit();
            });
            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/everyDayComplete/checkExportTask?"+ (new Date()).getTime(),
                    data:$(searchForm).serialize(),
                    success: function (data) {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if(data && data.success == true){
                            top.$.jBox.closeTip();
                            top.$.jBox.confirm("确认要导出数据吗？", "系统提示", function (v, h, f) {
                                if (v == "ok") {
                                    top.$.jBox.tip('请稍候...', 'loading');
                                    $.ajax({
                                        type: "POST",
                                        url: "${ctx}/rpt/provider/everyDayComplete/export?"+ (new Date()).getTime(),
                                        data:$(searchForm).serialize(),
                                        success: function (data) {
                                            if(ajaxLogout(data)){
                                                return false;
                                            }
                                            if(data && data.success == true){
                                                top.$.jBox.closeTip();
                                                top.$.jBox.tip(data.message, "success");
                                                $('#btnExport').removeAttr('disabled');
                                                return false;
                                            }
                                            else if( data && data.message){
                                                top.$.jBox.error(data.message,"导出错误");
                                            }
                                            else{
                                                top.$.jBox.error("导出错误","错误提示");
                                            }
                                            $('#btnExport').removeAttr('disabled');
                                            top.$.jBox.closeTip();
                                            return false;
                                        },
                                        error: function (e) {
                                            $('#btnExport').removeAttr('disabled');
                                            ajaxLogout(e.responseText,null,"导出错误，请重试!");
                                            top.$.jBox.closeTip();
                                        }
                                    });
                                }
                            }, {buttonsFocus: 1});
                            $('#btnExport').removeAttr('disabled');
                            top.$.jBox.closeTip();
                            return false;
                        }
                        else if( data && data.message){
                            top.$.jBox.error(data.message,"导出错误");
                        }
                        else{
                            top.$.jBox.error("导出错误","错误提示");
                        }
                        $('#btnExport').removeAttr('disabled');
                        top.$.jBox.closeTip();
                        return false;
                    },
                    error: function (e) {
                        $('#btnExport').removeAttr('disabled');
                        ajaxLogout(e.responseText,null,"导出错误，请重试!");
                        top.$.jBox.closeTip();
                    }
                });
                top.$('.jbox-body .jbox-icon').css('top', '55px');
            });
        });
    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">每日完工时效报表</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser()}"/>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/rpt/provider/everyDayComplete/areaOrderCompleteRateReport"
           method="post" class="breadcrumb form-search">
    <div style="width: 90%;">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>区域：</label>
        <md:arearemotselect id="areaId" name="areaId" value="${rptSearchCondition.areaId}"
                                 labelValue="${rptSearchCondition.areaName}" labelName="areaName"
                                 title="区域" mustSelectCounty="false" cssClass="required"  > </md:arearemotselect>

        <c:choose>
            <c:when test="${currentuser.isCustomer()}">
                <input type="hidden" readonly="true" id="customerName" name="customerName"
                       value="${currentuser.customerAccountProfile.customer.name}"/>
                <input type="hidden" id="customerId" name="customerId"
                       value="${currentuser.customerAccountProfile.customer.id}"/>
            </c:when>
            <c:otherwise>
                <label>客户：</label>
                <select id="customerId" name="customerId" class="input-small" style="width:225px;">
                    <option value="" <c:out value="${(empty rptSearchCondition.customerId)?'selected=selected':''}"/>>
                        所有
                    </option>
                    <c:forEach items="${fns:getMyCustomerList()}" var="dict">
                        <option value="${dict.id}" <c:out
                                value="${(rptSearchCondition.customerId eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
                    </c:forEach>
                </select>
            </c:otherwise>
        </c:choose>
        <label>时间：</label>
        <input id="dateString" name="dateString" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false,maxDate:'${rptSearchCondition.beginDate}'});"/>


        <input id="btnSubmit" class="btn btn-primary"type="button" value="查询" />
        &nbsp;&nbsp;
        <input id="btnExport"class="btn btn-primary" type="button" value="导出" />
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 150px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：实时数据(5分钟延迟)<br/>
                    统计方式：下单量,  0#F! 代表有条件的下单数(分母) 为0
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}" />
<table id="contentTable" class="table table-bordered table-condensed table-hover" style="table-layout:fixed;">
    <thead>
    <tr>
        <th rowspan="2" colspan="2" width="100">省</th>
        <th rowspan="2" colspan="2" width="100">市</th>
        <th colspan="6">${dateStr}</th>
    </tr>
    <tr>
        <th width="100">下单</th>
        <th width="100">完成单</th>
        <th width="100">完成率</th>
        <th width="100">72H完成率</th>
        <th width="100">到货48H完成率</th>
        <th width="100">无到货72H完成率</th>
        <th width="100">无到货周完成率</th>
        <th width="100">到货72H完成率</th>
        <th width="100">到货周完成率</th>
        <th width="100">未完工单</th>
    </tr>
    <tbody>
    <c:if test="${not empty cityList}">
        <c:forEach items="${cityList}" var="item">
            <c:if test="${item.provinceId ne pfid}">
                <c:set value="${item.provinceId}" var="pfid"/>
                <tr id="${item.provinceId}" pId="" style="color: blue;">
                    <td colspan="2" >${item.provinceName}</td>
                    <td colspan="2" ></td>

                    <c:forEach items="${provinceList}" var="pItem">
                        <c:if test="${pItem.provinceId eq item.provinceId }">
                                <td>
                                    <fmt:formatNumber maxFractionDigits="0">${pItem.planOrder}</fmt:formatNumber>
                                </td>

                            <td>
                                <fmt:formatNumber maxFractionDigits="0">${pItem.completeOrder}</fmt:formatNumber>
                            </td>
                            <td>
                                <c:if test="${pItem.completeRate eq '' }">
                                    0.00%
                                </c:if>
                                ${pItem.completeRate}
                            </td>
                            <td>
                                <c:if test="${pItem.completeRate72 eq '' }">
                                    0.00%
                                </c:if>
                                ${pItem.completeRate72}
                            </td>
                            <td>
                        <c:if test="${pItem.arrivalCompleteRate48 eq '' }">
                                0.00%
                        </c:if>
                                    ${pItem.arrivalCompleteRate48}
                            </td>

                            <td>
                                <c:if test="${pItem.unArrivalDateCompletedRate72 eq '' }">
                                    0.00%
                                </c:if>
                                    ${pItem.unArrivalDateCompletedRate72}
                            </td>
                            <td>
                                <c:if test="${pItem.unArrivalDateCompletedRateWeek eq '' }">
                                    0.00%
                                </c:if>
                                    ${pItem.unArrivalDateCompletedRateWeek}
                            </td>
                            <td>
                                <c:if test="${pItem.arrivalDateCompletedRate72 eq '' }">
                                    0.00%
                                </c:if>
                                    ${pItem.arrivalDateCompletedRate72}
                            </td>
                            <td>
                                <c:if test="${pItem.arrivalDateCompletedRateWeek eq '' }">
                                    0.00%
                                </c:if>
                                    ${pItem.arrivalDateCompletedRateWeek}
                            </td>

                            <td>
                                    ${pItem.unCompletedOrder}
                            </td>
                    </c:if>
                    </c:forEach>
                </tr>
        </c:if>


                <tr id="${item.cityId}" pId="${item.provinceId}" style="color: green;">
                    <td colspan="2"></td>
                    <td colspan="2">${item.cityName}</td>
                            <td>
                                <fmt:formatNumber maxFractionDigits="0">${item.planOrder}</fmt:formatNumber>
                            </td>

                            <td>
                                <fmt:formatNumber maxFractionDigits="0">${item.completeOrder}</fmt:formatNumber>
                            </td>
                            <td>
                                <c:if test="${item.completeRate eq '' }">
                                    0.00%
                                </c:if>
                                ${item.completeRate}
                            </td>
                            <td>
                                <c:if test="${item.completeRate72 eq '' }">
                                    0.00%
                                </c:if>
                                ${item.completeRate72}
                            </td>
                            <td>
                                <c:if test="${item.arrivalCompleteRate48 eq '' }">
                                    0.00%
                                </c:if>
                                ${item.arrivalCompleteRate48}
                            </td>


                        <td>
                            <c:if test="${item.unArrivalDateCompletedRate72 eq '' }">
                                0.00%
                            </c:if>
                                ${item.unArrivalDateCompletedRate72}
                        </td>
                        <td>
                            <c:if test="${item.unArrivalDateCompletedRateWeek eq '' }">
                                0.00%
                            </c:if>
                                ${item.unArrivalDateCompletedRateWeek}
                        </td>
                        <td>
                            <c:if test="${item.arrivalDateCompletedRate72 eq '' }">
                                0.00%
                            </c:if>
                                ${item.arrivalDateCompletedRate72}
                        </td>
                        <td>
                            <c:if test="${item.arrivalDateCompletedRateWeek eq '' }">
                                0.00%
                            </c:if>
                                ${item.arrivalDateCompletedRateWeek}
                        </td>


                    <td>
                            ${item.unCompletedOrder}
                    </td>
                </tr>
        </c:forEach>
    </c:if>

    </tbody>

    </thead>
</table>
</body>
</html>
