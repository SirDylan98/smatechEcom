package com.smatech.notification_service.service;

import com.smatech.commons_library.dto.OrderItemResponse;
import com.smatech.commons_library.dto.OrderResponse;
import com.smatech.notification_service.dto.JsonUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/13/2025
 */
@Service
@Slf4j
public class EmailSenderService {

    private  JavaMailSender mailSender;

    public EmailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    //    @EventListener(OrderCompletedEvent.class)
    public void sendEmailOnSuccessfulPayment(OrderResponse orderResponse) throws MessagingException, jakarta.mail.MessagingException {
        String finalEmailBody=generateOrderConfirmationEmail(orderResponse);
        String subject = "Order Completed #"+orderResponse.getOrderId();
        String sentTo="dylanDzvenetashinga@gmail.com";
        MimeMessage message=mailSender.createMimeMessage();
        MimeMessageHelper emailHelper = new MimeMessageHelper(message,true,"UTF-8");
        emailHelper.setFrom("hradmin@ophid.co.zw");
        emailHelper.setTo(sentTo);
        emailHelper.setSubject(subject);
        emailHelper.setText(finalEmailBody,true);
        mailSender.send(message);
        System.out.println("The message has been sent successfully ON COURSE COMPLETED AFTER DUE DATE  **************************************");



    }
//    @EventListener(OrderFailedEvent.class)
    public void sendEmailOnFailurePayment(OrderResponse orderResponse) throws MessagingException, jakarta.mail.MessagingException {
        String finalEmailBody=generateOrderFailureEmail(orderResponse);
        String subject = "Order Failed #"+orderResponse.getOrderId();
        String sentTo="dylanDzvenetashinga@gmail.com";
        MimeMessage message=mailSender.createMimeMessage();
        MimeMessageHelper emailHelper = new MimeMessageHelper(message,true,"UTF-8");
        emailHelper.setFrom("hradmin@ophid.co.zw");
        emailHelper.setTo(sentTo);
        emailHelper.setSubject(subject);
        emailHelper.setText(finalEmailBody,true);
        mailSender.send(message);
        System.out.println("The message has been sent successfully ON COURSE COMPLETED AFTER DUE DATE  **************************************");



    }
    public String generateOrderFailureEmail(OrderResponse order) {
        StringBuilder emailContent = new StringBuilder();

        emailContent.append("<html><body>");
        emailContent.append("<h2>Order Processing Failed</h2>");
        emailContent.append("<p>Unfortunately, we were unable to process your payment.</p>");
        emailContent.append("<p>Please try again or use a different payment method.</p>");

        emailContent.append("<h3>Order Details</h3>");
        emailContent.append("<p><strong>Order ID:</strong> ").append(order.getOrderId()).append("</p>");
        emailContent.append("<p><strong>Order Status:</strong> ").append(order.getOrderStatus()).append("</p>");

        emailContent.append("<p>If you need assistance, please contact our support team.</p>");

        emailContent.append("<p>Best regards,</p>");
        emailContent.append("<p><strong>SmatCommerce Team</strong></p>");
        emailContent.append("</body></html>");

        return emailContent.toString();
    }
    public String generateOrderConfirmationEmail(OrderResponse order) {
        StringBuilder emailContent = new StringBuilder();

        emailContent.append("<html><body>");
        emailContent.append("<h2>Thank You for Shopping on SmatCommerce!</h2>");
        emailContent.append("<p>We truly value your support and loyalty.</p>");
        emailContent.append("<p>Your order has been received and is being processed.</p>");

        emailContent.append("<h3>Order Details</h3>");
        emailContent.append("<p><strong>Order ID:</strong> ").append(order.getOrderId()).append("</p>");
        emailContent.append("<p><strong>Order Status:</strong> ").append(order.getOrderStatus()).append("</p>");
        emailContent.append("<p><strong>Total Amount:</strong> ").append(order.getCurrency()).append(" ").append(order.getTotalAmount()).append("</p>");
        emailContent.append("<p><strong>Shipping Address:</strong> ").append(order.getShippingAddress()).append("</p>");
        emailContent.append("<p><strong>Order Date:</strong> ").append(order.getCreatedDate()).append("</p>");

        emailContent.append("<h3>Items in Your Order</h3>");
        emailContent.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse; width: 100%;'>");
        emailContent.append("<tr><th>Product</th><th>Quantity</th><th>Price</th><th>Total</th></tr>");
        for (OrderItemResponse item : order.getOrderItems()) {
            log.info("=============> {}", JsonUtil.toJson(item));
            double itemTotal = item.getPrice() * item.getQuantity();
            emailContent.append("<tr>")
                    .append("<td>").append(item.getProductName()).append("</td>")
                    .append("<td>").append(item.getQuantity()).append("</td>")
                    .append("<td>").append(order.getCurrency()).append(" ")
                    .append( item.getPrice()).append("</td>")
                    .append("<td>").append(order.getCurrency()).append(" ").append(itemTotal).append("</td>")
                    .append("</tr>");
        }

        emailContent.append("</table>");

        emailContent.append("<p>You can track your order or complete the checkout by clicking the link below:</p>");


        emailContent.append("<p>If you have any questions, feel free to contact our support team.</p>");
        emailContent.append("<p>Best regards,</p>");
        emailContent.append("<p><strong>SmatCommerce Team</strong></p>");
        emailContent.append("</body></html>");

        return emailContent.toString();
    }

}
