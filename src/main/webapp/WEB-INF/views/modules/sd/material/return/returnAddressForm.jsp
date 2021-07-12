<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>添加收件信息</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<meta name="decorator" content="default"/>
	<style type="text/css">
		.fromInput {
			border:1px solid #ccc;padding:3px 6px;color:#555;border-radius:4px;
		}
		.form-horizontal{margin-top:5px;}
		.form-horizontal .control-label {width: 80px;}
		.form-horizontal .controls {margin-left: 90px;}
		.prohibit{
			pointer-events: none;
		}
	</style>
	<script type="text/javascript">
        <%String parentIndex = request.getParameter("parentIndex");%>
        var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        var this_index = top.layer.index;
        var  clickTag = 0;
        $(document).ready(function(){
            $("#btnSubmit").click(function(){
                $("#inputForm").submit();
            });

            $("#inputForm").validate({
                submitHandler: function(form){
                    var areaId = $("#receiverAreaId").val();
                    if(areaId==null || areaId==''){
                        layerAlert('请选中区域!');
                        return false;
					}
                    //var loadingIndex = layerLoading('正在提交，请稍候...');
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    if(clickTag==1){
                        event.preventDefault();
                        return false;
					}
                    clickTag==1
                    $btnSubmit.prop("disabled", true);
                    layerMsg("操作成功");
                    var entity={};
                    entity.areaName= $("#receiverAreaName").val();
                    entity.subAreaName = $("#receivorAddress").val();
                    entity.receivor = $("#receivor").val();
                    entity.receivorPhone = $("#receivorPhone").val();
                    entity.receiverAreaId = $("#receiverAreaId").val();
                    if(parentIndex && parentIndex != undefined && parentIndex != ''){
                        var layero = $("#layui-layer" + parentIndex,top.document);
                        var iframeWin = top[layero.find('iframe')[0]['name']];
                        iframeWin.setDate(entity);
                        top.layer.close(this_index)
                    }
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

            $("input[name='receiveType']").change(function(){
               var customerAddressId = $(this).val();
				$("#receivor").val($("#userName_"+customerAddressId).text());
                $("#receivorPhone").val($("#contactInfo_"+customerAddressId).text());
                $("#receiverAreaId").val($("#areaId_"+customerAddressId).val());
                $("#receiverAreaName").val($("#areaName_"+customerAddressId).text());
                $("#receivorAddress").val($("#address_"+customerAddressId).text());
			});
		});

        function cancel() {
            top.layer.close(this_index);
        }
	</script>
</head>
<body>
   <sys:message content="${message}"/>
    <c:if test="${canSave==true}">
    <fieldset style="width: 90%;margin-left: 5%">
		<form:form id="inputForm"  action="" method="post" class="form-horizontal">
			<input type="hidden" name="id" value="${materialReturn.id}">
			<div style="margin-top: 40px">
				注：此处修改，只修改本配件申请单的返件地址，如需修改默认返件地址，请到客户资料处修改！
			</div>
			<div style="width: 100%;height: auto;background-color: #F6F6F6;margin-top: 10px">
				<div class="row-fluid">
					<div class="span12">
						<div class="control-group">
							<div class="controls" style="margin-left: 10px">
								<c:forEach items="${customerAddressList}" var="customerAddress">
									<c:set var="addressType" value=""/>
									<c:choose>
										<c:when test="${customerAddress.addressType==1}">
											<c:set var="addressType" value="公司地址" />
										</c:when>
										<c:when test="${customerAddress.addressType==2}">
											<c:set var="addressType" value="发货地址" />
										</c:when>
										<c:otherwise>
											<c:set var="addressType" value="返件地址" />
										</c:otherwise>
									</c:choose>
									<c:if test="${!empty addressType}">
										<div style="margin-top: 10px">
											<input  id="customerAddress_${customerAddress.id}" type="radio" name="receiveType" value="${customerAddress.id}">
											<label for="customerAddress_${customerAddress.id}">${addressType}</label>
											<span style="margin-left: 12px;" id="userName_${customerAddress.id}">${customerAddress.userName}</span>
											<span style="margin-left: 12px;" id="contactInfo_${customerAddress.id}">${customerAddress.contactInfo}</span>
											<c:if test="${not empty customerAddress.areaName && not empty address}">
											   <span style="margin-left: 12px;">地址：</span>
											</c:if>
											<span id="areaName_${customerAddress.id}">${customerAddress.areaName}</span>
											<span id="address_${customerAddress.id}">${customerAddress.address}</span>
											<input type="hidden" id="areaId_${customerAddress.id}" value="${customerAddress.areaId}">
										</div>
									</c:if>
								</c:forEach>
								<div style="margin-top: 5px;margin-bottom: 10px" >
									<input  id="customize_1" type="radio" name="receiveType" value="3" checked>
									<label for="customize_1">自定义</label>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid" style="margin-top: 20px">
				<div class="span6">
					<div class="control-group">
						<label class="control-label"><font style="color: red">*</font>姓&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;名：</label>
						<div class="controls">
							<input name="receivor" id="receivor" class="input-block-level required fromInput" value="${materialReturn.receivor}">
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label"><font style="color: red">*</font>联系电话：</label>
						<div class="controls">
							<input name="receivorPhone" id="receivorPhone" maxlength="11" class="input-block-level required fromInput mobile" value="${materialReturn.receivorPhone}">
						</div>
					</div>
				</div>
			</div>

			<div class="row-fluid" style="margin-top: 5px">
				<div class="span6">
					<div class="control-group">
						<label class="control-label"><font style="color: red">*</font>返件地址：</label>
						<div class="controls">
							<sys:areaselect name="receiverAreaId" id="receiverArea" value="${materialReturn.receiverAreaId}"
											labelValue="${materialAreaName}" labelName="areaName" title=""
											mustSelectCounty="true" cssClass="required" cssStyle="width:188px"></sys:areaselect>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<div class="controls" style="margin-left: -5px">
							<input name="receivorAddress" id="receivorAddress" class="input-block-level required fromInput" value="${materialReturn.receivorAddress}">
						</div>
					</div>
				</div>
			</div>
		</form:form>
	  </fieldset>
	</c:if>
   <div style="height: 60px;width: 100%"></div>
   <div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
	   <hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
	   <div style="float: right;margin-top: 10px;margin-right: 20px">
		   <c:if test="${canSave==true}">
			   <input id="btnSubmit" class="btn btn-primary" type="button" style="margin-right: 5px;width: 96px;height: 40px" value="保 存"/>
		   </c:if>
		   <input id="btnCancel" class="btn" type="button" value="取 消" style="width: 96px;height: 40px" onclick="cancel()"/>
	   </div>
   </div>
</body>
</html>