<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户好评</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treeview.jsp" %>
    <script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
    <script type="text/javascript">
        var this_index = top.layer.index;
        var clickTag = 0;

        $(document).ready(function() {
            praiseFee();
            $("#inputForm").validate({
                submitHandler: function(form){
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        return false;
                    }
                    var praiseFee = $("#praiseFee").val();
                    if (isNaN(praiseFee) == true || praiseFee =="") {
                        layerError("起始费用不能为空!", "错误提示");
                        return false;
                    }
                    var maxPraiseFee = $("#maxPraiseFee").val();
                    if (isNaN(maxPraiseFee) == true || maxPraiseFee =="") {
                        layerError("上限费用不能为空!", "错误提示");
                        return false;
                    }
                    var bval = parseFloat(praiseFee);
                    var cval = parseFloat(maxPraiseFee);
                    var compareResult = true;
                    if(bval<=0 && cval<0){
                        compareResult =  Math.abs(bval) <= Math.abs(cval);
                    }else {
                        compareResult = bval <= cval;
                    }
                    if (!compareResult) {
                        layerError("起始费用不能大于上限费用。", "错误提示");
                        return false;
                    }

                    var discount = $("#discount").val();
                    if (isNaN(discount) == true || discount =="") {
                        layerError("扣点不能为空!", "错误提示");
                        return false;
                    }

                    var mustFlagCount = $("input[name='mustFlag']:checkbox:checked").length;
                    var visibleFlagCount = $("input[name='visibleFlag']:checkbox:checked").length;
                    if (mustFlagCount ==0 && visibleFlagCount ==0) {
                        layerError("至少要选择一项图片要求!", "错误提示");
                        return false;
                    }

                    var  validPraiseStardFee = true;
                    var  hintInfo = "";
                    $("input[name='praiseStandardFee']").each(function(index){
                        var psFee = $(this).val();
                        var id = $(this).prop("id");
                        var newId = id.replace(/pfee-/g, "");
                        var currentVisibleFlag = $("#visibleFlag-"+newId).is(":checked");
                        var currentMustFlag = $("#mustFlag-"+newId).is(":checked");
                        var currentName = $("#name-"+newId).val();
                        if ((psFee =="" || isNaN(psFee) == true) && (currentVisibleFlag || currentMustFlag)) {
                            validPraiseStardFee = false;
                            hintInfo = hintInfo + currentName + " "
                        }
                    });

                    if (!validPraiseStardFee) {
                        layerError("好评标准中 "+hintInfo+"费用输入不正确,请更正。", "错误提示");
                        return false;
                    }

                    if ($("#contentTableCheck tr").not("tr#-1").length ==0) {
                        layerError("至少需要一笔审核标准!", "错误提示");
                        return false;
                    }

                    var  validCheckDescr = true;
                    $("input[name='checkdescr']").each(function(index){
                        var descr = $(this).val();
                        if (descr =="") {
                            validCheckDescr = false;
                        }
                    });
                    if (!validCheckDescr) {
                        layerError("审核标准中描述不能为空,请更正。", "错误提示");
                        return false;
                    }

                    var  validCheckFee = true;
                    $("input[name='checkfee']").each(function(index){
                        var checkfee = $(this).val();
                        if (checkfee =="" || isNaN(checkfee) == true) {
                            validCheckFee = false;
                        }
                    });
                    if (!validCheckFee) {
                        layerError("审核标准中费用输入不正确,请更正。", "错误提示");
                        return false;
                    }

                    var praiseFeeFlag = $("input[name='praiseFeeFlag']:checked").val();
                    var onlineFlag = $("input[name='onlineFlag']:checked").val();
                    var entity = {};
                    entity['id'] = $("#id").val();
                    entity['customerId'] = $("#customerId").val();
                    entity['praiseFeeFlag'] = praiseFeeFlag;
                    entity['onlineFlag'] = onlineFlag;
                    entity['praiseFee'] = praiseFee;
                    entity['maxPraiseFee'] = maxPraiseFee;
                    entity['discount'] = discount;
                    entity['praiseRequirement'] = $("#praiseRequirement").val();
                    var iSelCount = -1;
                    $("#contentTable tbody tr").each(function(index) {
                        var id = $(this).prop('id');
                        var code =$("#code-"+id).val();
                        var name =$("#name-"+id).val();
                        var sort =$("#sort-"+id).val();
                        var remarks = $("#remarks-"+id).val();
                        var pfee = $("#pfee-"+id).val();
                        var isMustFlagChecked = $("#mustFlag-" + id).is(":checked");
                        var isVisibleChecked = $("#visibleFlag-" + id).is(":checked");
                        if (isMustFlagChecked ==1 || isVisibleChecked == 1) {
                            iSelCount++;
                            entity['praiseStandardItems[' + iSelCount + '].code'] = code;
                            entity['praiseStandardItems[' + iSelCount + '].name'] = name;
                            entity['praiseStandardItems[' + iSelCount + '].sort'] = sort;
                            entity['praiseStandardItems[' + iSelCount + '].mustFlag'] = isMustFlagChecked ? 1 : 0;
                            entity['praiseStandardItems[' + iSelCount + '].visibleFlag'] = isVisibleChecked ? 1 : 0;
                            entity['praiseStandardItems[' + iSelCount + '].remarks'] = remarks;
                            entity['praiseStandardItems[' + iSelCount + '].fee'] = pfee;
                        }
                    });

                    iSelCount = -1;
                    $("#contentTableCheck tr").each(function(index) {
                        var id = $(this).prop('id');
                        var descr =$("#descr-"+id).val();
                        var fee =$("#fee-"+id).val();

                        if (descr != undefined && fee != undefined ) {
                            iSelCount++;
                            entity['checkStandardItems[' + iSelCount + '].description'] = descr;
                            entity['checkStandardItems[' + iSelCount + '].fee'] = fee;
                        }
                    });

                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    $btnSubmit.prop("disabled", true);
                    $.ajax({
                        url:"${ctx}/provider/md/customerPraiseFee/save",
                        type:"POST",
                        data: entity,
                        dataType:"json",
                        success: function(data){
                            //提交后的回调函数
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            if(ajaxLogout(data)){
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                layerMsg("保存成功");
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe){
                                    pframe.repage();
                                }
                                top.layer.close(this_index);//关闭本身
                            }else{
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                layerError(data.message, "错误提示");
                            }
                            return false;
                        },
                        error: function (data)
                        {
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                            //var msg = eval(data);
                        },
                        timeout: 30000               //限制请求的时间，当请求大于30秒后，跳出请求
                    });
                },
                errorContainer: "#messageBox",
                errorPlacement: function(error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });

            $(document).on('change',"#maxPraiseFee",function (e) {
                praiseFee();
            });

            $(document).on('change',"#discount",function (e) {
                praiseFee();
            });
            $("input[name='praiseFeeFlag'][value='${customerPraiseFee.praiseFeeFlag}']").prop("checked",true);
            $("input[name='onlineFlag'][value='${customerPraiseFee.onlineFlag}']").prop("checked",true);
        });

        function praiseFee() {
            var maxPraiseFee = $("#maxPraiseFee").val();
            var discount = $("#discount").val();
            if(maxPraiseFee != ''){
                var kklDiscount = maxPraiseFee * discount /100;
                var masterDiscount = maxPraiseFee * (100 - discount) / 100;
                var msg = "(平台:<font color='red'>" + kklDiscount +"</font>  师傅:<font color='blue'> " + masterDiscount + "</font>)";
                $("#praiseFeeDiscount").html(msg);
            }
        }

        function closeDialog() {
            var pframe = getActiveTabIframe();//定义在jeesite.min.js中
            if (pframe) {
                pframe.repage();
            }
            top.layer.close(this_index);   //关闭本身
        }

        function addRow() {
            var id = Math.ceil(Math.random()*1000);
            var trTemp = $("<tr id='"+id+"'></tr>");
            trTemp.append("<td style='width:600px;'><input type='text' id='descr-"+id +"' name='checkdescr' style='width:602px;' maxlength='100' placeholder='如:有文字有图片有五星/有文字无图片/只有五星'></td>");
            trTemp.append("<td style='width:97px;' ><div class=\"input-append\" style=\"margin-left: 15px;\">\n" +
                "                                <input class=\"span2\" id=\"fee-" + id +"\" name=\"checkfee\" type=\"number\" maxlength=\"4\" min=\"0\" max=\"100\" style=\"width: 50px;\" value=\"0\">\n" +
                "                                <span class=\"add-on\">元</span>\n" +
                "                            </div></td>");

            var length = $("#contentTableCheck tbody").children("tr").length;
            if (length > 1) {
                $("#contentTableCheck tbody").children("tr:eq(0)").find("a").attr("style","display:show;margin-left: 5px");
                var a =$("#contentTableCheck tbody").children("tr:eq(0)").find("a");
                var sid = $("#contentTableCheck tbody").children("tr:eq(0)").attr("id");

                if (a.length < 1) {

                    $("#contentTableCheck tbody").children("tr:eq(0)").append("<td style='width:50px'><a style='margin-left: 5px' onclick='delRow("+sid+")'>删除</a></td>");
                }
                trTemp.append("<td style='width:50px'><a style='margin-left: 5px' onclick='delRow("+id+")'>删除</a></td>");
            }



            $("#contentTableCheck tbody").children("tr:eq(-1)").before(trTemp);
        }

        function delRow(id) {
            var length = $("#contentTableCheck tbody").children("tr").length;
            $('#contentTableCheck #' + id).remove();
            if (length == 3) {
                $("#contentTableCheck tbody").children("tr:eq(0)").find("a").attr("style","display:none");
            }
        }
    </script>

    <style type="text/css">
        .form-horizontal {margin-top: 10px;margin-left: 20px;margin-right: 0px;}
        .form-horizontal .control-label {width: 80px;}
        .form-horizontal .controls {margin-left: 90px;}
        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 0;
            width: 100%;
            height: 60px;
            background: #fff;
            z-index: 10;
            padding-left: 190px;
            border-top: 1px solid #e5e5e5;
        }
    </style>

</head>
<body>

<form:form id="inputForm" modelAttribute="customerPraiseFee" action="${ctx}/provider/md/customerPraiseFee/save" method="post" class="form-horizontal">

    <form:hidden path="id"/>
    <sys:message content="${message}"/>
    <legend>客户费用</legend>
    <div class="row-fluid">
        <div class="span8">
            <div class="control-group">
                <label class="control-label">客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户:</label>
                <div class="controls">
                    <c:choose>
                        <c:when test="${customerPraiseFee.id>0}">
                            <form:input  path="customerName" readonly="true" class="input-medium" style="width:386px;"></form:input>
                            <form:hidden path="customerId"/>
                        </c:when>
                        <c:otherwise>
                            <select id="customerId" name="customerId" class="input-small required selectCustomer" style="width:400px;">
                                <option value="" <c:out value="${(empty customerPraiseFee.id)?'selected=selected':''}" />>请选择</option>
                                <c:forEach items="${fns:getMyCustomerListFromMS()}" var="customer">
                                    <option value="${customer.id}"
                                            <c:out value="${(customerPraiseFee.customerId eq customer.id)?'selected=selected':''}" />>${customer.name}</option>
                                </c:forEach>
                            </select>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
        <div class="span4">
            <div class="control-group">
                <label class="control-label" style="width: 60px;">好评费用:</label>
                <div class="controls" style="margin-left: 70px">
                    <span>
                        <input id="praiseFeeFlagYes" name="praiseFeeFlag" class="required" type="radio" value="1"><label for="praiseFeeFlagYes">有</label>
                    </span>
                    <span>
                        <input id="praiseFeeFlagNo" name="praiseFeeFlag" class="required" type="radio" value="0" style="margin-left:20px"><label for="praiseFeeFlagNo">无</label>
                    </span>
                </div>
            </div>
        </div>
    </div>
    <%--
    <div class="row-fluid">
        <div class="span4">
            <div class="control-group">
                <label class="control-label">起始费用:</label>
                <div class="input-append" style="margin-left: 10px;">
                    <input class="span2 required" id="praiseFee" style="width: 60px;" type="number" maxlength="4" min="0" max="100" value="${customerPraiseFee.praiseFee}">
                    <span class="add-on">元</span>
                </div>
            </div>
        </div>
        <div class="span4">
            <div class="control-group" style="margin-left: 10px;">
                <label class="control-label">上限费用:</label>
                <div class="input-append" style="margin-left: 20px;">
                    <input class="span2 required" id="maxPraiseFee" style="width: 60px;" type="number" maxlength="4" min="0" max="100"
                           value="${customerPraiseFee.maxPraiseFee}" validPraiseFee="[id='praiseFee']">
                    <span class="add-on">元</span>
                </div>
            </div>
        </div>
        <div class="span4">
            <div class="control-group">
                <label class="control-label">师傅提成:</label>
                <div class="input-append" style="margin-left: 10px;">
                    <input class="span2 required" id="discount" style="width: 60px;" type="number" maxlength="4" min="0" max="100" value="${customerPraiseFee.discount}">
                    <span class="add-on">%</span>
                </div>
            </div>
        </div>
    </div>
    --%>
    <div class="row-fluid">
        <div class="span4">
            <div class="control-group">
                <label class="control-label">起始费用:</label>
                <div class="input-append" style="margin-left: 10px;">
                    <input class="span2 required" id="praiseFee" style="width: 60px;" type="number" maxlength="4" min="0" max="100" value="${customerPraiseFee.praiseFee}">
                    <span class="add-on">元</span>
                </div>
            </div>
        </div>
        <div class="span4">
        </div>
        <div class="span4">
            <div class="control-group">
                <label class="control-label" style="width: 60px">平台提成:</label>
                <div class="input-append" style="margin-left: 10px;">
                    <input class="span2 required" id="discount" style="width: 60px;" type="number" maxlength="4" min="0" max="100" title="值为100时师傅没有提成;值为0时提成都归师傅;值为1-99时按100减比值给师傅提成" value="${customerPraiseFee.discount}">
                    <span class="add-on">%</span>
                    <span style="margin-left: 5px;margin-top: 5px;font-size: 10px" id="praiseFeeDiscount"></span>
                </div>

            </div>

        </div>
    </div>

    <div class="row-fluid">
        <div class="span4">
            <div class="control-group">
                <label class="control-label">上限费用:</label>
                <div class="input-append" style="margin-left: 10px;">
                    <input class="span2 required" id="maxPraiseFee" style="width: 60px;" type="number" maxlength="4" min="0" max="100"
                           value="${customerPraiseFee.maxPraiseFee}" validPraiseFee="[id='praiseFee']">
                    <span class="add-on">元</span>
                </div>
            </div>
        </div>
        <div class="span4">
        </div>
        <div class="span4">
            <div class="control-group">
                <label class="control-label" style="width: 60px;">线&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;上:</label>
                <div class="controls" style="margin-left: 70px">
                    <span>
                        <input id="onlineFlagYes" name="onlineFlag" class="required" type="radio" value="1"><label for="onlineFlagYes">是</label>
                    </span>
                    <span>
                        <input id="onlineFlagNo" name="onlineFlag" class="required" type="radio" value="0" style="margin-left:20px"><label for="onlineFlagNo">否</label>
                    </span>
                </div>
            </div>
        </div>
    </div>
    <legend>好评标准</legend>
    <div class="control-group">
        <label class="control-label">照片要求:</label>
        <div class="controls">
            <table id="contentTable" class="table-condensed" style="table-layout:fixed" cellspacing="0" width="50%">
                <tbody>
                <c:forEach items="${customerPraiseFee.praiseStandardItems}" var="item" varStatus="i" begin="0">
                    <c:set var="index" value="${i.index}" />
                    <tr id="${index}">
                        <td style="width:80px;">
                                ${item.name}
                            <input type="hidden" id="code-${index}" value="${item.code}" />
                            <input type="hidden" id="sort-${index}" value="${item.sort}" />
                            <input type="hidden" id="name-${index}" value="${item.name}" />
                        </td>
                        <td style="width:50px;">
                            <label><input type="checkbox" id="visibleFlag-${index}" name="visibleFlag" ${item.visibleFlag==1?'checked':''} />显示</label>
                        </td>
                        <td style="width:50px;">
                            <label> <input type="checkbox" id="mustFlag-${index}" name="mustFlag" ${item.mustFlag==1?'checked':''} />必选</label>
                        </td>
                        <td style="width:400px;">
                            <input type="text" id="remarks-${index}" name="remarks" style="width: 395px;" placeholder="描述" value="${item.remarks}" maxlength="100"/>
                        </td>
                        <td style="width:50px;">
                            <div class="input-append" style="margin-left: 10px;">
                                <input class="span2" id="pfee-${index}" name="praiseStandardFee" type="number" maxlength="4" min="0" max="100"  style="width: 50px;" value="${item.fee}">
                                <span class="add-on">元</span>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    <legend>审核标准</legend>
    <div class="control-group">
        <label class="control-label"><span class="red">*</span>内容要求:</label>

        <div class="controls" style="margin-left: 85px">
            <table id="contentTableCheck" class="table-condensed" style="table-layout:fixed" cellspacing="0" width="50%">
                <c:forEach items="${customerPraiseFee.checkStandardItems}" var="item" varStatus="i" begin="0">
                    <c:set var="index" value="${i.index}" />
                    <tr id="${index}">
                        <td style="width:600px;">
                             <input type="text" id="descr-${index}" name="checkdescr"  value="${item.description}" style="width:602px;" maxlength="100"/>
                        </td>
                        <td style='width:97px;'>
                            <div class="input-append" style="margin-left: 15px;">
                                <input class="span2" id="fee-${index}" name="checkfee" type="number" maxlength="4" min="0" max="100" style="width: 50px;" value="${item.fee}">
                                <span class="add-on">元</span>
                            </div>
                        </td>
                        <td style="width:50px"><a style="margin-left: 5px" onclick="delRow(${index})">删除</a></td>


                    </tr>
                </c:forEach>
                    <tr id="-1">
                        <td >
                            <a  class="btn btn-primary" href="javascript:void(0);" onclick="javascript:addRow();">+添加</a>
                        </td>
                        <td></td>
                        <td></td>
                    </tr>
            </table>
        </div>
    </div>
    <legend>好评要求</legend>
    <div class="control-group">
        <label class="control-label">描&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;述:</label>
        <div class="controls">
            <form:textarea path="praiseRequirement" htmlEscape="false" rows="6" maxlength="199" style="width:710px;" />
        </div>
    </div>
</form:form>
<div style="height: 60px">
    </div>
<shiro:hasPermission name="md:customerpraisefee:edit">

    <div id="editBtn"  class="line-row" style="width: 79%;">

            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" onclick="$('#inputForm').submit();" style="width: 96px;height: 40px;margin-top: 10px;margin-left: 470px"/>
            <input id="btnClose" class="btn " type="button" onclick="javascript:closeDialog();" value="取消" style="margin-left: 20px;width: 96px;height: 40px;margin-top: 10px;"/>

    </div>
</shiro:hasPermission>

<script type="text/javascript">
    <c:if test="${empty customerPraiseFee.id}">
        addRow();
        $("#praiseFeeFlagNo").attr("checked","checked");
        $("#onlineFlagYes").attr("checked","checked");
    </c:if>
</script>
</body>
</html>
