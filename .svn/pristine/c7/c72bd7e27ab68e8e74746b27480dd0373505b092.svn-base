<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>开发均单费用</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery.supertable/superTables.css" type="text/css" rel="stylesheet"/>
    <script src="${ctxStatic}/jquery.supertable/jquery.superTable.js" type="text/javascript"></script>
    <style type="text/css">
        a:LINK { /**连接文字本身的颜色**/
            color: #333333
        }

        a:VISITED { /**连接文字被点击后的颜色**/
            color: #333333;
        }

        a:HOVER { /**鼠标移到连接文字上，文字的颜色**/
            color: #0000ff;
            text-decoration: underline;
        }

        .table thead th, .table tbody td {
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
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
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
            $("th").css({"text-align": "center", "vertical-align": "middle"});
            $("td").css({"vertical-align": "middle"});
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity: 'east'});

            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action", "${ctx}/rpt/provider/gradedOrder/developAverageOrderFeeRpt");
                $("#searchForm").submit();
            });

            $("#btnExport").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#btnExport").prop("disabled", true);
                $.ajax({
                    type: "POST",
                    url: "${ctx}/rpt/provider/gradedOrder/developAverageCheckExportTask?"+ (new Date()).getTime(),
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
                                        url: "${ctx}/rpt/provider/gradedOrder/developAverageFeeExport?"+ (new Date()).getTime(),
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
    <li class="active"><a href="javascript:void(0);">开发均单费用</a></li>
</ul>
<c:set var="currentuser" value="${fns:getUser() }"/>
<form:form id="searchForm" modelAttribute="rptSearchCondition"
           action="${ctx}/rpt/provider/gradedOrder/developAverageOrderFeeRpt" method="post" class="breadcrumb form-search">

    <div style="width: 90%">
        <input type="hidden" name="isSearching" value="${rptSearchCondition.isSearchingYes}"/>
        <label>客评时间：</label>
        <input id="endDate" name="endDate" type="text" readonly="readonly" style="width:99px;margin-left:4px"
               maxlength="20" class="input-small Wdate"
               value="<fmt:formatDate value='${rptSearchCondition.endDate}' pattern='yyyy-MM-dd' type='date'/>"
               onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false,maxDate:'${rptSearchCondition.beginDate}'});"/>
        &nbsp;&nbsp;
        <label>服务品类：</label>
        <select id="productCategory" name="productCategory" class="input-small" style="width:125px;">
            <option value="0" <c:out
                    value="${(empty rptSearchCondition.productCategory)?'selected=selected':''}"/>>
                所有
            </option>
            <c:forEach items="${productCategoryList}" var="dict">
                <option value="${dict.id}" <c:out
                        value="${(rptSearchCondition.productCategory eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
            </c:forEach>
        </select>
        &nbsp; &nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="button" value="查询"/>
        &nbsp;&nbsp;
        <input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
    </div>
    <div style="position: absolute;top: 54px;right:5px;width: 100px;height: 30px">
        <div style="position: relative" class="parent">
            <div style="color:#808695;font-size: 14px;float: right;" class="triggle triggle1">
                报表说明 <img src="${ctxStatic}/images/rpt/interrogation.png">
            </div>
            <div style="text-align:right;position: absolute;width: 250px;height: 100px;right:1px;top:31px;" class="target">
                <div style="text-align:left;background-color: #1B1E24;border-radius: 5px;opacity: 0.8;padding: 10px;font-size:14px;color:white;min-width:100px;max-width:400px;">
                    数据时效：隔天数据 <br/>
                    统计方式：客评时间<br/>
                    栏位说明：<br/>
                    【使用费用】远程费用+其他费用<br/>
                    【均单费用】使用费用/当日处理完成<br/>
                    【96小时完成率】96小时完成/当日处理完成*100%<br/>
                </div>
            </div>
            <div style="position:absolute;right: 4px;bottom: -10px " class="border border1">
            </div>
        </div>
    </div>
</form:form>
<sys:message content="${message}"/>

<div id="divGrid" style="overflow: auto;">
    <table id="contentTable" class="table  table-bordered table-condensed"
           style="table-layout:fixed; margin-top: 0px;border-top-width: 0px;">
        <thead>
            <th width="70">序号</th>
            <th width="140">日期</th>
            <th width="140">部门</th>
            <th width="120">人员</th>
            <th width="140">当日处理完成</th>
            <th width="140">使用费用</th>
            <th width="140">均单费用</th>
            <th width="140">96小时完成</th>
            <th width="140">96小时完成率</th>
        </tr>
        </thead>
        <tbody>

        <c:set var="totalmakeCharge"/>
        <c:set var="totalCount"/>
        <c:set var="total96Count"/>

        <c:set var="rowIndex" value="0"/>
        <c:forEach items="${list}" var="item">
            <c:set var="rowIndex" value="${rowIndex+1}"/>
            <c:choose>
            <c:when test="${rowIndex < list.size()}">
            <tr>

            <td>${rowIndex}</td>
            <td><fmt:formatDate value="${item.closeDateDt}"
                                                             pattern="yyyy-MM-dd"/></td>
            <td>${item.productCategoryNames}</td>
            <td>${item.crushCreateByName}</td>
            <td>${item.completedQty}</td>
            <td><fmt:formatNumber pattern="0.00">${item.makeCharge}</fmt:formatNumber></td>
            <td><fmt:formatNumber pattern="0.00">${item.averageCharge}</fmt:formatNumber></td>
            <td>${item.order96HourCompletedQty}</td>
            <td>${item.order96HourCompletedRate}%</td>
            </tr>
            </c:when>
                <c:otherwise>
                <tr style="color: blue;">
                    <td></td>
                    <td colspan="2"></td>
                    <td>合计：</td>
                    <td>${item.completedQty}</td>
                    <td><fmt:formatNumber pattern="0.00">${item.makeCharge}</fmt:formatNumber></td>
                    <td><fmt:formatNumber pattern="0.00">${item.averageCharge}</fmt:formatNumber></td>
                    <td>${item.order96HourCompletedQty}</td>
                    <td>${item.order96HourCompletedRate}%</td>

                </tr>
            </c:otherwise>
            </c:choose>
        </c:forEach>


        </tbody>
    </table>

</div>
</body>
</html>
