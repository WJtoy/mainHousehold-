package com.wolfking.jeesite.test.common;

import com.kkl.kklplus.entity.voiceservice.mq.MQSmsCallbackMessage;
import com.wolfking.jeesite.modules.api.entity.md.RestGetVerifyCode;
import com.wolfking.jeesite.modules.api.util.RestEnum;
import com.wolfking.jeesite.modules.mq.sender.sms.SmsCallbackTaskMQSender;
import com.wolfking.jeesite.modules.td.entity.Message2;
import com.wolfking.jeesite.modules.td.service.MessageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 短信发送
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ShortMessageTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private SmsCallbackTaskMQSender smsCallbackTaskMQSender;

    @Test
    public void sendMessage(){
        try {
            //Message message = new Message();
            //message.setMobile("13760468206");
            //message.setContent("测试");
            //SendMessageUtils.SendMessage(message);
            //String rtn = SendMessageUtils.SendMessage("13760468206","张师傅，在您附近有一张上门安装百得油烟机的工单，请尽快登陆APP接单~");
            //System.out.println(rtn);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 新版
     */
    @Test
    public void orgSendMessage(){
        try {
            Message2 message = new Message2();
            message.setMobile("13760468206");
            message.setContent("卢师傅，在您附近有一张上门安装百得油烟机的工单，请尽快登陆APP接单~");
            //String rtn = SendMessageUtils.SendMessage(message);
            //System.out.println("rtn:" + rtn);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //region 并发测试
    @Test
    public void testBatchSend(){
        final int threadNum = 5;
        CyclicBarrier cb = new CyclicBarrier(threadNum);
        Message2 message = new Message2();
        message.setMobile("13760468206");
        ExecutorService es = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            int ids = java.util.concurrent.ThreadLocalRandom.current().nextInt(100);
            message.setContent("卢师傅" + ids + "，在您附近有一张上门安装百得油烟机的工单，请尽快登陆APP接单~");
            es.execute(new SendMessageThread(cb,message));
        }
        es.shutdown();
    }

    static class SendMessageThread implements Runnable {
        private CyclicBarrier cb;
        private Message2 message;

        public SendMessageThread(CyclicBarrier cb,Message2 message) {
            this.cb = cb;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                // 等待所有任务准备就绪
                cb.await();
                long startTime = System.currentTimeMillis();
                // 定义每个线程负责的业务逻辑实现
                //Thread.sleep(100);
                //SendMessageUtils.SendMessage(message);
                System.out.println(String.format(">>> Write-%s: ok,用时:%d 秒",this, TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - startTime))));
                //Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //endregion 并发测试

    // 测试发送验证码
    @Test
    public void testGetVerifyCode(){
        RestGetVerifyCode verifyCode = new RestGetVerifyCode();
        verifyCode.setPhone("13760468206");
        //verifyCode.setType(RestEnum.VerifyCodeType.resetPassword.ordinal());
        //messageService.getVerifyCode(verifyCode);

        verifyCode.setType(RestEnum.VerifyCodeType.register.ordinal());
        messageService.getVerifyCode(verifyCode);
    }

    // 测试发送短信回访
    @Test
    public void testSmsCallback(){
        MQSmsCallbackMessage.SmsTask.Builder builder = MQSmsCallbackMessage.SmsTask.newBuilder()
                .setSite("CW")
                .setOrderId(1026731553088212992l)
                .setQuarter("20183")
                .setMobile("13760468206")
                .setContent("您的售后工单已完成，请回复数字对师傅的服务进行评价：1非常满意 ,2一般, 3不满意,4还有产品未完成，谢谢您的支持！")
                .setTriggerBy(1l)
                .setTriggerDate(System.currentTimeMillis())
                .setSendTime(System.currentTimeMillis())
                .setExtNo("");
        smsCallbackTaskMQSender.send(builder.build());
    }

}
