<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>故障信息</title>
    <meta name="decorator" content="default" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <script src="${ctxStatic}/js/ajaxfileupload.js"></script>
    <c:set var="currentuser" value="${fns:getUser()}"/>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center !important;
            vertical-align: middle;
            BackColor: Transparent;
            height: 30px;
        }
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
        .row-fluid .span4 {
            width: 38%;
        }
    </style>
    <script type="text/javascript">

        var this_index = top.layer.index;
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }
        var clickTag = 0;
        $(document).ready(function() {
            if($("#customerId").val()!=null && $("#customerId").val()!=''){
                if($("#customerProductTypeId").val() == null || $("#customerProductTypeId").val() == ''){
                    getCustomerProductTypeList();
                }
            }
            $("#inputForm").validate({

                submitHandler: function(form){
                    var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");

                    if(clickTag == 1){
                        return false;
                    }

                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    clickTag = 1;

                    var customerId = $("#customerId").val();
                    if(customerId == null || customerId == ''){
                        clickTag = 0;
                        layerMsg("请选择客户");
                        return false;
                    }

                    var customerProductTypeId = $("#customerProductTypeId").val();
                    if(customerProductTypeId == null || customerProductTypeId == ''){
                        clickTag = 0;
                        layerMsg("请选择客户产品分类");
                        return false;
                    }
                    var errorTypeName = $("#errorTypeName").val();
                    if(errorTypeName == null || errorTypeName == ''){
                        clickTag = 0;
                        layerMsg("请输入故障分类");
                        return false;
                    }
                    var errorAppearanceName = $("#errorAppearanceName").val();
                    if(errorAppearanceName == null || errorAppearanceName == ''){
                        clickTag = 0;
                        layerMsg("请输入故障现象");
                        return false;
                    }

                    var entity = {};
                    entity['customerId'] = customerId;
                    entity['customerProductTypeId'] = customerProductTypeId;
                    entity['errorTypeName'] = errorTypeName;
                    entity['errorTypeCode'] = $("#errorTypeCode").val();
                    entity['errorAppearanceName'] = errorAppearanceName;
                    entity['errorAppearanceCode'] = $("#errorAppearanceCode").val();
                    entity['newFlag'] = $("#newFlag").val();
                    $("#customerAction tr").each(function(i,element){
                        entity['customerActionDtoList['+i+'].id'] = $("#id_" + i + "").val();
                        entity['customerActionDtoList['+i+'].errorAnalysisName'] = $("#errorAnalysisName_" + i + "").val();
                        entity['customerActionDtoList['+i+'].errorProcess'] = $("#errorProcess_" + i + "").val();
                        entity['customerActionDtoList['+i+'].serviceLevel'] = $("#serviceLevel_" + i + "").val();
                    });

                    $btnSubmit.prop("disabled", true);
                    $.ajax({
                        url:"${ctx}/md/customerProductType/ajax/save",
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
                                top.layer.close(this_index);//关闭本身
                                layerMsg(data.message);
                                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                                if(pframe!=undefined){
                                    pframe.document.location="${ctx}/md/customerProductType/customerActionList?customerId="+customerId;
                                }

                            }else{
                                setTimeout(function () {
                                    clickTag = 0;
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                top.layer.close(loadingIndex);
                                layerError(data.message, "提示");
                            }
                            return false;
                        },
                        error: function (data) {
                            if(loadingIndex) {
                                layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            top.layer.close(loadingIndex);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                            //var msg = eval(data);
                            top.layer.close(loadingIndex);
                        },
                        timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                    });
                },
                errorContainer : "#messageBox",
                errorPlacement : function(error, element)
                {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox")
                        || element.is(":radio")
                        || element.parent().is(
                            ".input-append"))
                    {
                        error.appendTo(element.parent()
                            .parent());
                    } else
                    {
                        error.insertAfter(element);
                    }
                }

            });


            $(document).on('change',"#customerId",function (e) {
                getCustomerActionList();
                getCustomerProductTypeList();
            });


        });
        $(document).on('change',"[name=errorAnalysisName]",function (e) {
            var errorAnalysisName = $(this).val();
            var currentId = $(this).attr("id");
            var ter = 0;
            var idStr = "#"+currentId;
            $("[name=errorAnalysisName]").not($(idStr)[0]).each(function(){
                if (errorAnalysisName !='') {
                    if(errorAnalysisName == $(this).val()){
                        layerMsg('该故障分析已存在！');
                        ter = 1;
                    }
                }
            });
            if(ter == 1){
                $(this).val("");
            }

        });
        $(document).on('change',"#customerProductTypeId",function (e) {
            getCustomerActionList();
        });

        $(document).on('change',"#errorTypeName",function (e) {
            getCustomerActionList();
        });

        $(document).on('change',"#errorAppearanceName",function (e) {
            getCustomerActionList();
        });
        function getCustomerProductTypeList() {
            var customerId = $("#customerId").val();
            if (customerId !='') {
                $.ajax({
                    url: "${ctx}/md/customerProductType/ajax/getCustomerProductTypeList",
                    data: {customerId: customerId},
                    success:function (e) {
                        if(e.success){
                            $("#customerProductTypeId").empty();
                            var programme_sel=[];
                            programme_sel.push('<option value="" selected="selected">请选择</option>')
                            for(var i=0,len = e.data.length;i<len;i++){
                                var programme = e.data[i];
                                programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                            }
                            $("#customerProductTypeId").append(programme_sel.join(' '));
                            $("#customerProductTypeId").val("");
                            $("#customerProductTypeId").change();
                        }else {
                            $("#customerProductTypeId").html('<option value="" selected>请选择</option>');
                            layerMsg('该客户还没有配置产品分类！');
                        }
                    },
                    error:function (e) {
                        layerError("请求产品失败","错误提示");
                    }
                });
            }
        }
        function addRow() {
            var size = $("#customerAction tr").length;
            var index = size + 1;
            var trTemp = $("<tr id='trId_"+size +"'></tr>");
            trTemp.append("<td style='text-align: center !important;vertical-align: middle;height: 30px;'>"+ index +"</td>");
            trTemp.append('<td style=\'text-align: center !important;vertical-align: middle;height: 30px;\'><input type="text" name="errorAnalysisName" style="width: 375px" id="errorAnalysisName_'+size+'" value="" placeholder="例：水泵故障" ></td>');
            trTemp.append('<td style=\'text-align: center !important;vertical-align: middle;height: 30px;\'><input type="text" id="errorProcess_'+size+'"  style="width: 375px" value="" placeholder="例：更换直流水泵（售后）"></td>');
            trTemp.append('<td style=\'text-align: center !important;vertical-align: middle;height: 30px;\'><input type="text" style="width: 150px" id="serviceLevel_'+size+'" value="" placeholder="例：大修，中修"></td>');
            trTemp.append('<td style=\'text-align: center !important;vertical-align: middle;height: 30px;\'><a href="javascript:deleteAction('+null+','+ size +')">删除</a></td>');
            if(size < 1){
                $("#customerAction").append(trTemp);
            }else {
                $("#customerAction").children("tr:eq(-1)").after(trTemp);
            }

        }

        function getCustomerActionList() {
            var customerId = $("#customerId").val();
            var customerProductTypeId = $("#customerProductTypeId").val();
            var errorTypeName = $("#errorTypeName").val();
            var errorAppearanceName = $("#errorAppearanceName").val();

            $.ajax({
                url: "${ctx}/md/customerProductType/ajax/getCustomerActionList",
                data: {customerId: customerId,customerProductTypeId:customerProductTypeId,errorTypeName:errorTypeName,errorAppearanceName:errorAppearanceName},
                success:function (e) {
                    if(e.success){
                        var customerAction_sel=[];
                        for(var i=0,len=e.data.customerActionDtoList.length;i<len;i++){
                            var programme = e.data.customerActionDtoList[i];
                            var index = i + 1;
                            customerAction_sel.push('<tr id="trId_'+i+'"><td>'+index+'</td><td><input type="text" style="width: 375px" name="errorAnalysisName" id="errorAnalysisName_'+i+'" readonly="readonly" value="'+programme.errorAnalysisName+'" ><input type="hidden" id="id_'+i+'" value="'+programme.id+'"></td><td><input type="text" id="errorProcess_'+i+'"  style="width: 375px" readonly="readonly" value="'+programme.errorProcess+'"></td><td><input type="text" style="width: 150px" id="serviceLevel_'+i+'" readonly="readonly" value="'+programme.serviceLevel+'"></td><td><a href="javascript:editCustomerAction('+ i+')">修改</a>&nbsp;&nbsp;<a href="javascript:deleteAction('+programme.id+','+ i+')" onclick="return confirmx(\'确认要删除吗？\', this.href)">删除</a></td></tr>');

                        }
                        $("#errorTypeCode").val(e.data.errorTypeCode);
                        $("#errorAppearanceCode").val(e.data.errorAppearanceCode);
                        $("#customerAction").empty();
                        $("#customerAction").append(customerAction_sel.join(' '));
                    }
                },
                error:function (e) {
                    layerError("请求失败","错误提示");
                }
            });
        }

        function editCustomerAction(i) {
            $("#errorAnalysisName_" + i + "").attr('readonly',false);
            $("#errorProcess_" + i + "").attr('readonly',false);
            $("#serviceLevel_" + i + "").attr('readonly',false);
        }

        function deleteAction(id,i) {
            if (id !='' && id != null) {
                $.ajax({
                    url: "${ctx}/md/customerProductType/ajax/delete",
                    data: {id: id},
                    success:function (e) {
                        if(e.success){
                            layerMsg('删除成功');
                            $("#trId_"+i+"").remove();
                        }else {
                            layerMsg('删除失败');
                        }
                    },
                    error:function (e) {
                        layerError("删除产品失败","错误提示");
                    }
                });
            }else {
                $("#trId_"+i+"").remove();
            }
        }

        function showCustomerProductType() {
            var customerId = $("#customerId").val();
            var customerName = $("#customerId").text();
            var text = "添加客户产品分类";
            var url = "${ctx}/md/customerProductType/customerProductTypeForm?customerId=" + customerId + "&customerName=" + customerName ;
            var area = ['640px', '400px'];
            top.layer.open({
                type: 2,
                id:"customerProductType",
                zIndex:19,
                title:text,
                content: url,
                area: area,
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                    getCustomerProductTypeList();
                }
            });
        }
    </script>
</head>
<body>
<form:form id="inputForm" modelAttribute="mdCustomerActionDto" action="${ctx}/md/customerProductType/ajax/save" method="post" class="form-horizontal">
    <sys:message content="${message}" />
    <form:hidden path="id"></form:hidden>
    <c:set var="customerId" value="${mdCustomerActionDto.customerId}"></c:set>
    <input type="hidden" id="newFlag" value="${mdCustomerActionDto.newFlag}">
    <input type="hidden" value="${mdCustomerActionDto.customerActionDtoList}" id="customerActionDtoList">
        <div class="row-fluid" style="margin-top: 40px;">
            <div class="span4">
                <label class="control-label" style="width: 80px"><span class=" red">*</span>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
                <div class="controls" style="margin-left: 80px">
                    <c:choose>
                        <c:when test="${mdCustomerActionDto.customerId != null && mdCustomerActionDto.customerId != ''}">
                            <input type="hidden" value="${mdCustomerActionDto.customerId}" id="customerId">
                            <input id="customerName" style="width:337px;" readonly="readonly" type="text" value="${mdCustomerActionDto.customerName}" class="valid" aria-invalid="false">
                        </c:when>
                        <c:otherwise>
                            <form:select path="customerId" class="input-large" style="width:350px;">
                                <form:option value="" label="所有"/>
                                <form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id" htmlEscape="false" />
                            </form:select>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="span4" style="width: 48%;">
                <label class="control-label"><span class=" red">*</span>客户产品分类：</label>
                <div class="controls" style="margin-left: 160px">
                    <c:choose>
                        <c:when test="${mdCustomerActionDto.customerProductTypeId != null && mdCustomerActionDto.customerProductTypeId != ''}">
                            <input type="hidden" value="${mdCustomerActionDto.customerProductTypeId}" id="customerProductTypeId">
                            <input id="customerProductTypeName" style="width:237px;" readonly="readonly" type="text" value="${mdCustomerActionDto.customerProductTypeName}" class="valid" aria-invalid="false">
                        </c:when>
                        <c:otherwise>
                            <form:select path="customerProductTypeId" class="input-large" style="width:250px;">
                                <form:option value="" label="请选择"/>
                            </form:select>
                            <a style="cursor: pointer;width: 150px;font-size: 14px;line-height: 35px;margin-left: 5px" href="javascript:void(0)" onclick="showCustomerProductType()">+添加客户产品分类</a>
                        </c:otherwise>
                    </c:choose>

                </div>
            </div>
        </div>
        <div class="row-fluid" style="margin-top: 15px">
            <div class="span4">
                <label class="control-label"style="width: 80px"><span class=" red">*</span>故障分类：</label>
                <div class="controls" style="margin-left: 80px">
                    <c:choose>
                        <c:when test="${mdCustomerActionDto.errorTypeName != null && mdCustomerActionDto.errorTypeName != ''}">
                            <input id="errorTypeName" style="width:337px;" readonly="readonly" type="text" value="${mdCustomerActionDto.errorTypeName}" class="valid" aria-invalid="false">
                        </c:when>
                        <c:otherwise>
                            <form:input path="errorTypeName" htmlEscape="false" maxlength="20" class="required"  cssStyle="width: 337px" placeholder="例：功能不良"/>
                        </c:otherwise>
                    </c:choose>

                </div>
            </div>
            <div class="span4">
                <label class="control-label">故障分类编码：</label>
                <div class="controls" style="margin-left: 160px">
                    <c:choose>
                        <c:when test="${mdCustomerActionDto.errorTypeCode != null && mdCustomerActionDto.errorTypeCode != ''}">
                            <input id="errorTypeCode" style="width:237px;" readonly="readonly" type="text" value="${mdCustomerActionDto.errorTypeCode}" class="valid" aria-invalid="false">
                        </c:when>
                        <c:otherwise>
                            <form:input path="errorTypeCode" htmlEscape="false" cssStyle="width: 237px" placeholder="字母和数字组合，例：ET00101"/>
                        </c:otherwise>
                    </c:choose>

                </div>
            </div>
        </div>


    <div class="row-fluid" style="margin-top: 15px">
        <div class="span4">
            <label class="control-label" style="width: 80px" >故障现象：</label>
            <div class="controls" style="margin-left: 80px">
                <c:choose>
                    <c:when test="${mdCustomerActionDto.errorAppearanceName != null && mdCustomerActionDto.errorAppearanceName != ''}">
                        <input id="errorAppearanceName" style="width:337px;" readonly="readonly" type="text" value="${mdCustomerActionDto.errorAppearanceName}" class="valid" aria-invalid="false">
                    </c:when>
                    <c:otherwise>
                        <form:input path="errorAppearanceName" htmlEscape="false" cssStyle="width: 337px" placeholder="例：Eb水泵故障"/>
                    </c:otherwise>
                </c:choose>

            </div>
        </div>
        <div class="span4">
            <label class="control-label">故障现象编码：</label>
            <div class="controls" style="margin-left: 160px">
                <c:choose>
                    <c:when test="${mdCustomerActionDto.errorAppearanceCode != null && mdCustomerActionDto.errorAppearanceCode != ''}">
                        <input id="errorAppearanceCode" style="width:237px;" readonly="readonly" type="text" value="${mdCustomerActionDto.errorAppearanceCode}" class="valid" aria-invalid="false">
                    </c:when>
                    <c:otherwise>
                        <form:input path="errorAppearanceCode" htmlEscape="false"  cssStyle="width: 237px" placeholder="字母和数字组合，例：EC00101"/>
                    </c:otherwise>
                </c:choose>

            </div>
        </div>
    </div>

    <div class="row-fluid">
        <div class="controls" style="margin-left: 5px">
            <table id="treeTable"
                   class="table table-striped table-bordered table-condensed" style="margin-top: 20px;">
                <thead>
                <tr>
                    <th width="20px">序号</th>
                    <th width="380px">故障分析</th>
                    <th width="380px">故障处理</th>
                    <th width="150px">服务级别</th>
                    <th width="100px">操作</th>
                </tr>
                </thead>
                <tbody id="customerAction">

                </tbody>
            </table>
            <div>
                <a  class="btn btn-primary" href="javascript:void(0);" onclick="javascript:addRow();">+添加</a>
            </div>

        </div>

    </div>
    <div style="height: 80px;width: 98%;float: left"></div>
    <div id="editBtn" class="line-row">
        <shiro:hasPermission name="md:customermaterial:edit">
            <input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存" style="width: 96px;height: 40px;margin-right: 10px;margin-left: 770px;margin-top: 10px"/>
            <input id="btnSubmit1" class="btn " type="button" onclick="javascript:cancel();" value="取消" style="width: 104px;height: 40px;margin-right: 25px;margin-top: 10px"/>
        </shiro:hasPermission>
    </div>
</form:form>
</body>
<script type="text/javascript">
    if($("#errorAppearanceName").val()!=null && $("#errorAppearanceName").val()!=''){
        getCustomerActionList();
    }
</script>
</html>
