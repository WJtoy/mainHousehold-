<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>客户充值汇总</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <script src="${ctxStatic}/common/moment.min.js" type="text/javascript"></script>

    <script type="text/javascript">
        $(document).ready(function() {
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
            $("th").css({"text-align":"center","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});
            $("#btnSubmit").click(function() {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action","${ctx}/finance/rpt/depositRecharge/depositRechargeDetails");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/finance/rpt/depositRecharge/checkExportDetalis?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/finance/rpt/depositRecharge/exportDetalis?"+ (new Date()).getTime(),
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
</head>

<body>
<ul class="nav nav-tabs">
    <li>
        <a href="${ctx}/finance/rpt/depositRecharge/depositRechargeSummary">质保金汇总</a>
    </li>
    <li class="active"><a href="javascript:void(0);">质保金明细</a></li>
</ul>
<form:form id="searchForm" modelAttribute="rptSearchCondition" action="${ctx}/finance/rpt/depositRecharge/depositRechargeDetails" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <div style="width: 90%">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label class="control-label">服务网点：</label>
        <rpt:servicePointSelector id="servicePoint" name="servicePointId" value="${rptSearchCondition.servicePointId}"
                                  labelName="servicePointName" labelValue="${rptSearchCondition.servicePointName}"
                                  width="900" height="700" noblackList="true" callbackmethod="" cssClass="required"/>

        <label>充值日期：</label>
        <input id="beginDate" name="beginDate" type="text" readonly="readonly" style="width:99px;margin-left:4px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.beginDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>
        <label>~</label>
        &nbsp;&nbsp;&nbsp;
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:98px" maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});"/>

        <label>充值类型:</label>
        <c:set var="actionType" value="${fns:getDictListFromMS('CustomerActionType')}" /><!-- 切换为微服务 -->
        <select id="actionType" name="actionType" style="width:200px;">
            <option value="" selected="selected">所有</option>
            <option value="10" <c:out value="${(rptSearchCondition.actionType eq 10)?'selected=selected':''}" />>线下充值</option>
            <option value="20" <c:out value="${(rptSearchCondition.actionType eq 20)?'selected=selected':''}" />>订单完成扣款</option>
                <%--<c:forEach items="${actionType}" var="Dict">--%>
                <%--<option value="${Dict.value}" <c:out value="${(rptSearchCondition.actionType eq Dict.value)?'selected=selected':''}" />>${Dict.label}</option>--%>
                <%--</c:forEach>--%>
        </select>

        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:finance:depositRecharge:view"><input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/></shiro:hasPermission>
        &nbsp;&nbsp;
        <shiro:hasPermission name="rpt:finance:depositRecharge:export"><input id="btnExport" class="btn btn-primary" type="button" value="导出"/></shiro:hasPermission>
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 250px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：实时数据<br/>
                    统计方式：充值时间<br/>
                    栏位说明：<br/>
                    【合计】=线下充值+订单完成扣款
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>

</form:form>
<sys:message content="${message}"/>

<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table table-striped table-bordered table-condensed table-hover" style="table-layout: fixed;">
        <thead>
        <tr>
            <th width="60">序号</th>
            <th width="200">网点编号</th>
            <th width="200">网点名称</th>
            <th width="120">充值类型</th>
            <th width="160">创建时间</th>
            <th width="160">充值时间</th>
            <th width="120">充值金额</th>
            <th width="120">收入方式</th>
            <th width="200">描述</th>
            <th width="250">相关单号</th>
        </tr>
        </thead>
        <tbody>
        <c:set var="totalCount10" value="0" />
        <c:set var="totalCount20" value="0" />
        <c:set var="totalCount90" value="0" />
        <c:set var="totalCharge10" value="0" />
        <c:set var="totalCharge20" value="0" />
        <c:set var="rowIndex" value="0"/>
        <c:set var="totalCharge" value="0" />
        <c:forEach items="${page.list}" var="item">
            <tr>
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <td>${rowIndex}</td>
            <td>${item.customerCode}</td>
            <td>${item.customerName}</td>
            <td>${item.actionType.label}</td>
            <td><fmt:formatDate value="${item.createDate}" pattern="yyyy-MM-dd HH:mm:ss "/></td>
            <td><fmt:formatDate value="${item.updateDate}" pattern="yyyy-MM-dd HH:mm:ss "/></td>
            <td style="color:blue;"><fmt:formatNumber pattern="0.00" value="${item.amount}"/></td>
            <td>${item.actionType.value==10?"银行":"现金"}</td>
            <td class="autocut"><a href="javascript:" data-toggle="tooltip"  data-tooltip="${item.remarks}">${fns:abbr(item.remarks,40)}</a></td>
            <td>${item.currencyNo}</td>
            <c:set var="totalCharge" value="${totalCharge+item.amount}" />
            <c:if test="${item.actionType.value == 10}">
                <c:set var="totalCount10" value="${totalCount10+1}" />
                <c:set var="totalCharge10" value="${totalCharge10+item.amount}" />
            </c:if>
            <c:if test="${item.actionType.value == 20}">
                <c:set var="totalCount20" value="${totalCount20+1}" />
                <c:set var="totalCharge20" value="${totalCharge20+item.amount}" />
            </c:if>

            <c:if test="${i ne 0}">
                </tr>
            </c:if>


            </tr>
        </c:forEach>
        <c:if test="${page.list.size()>0}">
            <tr>
                <th colspan="10">线下充值：${totalCharge10}元，共${totalCount10}笔，订单完成扣款：${totalCharge20}元，共${totalCount20}笔，合计充值${totalCharge}元</th>
                    <%--<th>合计</th>--%>
                    <%--<th>合计${totalCharge}</th>--%>
                    <%--<th colspan="3"></th>--%>
            </tr>
        </c:if>
        </tbody>
    </table>
    <style type="text/css">
        .autocut {min-width:40px;overflow:hidden;white-space:nowrap;}
    </style>
</div>
<div class="pagination">${page}</div>
</body>
</html>
