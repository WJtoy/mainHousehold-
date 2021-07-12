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
    <script type="text/javascript">
        function validSearch(){
            if ($("#bank").val()==null || $("#bank").val()==""){
                top.$.jBox.error("请选择银行!", '下游网点付款确认');
            }
            else{
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").submit();
            }
        }

        function page(n,s){
            $("#pageNo").val(n);
            $("#pageSize").val(s);
            $("#searchForm").attr("action","${ctx}/fi/servicepointinvoice/confirm/new");
            $("#searchForm").submit();
            return false;
        }
        function repage(){
            $("#searchForm").submit();
            return false;
        }
        $(document).ready(function() {
            $("#aSave").fancybox({
                fitToView : false,
                width  : 700,
                height  : 330,
                autoSize : false,
                closeClick : false,
                type  : 'iframe',
                openEffect : 'none',
                closeEffect : 'none'
            });
            $('a[data-toggle=tooltip]').darkTooltip();
            $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});

            if ($("#payment").val() == 10) {
                $("#lblYear").show();
                $("#s2id_payableYear").show();
                $("#s2id_payableMonth").show();
            } else {
                $("#lblYear").hide();
                $("#s2id_payableYear").hide();
                $("#s2id_payableMonth").hide();
            }

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

        var ids = [];
        function setIds(){
            ids = [];
            $("input:checkbox").each(function(){
                if ($(this).attr("checked")){
                    var temp=$(this).attr("id");
                    if(temp!="selectAll")
                    {
                        temp=$(this).next().attr("value");
                        if(temp!="" && temp!="on")
                        {
                            ids.push($(this).val());
                        }
                    }
                }
            });
        }

        $(document).off('click','#btnConfirmSelected');//先解除事件绑定
        $(document).on("click", "#btnConfirmSelected", function () {
            if ($("#btnConfirmSelected").prop("disabled") == true) {
                return false;
            }

            $("#btnConfirmSelected").prop("disabled", true);

            setIds();
            if (ids.length == 0) {
                top.$.jBox.error('请选择要批量要付款确认的网点', '网点批量付款确认');
                $("#btnConfirmSelected").removeAttr("disabled");
                return;
            }

            var submit = function (v, h, f) {
                if (v == 'ok') {
                    top.$.jBox.tip("批量付款确认中...", "loading");
                    var data = {ids:ids.join(",")};
                    $.ajax({
                        cache: false,
                        type: "POST",
                        url: "${ctx}/fi/servicepointinvoice/confirm/selected",
                        data: data,
                        success: function (data) {
                            setTimeout(function() {
                                if (data.success){
                                    top.$.jBox.tip("网点批量付款确认成功", "success");
                                    repage();
                                }
                                else{
                                    top.$.jBox.error(data.message, '批量付款确认失败');
                                }
                                $("#btnConfirmSelected").removeAttr("disabled");
                                top.$.jBox.closeTip();
                            }, 1500);
                        },
                        error: function (xhr, ajaxOptions, thrownError) {
                            $("#btnConfirmSelected").removeAttr("disabled");
                            top.$.jBox.closeTip();
                            top.$.jBox.error(thrownError.toString(), '批量付款确认失败');
                        }
                    });
                }
                else if (v == 'cancel') {
                    $("#btnConfirmSelected").removeAttr("disabled");
                }

                return true; //close
            };

            top.$.jBox.confirm('确定要批量付款确认吗？', '网点批量付款确认', submit);

        });

        function confirmFail(withdrawId, servicePointId){
            $("#aSave").attr("href", "${ctx}/fi/servicepointinvoice/confirm/fail?withdrawId="+withdrawId+"&servicePointId="+servicePointId);
            $("#aSave").click();
        }

        function confirmEdit(withdrawId){
            $("#aSave").attr("href", "${ctx}/fi/servicepointinvoice/confirm/edit?withdrawId="+withdrawId);
            $("#aSave").click();
        }

        //结算方式,银行联动
        $(document).on("change", "#payment", function() {
            if ($(this).val()==""){
                top.$.jBox.confirm('请选择结算方式', '下游网点付款确认');
                $("#bank option").remove();
                $("#s2id_bank").find("span.select2-chosen").html('请选择');
                $("#engineer option").remove();
                $("#s2id_engineer").find("span.select2-chosen").html('所有');
            }
            else{
                $.ajax({
                    type: "GET",
                    url: "${ctx}/fi/servicepointinvoice/confirm/getbanklist?paymentType="+$(this).val() + "&" + (new Date()).getTime(),
                    data:"",
                    async: false,
                    success: function (data) {
                        if (data.success){
                            $("#bank option").remove();
                            $("#s2id_bank").find("span.select2-chosen").html('请选择');
                            $("#engineer option").remove();
                            $("#s2id_engineer").find("span.select2-chosen").html('所有');
                            var option = document.createElement("option");
                            option.text =  "请选择";
                            option.value =  "";
                            $("#bank")[0].options.add(option);
                            $.each(data.data, function(i, item) {
                                var option = document.createElement("option");
                                option.text =  item.text;
                                option.value =  item.value;
                                $("#bank")[0].options.add(option);
                            });
                            $("#bank option:nth-child(1)").attr("selected","selected");
                            //                        $("#s2id_bank").find("span.select2-chosen").html('请选择');
                        }
                        else{
                            top.$.jBox.closeTip();
                            top.$.jBox.error(data.message);
                        }
                    },
                    error: function (xhr, ajaxOptions, thrownError) {
                        top.$.jBox.closeTip();
                        top.$.jBox.error(thrownError.toString());
                    }
                });
            }
        });

        $(document).off('click','#btnExport');//先解除事件绑定
        $(document).on("click", "#btnExport", function () {
            if ($("#btnExport").prop("disabled") == true) {
                return false;
            }

            $("#btnExport").prop("disabled", true);

            if ($("#bank").val()==null || $("#bank").val()==""){
                top.$.jBox.error("请选择银行!", '网点付款确认');
                $("#btnExport").removeAttr("disabled");
            }
            else{

                top.$.jBox.confirm("确认要导出网点付款明细吗？","系统提示",function(v,h,f){
                    if(v=="ok"){
                        $("#searchForm").attr("action","${ctx}/fi/servicepointinvoice/confirm/exportNew");
                        $("#searchForm").submit();
                        $("#searchForm").attr("action","${ctx}/fi/servicepointinvoice/confirm/new");
                    }

                    $("#btnExport").removeAttr("disabled");

                },{buttonsFocus:1});
                top.$('.jbox-body .jbox-icon').css('top','55px');
            }
        });

        $(document).off('click','#btnExportGD');//先解除事件绑定
        $(document).on("click", "#btnExportGD", function () {
            if ($("#btnExportGD").prop("disabled") == true) {
                return false;
            }

            $("#btnExportGD").prop("disabled", true);

            if ($("#bank").val()==null || $("#bank").val()==""){
                top.$.jBox.error("请选择银行!", '网点付款确认');
                $("#btnExportGD").removeAttr("disabled");
            }
            else{

                top.$.jBox.confirm("确认要导出网点付款明细吗？","系统提示",function(v,h,f){
                    if(v=="ok"){
                        $("#searchForm").attr("action","${ctx}/fi/servicepointinvoice/confirm/exportNewGD");
                        $("#searchForm").submit();
                        $("#searchForm").attr("action","${ctx}/fi/servicepointinvoice/confirm/new");
                    }

                    $("#btnExportGD").removeAttr("disabled");

                },{buttonsFocus:1});
                top.$('.jbox-body .jbox-icon').css('top','55px');
            }
        });

        //银行,安维人员联动
        $(document).on("change", "#bank", function() {
            if ($(this).val()==""){
                top.$.jBox.confirm('请选择银行', '网点付款确认');
                $("#engineer option").remove();
                $("#s2id_engineer").find("span.select2-chosen").html('所有');
            }
            else{
                $.ajax({
                    type: "GET",
                    url: "${ctx}/fi/servicepointinvoice/confirm/getengineerlist?bank="+$(this).val() + "&paymentType=" + $("#payment").val() + "&" + (new Date()).getTime(),
                    data:"",
                    async: false,
                    success: function (data) {
                        if (data.success){
                            $("#engineer option").remove();
                            $("#s2id_engineer").find("span.select2-chosen").html('所有');
                            var option = document.createElement("option");
                            option.text =  "所有";
                            option.value =  "";
                            $("#engineer")[0].options.add(option);
                            $.each(data.data, function(i, item) {
                                var option = document.createElement("option");
                                option.text =  item.text;
                                option.value =  item.value;
                                $("#engineer")[0].options.add(option);
                            });
                        }
                        else{
                            top.$.jBox.closeTip();
                            top.$.jBox.error(data.message);
                        }
                    },
                    error: function (xhr, ajaxOptions, thrownError) {
                        top.$.jBox.closeTip();
                        top.$.jBox.error(thrownError.toString());
                    }
                });
            }
        });
    </script>
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">网点付款确认</a></li>
</ul>
<form:form id="searchForm" action="${ctx}/fi/servicepointinvoice/confirm/new" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
    <a id="aSave" type="hidden" href="" class="fancybox"  data-fancybox-type="iframe"></a>
    <div>
        <c:set var="paymentTypeList" value="${paymentTypeList}" />
        <label>结算方式：</label>
        <select id="payment" name="payment" style="width:140px;">
            <option value="" selected="selected">请选择</option>
            <c:forEach items="${paymentTypeList}" var="paymentType">
                <option value="${paymentType.value}" <c:out value="${(payment eq paymentType.value)?'selected=selected':''}" />>${paymentType.label}</option><%--切换为微服务--%>
            </c:forEach>
        </select>
        <c:set var="bankList" value="${bankList}" />
        <label>银行：</label>
        <select id="bank" name="bank" style="width:140px;">
            <option value="" selected="selected">请选择</option>
            <c:forEach items="${bankList}" var="b">
                <option value="${b.value}" <c:out value="${(bank eq b.value)?'selected=selected':''}" />>${b.text}</option>
            </c:forEach>
        </select>
        <label>服务网点：</label>
        <select id="engineer" name="engineer" style="width:180px;">
            <option value="" selected="selected">所有</option>
            <c:forEach items="${engineerList}" var="e">
                <option value="${e.value}" <c:out value="${(engineer eq e.value)?'selected=selected':''}" />>${e.text}</option>
            </c:forEach>
        </select>
        <label>网点状态：</label>
        <select id="engineerStatus" name="engineerStatus" style="width:140px;">
            <option value="" selected="selected">所有</option>
            <option value="0" <c:out value="${(engineerStatus eq '0')?'selected=selected':''}" />>正常</option>
            <option value="1" <c:out value="${(engineerStatus eq '1')?'selected=selected':''}" />>异常</option>
        </select>
        <label>是否开票：</label>
        <select id="engineerInvoiceFlag" name="engineerInvoiceFlag" style="width:80px;">
            <option value="" selected="selected">所有</option>
            <option value="1" <c:out value="${(engineerInvoiceFlag eq '1')?'selected=selected':''}" />>是</option>
            <option value="0" <c:out value="${(engineerInvoiceFlag eq '0')?'selected=selected':''}" />>否</option>
        </select>
        <label>是否扣点：</label>
        <select id="engineerDiscountFlag" name="engineerDiscountFlag" style="width:80px;">
            <option value="" selected="selected">所有</option>
            <option value="1" <c:out value="${(engineerDiscountFlag eq '1')?'selected=selected':''}" />>是</option>
            <option value="0" <c:out value="${(engineerDiscountFlag eq '0')?'selected=selected':''}" />>否</option>
        </select>
        &nbsp;&nbsp;<input id="btnSubmit" class="btn btn-primary" type="button" value="查    询" onclick="validSearch();"/>
        <div class="btn-group">
            <a class="btn btn-primary dropdown-toggle" data-toggle="dropdown" href="#">导    出<span class="caret"></span>
            </a>
            <ul class="dropdown-menu">
                <li>
                    <a id="btnExport">导出银行格式</a>
                </li>
                <li>
                    <a id="btnExportGD">导出高灯格式</a>
                </li>
            </ul>
        </div>
    </div>
    <div style="margin-top:8px">
        <label>操作日期：</label>
        <input id="createBeginDate" name="createBeginDate" type="text" readonly="readonly" style="width:125px;" maxlength="20" class="input-small Wdate"
               value="${createBeginDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
        <label>　~　</label>&nbsp;&nbsp;&nbsp;<input id="createEndDate" name="createEndDate" type="text" readonly="readonly" style="width:127px;" maxlength="20" class="input-small Wdate"
                                                   value="${createEndDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
        <label>付款单号：</label>
        <input type = "text" id="withdrawNo" name="withdrawNo" value="${withdrawNo}" maxlength="30" class="input-small" style="width:166px;"/>
        <label>付款日期：</label>
        <input id="payBeginDate" name="payBeginDate" type="text" readonly="readonly" style="width:123px; maxlength:20" class="input-small Wdate"
               value="${payBeginDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
        <label>~　</label><input id="payEndDate" name="payEndDate" type="text" readonly="readonly" style="width:118px" maxlength="20" class="input-small Wdate"
                                value="${payEndDate}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
        <label>付款状态：</label>
        <select id="withdrawStatus" name="withdrawStatus" class="input-small" style="width:80px;">
            <option value="" <c:out value="${(empty withdrawStatus)?'selected=selected':''}" />>所有</option>
            <c:forEach items="${fns:getDictExceptListFromMS('ServicePointWithdrawStatus', '10')}" var="dict"><%--切换为微服务--%>
                <option value="${dict.value}" <c:out value="${(withdrawStatus eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;<input id="btnConfirmSelected" class="btn btn-success" type="button" value="付款成功"/>
    </div>
</form:form>
<sys:message content="${message}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="40"><input type="checkbox" id="selectAll" name="selectAll"/></th>
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
        <th>身份证号</th>
        <th>银行帐号</th>
        <th>预留号码</th>
        <th>金额</th>
        <th>操作日期</th>
        <th>描述</th>
        <th>付款日期</th>
        <th>状态</th>
        <th>操作</th>
    </tr>
    </thead>
    <tbody>
    <%int i=0; %>
    <c:set var="totalApplyAmount" value="0" />
    <c:forEach items="${list}" var="wd">
        <tr>
            <c:set var="index" value="${index+1}" />
            <td>
                <c:if test="${wd.status eq 20}">
                    <input type="checkbox" id="cbox<%=i%>" value="${wd.id}" name="checkedRecords"/>
                </c:if>
            </td>
            <td>${index+(page.pageNo-1)*page.pageSize}</td>
            <td>${wd.paymentTypeName}</td><%--切换为微服务--%>
            <td>${wd.servicePoint.servicePointNo}</td>
            <c:choose>
                <c:when test="${wd.servicePoint.finance.bankIssue.value eq '0'}">
                    <td>${wd.servicePoint.name}</td>
                </c:when>
                <c:otherwise>
                    <td style="background-color: #f2dede;"><a href="javascript:" data-toggle="tooltip"  data-tooltip="${fns:getDictLabelFromMS(wd.servicePoint.finance.bankIssue.value, 'BankIssueType', '')}">${wd.servicePoint.name}</a></td><%-- 切换为微服务--%>
                </c:otherwise>
            </c:choose>
            <td>${wd.servicePoint.primary.name}</td>
            <td>${wd.servicePoint.contactInfo1}</td>
            <td>
                <c:if test="${wd.servicePoint.finance.invoiceFlag == 1}"><span class="label status_Canceled">${fns:getDictLabelFromMS(wd.servicePoint.finance.invoiceFlag, "yes_no","")}</span></c:if><%--切换为微服务--%>
                <c:if test="${wd.servicePoint.finance.invoiceFlag == 0}">${fns:getDictLabelFromMS(wd.servicePoint.finance.invoiceFlag, "yes_no","")}</c:if><%--切换为微服务--%>
            </td>
            <td>
                <c:if test="${wd.servicePoint.finance.discountFlag == 1}"><span class="label status_Canceled">${fns:getDictLabelFromMS(wd.servicePoint.finance.discountFlag, "yes_no","")}</span></c:if><%--切换为微服务--%>
                <c:if test="${wd.servicePoint.finance.discountFlag == 0}">${fns:getDictLabelFromMS(wd.servicePoint.finance.discountFlag, "yes_no","")}</c:if><%--切换为微服务--%>
            </td>
            <td>${wd.bankName}</td><%--切换为微服务--%>
            <td>${wd.bankOwner}</td>
            <td>${wd.bankOwnerIdNo}</td>
            <td>${wd.bankNo}</td>
            <td>${wd.bankOwnerPhone}</td>
            <td>${fns:formatNum(wd.applyAmount)}</td>
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
        <td style="text-align:right;" colspan="14" ><B>合计</B></td>
        <td style="color:red;"><B>${fns:formatNum(totalApplyAmount)}</B>
        </td>
        <td colspan="11" colspan="5" ></td>
    </tr>
    </tbody>
</table>
<%--<div class="pagination">${page}</div>--%>
</body>
</html>
