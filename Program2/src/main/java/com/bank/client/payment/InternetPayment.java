package com.bank.client.payment;

import com.bank.Util.CommonUtil;
import com.bank.Util.ISOUtil;
import com.bank.Util.MQUtil;
import com.bank.client.Client;
import com.bank.client.ClientHelper;
import com.bank.client.interfaceClient.Payment;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InternetPayment implements Payment {
    private static final Logger logger = LoggerFactory.getLogger(InternetPayment.class);
    private String areaCode;
    private String phoneNumber;
    private String internetNumber;
    private ISOUtil isoUtil = new ISOUtil();

    public static class InternetBuilder{
        private String areaCode;
        private String phoneNumber;
        private String internetNumber;

        public InternetBuilder(String phoneNumber, String internetNumber){
            this.phoneNumber = phoneNumber;
            this.internetNumber = internetNumber;
        }

        public InternetBuilder addAreaCode(String areaCode){
            this.areaCode = areaCode;
            return this;
        }

        public InternetPayment build(){
            return new InternetPayment(this);
        }
    }

    private InternetPayment(InternetBuilder builder){
        this.areaCode = builder.areaCode;
        this.phoneNumber = builder.phoneNumber;
        this.internetNumber = builder.internetNumber;
    }

    @Override
    public String payInquiry(String accountNumber, String pinNumber, int bill) {
        // The bill was receive from the third party

        String message = buildISOInquiry(accountNumber,pinNumber,bill);
//        String response = ClientHelper.sendData(message, "http://localhost:8080/payment/internet/inquiry");
        new MQUtil().sendToExchange("mainExchange", message);
        String response = CommonUtil.receiveFromSocket(Integer.parseInt(Client.getPort()));

        return response;
    }

    private String buildISOInquiry(String accountNumber, String pinNumber, int amount) {
        try {
            InputStream is = getClass().getResourceAsStream("/fields.xml");
            GenericPackager packager = new GenericPackager(is);

            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);
            isoMsg.setMTI("0200");

            isoMsg.set(2, accountNumber);
            isoMsg.set(3, "380000");
            isoMsg.set(4, amount + "");
            isoMsg.set(7, new SimpleDateFormat("MMddHHmmss").format(new Date()));
            isoMsg.set(11, "000001");
            isoMsg.set(12, new SimpleDateFormat("HHmmss").format(new Date()));
            isoMsg.set(13, new SimpleDateFormat("MMdd").format(new Date()));
            isoMsg.set(15, new SimpleDateFormat("MMdd").format(new Date()));
            isoMsg.set(18, "0000");
            isoMsg.set(32, "00000000000");
            isoMsg.set(33, "00000000000");
            isoMsg.set(37, "000000000000");
            isoMsg.set(41, "12340001");
            isoMsg.set(42, "000000000000000");
            isoMsg.set(43, "0000000000000000000000000000000000000000");
            isoMsg.set(49, "0");
            isoMsg.set(49, "360");
            isoMsg.set(52, pinNumber);
            isoMsg.set(54, Client.getServer()+":"+Client.getPort());
            isoMsg.set(62, "0");
            isoMsg.set(102, "1234567890");

            byte[] result = isoMsg.pack();
            return new String(result);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public String pay(String message) {
        ISOMsg isoMessage = isoUtil.stringToISO(message);

        String sendMessage = buildISOPayment(isoMessage.getString(2),Integer.parseInt(isoMessage.getString(4)));

//        String response = ClientHelper.sendData(sendMessage, "http://localhost:8080/payment/internet");
        new MQUtil().sendToExchange("mainExchange", sendMessage);
        String response = CommonUtil.receiveFromSocket(Integer.parseInt(Client.getPort()));

        return response;
    }

    private String buildISOPayment(String accountNumber, int amount) {
        try {
            InputStream is = getClass().getResourceAsStream("/fields.xml");
            GenericPackager packager = new GenericPackager(is);

            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);
            isoMsg.setMTI("0200");

            isoMsg.set(2, accountNumber);
            isoMsg.set(3, "180000");
            isoMsg.set(4, amount + "");
            isoMsg.set(7, new SimpleDateFormat("MMddHHmmss").format(new Date()));
            isoMsg.set(11, "000001");
            isoMsg.set(12, new SimpleDateFormat("HHmmss").format(new Date()));
            isoMsg.set(13, new SimpleDateFormat("MMdd").format(new Date()));
            isoMsg.set(15, new SimpleDateFormat("MMdd").format(new Date()));
            isoMsg.set(18, "0000");
            isoMsg.set(32, "00000000000");
            isoMsg.set(33, "00000000000");
            isoMsg.set(37, "000000000000");
            isoMsg.set(41, "12340001");
            isoMsg.set(42, "000000000000000");
            isoMsg.set(43, "0000000000000000000000000000000000000000");
            isoMsg.set(48, "0");
            isoMsg.set(49, "360");
            isoMsg.set(54, Client.getServer()+":"+Client.getPort());
            isoMsg.set(62, "0");
            isoMsg.set(63, "0");
            isoMsg.set(102, "1234567890");

            byte[] result = isoMsg.pack();
            return new String(result);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}
