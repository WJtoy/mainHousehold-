<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
	<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
    <script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
    <link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
    <title>添加网点</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
        <%String parentIndex = request.getParameter("parentLayerIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';

        var this_index = top.layer.index;
        var clickTag = 0;
		$(document).ready(function() {
            $("#btnSubmit").click(function () {
                var $btnSubmit = $("#btnSubmit");
                if($btnSubmit.prop("disabled") == true){
                    return false;
                }
                $("#btnSubmit").prop("disabled",true);
                if (!$("#inputForm").valid()) {
                    $("#btnSubmit").prop("disabled",false);
                    return false;
                }
                //$btnSubmit.attr("disabled", "disabled");
                //$("#inputForm").attr("action", "${ctx}/md/servicepoint/save");
                $("#inputForm").submit();
            });

            $("#inputForm").validate({
                onfocusout: function(element){
                    $(element).valid();//失去焦点时再验证
                },
                submitHandler: function (form) {
                    //check
                    var loadingIndex = layerLoading('正在提交，请稍等...');
                    //form.submit();
                    $.ajax({
                        url:"${ctx}/sd/order/crush/updateAddress",
                        type:"POST",
                        data:$(form).serialize(),
                        dataType:"json",
                        success: function(data){
                            //提交后的回调函数
                            if(loadingIndex) {
                                top.layer.close(loadingIndex);
                            }
                            if(ajaxLogout(data)){
                                setTimeout(function () {
                                    clickTag = 0;
                                    $("#btnSubmit").removeAttr('disabled');
                                }, 2000);
                                return false;
                            }
                            if (data.success) {
                                layerMsg("保存成功");
                                if(parentIndex && parentIndex != undefined && parentIndex != ''){
                                    var layero = $("#layui-layer" + parentIndex,top.document);
                                    var iframeWin = top[layero.find('iframe')[0]['name']];
                                    iframeWin.reload();
                                    cancel();//关闭本窗口
                                }
                            }else{
                                setTimeout(function () {
                                    clickTag = 0;
                                    $("#btnSubmit").removeAttr('disabled');
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
                                $("#btnSubmit").removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                            //var msg = eval(data);
                        }
                    });
                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $("#btnSubmit").removeAttr('disabled');
                    $("#btnApprove").removeAttr('disabled');

                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });
        });

		//取消
		function cancel() {
            top.layer.close(this_index);//关闭本身
        }
	</script>
	  <style type="text/css">
		  .form-horizontal .control-label{
			  width: 130px;
		  }
		  .form-horizontal .controls{
			  margin-left: 140px;
		  }
          .fromInput {
              border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;
          }
	  </style>
  </head>
  
  <body>
    <sys:message content="${message}"/>
    <c:if test="${canSave==true}">
        <div style="width: 80%;margin-left: 10%;margin-top: 50px">
            <form:form id="inputForm" modelAttribute="orderCondition" action="${ctx}/sd/order/crush/updateAddress" method="post" class="form-horizontal">
                <form:hidden path="orderId"/>
                <form:hidden path="quarter"/>
                <input type="hidden" name="crushId" value="${crushId}">
                <div class="row-fluid">
                    <div class="span12">
                        <div class="control-group">
                            <label class="control-label">下单地址：</label>
                            <div class="controls">
                                <input class="fromInput input-block-level" value="${orderCondition.area.name}${orderCondition.address}" disabled>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="row-fluid" style="margin-top: 10px">
                    <div class="span12">
                        <div class="control-group">
                            <label class="control-label">上门地区：</label>
                            <div class="controls">
                                <input class="fromInput input-block-level required" name="areaName" value="${orderCondition.areaName}" readonly>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="row-fluid" style="margin-top: 10px">
                    <div class="span6">
                        <div class="control-group">
                            <label class="control-label">上门地址：</label>
                            <div class="controls">
                               <%-- <form:select path="subArea.id" cssClass="input-block-level required">
                                    <form:option value="" label="请选择"/>
                                    <form:options items="${subAreaList}" itemLabel="name" itemValue="id" htmlEscape="false"/>
                                </form:select>--%>
                               <md:subareaselect id="subArea.id" name="subArea.id" value="${orderCondition.subArea.id}"
                                                           labelValue="${orderCondition.subArea.name}" labelName="subArea.name"
                                                           title="区域" mustSelectCounty="false" cssClass="required" cssStyle="width:150px" areaId="${orderCondition.area.id}"> </md:subareaselect>
                            </div>
                        </div>
                    </div>
                    <div class="span6">
                        <div class="control-group">
                             <input class="input-block-level fromInput required" name="serviceAddress" value="${orderCondition.serviceAddress}">
                        </div>
                    </div>
                </div>
            </form:form>
        </div>
        <div style="height: 60px;width: 100%"></div>
        <div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
            <hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
            <div style="float: right;margin-top: 10px;margin-right: 20px">
                <input id="btnSubmit" class="btn btn-primary" type="button" style="margin-right: 5px;width: 96px;height: 40px" value="保 存"/>
                <input id="btnCancel" class="btn" type="button" value="取 消" style="width: 96px;height: 40px" onclick="cancel()"/>
            </div>
        </div>
     </c:if>
  </body>
</html>
