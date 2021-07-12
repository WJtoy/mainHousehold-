<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户图片定义</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
        top.layer.closeAll();
        var clickTag = 0;
        $(document).on("click", "#btnSubmit", function () {
            if(clickTag == 1){
                return false;
            }
			if (!$("#inputForm").valid()) {
				return false;
			}

            clickTag = 1;
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            if($("#contentTable input[type='checkbox'][name='checkedRecords']:checkbox:checked").length == 0){
                clickTag = 0;
                $btnSubmit.removeAttr('disabled');
                layerInfo("请选择子少一个图片类型.","系统提示");
                return false;
            }

            var entity = {};
            entity['id'] = $("#id").val();
            entity['product.id'] = $("#productId").val();
            if($("#id").val()!=null && $("#id").val() !='' && $("#id").val()!=undefined){
                entity['product.name'] = $("#productName").val();
			}else{
                entity['product.name'] = $("#productId").find("option:selected").text();
			}
            var customerId = $("#customerId").val();
            entity['customer.id'] = customerId;
            entity['barcodeMustFlag'] =  $("#barcodeMustFlag").is(':checked')?1:0;
            $("input[type='checkbox'][name='checkedRecords']:checkbox:checked").each(function(index,element){
                var id = $(this).data('id');
                entity['items['+index+'].checked'] = 1;
                var code = $(this).val();
                entity['items['+index+'].pictureCode'] = code;
                var title = $(this).data("title");
                entity['items['+index+'].title'] = title;
                var sort = $(this).data("sort");
                entity['items['+index+'].sort'] = sort;
                var remarks = $(this).data("remarks");
                entity['items['+index+'].remarks'] = remarks;
                var isChecked = $("#mustFlag-" + id).is(":checked");
                entity['items['+index+'].mustFlag'] = isChecked?1:0;
            });
			var loadingIndex;
            var options = {
                url: "${ctx}/md/customer/pic/save",                 //默认是form的action， 如果申明，则会覆盖
                type: 'post',               //默认是form的method（get or post），如果申明，则会覆盖
                dataType: 'json',           //html(默认), xml, script, json...接受服务端返回的类型
                data: entity,
                beforeSubmit: function(formData, jqForm, options){
                    loadingIndex = layer.msg('正在提交，请稍等...', {
                        icon: 16,
                        time: 0,
                        shade: 0.3
                    });
                    return true;
                },  //提交前的回调函数
                success:function (data)
                {
                    //提交后的回调函数
                    if(loadingIndex) {
                        layer.close(loadingIndex);
                    }
                    if(ajaxLogout(data)){
                        setTimeout(function () {
                            clickTag = 0;
                            $btnSubmit.removeAttr('disabled');
                        }, 2000);
                        return false;
                    }
                    //var msg = eval(data);
                    if (data.success) {0
                        layerMsg("保存成功");
                        var curFrame = getActiveTabIframe();
                        if(curFrame!=undefined){
                            curFrame.document.location="${ctx}/md/customer/pic/list?customer.id="+customerId;
                            //.attr('src', "${ctx}/md/product/pic");
                        }else{
                            history.go(-1);
                        }
                    }else{
                        setTimeout(function () {
                            clickTag = 0;
                            $btnSubmit.removeAttr('disabled');
                        }, 2000);
                        layerError("数据保存错误:" + data.message, "错误提示");
                    }
                    return false;
                },
                error: function (data)
                {
                    setTimeout(function () {
                        clickTag = 0;
                        $btnSubmit.removeAttr('disabled');
                    }, 2000);
                    ajaxLogout(data,null,"数据保存错误，请重试!");
                    //var msg = eval(data);
                },
                //clearForm: true,          //成功提交后，清除所有表单元素的值
                //resetForm: true,          //成功提交后，重置所有表单元素的值
                timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
            };
            $("#submitForm").ajaxSubmit(options);
            // $("#inputForm").ajaxForm(options).submit(function(){
            //     return false;//防止默认的提交动作，即防止二次提交
            // });
            return false;
        });

		$(document).ready(function() {
            $("#selectAll").change(function() {
                var $check = $(this);
                $("input:checkbox").each(function(){
                    if ($(this).val() != "on"){
                        if ($check.prop("checked") == "checked" || $check.prop("checked") == true) {
                            $(this).prop("checked", true);
                        }
                        else{
                            $(this).prop("checked", false);
                        }
                    }
                });
            });
		});

        $(document).on('change','.selectCustomer',function(e){
            var customerId =$(this).val();
            if (customerId == "")
            {
                return false;
            }
            $.ajax({
                    url:"${ctx}/md/customer/pic/ajax/customerProductList?customerId="+customerId,
                    success:function (e) {
                        if(e.success){
                            $("#productId").empty();
                            var programme_sel=[];
                            programme_sel.push('<option value="" selected="selected">请选择</option>')
                            for(var i=0,len=e.data.length;i<len;i++){
                                var programme = e.data[i];
                                programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                            }
                            $("#productId").append(programme_sel.join(' '));
                            $("#productId").val("");
                            $("#productId").change();
                        }else {
                            $("#productId").html('<option value="" selected>请选择</option>');
                            layerMsg('该客户还没有关联商品！');
                        }
                    },
                    error:function (e) {
                        layerError("请求客户产品失败","错误提示");
                    }
                }
            );
        });

        //获取产品图片配置规格
        $(document).on('change','#productId',function (e){
            $("input[type=checkbox][name=checkedRecords]").each(function () {
                $(this).attr("checked", false);
            })
            $("input[type=checkbox][name=mustFlag]").each(function () {
                $(this).attr("checked", false);
            })
            if($("#productId").val()==null || $("#productId").val()==undefined || $("#productId").val()==''){
                return false;
			}else{
                $.ajax({
                    type: 'post',               //默认是form的method（get or post），如果申明，则会覆盖
                    dataType: 'json',
                    url: "${ctx}/md/customer/pic/ajax/getCompletePicItem?productId=" + $("#productId").val(),
                    success:function (e) {
                        if(e.success && e.data.items && e.data.items.length>0){
                         $.each(e.data.items,function (index,value){
                             $("#cbox-" + index).attr("checked", true);
                             if(value.mustFlag==1){
                                $("#mustFlag-" + index).attr("checked", true);
							 }
						 });
                        }
                    },
                    error:function (e) {
                        layerError("请求产品图片配置失败","错误提示");
                    }
                });
			}

		});

	</script>
  </head>
  <body>
    <ul class="nav nav-tabs">
		<li><a href="${ctx}/md/customer/pic">列表</a></li>
		<li class="active"><a href="javascript:void(0);"><shiro:hasPermission name="md:customerpic:edit">${not empty entity.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="md:customerpic:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="entity" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<sys:message content="${message}"/>
		<c:if test="${canAction == true}">
			<div class="control-group">
				<label class="control-label">客户:</label>
				<div class="controls">
					<c:choose>
						<c:when test="${entity.customer.id > 0}">
							<form:hidden path="customer.id" id="customerId"></form:hidden>
							<form:input path="customer.name" id="customerName" readonly="true"></form:input>
						</c:when>
						<c:otherwise>
							<form:select path="customer.id" id="customerId" cssClass="input-small required selectCustomer" cssStyle="width:225px;">
								<form:option value="" label="请选择"/>
								<form:options items="${customerList}" itemLabel="name" itemValue="id" htmlEscape="false"/>
							</form:select>
						</c:otherwise>
					</c:choose>
				</div>
			</div>

			<div class="control-group">
				<label class="control-label">产品:</label>
				<div class="controls">
					<c:choose>
						<c:when test="${entity.customer.id > 0}">
							<form:hidden path="product.id" id="productId"></form:hidden>
							<form:input path="product.name" id="productName" readonly="true"></form:input>
						</c:when>
						<c:otherwise>
							<select id="productId" name ="product.id" style="width:225px;" class="required">
								<option value="" selected>请选择</option>
							</select>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">是否需要上传产品条码:</label>
				<div class="controls">
					<input type="checkbox" id="barcodeMustFlag" ${entity.barcodeMustFlag ==null || entity.barcodeMustFlag==0 ? '':'checked'}>
				</div>
			</div>
			<legend>图片类型</legend>
			<div id="divGrid" style="overflow: auto;height:480px;">
				<table id="contentTable" class="table table-striped table-bordered table-condensed" style="table-layout:fixed" cellspacing="0" width="100%">
					<thead>
					<tr>
						<th width="40px"><input type="checkbox" id="selectAll" name="selectAll"/></th>
						<th width="40px">序号</th>
						<th width="150px">标题</th>
						<th width="120px">属性</th>
						<th width="60px">必须上传</th>
						<th width="*">说明</th>
					</tr>
					</thead>
					<tbody>
					<c:forEach items="${entity.items}" var="item" varStatus="i" begin="0">
						<c:set var="index" value="${i.index}" />
						<tr id="tr_${index}">
							<td>
								<input type="checkbox" name="checkedRecords" id="cbox-${index}" ${item.checked==1?'checked':''} value="${item.pictureCode}"
									   data-id="${index}" data-remarks="${item.remarks}" data-title="${item.title}" data-sort="${item.sort}" />
							</td>
							<td>${index+1}</td>
							<td>${item.title}&nbsp;</td>
							<td>${item.pictureCode}&nbsp;</td>
							<td>
								<input type="checkbox" id="mustFlag-${index}" name="mustFlag" ${item.mustFlag==1?'checked':''} />
							</td>
							<td >
								${item.remarks}&nbsp;
							</td>
						</tr>
					</c:forEach>
					</tbody>
				</table>
			</div>
		</c:if>
		<div class="form-actions">
			<shiro:hasPermission name="md:customerpic:edit"><input id="btnSubmit" class="btn btn-primary" type="button" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
  <form:form id="submitForm" ></form:form>
  </body>
</html>
