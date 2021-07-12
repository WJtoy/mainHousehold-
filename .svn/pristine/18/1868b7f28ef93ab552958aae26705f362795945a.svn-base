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
                var $obj = $('input[name="receiveType"]:checked');
                var val = $obj.val();
                if(val==null || val==''){
                    layerAlert('请选中收件地址!');
                    return false;
				}
                $("#inputForm").submit();
            });

            $("#inputForm").validate({
                submitHandler: function(form){
                    var areaId = $("#areaId").val();
                    if(areaId==null || areaId==''){
                        layerAlert('请选中区域!');
                        return false;
					}
                    var loadingIndex = layerLoading('正在提交，请稍候...');
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
                    $.ajax({
                        url:"${ctx}/sd/material/saveEngineerAddress",
                        type:"POST",
                        data:$(form).serialize(),
                        dataType:"json",
                        success: function(data){
                            clickTag = 0;
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
                                var entity={};
                                entity.areaId=$("#areaId").val();
                                entity.receiveName = $("#userName").val();
                                entity.receivePhone = $("#contactInfo").val();
                                entity.detailAddre = $("#areaName").val();
                                entity.address = $("#address").val();
                                entity.receiveProvinceId = data.data.provinceId;
                                entity.receiveCityId = data.data.cityId;
                                if(parentIndex && parentIndex != undefined && parentIndex != ''){
                                    var layero = $("#layui-layer" + parentIndex,top.document);
                                    var iframeWin = top[layero.find('iframe')[0]['name']];
                                    iframeWin.setReceiveData(entity);
                                    top.layer.close(this_index)
                                }
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
                                layer.close(loadingIndex);
                            }
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                            ajaxLogout(data,null,"数据保存错误，请重试!");
                            //var msg = eval(data);
                        },
                        timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
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

            $("input[name='receiveType']").change(function(){
               $("#addressFlag").val($(this).val());
               if($(this).val()!=3){
                   $("#areaId").val($(this).data("areaid"));
                   $("#areaName").val($(this).data("detailaddre"));
                   $("#userName").val($(this).data("receivename"));
                   $("#contactInfo").val($(this).data("receivephone"));
                   $("#address").val($(this).data("address"));
                   $("#areaButton").addClass("prohibit");
                   $("#address").attr("readonly","readonly");
			   }else{
                   $("#areaButton").removeClass("prohibit");
                   $("#address").removeAttr("readonly");
			   }
			});
            if( $("#addressFlag").val()!=3){
                $("#areaButton").addClass("prohibit");
                $("#address").attr("readonly","readonly");
			}
		});

        function cancel() {
            top.layer.close(this_index);
        }
	</script>
</head>
<body>
   <sys:message content="${message}"/>
    <c:if test="${canSave==true}">
   <fieldset style="width: 90%;margin-left: 50px">
		<form:form id="inputForm"  action="" method="post" class="form-horizontal">
			<input type="hidden" name="id" value="${materialReceives[2].id}">
			<input type="hidden" name="servicePointId" value="${materialReceives[2].servicePointId}">
			<input type="hidden" name="engineerId" value="${materialReceives[2].engineerId}">
			<input type="hidden" id="addressFlag" name="addressFlag" value="${addressFlag}">
			<div style="width: 100%;height: auto;background-color: #F6F6F6;margin-top: 40px">
				<div class="row-fluid">
					<div class="span12">
						<div class="control-group">
							<div class="controls" style="margin-left: 10px">
								<div style="margin-top: 10px">
									<c:choose>
										<c:when test="${materialReceives[0].areaId==null || materialReceives[0].areaId==0 || empty materialReceives[0].address}">
											<input  id="engineer_1" type="radio" name="receiveType" value="1" disabled="disabled">
											<label for="engineer_1">网点地址</label>
											<span style="margin-left: 12px;">无</span>
										</c:when>
										<c:otherwise>
											<input  id="engineer_1" type="radio" name="receiveType" value="1" data-areaid="${materialReceives[0].areaId}" data-receivename="${materialReceives[0].receiveName}"
													data-receivephone="${materialReceives[0].receivePhone}" data-detailaddre="${materialReceives[0].detailAddress}" data-address="${materialReceives[0].address}" <c:out value="${addressFlag==1?'checked':''}"/>>
											<label for="engineer_1">网点地址</label>
											<span style="margin-left: 12px;">${materialReceives[0].receiveName} ${materialReceives[0].receivePhone}</span>
											<span style="margin-left: 12px">地址：${materialReceives[0].detailAddress} ${materialReceives[0].address}</span>
										</c:otherwise>
									</c:choose>
								</div>
								<div style="margin-top: 5px">
									<c:choose>
										<c:when test="${materialReceives[1].areaId==null || materialReceives[1].areaId==0 || empty materialReceives[1].address}">
											<input  id="user_1" type="radio" name="receiveType" value="2" disabled>
											<label for="user_1">师傅地址</label>
											<span style="margin-left: 12px;">无</span>
										</c:when>
										<c:otherwise>
											<input  id="user_1" type="radio" name="receiveType" value="2" data-areaid="${materialReceives[1].areaId}" data-receivename="${materialReceives[1].receiveName}"
													data-receivephone="${materialReceives[1].receivePhone}" data-detailaddre="${materialReceives[1].detailAddress}" data-address="${materialReceives[1].address}" <c:out value="${addressFlag==2?'checked':''}"/>>
											<label for="user_1">师傅地址</label>
											<span style="margin-left: 12px;">${materialReceives[1].receiveName} ${materialReceives[1].receivePhone}</span><span style="margin-left: 12px;">地址：${materialReceives[1].detailAddress} ${materialReceives[1].address}</span>
										</c:otherwise>
									</c:choose>
								</div>
								<div style="margin-top: 5px;margin-bottom: 10px" >
									<input  id="customize_1" type="radio" name="receiveType" value="3" <c:out value="${addressFlag==3?'checked':''}"/>>
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
							<input name="userName" id="userName" class="input-block-level required fromInput" value="${materialReceives[2].receiveName}">
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label"><font style="color: red">*</font>联系电话：</label>
						<div class="controls">
							<input name="contactInfo" id="contactInfo" maxlength="11" class="input-block-level required fromInput mobile" value="${materialReceives[2].receivePhone}">
						</div>
					</div>
				</div>
			</div>

			<div class="row-fluid" style="margin-top: 5px">
				<div class="span6">
					<div class="control-group">
						<label class="control-label"><font style="color: red">*</font>收件地址：</label>
						<div class="controls">
							<sys:areaselect name="areaId" id="area" value="${materialReceives[2].areaId}"
											labelValue="${materialReceives[2].detailAddress}" labelName="areaName" title=""
											mustSelectCounty="true" cssClass="required" cssStyle="width:188px"></sys:areaselect>
						</div>
					</div>
				</div>
				<div class="span6">
					<div class="control-group">
						<div class="controls" style="margin-left: -5px">
							<input name="address" id="address" class="input-block-level required fromInput" value="${materialReceives[2].address}" placeholder="详情地址(不包括省市区)">
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