<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<%@ include file="/WEB-INF/views/include/dialog.jsp" %>

<%@ attribute name="id" type="java.lang.String" required="true" description="编号" %>
<%@ attribute name="name" type="java.lang.String" required="true" description="隐藏域名称（ID）" %>
<%@ attribute name="value" type="java.lang.String" required="true" description="隐藏域值（ID）" %>
<%@ attribute name="labelName" type="java.lang.String" required="true" description="输入框名称（Name）" %>
<%@ attribute name="labelValue" type="java.lang.String" required="true" description="输入框值（Name）" %>
<%@ attribute name="cssClass" type="java.lang.String" required="false" description="css样式" %>
<%@ attribute name="width" type="java.lang.String" required="false" description="字段列表" %>
<%@ attribute name="height" type="java.lang.String" required="false" description="字段表头" %>
<%@ attribute name="formid" type="java.lang.String" required="false" description="form的id,已#开头,页面有多个form且有相同属性时使用" %>
<%@ attribute name="callbackmethod" type="java.lang.String" required="true" description="回调方法" %>
<%@ attribute name="cssStyle" type="java.lang.String" required="false" description="style样式" %>
<%@ attribute name="noSubEnginner" type="java.lang.Boolean" required="false" description="是否显示子账号"%>
<%@ attribute name="noblackList" type="java.lang.Boolean" required="false" description="是否不显示黑名单"%>
<div class="input-append">
    <input id="${id}Id" name="${name}" class="${cssClass}" type="hidden"
           value="${value}"${disabled eq 'true' ? ' disabled=\'disabled\'' : ''}/>
    <input id="${id}Name" name="${labelName}" readonly="readonly" type="text" value="${labelValue}"
           maxlength="50"${disabled eq 'true' ? ' disabled=\'disabled\'' : ''}
           class="${cssClass}" style="${cssStyle}"/><a id="${id}Button" href="javascript:"
                                                       class="btn${disabled eq 'true' ? ' disabled' : ''}"><i
        class="icon-search"></i></a>&nbsp;&nbsp;
</div>
<script type="text/javascript">
    $("#${id}Button").click(function () {
        // 是否限制选择，如果限制，设置为disabled
        if ($("#${id}Id").attr("disabled")) {
            return true;
        }

        var replyIdex = top.layer.open({
            type: 2,
            id: 'layer_pointlist_rpt',
            zIndex: 19891015,
            title: '选择服务网点',
            content: "${ctx}/rpt/common/servicePointSelector?noSubEnginner=${noSubEnginner}&noblackList=${noblackList}&searchTag=1",
            shade: 0.3,
            area: ['${width}px', '${height}px'],
            maxmin: true,
            btn: ['确定', '关闭', '清除'],
            yes: function (index, layero) {
                var iframeWin = parent.window['layui-layer-iframe' + index];
                var data = iframeWin.data;
                $("${formid} #${id}Id").val(data.id);
                $("${formid} #${id}Name").val(data.name);
                if ("${callbackmethod}" != "") {
                    ${callbackmethod}(data);
                }
                top.layer.close(index);
            },
            btn2: function (index, layero) {
                top.layer.close(index);
            },
            btn3: function (index, layero) {
                $("${formid} #${id}Id").val("").trigger('change');
                $("${formid} #${id}Name").val("");
            },
            success: function (layero, index) {

            }
        });
    });
</script>
