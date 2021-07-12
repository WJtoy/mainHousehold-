<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>申请配件</title>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default" /> 
	<%@include file="/WEB-INF/views/include/dialog.jsp"%>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	<script src="${ctxStatic}/js/fixtable.js" type="text/javascript"></script>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
	<script src="${ctxStatic}/common/doT.min.js" type="text/javascript"></script>
	<script src="${ctxStatic}/jquery-viewer/viewer.min.js"></script>
	<link href="${ctxStatic}/jquery-viewer/viewer.min.css" rel="stylesheet">
	<script type="text/javascript">

        //防止拖文件到浏览器后自动打开或下载文件
        window.addEventListener("dragover",function(e){
            e = e || event;
            e.preventDefault();
        },false);
        window.addEventListener("drop",function(e){
            e = e || event;
            e.preventDefault();
        },false);
        //end
		<%String parentIndex = request.getParameter("parentIndex");%>
		var parentIndex = '<%=parentIndex==null?"":parentIndex %>';
        var parentLayerIndex = parent.layer.getFrameIndex(window.name);
		Order.rootUrl = "${ctx}";
		var clickTag = 0;
		var $btnSubmit = $("#btnSubmit");
        //var this_index = top.layer.index;
        var this_index = top.layer.getFrameIndex(window.name);
        var isExecute = false;
        var maxUploadSize = 9; //最大上传数量
        var uploadCount = 0;
		$(document).ready(function(){
            uploadCount = $("#attachCount").val(); //当前已经上传的数量
			$("#treeTable").treeTable({expandLevel : 2});
			$("#buttonUploadLogo").click(function() {
                var $btn = $(self);
                if (clickTag == 1){
                    return false;
                }
                clickTag = 1;
                $btn.attr('disabled', 'disabled');
                $btnSubmit.attr('disabled', 'disabled');
				uploadfile($("#logo"),$("#logo_image"), "fileToUploadlogo");
				return false;
			});

			$("#btnSubmit").click(function(event){
			    if(clickTag == 1){
                    event.preventDefault();
			        return false;
				}
				clickTag = 1;
				if($("#treeTable input[type='checkbox']:checked").length ==0){
                    event.preventDefault();
					layerAlert('请选择至少一个配件!');
                    clickTag = 0;
					return false;
				}
                var applyForm = {};
				var applyType = $("input[name='applyType']:checked").val();
				if(applyType==2){
                    if($("input[name='receiveType']").is(":checked")){
                        var $obj = $("input[name='receiveType']:checked");
                        applyForm.receiver = $obj.attr("data-receivename");
                        applyForm.receiverPhone = $obj.attr("data-receivephone");
                        applyForm.receiverAreaId = $obj.attr("data-areaid");
                        applyForm.receiverAddress = $obj.attr("data-address");
                        applyForm.receiverProvinceId = $obj.attr("data-receiveprovinceid");
                        applyForm.receiverCityId = $obj.attr("data-receivecityid");
                        applyForm.receiverType = $obj.attr("data-receivetype");
					}else{
                        event.preventDefault();
                        layerAlert('请选择收件地址!');
                        clickTag = 0;
                        return false;
					}
				}
                $btnSubmit.attr('disabled', 'disabled');
                var ajaxSuccess = 0;
                applyForm.quarter = $("#quarter").val();
                applyForm.orderId = $("#orderId").val();
                applyForm.productId = $("#productId").val();
                applyForm.orderDetailId = $("#orderDetailId").val();
                applyForm.remarks = $("#remarks").val();

                var option = $("[name='applyType']:checked");
                var applyType = {};
                applyType["value"] = option.val();
                lblid = option.attr("id");
                applyType["label"] = $("label[for='" + lblid + "']").text();
                applyForm.applyType = applyType;
				//对应productGroup,Map<ProductId,List<MaterialItem>>
                var materials = {};
                var items = [];
                var parentRowId,rowId;
                var parentRow,product;
                var rowProducts = {};
                var products = {};//提交的产品列表
                var row,item,pid,pname,pbrand,pmodel,pserviceid,pservicename,pwarranty;
                $("input[type='checkbox'][name='chooseFlag']:checkbox:checked").each(function(index,element){
                    parentRowId = $(this).data('parentrowid');
                    product = rowProducts[parentRowId];
                    if(!product){
                        parentRow = $("#" + parentRowId);
                        pid = parentRow.data("pid");
                        pname = parentRow.data("pname");
						pbrand = parentRow.data("brand");
						pmodel = parentRow.data("model");
						pserviceid = parentRow.data("servicetypeid");
						pservicename = parentRow.data("servicetypename");
						pwarranty = parentRow.data("warranty");
                        product = {id:pid,name:pname,brand:pbrand,model:pmodel,serviceType:{id:pserviceid,name:pservicename,warrantyStatus:{value:pwarranty}}};
						rowProducts[parentRowId] = product;
						products[pid] = product;
                        materials[pid]= [];
					}
					items = materials[product.id];
                    rowId = $(this).data("rowid");
					row = $("#" + rowId);
					item = {};
					var material = {};
					material["id"] = row.find("#mId").val();
					material["name"] = row.find("#mName").val();
					item["material"] = material;
					item["qty"] = row.find("#qty").val();
					item["returnFlag"] = row.find("#returnFlag").val();
					item["price"] = row.find("#price").val();
					item["factoryPrice"] = row.find("#factoryPrice").val();
					items.push(item);
                });
                applyForm.productGroup = materials;
                applyForm.products = products;
                /* test code
                console.log('data:');
                console.log(JSON.stringify(applyForm));
                clickTag = 0;
				$btnSubmit.removeAttr('disabled');
				return false;
                 */
                var loadingIndex;
                $.ajax({
                    async: false,
                    cache: false,
                    type: "POST",
                    url: "${ctx}/sd/material/saveMaterialApply?at="+ (new Date()).getTime(),
                    data: {"materialMaster":JSON.stringify(applyForm)},
                    dataType: 'json',
                    beforeSend: function () {
                        loadingIndex = top.layer.msg('正在提交，请稍等...', {
                            icon: 16,
                            time: 0,//不定时关闭
                            shade: 0.3
                        });
                    },
                    complete: function () {
                        if(ajaxSuccess == 0) {
                            setTimeout(function () {
                                clickTag = 0;
                                $btnSubmit.removeAttr('disabled');
                            }, 2000);
                        }
                    },
                    success: function (data) {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if(loadingIndex) {
                            top.layer.close(loadingIndex);
                        }
                        if(data && data.success == true){
                            layerMsg(data.message);
                            $("#inputForm").resetForm();
                            $("#tbd_attach tr").remove();
							var layero = $("#layui-layer" + (parentIndex || '0'), top.document);
							var iframeWin = top[layero.find('iframe')[0]['name']];
							if(iframeWin) {
								iframeWin.refreshMaterialButton();
							}
                            ajaxSuccess = 1;
                            top.layer.close(this_index);
                        }
                        else if( data && data.message){
                            layerError(data.message,"错误提示");
                        }
                        else{
                            layerError("配件申请错误","错误提示");
                        }
                        return false;
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText,null,"配件申请错误，请重试!");
                    }
                });
			});

			$("input[name='chooseFlag']").change(function() {
				var $check = $(this);
				var trid = $check.data("rowid");
				if(trid){
					if ($check.attr("checked") == "checked") {
						$("#" + trid).addClass("info");
					}else{
						$("#" + trid).removeClass("info");
					}
				}
			});

			$("input[name='applyType']").change(function(){
			    if($(this).val()==2){
                    $("#receiveFristInfoDiv").show()
				}else{
                    $("#receiveFristInfoDiv").hide();
				}
			});
            $("#divImg_0").click(function () {
                $("#upload_file").click();
                return false;
            });
            imageViewer();
            bindImageDragEvent();
            if(uploadCount>=maxUploadSize){
                $("#divImg_0").hide();
			}
		});

        function uploadFileChange(){
            isExecute = true;
            var files = document.getElementById("upload_file").files;
            if(files == null || files.size==0){
                isExecute = false;
                return false;
            }
            uploadfile($("#logo"),$("#logo_image"),"upload_file");
        }

		function uploadfile($obj1, $obj1_image, obj2)
		{
            var loadingIndex = top.layer.msg('正在上传，请稍等...', {
                icon: 16,
                time: 0,
                shade: 0.3
            });
			var data =
			{
				fileName : $obj1.val()
			};
			$.ajaxFileUpload({
				url : '${pageContext.request.contextPath}/servlet/Upload?fileName=' + $obj1.val(),//处理图片脚本
				secureuri : false,
				data : data,
				fileElementId : obj2,//file控件id
				dataType : 'json',
				success : function(data, status)
				{
                    isExecute = false;
                    if(ajaxLogout(data)){
                        return false;
                    }
					$obj1.val(data.fileName);
					$("#orignalName").val(data.origalName);

					if(data.origalName =="" )
					{
						layerAlert("请先选择照片，再点击上传","提示");
						return true;
					}
					//save to system
					var param = {};
					param.logo = $("#logo").val();
					param.orignalName = data.origalName;
					param.orderDetailId = $("#orderDetailId").val();
					param.orderId = $("#orderId").val();
					param.productId = $("#productId").val();
					param.quarter = $("#quarter").val();

					$.ajax(
						{
							cache : false,
							type : "POST",
							async : false,
							url : "${ctx}/sd/material/ajax/saveMaterialApplyTempAttachment",
							data : param,
							success : function(data)
							{
                                if(ajaxLogout(data)){
                                    return false;
                                }
								if (data)
								{
									if (data && data.success == true)
									{
                                        uploadCount = parseInt(uploadCount)+1;
                                        addUploadWarp(data.data.id,$("#logo").val());
                                        $("#upload_file").val('');
                                        imageViewer();
                                        $("#divImg_0").removeClass("imgOnDarg");
                                        if(uploadCount >= maxUploadSize){
                                            $("#divImg_0").hide();
                                        }
                                        isExecute = false;
									} else
									{
										layerError("照片上传失败!");
									}
								}
								return false;
							},
							error : function(e)
							{
								layerError("照片上传失败!");
							}
						});
				},
				error : function(e)
				{
                    ajaxLogout(e.responseText,null,"照片上传错误，请重试!");
                    isExecute = false;
				}
			});
            top.layer.close(loadingIndex);
            return false;
		}

        var confirmClickTag = 0;
		function delRow(i)
		{
			if (!i)
			{
				return false;
			}
            //var id = $("#id" + i).val();
			var id=i;
            if (id.length == 0){
                return false;
			}
            if(confirmClickTag == 1){
                return false;
            }
            confirmClickTag = 1;
            var data1 =
                {
                    attachmentId : id,
                    orderId : $("#orderId").val(),
                    orderDetailId : $("#orderDetailId").val(),
                    productId: $("#productId").val()
                };
            $.ajax(
                {
                    cache : false,
                    type : "POST",
                    async : false,
                    url : "${ctx}/sd/material/deleteMaterialApplyAttachment",
                    data : data1,
                    beforeSend: function () {
                    },
                    complete: function () {
                    },
                    success : function(data)
                    {
                        confirmClickTag = 0;
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if (data)
                        {
                            if (data && data.success == true)
                            {
                                //$("#attach_tr_" + i).remove();
                                //refreshUploadButon();
                                $("#divImg_" + id).remove();
                                $("#divImg_0").show();
                                uploadCount = uploadCount - 1;
                            } else
                            {
                                layerError(" 删除失败，请重试!");
                            }
                        }
                        return false;
                    },
                    error : function(e)
                    {
                        confirmClickTag = 0;
                        ajaxLogout(e.responseText,null,"删除失败，请重试!");
                    }
                });
		}

		function editReceiveAddress(servicePointId,engineerId) {
            top.layer.open({
                type: 2,
                id:'layer_editReceiveAddress',
                zIndex:19891016,
                title:'收件信息',
                content:"${ctx}sd/material/editReceiveAddress?servicePointId="+ servicePointId +"&engineerId=" + engineerId + "&parentIndex=" + (parentLayerIndex || ''),
                area: ['800px', '500px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){

                }
            });
        }
        
        function setReceiveData(data) {
            var $obj = $("input[type=radio][name='receiveType']").eq(0);
            $obj.attr("data-areaid",data.areaId);
            $obj.attr("data-receivename",data.receiveName);
            $obj.attr("data-receivephone",data.receivePhone);
            $obj.attr("data-detailaddre",data.detailAddre);
            $obj.attr("data-address",data.address);
            $obj.attr("data-receiveprovinceid",data.receiveProvinceId);
            $obj.attr("data-receivecityid",data.receiveCityId);
            $("#receiveNameDiv").text(data.receiveName);
            $("#receivePhoneDiv").text(data.receivePhone);
            $("#addressDiv").text("地址："+data.detailAddre+" "+data.address);
            if($obj.prop("disabled")==true){
                $obj.removeAttr("disabled");
                $("#textInfo").hide();
                $("#receiveInfoDiv").show();
			}
        }

        function addUploadWarp(imgIndex,imagesUrl) {
            var strHtml = "<div class='upload_warp_img_div' id='divImg_"+imgIndex+ "'>" +
                "<a href=\"javascript:;\" title=\"点击删除图片\" onclick=\"delRow('"+imgIndex+"')\" class=\"upload_warp_img_div_del\"></a>" +
                "<img title='点击放大' src='${ctxUpload}/"+imagesUrl+"'  data-original='${ctxUpload}/"+imagesUrl+"'/>" + "</div>"
            $("#divImg_0").before(strHtml);
        }

        function imageViewer(){
            var viewer = $("#divUploadWarp").viewer('destroy').viewer(
                {
                    url: "data-original",
                    filter:function(image) {
                        if(image.src.lastIndexOf("/upload-photo.png")>0){
                            return false;
                        }
                        return true;
                    },
                    viewed: function(image) {
                    },
                    shown:function () {
                        if(this.viewer.index == -1){
                            this.viewer.hide();
                        }
                    }
                }
            );
        }

        //图片拖拽
        function bindImageDragEvent(){
            var _item = $("#divImg_0")[0];
            var $item = $(_item);
            _item.ondragover = function(ev) {
                $item.addClass("imgOnDarg");
                ev.preventDefault();
            };
            _item.ondragleave = function(ev){
                $item.removeClass("imgOnDarg");
                ev.preventDefault();
            };
            _item.ondrop = function(e) {
                e.preventDefault();
                var fs = e.dataTransfer.files;
                var len = fs.length; //获取文件个数
                if(len === 0){
                    layerInfo("请拖拽要上传的文件","系统提示");
                    $item.removeClass("imgOnDarg");
                    return false;
                }
                if(len > 1){
                    layerInfo("一次只能上传一个文件","系统提示");
                    $item.removeClass("imgOnDarg");
                    return false;
                }
                var _type = fs[0].type;
                if(!_type.match('image.*')){
                    layerInfo("文件不是图片文件!","系统提示");
                    $item.removeClass("imgOnDarg");
                    return false;
                }
                document.getElementById("upload_file").files = fs; // 有的浏览器会触发onchange事件,有的不会
                if(!isExecute){ // 部分浏览器 js给input赋值会触发onchange事件 所以用isExecute判断onchange是否执行了
                    $("#upload_file").change();
                }
            };
        }

        function closeme() {
            top.layer.close(this_index);
        }

	</script>
<style type="text/css">
	.table th,.table td {  padding: 4px;  }
	.table thead th,.table tbody td {  text-align: center;  vertical-align: middle;  }
	.table .tdcenter {  text-align: center;  vertical-align: middle;  }
	.alert {padding: 4px 5px 4px 4px;  }
	.span-tr { padding: 4px 5px 4px 0px;}
	.span-product {margin-left: 10px;float: left; margin-bottom: 0px !important;}
	.upload_warp_img_div img{max-width:100%;max-height:100%;vertical-align:middle;width:100%;height: 100%;margin-bottom: 5px;}
	.upload_warp_img_div {position: relative;height: 70px;width: 70px;border: 1px solid #ccc;float: left;
		display: table-cell;text-align: center;background-color: #eee;cursor: pointer;margin-right: 10px;
	}
	.upload_warp{margin-top:8px;text-align: center;display: inline-block;}
	.upload_warp_img_div .upload_warp_img_div_del{position:absolute;top:0px;width:20px !important;height:20px !important;right:0px;margin-top: 0px !important;
		background-size: 20px 20px !important;background:url('${ctxStatic}/images/delUploadFile.png') no-repeat; }
	.imgOnDarg{
		border:2px dashed indianred;
		background: bisque;
	}
</style>
</head>
<body style="margin-bottom: 5px;">
	<form:form id="inputForm" modelAttribute="materialMaster" method="post" class="form-horizontal" cssStyle="margin-top: 10px">
		<sys:message content="${message}" />
		<c:if test="${canAction}">
		<input type="hidden" id="parentIndex" name="parentIndex" value="${parentIndex}" />
		<form:hidden path="orderId" />
		<form:hidden path="quarter" />
		<form:hidden path="orderDetailId" />
		<!-- 存储原始产品id,套组不变，用来查找上门服务 -->
		<form:hidden path="productId" />
		<form:hidden path="product.id" />
		<form:radiobuttons path="applyType" items="${fns:getDictListFromMS('material_apply_type')}" itemLabel="label"
							itemValue="value" htmlEscape="false" class="required" /> <%-- 切换为微服务 --%>
		<table id="treeTable" class="table table-striped table-bordered table-condensed" style="margin-top: 20px;">
			<thead>
				<tr>
					<th width="60px">选择</th>
					<th>配件</th>
					<th width="80px">数量</th>
					<th width="100px">返件</th>
					<th width="100px">价格</th>
					<th width="100px">参考价格</th>
				</tr>
			</thead>
			<tbody>
				<c:set var="idx" value="-1"/>
				<c:forEach items="${materialMaster.mateirals}" var="map" varStatus="i" begin="0">
					<tr id="tr_${i.index+1}" pId="0"
							data-pid="${map.key.id}" data-pname="${map.key.name}" data-brand="${map.key.brand}"
							data-model="${map.key.model}" data-servicetypeid="${map.key.serviceType.id}" data-servicetypename="${map.key.serviceType.name}"
							data-warranty="${map.key.serviceType.warrantyStatus.value}"
					>
						<td>${i.index+1}</td>
						<td colspan="5" style="text-align:left;">
							<span class="span-product alert alert-success">${map.key.name}</span> <span class="span-tr">品牌: ${map.key.brand}</span> <span class="span-tr">型号: ${map.key.model}</span> <span class="span-tr">服务项目: ${map.key.serviceType.name}</span>
						</td>
					</tr>
					<c:forEach items="${map.value}" var="materialItem" varStatus="j" begin="0">
						<c:set var="idx" value="${idx + 1}"/>
						<tr id="tr_${i.index}_${j.index}" pId="tr_${i.index+1}">
							<td>
								<input type="checkbox" data-parentrowid="tr_${i.index+1}" data-rowid="tr_${i.index}_${j.index}" name="chooseFlag" cssclass="chkitem"/>
							</td>
							<td>${materialItem.material.name}
								<input type="hidden" id="mId" value="${materialItem.material.id}"/>
								<input type="hidden" id="mName" value="${materialItem.material.name}"/>
							</td>
							<td><input type="text" id="qty" value="${materialItem.qty}"
											htmlEscape="false" maxlength="7" class="required number int input-mini" />
							</td>
							<td>
								<c:choose>
									<c:when test="${materialItem.returnFlag == 1}"><span class="label label-important">是</span></c:when>
									<c:otherwise>否</c:otherwise>
								</c:choose>
								<input type="hidden" id="returnFlag" value="${materialItem.returnFlag}"/>
							</td>
							<td><input type="text" id="price" value="${materialItem.price}" htmlEscape="false" maxlength="7" class="required number double input-mini"/></td>
							<td><input type="text" id="factoryPrice" readonly="readonly" value="${materialItem.factoryPrice}" htmlEscape="false" maxlength="7" class="required number double input-mini"/></td>
						</tr>
					</c:forEach>
				</c:forEach>

			</tbody>
		</table>
		<div><label class="control-label" style="width: 70px">故障描述：</label>
			<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="255" class="input-xlarge" style="width: 91%;" />
		</div>
	    <div id="divUploadWarp">
			<c:set var="attachSize" value="0"/>
			<c:if test="${attachments != null}">
				<c:set var="attachSize" value="${attachments.size()}" />
			</c:if>
			<input type="hidden" id="attachCount" value="${attachSize}">
			<label class="control-label" style="width: 70px;padding-top: 6px">配件照片：</label>
			<div class="upload_warp" id="divUploadWarp">
				<c:forEach items="${attachments}" var="attach" varStatus="i" begin="0">
					<div class="upload_warp_img_div" id="divImg_${attach.id}">
						<a href="javascript:" title="点击删除图片" onclick="delRow('${attach.id}')" class="upload_warp_img_div_del"></a>
						<img title='点击放大' src='${ctxUpload}/${attach.filePath}'  data-original='${ctxUpload}/${attach.filePath}'/>
					</div>
				</c:forEach>
				<div class="upload_warp_img_div" id="divImg_0">
					<img title="点击上传图片" src="${ctxStatic}/images/upload-photo.png" />
				</div>
				<input name="logo" id="logo" type="hidden"  htmlEscape="false" />
				<input name="orignalName" id="orignalName" type="hidden" htmlEscape="false" />
				<input id="upload_file" name="upload_file" type="file" style="display: none" accept="image/gif,image/jpeg,image/png" onchange="uploadFileChange()">
			</div>
		<div>
		<div style="margin-top: 10px" class="row-fluid" id="receiveFristInfoDiv">
			<div class="span12">
				<div class="control-group" style="border-bottom:none">
					<label style="width: 70px;text-align: left" class="control-label">申请方式：</label>
					<div class="controls" style="margin-left: 70px;margin-top: 3px">
						<c:choose>
							<c:when test="${materialReceives[0].areaId==null || materialReceives[0].areaId==0 || empty materialReceives[0].address}">
								<div style="float: left">
									<input  id="engineer_1" type="radio" name="receiveType" disabled="true" data-receivetype="2">
									<label for="engineer_1">师傅收件</label>
								</div>
								<div style="margin-left: 12px;float: left;" id="textInfo">未设置师傅收件信息，<a href="javascript:editReceiveAddress('${materialReceives[0].servicePointId}','${materialReceives[0].engineerId}')">去添加</a></div>
								<div style="float: left;display: none" id="receiveInfoDiv">
									<div style="margin-left: 12px;float: left;width: 70px" id="receiveNameDiv"></div>
									<div style="float: left;width: 90px" id="receivePhoneDiv"></div>
									<div style="margin-left: 12px;float: left" id="addressDiv"></div>
									<div style="float: left">
										<a href="javascript:editReceiveAddress('${materialReceives[0].servicePointId}','${materialReceives[0].engineerId}')" style="margin-left: 20px"><i class="icon-edit">修改</i></a>
									</div>
								</div>
							</c:when>
							<c:otherwise>
								<div style="float: left">
									<input  id="engineer_1" type="radio" name="receiveType"  data-areaid="${materialReceives[0].areaId}" data-receivename="${materialReceives[0].receiveName}"
											data-receivephone="${materialReceives[0].receivePhone}" data-detailaddre="${materialReceives[0].detailAddress}" data-address="${materialReceives[0].address}"
									        data-receiveprovinceid="${materialReceives[0].provinceId}" data-receivecityid="${materialReceives[0].cityId}" data-receivetype="2">
									<label for="engineer_1">师傅收件</label>
								</div>
								<div style="float: left">
									<div style="margin-left: 12px;float: left;width: 70px" id="receiveNameDiv">${materialReceives[0].receiveName}</div>
									<div style="float: left;width: 90px" id="receivePhoneDiv">${materialReceives[0].receivePhone}</div>
									<div style="margin-left: 12px;float: left" id="addressDiv">地址：${materialReceives[0].detailAddress} ${materialReceives[0].address}</div>
									<div style="float: left">
										<a href="javascript:editReceiveAddress('${materialReceives[0].servicePointId}','${materialReceives[0].engineerId}')" style="margin-left: 20px"><i class="icon-edit">修改</i></a>
									</div>
								</div>
							</c:otherwise>
						</c:choose>
						<%--<br style="margin-top: 5px">
						<div style="float: left">
							<input  id="user_1" type="radio" name="receiveType"  data-areaid="${materialReceives[1].areaId}" data-receivename="${materialReceives[1].receiveName}"
									data-receivephone="${materialReceives[1].receivePhone}" data-detailaddre="${materialReceives[1].detailAddress}" data-address="${materialReceives[1].address}"
									data-receiveprovinceid="${materialReceives[1].provinceId}" data-receivecityid="${materialReceives[1].cityId}" data-receivetype="1">
							<label for="user_1">用户收件</label>
						</div>
						<div style="margin-left: 12px;float: left;width: 70px">${materialReceives[1].receiveName}</div><div style="float: left;width: 90px">${materialReceives[1].receivePhone}</div><div style="margin-left: 12px;float: left">地址：${materialReceives[1].detailAddress} ${materialReceives[1].address}</div>--%>
					</div>
			    </div>
				<div class="control-group" style="border-bottom:none">
					<div class="controls" style="margin-left: 70px">
						<div style="float: left">
							<input  id="user_1" type="radio" name="receiveType"  data-areaid="${materialReceives[1].areaId}" data-receivename="${materialReceives[1].receiveName}"
									data-receivephone="${materialReceives[1].receivePhone}" data-detailaddre="${materialReceives[1].detailAddress}" data-address="${materialReceives[1].address}"
									data-receiveprovinceid="${materialReceives[1].provinceId}" data-receivecityid="${materialReceives[1].cityId}" data-receivetype="1">
							<label for="user_1">用户收件</label>
						</div>
						<div style="margin-left: 12px;float: left;width: 70px">${materialReceives[1].receiveName}</div><div style="float: left;width: 90px">${materialReceives[1].receivePhone}</div><div style="margin-left: 12px;float: left">地址：${materialReceives[1].detailAddress} ${materialReceives[1].address}</div>
					</div>
				</div>
			</div>
		</div>
		<%--<div class="form-actions">
				<input id="btnSubmit" class="btn btn-primary" type="button" value="保 存" />
		</div>--%>
		</c:if>
	</form:form>
	<c:if test="${canAction}">
		<div style="height: 60px;width: 100%"></div>
		<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
			<hr style="margin: 0px;"/>
			<div style="float: right;margin-top: 10px;margin-right: 35px">
				<input id="btnSubmit" class="btn btn-primary" type="button" style="margin-right: 5px;width: 96px;height: 40px" value="确 定" />
				<input id="btnCancel" class="btn" type="button" value="关 闭" style="width: 96px;height: 40px" onclick="closeme()"/>
			</div>
		</div>
	</c:if>
</body>
</html>