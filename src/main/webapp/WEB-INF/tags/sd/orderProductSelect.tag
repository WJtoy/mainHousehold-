<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<%@ include file="/WEB-INF/views/include/dialog.jsp"%>

<%@ attribute name="id" type="java.lang.String" required="true" description="编号"%>
<%@ attribute name="name" type="java.lang.String" required="true" description="隐藏域名称（ID）"%>
<%@ attribute name="value" type="java.lang.String" required="true" description="隐藏域值（ID）"%>
<%@ attribute name="labelName" type="java.lang.String" required="true" description="输入框名称（Name）"%>
<%@ attribute name="labelValue" type="java.lang.String" required="true" description="输入框值（Name）"%>
<%@ attribute name="title" type="java.lang.String" required="true" description="选择框标题"%>
<%@ attribute name="cssClass" type="java.lang.String" required="false" description="css样式"%>
<%@ attribute name="width" type="java.lang.String" required="false" description="字段列表"%>
<%@ attribute name="height" type="java.lang.String" required="false" description="字段表头"%>
<%@ attribute name="callbackmethod" type="java.lang.String" required="true" description="回调方法"%>
<%@ attribute name="orderId" type="java.lang.String" required="true" description="订单ID"%>
<%@ attribute name="quarter" type="java.lang.String" required="false" description="分片"%>
<div class="input-append">
	<input id="${id}Id" name="${name}" class="${cssClass}" type="hidden" value="${value}"${disabled eq 'true' ? ' disabled=\'disabled\'' : ''}/>
	<input id="${id}Name" name="${labelName}" readonly="readonly" type="text" value="${labelValue}" maxlength="50"${disabled eq 'true' ? ' disabled=\'disabled\'' : ''}
		class="${cssClass}" style="${cssStyle}"/><a id="${id}Button" href="javascript:" class="btn${disabled eq 'true' ? ' disabled' : ''}"><i class="icon-search"></i></a>&nbsp;&nbsp;
</div>
<script type="text/javascript">
	$("#${id}Button").click(function(){
		// 是否限制选择，如果限制，设置为disabled
		if ($("#${id}Id").attr("disabled")){
			return true;
		}
		//var quarter = '';
		// 正常打开	
		var myjbox;
		if($.jBox){
			myjbox = $.jBox;
		}
		else{
			myjbox = top.$.jBox;
		}
		myjbox.open("iframe:${ctx}/sd/order/selectproduct?orderId=${orderId}&quarter=${quarter}",
			"选择${title}", ${width}, ${height}, {top:'5px',id:'jbox_orderProductSelect',
			buttons:{"确定":"ok","关闭":true}, submit:function(v, h, f){
				if (v=="ok"){
				    var data = h.find("iframe")[0].contentWindow.data;
				    $("#${id}Id").val(data.id);
					$("#${id}Name").val(data.name);
					if("${callbackmethod}" != ""){
					    ${callbackmethod}(data);
                    }
				}
			}, loaded:function(h){
				$("#jbox-iframe",h).prop("height","98%");
				
			}
		});
	});
</script>
