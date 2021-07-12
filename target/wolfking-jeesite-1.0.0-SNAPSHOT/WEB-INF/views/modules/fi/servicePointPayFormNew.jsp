<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>网点付款</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
    <script type="text/javascript">
        function validSearch(){
            if ($("#bank").val()==null || $("#bank").val()==""){
                top.$.jBox.error("请选择银行!", '下游网点付款');
            }
            else{
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").submit();
            }
        }

        function page(n,s){
            $("#pageNo").val(n);
            $("#pageSize").val(s);
            $("#searchForm").attr("action","${ctx}/fi/servicepointwithdraw/payNew");
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
                width  : 1100,
                height  : 440,
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

        var datas = [];
        function setDatas(){
            datas = [];
            $("input:checkbox").each(function(){
                if ($(this).attr("checked")){
                    var temp=$(this).attr("id");
                    if(temp!="selectAll")
                    {
                        temp=$(this).next().attr("value");
                        if(temp!="" && temp!="on")
                        {
                            datas.push($(this).val());
                        }
                    }
                }
            });
        }

        $(document).off('click','#btnPaySelected');//先解除事件绑定
        $(document).on("click", "#btnPaySelected", function () {
            if ($("#btnPaySelected").prop("disabled") == true) {
                return false;
            }

            // $("#btnPaySelected").prop("disabled", true);

            setDatas();
            if (datas.length == 0) {
                top.$.jBox.error('请选择要批量付款的网点', '网点批量付款');
                $("#btnPaySelected").removeAttr("disabled");
                return;
            }

            var payBankHtml = "<div style='padding:20px;'><label class=\"control-label\">付款银行:</label><input type='text' id='txtPayBank' name='txtPayBank' value='招行' /></div>";

            var submit = function (v, h, f) {
                if (f.txtPayBank == '') {
                    $.jBox.tip("请输入付款银行", 'error', { focusId: "txtPayBank" });
                    return false;
                }

                top.$.jBox.tip("批量付款生成中...", "loading");
                var data = {datas:datas.join(","),qPayment:$("#qPayment").val(),qBank:$("#qBank").val(),payBank:f.txtPayBank};
                $.ajax({
                    cache: false,
                    type: "POST",
                    url: "${ctx}/fi/servicepointwithdraw/payselected",
                    data: data,
                    success: function (data) {
                        setTimeout(function() {
                            if (data.success){
                                top.$.jBox.tip("网点批量付款生成成功,请及时确认银行反馈结果", "success");
                                repage();
                            }
                            else{
                                top.$.jBox.error(data.message, '批量付款失败');
                            }
                            $("#btnPaySelected").removeAttr("disabled");
                            top.$.jBox.closeTip();
                        }, 1500);
                    },
                    error: function (xhr, ajaxOptions, thrownError) {
                        $("#btnPaySelected").removeAttr("disabled");
                        top.$.jBox.closeTip();
                        top.$.jBox.error(thrownError.toString(), '批量付款失败');
                    }
                });

                // $("#btnPaySelected").removeAttr("disabled");

                return true;
            };

            // top.$.jBox.confirm('确定要批量付款吗？', '网点批量付款', submit);
            top.$.jBox(payBankHtml, { title: "付款银行", submit: submit});

        });

        function payServicePoint(servicePointId, servicePointNo, servicePointName, phone1, phone2, paymentType, bank, branch, bankNo, bankOwner, bankOwnerIdNo, bankOwnerPhone,
                                 debtsAmount, totalAmount, debtsDesc, bankIssue, invoiceFlag, discountFlag){
            $("#aSave").attr("href", "${ctx}/fi/servicepointwithdraw/paysave?servicePointId="+servicePointId+"&servicePointNo="+encodeURI(encodeURI(servicePointNo))+
                "&servicePointName="+encodeURI(encodeURI(servicePointName))+"&phone1="+phone1+"&phone2="+phone2+
                "&paymentType="+paymentType+"&bank="+bank+"&branch="+encodeURI(encodeURI(branch))+"&bankNo="+bankNo+
                "&bankOwner="+encodeURI(encodeURI(bankOwner))+"&bankOwnerIdNo="+encodeURI(encodeURI(bankOwnerIdNo))+"&bankOwnerPhone="+encodeURI(encodeURI(bankOwnerPhone))+
                "&debtsAmount="+debtsAmount+"&totalAmount="+totalAmount+"&debtsDesc="+encodeURI(encodeURI(debtsDesc))+"&bankIssue="+bankIssue+"&invoiceFlag="+
                invoiceFlag+"&discountFlag="+discountFlag+"&qYear="+$("#qYear").val()+"&qMonth="+$("#qMonth").val());
            $("#aSave").click();
        }

        //结算方式,银行联动
        $(document).on("change", "#payment", function() {
            if ($(this).val()==""){
                top.$.jBox.confirm('请选择结算方式', '下游网点付款');
                $("#bank option").remove();
                $("#s2id_bank").find("span.select2-chosen").html('请选择');
                $("#engineer option").remove();
                $("#s2id_engineer").find("span.select2-chosen").html('所有');
                $("#lblYear").hide();
                $("#s2id_payableYear").hide();
                $("#s2id_payableMonth").hide();
            }
            else{
                $.ajax({
                    type: "GET",
                    url: "${ctx}/fi/servicepointwithdraw/getbanklist?paymenttype="+$(this).val() + "&" + (new Date()).getTime(),
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
                if ($("#payment").val() == 20) {
                    $("#lblYear").hide();
                    $("#s2id_payableYear").hide();
                    $("#s2id_payableMonth").hide();
                }
                if ($("#payment").val() == 10) {
                    $("#lblYear").show();
                    $("#s2id_payableYear").show();
                    $("#s2id_payableMonth").show();
                }
            }
        });

        $(document).on("click", "#btnExport", function () {
            if ($("#bank").val()==null || $("#bank").val()==""){
                top.$.jBox.error("请选择银行!", '网点付款');
            }
            else{

                top.$.jBox.confirm("确认要导出网点待付款明细吗？","系统提示",function(v,h,f){
                    if(v=="ok"){
                        $("#searchForm").attr("action","${ctx}/fi/servicepointwithdraw/payexport");
                        $("#searchForm").submit();
                        $("#searchForm").attr("action","${ctx}/fi/servicepointwithdraw/payNew");
                    }
                },{buttonsFocus:1});
                top.$('.jbox-body .jbox-icon').css('top','55px');
            }
        });

        //银行,安维人员联动
        $(document).on("change", "#bank", function() {
            if ($(this).val()==""){
                top.$.jBox.confirm('请选择银行', '网点付款');
                $("#engineer option").remove();
                $("#s2id_engineer").find("span.select2-chosen").html('所有');
            }
            else{
                $.ajax({
                    type: "GET",
                    url: "${ctx}/fi/servicepointwithdraw/getengineerlist?bank="+$(this).val() + "&paymenttype=" + $("#payment").val() + "&" + (new Date()).getTime(),
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
    <li class="active"><a href="javascript:void(0);">网点付款</a></li>
</ul>
<form:form id="searchForm" action="${ctx}/fi/servicepointwithdraw/payNew" method="post" class="breadcrumb form-search">
    <a id="aSave" type="hidden" href="" class="fancybox"  data-fancybox-type="iframe"></a>
    <input id="qYear" name="qYear" value="${payableYear}" type="hidden"/>
    <input id="qMonth" name="qMonth" value="${payableMonth}" type="hidden"/>
    <input id="qPayment" name="qPayment" value="${payment}" type="hidden"/>
    <input id="qBank" name="qBank" value="${bank}" type="hidden"/>
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
        <label>结算银行：</label>
        <select id="bank" name="bank" style="width:140px;">
            <option value="" selected="selected">请选择</option>
            <c:forEach items="${bankList}" var="b">
                <option value="${b.value}" <c:out value="${(bank eq b.value)?'selected=selected':''}" />>${b.text}</option>
            </c:forEach>
        </select>
        <label>网点：</label>
        <select id="engineer" name="engineer" style="width:180px;">
            <option value="" selected="selected">所有</option>
            <c:forEach items="${engineerList}" var="e">
                <option value="${e.value}" <c:out value="${(engineer eq e.value)?'selected=selected':''}" />>${e.text}</option>
            </c:forEach>
        </select>
        <label>区域：</label>

        <%--<sys:treeselect id="areaId" name="areaId" value="${areaId}" labelName="areaName" labelValue="${areaName}" title="区域"
                        url="/sys/area/treeData" nodesLevel="2" nameLevel="3" cssStyle="width:140px;" allowClear="true" />--%>

        <sys:treeselectareanew id="areaId" name="areaId" value="${areaId}" levelValue=""
                               labelName="areaName" labelValue="${areaName}" title="区域" clearIdValue="0"
                               url="/sys/area/treeDataNew" allowClear="true" nodesLevel="-1" nameLevel="3" cssStyle="width:140px;"/>
    </div>
    <div style="margin-top:8px">
        <label>网点状态：</label>
        <select id="engineerStatus" name="engineerStatus" style="width:140px;">
            <option value="" selected="selected">所有</option>
            <option value="0" <c:out value="${(engineerStatus eq '0')?'selected=selected':''}" />>正常</option>
            <option value="1" <c:out value="${(engineerStatus eq '1')?'selected=selected':''}" />>异常</option>
        </select>
        <label>是否开票：</label>
        <select id="engineerInvoiceFlag" name="engineerInvoiceFlag" style="width:140px;">
            <option value="" selected="selected">所有</option>
            <option value="1" <c:out value="${(engineerInvoiceFlag eq '1')?'selected=selected':''}" />>是</option>
            <option value="0" <c:out value="${(engineerInvoiceFlag eq '0')?'selected=selected':''}" />>否</option>
        </select>
        <label>扣点：</label>
        <select id="engineerDiscountFlag" name="engineerDiscountFlag" style="width:180px;">
            <option value="" selected="selected">所有</option>
            <option value="1" <c:out value="${(engineerDiscountFlag eq '1')?'selected=selected':''}" />>是</option>
            <option value="0" <c:out value="${(engineerDiscountFlag eq '0')?'selected=selected':''}" />>否</option>
        </select>
        <label id="lblYear">月份：</label>
        <select id="payableYear" name="payableYear" style="width:100px;">
            <c:forEach items="${fns:getReportQueryYears()}" var="year">
                <option value="${year}" <c:out value="${(payableYear eq year)?'selected=selected':''}" />>${year}</option>
            </c:forEach>
        </select>
        <select id="payableMonth" name="payableMonth" style="width:75px;">
            <c:forEach var="i" begin="0" end="11" step="1">
                <option value="${i+1}" <c:out value="${(payableMonth eq i+1)?'selected=selected':''}" />>${i+1}</option>
            </c:forEach>
        </select>
        &nbsp;&nbsp;&nbsp;<input id="btnSubmit" class="btn btn-primary" type="button" value="查  询" onclick="validSearch();"/>
        &nbsp;&nbsp;&nbsp;<input id="btnPaySelected" class="btn btn-danger" type="button" value="批量付款"/>
    </div>
</form:form>
<sys:message content="${message}"/>
<c:set var="totalPayable" value="0" />
<c:set var="totalMinusPayable" value="${totalMinus}" />
<c:set var="totalPlatformFee" value="${totalPlatformFee}" />
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
        <th>待支付</th>
        <c:if test="${totalMinusPayable != 0}"><th>待扣款</th></c:if>
        <c:if test="${totalPlatformFee != 0}"><th>平台费</th></c:if>
        <th>应付</th>
        <th>操作</th>
    </tr>
    </thead>
    <tbody>
    <%int i=0; %>
    <c:forEach items="${list}" var="servicePoint">
        <tr>
            <%i++;%>
            <td><input type="checkbox" id="cbox<%=i%>" value="${servicePoint.id};${servicePoint.finance.payableAmount};${servicePoint.finance.paymentType.value};${payableYear};${payableMonth};${servicePoint.finance.minusAmount};${servicePoint.finance.deductedAmount};${servicePoint.finance.platformFee};${servicePoint.bankOwnerIdNo};${servicePoint.bankOwnerPhone}" name="checkedRecords"/></td>
            <td><%=i%></td>
            <td>${servicePoint.finance.paymentType.label}</td><%--切换为微服务--%>
            <td>${servicePoint.servicePointNo}</td>
            <c:choose>
                <c:when test="${servicePoint.finance.bankIssue.value eq '0'}">
                    <td>${servicePoint.name}</td>
                </c:when>
                <c:otherwise>
                    <c:if test="${servicePoint.finance.debtsAmount ne 0}">
                        <td style="background-color: #f2dede;"><a href="javascript:" data-toggle="tooltip"  data-tooltip="${fns:getDictLabelFromMS(servicePoint.finance.bankIssue.value, 'BankIssueType', '')}<%-- 切换为微服务 --%>${','}${servicePoint.finance.debtsDescrption}${':'}${servicePoint.finance.debtsAmount}">${servicePoint.name}</a></td>
                    </c:if>
                    <c:if test="${servicePoint.finance.debtsAmount eq 0}">
                        <td style="background-color: #f2dede;"><a href="javascript:" data-toggle="tooltip"  data-tooltip="${fns:getDictLabelFromMS(servicePoint.finance.bankIssue.value, 'BankIssueType', '')}">${servicePoint.name}</a></td><%-- 切换为微服务 --%>
                    </c:if>
                </c:otherwise>
            </c:choose>
            <td>${servicePoint.primary.name}</td>
            <td>${servicePoint.contactInfo1}</td>
            <td>
                <c:if test="${servicePoint.finance.invoiceFlag == 1}"><span class="label status_Canceled">${fns:getDictLabelFromMS(servicePoint.finance.invoiceFlag, "yes_no","")}</span></c:if><%--切换为微服务--%>
                <c:if test="${servicePoint.finance.invoiceFlag == 0}">${fns:getDictLabelFromMS(servicePoint.finance.invoiceFlag, "yes_no","")}</c:if><%--切换为微服务--%>
            </td>
            <td>
                <c:if test="${servicePoint.finance.discountFlag == 1}"><span class="label status_Canceled">${fns:getDictLabelFromMS(servicePoint.finance.discountFlag, "yes_no","")}</span></c:if><%--切换为微服务--%>
                <c:if test="${servicePoint.finance.discountFlag == 0}">${fns:getDictLabelFromMS(servicePoint.finance.discountFlag, "yes_no","")}</c:if><%--切换为微服务--%>
            </td>
            <td>${servicePoint.finance.bank.label}</td><%--切换为微服务--%>
            <td>${servicePoint.finance.bankOwner}</td>
            <td>${servicePoint.bankOwnerIdNo}</td>
            <td>${servicePoint.finance.bankNo}</td>
            <td>${servicePoint.bankOwnerPhone}</td>
            <td>${servicePoint.finance.payableAmount}</td>
            <c:if test="${totalMinusPayable != 0}"><td style="color:green;"><c:if test="${servicePoint.finance.minusAmount != 0}">${fns:formatNum(servicePoint.finance.minusAmount)}</c:if></td></c:if>
            <c:if test="${totalPlatformFee != 0}"><td>${fns:formatNum(servicePoint.finance.platformFee)}</td></c:if>
            <td style="color:red;"><B>${fns:formatNum(servicePoint.finance.payableAmount + servicePoint.finance.minusAmount + servicePoint.finance.deductedAmount + servicePoint.finance.platformFee)}</B></td>

            <c:set var="totalPayable" value="${totalPayable+servicePoint.finance.payableAmount}" />

            <td>
                <input type="Button" id="${servicePoint.finance.id}" class="btn btn-mini btn-danger" value="付款"
                       onclick="payServicePoint('${servicePoint.finance.id}', '${servicePoint.servicePointNo}', '${servicePoint.name}',
                               '${servicePoint.contactInfo1}','${servicePoint.contactInfo2}', '${servicePoint.finance.paymentType.value}', '${servicePoint.finance.bank.value}',
                               '${servicePoint.finance.branch}', '${servicePoint.finance.bankNo}', '${servicePoint.finance.bankOwner}', '${servicePoint.bankOwnerIdNo}', '${servicePoint.bankOwnerPhone}',
                               '${servicePoint.finance.debtsAmount}', '${servicePoint.finance.payableAmount}',
                               '','${servicePoint.finance.bankIssue.value}','${servicePoint.finance.invoiceFlag}','${servicePoint.finance.discountFlag}')"/>
            </td>
        </tr>
    </c:forEach>
    <tr>
        <td style="text-align:right;" colspan="14" ><B>合计</B></td>
        <td><B>${fns:formatNum(totalPayable)}</B></td>
        <c:if test="${totalMinusPayable != 0}"><td style="color:green;"><B>${fns:formatNum(totalMinusPayable)}</B></td></c:if>
        <c:if test="${totalPlatformFee != 0}"><td><B>${fns:formatNum(totalPlatformFee)}</B></td></c:if>
        <td style="color:red;"><B>${fns:formatNum(totalPayable + totalMinusPayable + totalDeductedAmount + totalPlatformFee)}</B></td>
        <td></td>
    </tr>
    </tbody>
</table>
</body>
</html>
