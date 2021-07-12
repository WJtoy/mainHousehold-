var lockReconnect = false;//避免ws重复连接

function IM(){
    var emptyContent = '<font color="red">错误：通知无具体内容！</font><br>请反馈给系统管理员！';
    //创建要返回的对象
    var im = new Object();
    im.token = "";
    im.socket = null;

    //连接方法
    im.connect = function(){
        if(im.token == "") {
            var _this = $("#imNotice");
            if (_this) {
                var dataIm = _this.data("im");
                if (dataIm) {
                    im.token = dataIm;
                }
                else{
                    return;
                }
            } else {
                return;
            }
        }
        var socket;
        if (!window.WebSocket) {
            window.WebSocket = window.MozWebSocket;
        }
        if (window.WebSocket) {
            socket = new WebSocket("ws://127.0.0.1:7788/webSocket?token=" + im.token);
            //接收消息
            socket.onmessage = function (data) {
                im.heartCheck.reset().start(); //心跳检测重置
                if(data.data == "pong"){
                    console.log("[" + DateFormat.format(new Date(), 'yyyy-MM-dd hh:mm:ss') + "]服务端回复心跳:pong");
                    return;
                }
                //console.log(data);
                var notice = JSON.parse(data.data);
                if (notice) {
                    if(notice.noticeType == 0){
                        im.showIMNotice(notice);
                    }else if(notice.noticeType == 1){
                        im.showIMMessage(notice);
                    }
                }
            };
            //webSocket的链接
            socket.onopen = function (data) {
                im.heartCheck.reset().start(); //心跳检测重置
                console.log("[" + DateFormat.format(new Date(), 'yyyy-MM-dd hh:mm:ss') + "]即时通讯连接已建立");
                $("#imNoticeIcon").removeClass("im-offline").addClass("im-online");
            };
            //webSocket关闭
            socket.onclose = function (data) {
                console.log("[" + DateFormat.format(new Date(), 'yyyy-MM-dd hh:mm:ss') + "]即时通讯连接已关闭 ");
                $("#imNoticeIcon").removeClass("im-online").addClass("im-offline");
                //layerError("即时通讯连接已关闭", "错误提示");
                im.clientCloseAndReconnectDialog(im.userId, im.token, im.userType);
            };
            //webSocket错误信息
            socket.onerror = function (data) {
                console.log("[" + DateFormat.format(new Date(), 'yyyy-MM-dd hh:mm:ss') + "]即时通讯错误 - " + data);
                layerError("系统通知通讯错误", "错误提示");
            };
            im.socket = socket;
            return socket;
        } else {
            im.socket = socket;
            layerError("抱歉，您的浏览器不支持 即时通讯 协议，无法实时收到系统发布的通知!", "错误提示");
            return socket;
        }
    };

    // 延时多少毫秒后连接websocket
    im.imDelayConnect = function(timeout) {
        setTimeout(function timer() {
            return im.connect();//connect im
        }, timeout);
    };

    // 显示个人消息
    im.showIMMessage = function(msg) {
        if(msg){
            layer.open({
                type: 1 //Page层类型
                , area: ['750px','90%']
                , title: '个人消息'
                , shade: 0.6 //遮罩透明度
                , maxmin: false //允许全屏最小化
                , anim: -1 //0-6的动画形式，-1不开启
                , content: '<div style="padding:10px;">'+ (msg.title || emptyContent) + '</div>'
            });
        }
        else{
            console.log('message is undefined');
        }
    };

    //显示公告
    im.showIMNotice = function(notice) {
        if($("div#LAY_layuipro").length > 0){
            return;
        }
        if(notice){
            var noticeTitle = notice.title || '新消息提醒';
            layer.open({
                type: 1 //Page层类型
                , title: noticeTitle
                , area: ['750px','90%']
                , shade: 0.8
                , id: 'LAY_layuipro' //设定一个id，防止重复弹出
                , resize: true
                , btn: ['关闭']
                , btnAlign: 'c'
                , content: '<div style="padding: 10px; height: 100%; line-height: 22px; font-weight: 300;">' + (notice.content || emptyContent ) + '</div>',
                zIndex: 19891015,
                success: function (layero) {
                    layer.setTop(layero); //重点2
                }
                //background-color: #393D49; color: #fff;
            });
        }else{
            console.log('notice is undefined');
        }
    };

    // 连接关闭后，弹出提示，点击确认，重新连接
    im.clientCloseAndReconnectDialog = function() {
        var confirmClickTag = 0;
        var msgContent = '因网络或其他原因，系统通知连接已<font color="red">断开</font>！</br><font color="red">无法接收并显示后台发布的系统通知！！！</font></br>如重新连接，请点击 [<font color="blue">连接</font>] 按钮，否则，点击 [关闭]。</br></br>关闭后，可再次点击页面右上角的 <i class="icon-bullhorn" style="color:blue;"></i> ,重新连接。';
        top.layer.confirm(msgContent,
            {
                icon: 3,
                title: '系统确认',
                btn: ['连接', '关闭'],
                cancel: function (index, layero) {
                    //右上角关闭回调
                }
            },
            function (index, layero) {
                if (confirmClickTag == 1) {
                    return false;
                }
                var btn0 = $(".layui-layer-btn0", layero);
                if (btn0.hasClass("layui-btn-disabled")) {
                    return false;
                }
                confirmClickTag = 1;
                btn0.addClass("layui-btn-disabled").attr("disabled", "disabled");
                top.layer.close(index);//关闭本身
                im.connect();

            }, function (index) {//cancel
            });
    };

    //心跳检测
    im.heartCheck = {
        timeout: 300000,        //5分钟发一次心跳
        confirmTimeout: 30000,  //心跳30秒后检查，如通讯不顺畅，关闭websocket连接
        timeoutObj: null,
        serverTimeoutObj: null,
        reset: function(){
            clearTimeout(this.timeoutObj);
            clearTimeout(this.serverTimeoutObj);
            return this;
        },
        start: function(){
            var self = this;
            this.timeoutObj = setTimeout(function(){
                //这里发送一个心跳，后端收到后，返回一个心跳消息，
                //onmessage拿到返回的心跳就说明连接正常
                im.socket.send("ping");
                console.log("[" + DateFormat.format(new Date(), 'yyyy-MM-dd hh:mm:ss') + "]发送心跳包");
                self.serverTimeoutObj = setTimeout(function(){//如果超过一定时间还没重置，说明后端主动断开了
                    console.log("[" + DateFormat.format(new Date(), 'yyyy-MM-dd hh:mm:ss') + "]心跳30秒无响应，关闭客户端连接");
                    im.socket.close();     //如果onclose会执行reconnect，我们执行ws.close()就行了.如果直接执行reconnect 会触发onclose导致重连两次
                }, self.confirmTimeout);
            }, this.timeout)
        }
    };

    //返回对象
    return im;
}

// 当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
window.onbeforeunload = function() {
    if(im && im.socket){
        im.socket.close();
    }
};