<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<%@ include file="/WEB-INF/views/include/dialog.jsp"%>

<%@ attribute name="id" type="java.lang.String" required="true" description="编号"%>
<%@ attribute name="name" type="java.lang.String" required="true" description="隐藏域名称（ID）"%>
<%@ attribute name="value" type="java.lang.String" required="true" description="隐藏域值（ID）"%>
<%@ attribute name="labelName" type="java.lang.String" required="true" description="输入框名称（Name）"%>
<%@ attribute name="labelValue" type="java.lang.String" required="true" description="输入框值（Name）"%>
<%@ attribute name="title" type="java.lang.String" required="true" description="选择框标题"%>
<%@ attribute name="servicePointId" type="java.lang.String" required="true" description="网点ID"%>
<%@ attribute name="areaId" type="java.lang.String" required="false" description="负责的网点区域ID"%>
<%@ attribute name="delFlag" type="java.lang.String" required="false" description="安维访问"%>
<%@ attribute name="cssClass" type="java.lang.String" required="false" description="css样式"%>
<%@ attribute name="width" type="java.lang.String" required="false" description="字段列表"%>
<%@ attribute name="height" type="java.lang.String" required="false" description="字段表头"%>
<%@ attribute name="callbackmethod" type="java.lang.String" required="true" description="回调方法"%>
<%@ attribute name="exceptId" type="java.lang.String" required="false" description="例外ID"%>
<%@ attribute name="cssStyle" type="java.lang.String" required="false" description="style样式"%>
<%@ attribute name="allowClear" type="java.lang.Boolean" required="false" description="是否允许清除"%>
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
        var btnlist = ['确定', '关闭'${allowClear?",'清除'":""}];
        var replyIdex = top.layer.open({
            type: 2,
            id:'layer_pointlist',
            zIndex:19891015,
            title:'选择${title}',
            content: "${ctx}/md/engineer/select?servicePoint.id=${servicePointId}&exceptId=${exceptId}&area.id=${areaId}&delFlag=${delFlag}",
            shade: 0.3,
            area: ['${width}px', '${height}px'],
            maxmin: true,
            btn: btnlist,
            yes: function(index, layero){
                var iframeWin = parent.window['layui-layer-iframe' + index];
                var data = iframeWin.data;
                $("${formid} #${id}Id").val(data.id);
                $("${formid} #${id}Name").val(data.name);
                if("${callbackmethod}" != ""){
                    ${callbackmethod}(data);
                }
                top.layer.close(index);
            },
            btn2: function(index, layero){
                top.layer.close(index);
            },
            btn3: function(index, layero){
                $("${formid} #${id}Id").val("").trigger('change');
                $("${formid} #${id}Name").val("");
            },
            success: function(layero){
            }
        });
	});
</script>
